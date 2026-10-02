package com.kelompok8;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.power.models.PowerModelHostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.*;

/**
 * Builder untuk arsitektur multi-datacenter sesuai desain proyek Kelompok 8.
 *
 * Arsitektur: 3 Datacenter Heterogen yang dikelola oleh satu broker terpusat.
 * Setiap datacenter memiliki spesialisasi tier yang berbeda:
 *
 * DC-1 (Entry-Level)  : 8 Host Tipe A & B,  10 VM Small  (1 PE,  1 GB RAM, 1000 MIPS)
 * DC-2 (Standard)     : 8 Host Tipe B & C,  10 VM Medium (2 PE,  2 GB RAM, 1500 MIPS)
 * DC-3 (Premium)      : 8 Host Tipe C & D,  10 VM Large  (4 PE,  4 GB RAM, 2000 MIPS)
 *
 * Konfigurasi Host (setiap host 8 PE/core):
 * | Tipe | MIPS/core | RAM (GB) | Bandwidth | Power Idle | Power Max |
 * |------|-----------|----------|-----------|------------|-----------|
 * | A    | 1,000     | 8        | 10 Gbps   |  93.7 W    | 135.0 W   |
 * | B    | 1,500     | 16       | 10 Gbps   | 105.0 W    | 175.0 W   |
 * | C    | 2,000     | 24       | 10 Gbps   | 120.0 W    | 225.0 W   |
 * | D    | 3,000     | 32       | 10 Gbps   | 140.0 W    | 300.0 W   |
 *
 * Sinkronisasi: Semua VM dari 3 datacenter disubmit ke 1 broker tunggal,
 * sehingga broker dapat menjadwalkan task secara global lintas datacenter.
 * Metrik dihimpun bersama dari seluruh host dan VM ketiga datacenter.
 *
 * Kebijakan Alokasi VM: Best-Fit (berdasarkan kapasitas MIPS dan RAM)
 * Penjadwalan Task: Non-preemptive (Space-Shared pada VM)
 */
public class DatacenterBuilder {

    private static final int PES_PER_HOST = 8;
    private static final long HOST_STORAGE = 1_000_000; // 1 TB in MB
    private static final long HOST_BW = 10_000;         // 10 Gbps in Mbps
    private static final int NUM_HOSTS_PER_TYPE = 4;    // 4 host per tipe dalam setiap DC

    // Host Types: {MIPS/core, RAM_GB}
    public static final int[][] HOST_TYPES = {
            {1000, 8},     // Type A: 1000 MIPS/core, 8 GB RAM
            {1500, 16},    // Type B: 1500 MIPS/core, 16 GB RAM
            {2000, 24},    // Type C: 2000 MIPS/core, 24 GB RAM
            {3000, 32},    // Type D: 3000 MIPS/core, 32 GB RAM
    };

    // SPECpower benchmark power specs: {idle_power_watts, max_power_watts}
    // References: Beloglazov & Buyya (2012) - HP ProLiant and IBM System benchmarks
    public static final double[][] POWER_SPECS = {
            {93.7,  135.0},  // Type A: HP ProLiant ML110 G5
            {105.0, 175.0},  // Type B: IBM System x3250 M2
            {120.0, 225.0},  // Type C: IBM System x3550 M3
            {140.0, 300.0},  // Type D: High-end enterprise server
    };

    /**
     * Definisi konfigurasi tiap datacenter:
     * { dcId, hostTypeA_idx, hostTypeB_idx, vmPes, vmRamMB, vmMips, vmCount }
     *
     * DC-1: Host Tipe A(0) + B(1), VM Small  (1 PE, 1024 MB, 1000 MIPS, 10 unit)
     * DC-2: Host Tipe B(1) + C(2), VM Medium (2 PE, 2048 MB, 1500 MIPS, 10 unit)
     * DC-3: Host Tipe C(2) + D(3), VM Large  (4 PE, 4096 MB, 2000 MIPS, 10 unit)
     */
    private static final int[][] DC_CONFIGS = {
            // { hostTypeIdx1, hostTypeIdx2, vmPes, vmRamMB, vmMips, vmCount }
            {0, 1,  1, 1024, 1000, 10},   // DC-1: Entry-Level
            {1, 2,  2, 2048, 1500, 10},   // DC-2: Standard
            {2, 3,  4, 4096, 2000, 10},   // DC-3: Premium
    };

    private static final String[] DC_NAMES = {
            "DC-1 (Entry-Level)", "DC-2 (Standard)", "DC-3 (Premium)"
    };

    // -------------------------------------------------------------------------
    // API UTAMA: Bangun 3 Datacenter Sekaligus
    // -------------------------------------------------------------------------

    /**
     * Membangun 3 datacenter heterogen dalam satu simulasi.
     * Mengembalikan MultiDatacenterResult yang berisi:
     *   - Daftar DatacenterResult per DC
     *   - Daftar gabungan semua VM (untuk disubmit ke satu broker)
     *   - Daftar gabungan semua Host (untuk pengumpulan metrik energi)
     */
    public static MultiDatacenterResult buildMultiple(CloudSimPlus simulation) {
        List<DatacenterResult> dcResults = new ArrayList<>();
        List<Vm>   allVms   = new ArrayList<>();
        List<Host> allHosts = new ArrayList<>();
        Map<Vm, Datacenter> vmToDcMap = new IdentityHashMap<>();

        System.out.println("\n  Membangun 3 Datacenter Heterogen...");
        System.out.println("  " + "-".repeat(60));

        for (int dcIdx = 0; dcIdx < DC_CONFIGS.length; dcIdx++) {
            System.out.printf("%n  [%s]%n", DC_NAMES[dcIdx]);

            int[] cfg       = DC_CONFIGS[dcIdx];
            int typeA       = cfg[0];
            int typeB       = cfg[1];
            int vmPes       = cfg[2];
            int vmRamMB     = cfg[3];
            int vmMips      = cfg[4];
            int vmCount     = cfg[5];

            // Buat host untuk DC ini (4 host Tipe-A + 4 host Tipe-B)
            List<Host> hostList = createHostsForDc(typeA, typeB, DC_NAMES[dcIdx]);

            // Buat VM untuk DC ini
            List<Vm> vmList = createVmsForDc(vmPes, vmRamMB, vmMips, vmCount, DC_NAMES[dcIdx]);

            // Buat Datacenter CloudSim dengan Kebijakan Alokasi Host: Best-Fit (VmAllocationPolicyBestFit)
            // Rationale Alokasi Host:
            // 1. Setiap Datacenter memiliki 8 Host fisik (masing-masing 8 PE, total 64 PE) dan 10 VM.
            //    - DC-1: 10 VM Small (1 PE, 1 GB RAM) dialokasikan ke 8 Host (4 Tipe A + 4 Tipe B).
            //    - DC-2: 10 VM Medium (2 PE, 2 GB RAM) dialokasikan ke 8 Host (4 Tipe B + 4 Tipe C).
            //    - DC-3: 10 VM Large (4 PE, 4 GB RAM) dialokasikan ke 8 Host (4 Tipe C + 4 Tipe D).
            // 2. Pemahaman Best-Fit vs First-Fit:
            //    - First-Fit: Memilih host pertama yang memiliki resource mencukupi tanpa memikirkan sisa kapasitas.
            //    - Best-Fit: Mengevaluasi seluruh host dan memilih host dengan sisa resource (MIPS & RAM) paling minimal
            //      setelah VM ditempatkan. Hal ini memadatkan (packing) penempatan VM, mengurangi fragmentasi resource,
            //      dan mendukung efisiensi konsumsi daya (SPECpower) karena host yang tidak terpakai tetap berada pada
            //      tingkat idle terendah atau dapat dimatikan.
            // 3. Distribusi: Karena terdapat 10 VM dan 8 Host pada tiap DC, secara rata-rata 6 Host menampung 1 VM
            //    dan 2 Host menampung 2 VM, seluruhnya berada jauh di bawah batas maksimum kapasitas 8 PE per host.
            Datacenter datacenter = new DatacenterSimple(
                    simulation, hostList, new VmAllocationPolicyBestFit());
            datacenter.setName(DC_NAMES[dcIdx]);

            for (Vm vm : vmList) {
                vmToDcMap.put(vm, datacenter);
                vm.setDescription(DC_NAMES[dcIdx] + " [PE:" + vmPes + ", MIPS:" + vmMips + "]");
            }

            dcResults.add(new DatacenterResult(datacenter, vmList));
            allVms.addAll(vmList);
            allHosts.addAll(hostList);
        }

        printMultiDatacenterSummary(dcResults, allHosts, allVms);
        return new MultiDatacenterResult(dcResults, allVms, allHosts, vmToDcMap);
    }

    /**
     * Membangun datacenter tunggal (backward-compatible, digunakan jika diperlukan).
     */
    public static DatacenterResult build(CloudSimPlus simulation) {
        List<Host> hostList = createHostsForDc(0, 1, "Datacenter-Tunggal");
        hostList.addAll(createHostsForDc(2, 3, "Datacenter-Tunggal"));
        List<Vm> vmList = new ArrayList<>();
        vmList.addAll(createVmsForDc(1, 1024, 1000, 10, "Single"));
        vmList.addAll(createVmsForDc(2, 2048, 1500, 10, "Single"));
        vmList.addAll(createVmsForDc(4, 4096, 2000, 10, "Single"));
        Datacenter datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyBestFit());
        return new DatacenterResult(datacenter, vmList);
    }

    // -------------------------------------------------------------------------
    // HELPER: Pembuatan Host
    // -------------------------------------------------------------------------

    /**
     * Membuat 8 host untuk sebuah datacenter: 4 host Tipe-typeA dan 4 host Tipe-typeB.
     */
    private static List<Host> createHostsForDc(int typeA, int typeB, String dcName) {
        List<Host> hostList = new ArrayList<>();
        String[] typeNames = {"A", "B", "C", "D"};
        int[] types = {typeA, typeB};

        for (int typeIdx : types) {
            int mipsPerCore = HOST_TYPES[typeIdx][0];
            int ramGB       = HOST_TYPES[typeIdx][1];

            for (int unit = 0; unit < NUM_HOSTS_PER_TYPE; unit++) {
                List<Pe> peList = new ArrayList<>();
                for (int pe = 0; pe < PES_PER_HOST; pe++) {
                    peList.add(new PeSimple(mipsPerCore));
                }

                long ramMB = (long) ramGB * 1024;
                Host host = new HostSimple(ramMB, HOST_BW, HOST_STORAGE, peList)
                        .setVmScheduler(new VmSchedulerTimeShared());

                host.setPowerModel(new PowerModelHostSimple(
                        POWER_SPECS[typeIdx][1],   // max power (Watts)
                        POWER_SPECS[typeIdx][0]    // idle power (Watts)
                ));
                host.enableUtilizationStats();
                hostList.add(host);

                System.out.printf("    [Host Tipe-%s #%d] %d MIPS/core x %d PEs, %d GB RAM%n",
                        typeNames[typeIdx], unit + 1, mipsPerCore, PES_PER_HOST, ramGB);
            }
        }
        return hostList;
    }

    // -------------------------------------------------------------------------
    // HELPER: Pembuatan VM
    // -------------------------------------------------------------------------

    /**
     * Membuat VM dengan spesifikasi yang diberikan.
     * Menggunakan CloudletSchedulerSpaceShared (non-preemptive).
     */
    private static List<Vm> createVmsForDc(int pes, int ramMB, int mips, int count, String dcName) {
        List<Vm> vmList = new ArrayList<>();
        String vmType = pes == 1 ? "Small" : (pes == 2 ? "Medium" : "Large");

        for (int i = 0; i < count; i++) {
            Vm vm = new VmSimple(mips, pes)
                    .setRam(ramMB)
                    .setBw(250)       // 250 Mbps per VM
                    .setSize(10_000)  // 10 GB storage
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vmList.add(vm);
        }

        System.out.printf("    [VM %s] %d MIPS x %d PE, %d MB RAM x %d unit%n",
                vmType, mips, pes, ramMB, count);
        return vmList;
    }

    // -------------------------------------------------------------------------
    // HELPER: Ringkasan Info
    // -------------------------------------------------------------------------

    private static void printMultiDatacenterSummary(
            List<DatacenterResult> dcResults, List<Host> allHosts, List<Vm> allVms) {

        System.out.println("\n  +============================================================+");
        System.out.println("  |            RINGKASAN ARSITEKTUR MULTI-DATACENTER           |");
        System.out.println("  +============================================================+");

        for (int i = 0; i < dcResults.size(); i++) {
            DatacenterResult dcr = dcResults.get(i);
            List<Host> hosts = dcr.datacenter().getHostList();
            long totalMips = hosts.stream()
                    .mapToLong(h -> h.getPeList().stream().mapToLong(Pe::getCapacity).sum())
                    .sum();
            long totalRam  = hosts.stream().mapToLong(h -> h.getRam().getCapacity()).sum();
            System.out.printf("    %-20s : %2d Host | %2d VM | %,7d MIPS | %5.1f GB RAM%n",
                    DC_NAMES[i], hosts.size(), dcr.vmList().size(), totalMips, totalRam / 1024.0);
        }

        long grandTotalMips = allHosts.stream()
                .mapToLong(h -> h.getPeList().stream().mapToLong(Pe::getCapacity).sum())
                .sum();
        long grandTotalRam = allHosts.stream().mapToLong(h -> h.getRam().getCapacity()).sum();

        System.out.println("  +------------------------------------------------------------+");
        System.out.printf("    %-20s : %2d Host | %2d VM | %,7d MIPS | %5.1f GB RAM%n",
                "TOTAL INFRASTRUKTUR", allHosts.size(), allVms.size(), grandTotalMips, grandTotalRam / 1024.0);
        System.out.println("    [SINKRONISASI]       : Seluruh VM dari 3 DC dikelola 1 Broker");
        System.out.println("  +============================================================+\n");
    }

    // -------------------------------------------------------------------------
    // RECORD TYPES
    // -------------------------------------------------------------------------

    /** Hasil build untuk satu datacenter. */
    public static record DatacenterResult(Datacenter datacenter, List<Vm> vmList) {}

    /**
     * Hasil build untuk 3 datacenter sekaligus.
     * Menyediakan akses ke:
     *   - dcResults : daftar DatacenterResult per DC (untuk info per-DC)
     *   - allVms    : gabungan semua VM dari 3 DC (disubmit ke satu broker)
     *   - allHosts  : gabungan semua host dari 3 DC (untuk metrik energi)
     */
    public static record MultiDatacenterResult(
            List<DatacenterResult> dcResults,
            List<Vm>              allVms,
            List<Host>            allHosts,
            Map<Vm, Datacenter>   vmToDatacenterMap
    ) {
        /** Host dari datacenter ke-dcIdx (0-based). */
        public List<Host> hostsOf(int dcIdx) {
            return dcResults.get(dcIdx).datacenter().getHostList();
        }

        /** Mendapatkan Datacenter tempat VM dialokasikan. */
        public Datacenter getDatacenterForVm(Vm vm) {
            if (vm == null) return null;
            if (vmToDatacenterMap != null && vmToDatacenterMap.containsKey(vm)) {
                return vmToDatacenterMap.get(vm);
            }
            for (DatacenterResult dcr : dcResults) {
                for (Vm v : dcr.vmList()) {
                    if (v == vm) {
                        return dcr.datacenter();
                    }
                }
            }
            return null;
        }

        /** Mendapatkan nama Datacenter tempat VM dialokasikan. */
        public String getDatacenterNameForVm(Vm vm) {
            Datacenter dc = getDatacenterForVm(vm);
            return dc != null ? dc.getName() : "Unknown-DC";
        }
    }
}
