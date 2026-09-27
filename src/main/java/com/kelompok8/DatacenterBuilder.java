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
 * Builder untuk arsitektur datacenter sesuai desain proyek Kelompok 8.
 *
 * Sesuai Dokumen Tugas 3 - Desain Awal Proyek:
 * - 1 Datacenter tunggal
 * - 8 Host heterogen (4 tipe x 2 unit)
 * - 30 VM heterogen (10 Small + 10 Medium + 10 Large)
 *
 * Konfigurasi Host (setiap host 8 PE/core):
 * | Tipe | MIPS/core | RAM (GB) | Bandwidth | Jumlah |
 * |------|-----------|----------|-----------|--------|
 * | A    | 1,000     | 8        | 10 Gbps   | 2      |
 * | B    | 1,500     | 16       | 10 Gbps   | 2      |
 * | C    | 2,000     | 24       | 10 Gbps   | 2      |
 * | D    | 3,000     | 32       | 10 Gbps   | 2      |
 *
 * Konfigurasi VM:
 * | Tipe   | PE | RAM (MB) | MIPS | Jumlah |
 * |--------|----|----------|------|--------|
 * | Small  | 1  | 1024     | 1000 | 10     |
 * | Medium | 2  | 2048     | 1500 | 10     |
 * | Large  | 4  | 4096     | 2000 | 10     |
 *
 * Kebijakan Alokasi VM: Best-Fit (berdasarkan kapasitas MIPS dan RAM)
 * Penjadwalan Task: Non-preemptive (Space-Shared pada VM)
 */
public class DatacenterBuilder {

    private static final int PES_PER_HOST = 8;
    private static final long HOST_STORAGE = 1_000_000; // 1 TB in MB
    private static final long HOST_BW = 10_000;         // 10 Gbps in Mbps
    private static final int NUM_HOSTS_PER_TYPE = 2;

    // Host Types: {MIPS/core, RAM_GB}
    public static final int[][] HOST_TYPES = {
            {1000, 8},     // Type A: 1000 MIPS/core, 8 GB RAM
            {1500, 16},    // Type B: 1500 MIPS/core, 16 GB RAM
            {2000, 24},    // Type C: 2000 MIPS/core, 24 GB RAM
            {3000, 32},    // Type D: 3000 MIPS/core, 32 GB RAM
    };

    // VM Types: {PE, RAM_MB, MIPS_per_PE, count}
    public static final int[][] VM_TYPES = {
            {1, 1024, 1000, 10},   // Small: 1 PE, 1 GB RAM, 1000 MIPS (10 units)
            {2, 2048, 1500, 10},   // Medium: 2 PE, 2 GB RAM, 1500 MIPS (10 units)
            {4, 4096, 2000, 10},   // Large: 4 PE, 4 GB RAM, 2000 MIPS (10 units)
    };

    // SPECpower benchmark power specs: {idle_power_watts, max_power_watts}
    // References: Beloglazov & Buyya (2012) - HP ProLiant and IBM System benchmarks
    public static final double[][] POWER_SPECS = {
            {93.7, 135.0},    // Type A: HP ProLiant ML110 G5
            {105.0, 175.0},   // Type B: IBM System x3250 M2
            {120.0, 225.0},   // Type C: IBM System x3550 M3
            {140.0, 300.0},   // Type D: High-end enterprise server
    };

    /**
     * Membangun datacenter lengkap dengan 8 host heterogen dan 30 VM.
     */
    public static DatacenterResult build(CloudSimPlus simulation) {
        List<Host> hostList = createHosts();
        List<Vm> vmList = createVms();

        // Datacenter dengan kebijakan alokasi VmAllocationPolicyBestFit
        Datacenter datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyBestFit());

        printDatacenterInfo(hostList, vmList);

        return new DatacenterResult(datacenter, vmList);
    }

    /**
     * Membuat 8 Host heterogen (4 tipe x 2 unit).
     */
    private static List<Host> createHosts() {
        List<Host> hostList = new ArrayList<>();
        String[] typeNames = {"A", "B", "C", "D"};

        for (int type = 0; type < HOST_TYPES.length; type++) {
            int mipsPerCore = HOST_TYPES[type][0];
            int ramGB = HOST_TYPES[type][1];

            for (int unit = 0; unit < NUM_HOSTS_PER_TYPE; unit++) {
                List<Pe> peList = new ArrayList<>();
                for (int pe = 0; pe < PES_PER_HOST; pe++) {
                    peList.add(new PeSimple(mipsPerCore));
                }

                long ramMB = (long) ramGB * 1024;
                Host host = new HostSimple(ramMB, HOST_BW, HOST_STORAGE, peList)
                        .setVmScheduler(new VmSchedulerTimeShared());

                // Set SPECpower benchmark power model
                host.setPowerModel(new PowerModelHostSimple(
                        POWER_SPECS[type][1],  // max power (Watts)
                        POWER_SPECS[type][0]   // idle power (Watts)
                ));

                host.enableUtilizationStats();
                hostList.add(host);

                System.out.printf("  [Host] Tipe %s (#%d): %d MIPS/core x %d PEs, %d GB RAM, %d Gbps BW%n",
                        typeNames[type], unit + 1, mipsPerCore, PES_PER_HOST, ramGB, (int)(HOST_BW / 1000));
            }
        }

        return hostList;
    }

    /**
     * Membuat 30 VM heterogen (10 Small + 10 Medium + 10 Large).
     * Menggunakan CloudletSchedulerSpaceShared untuk eksekusi non-preemptive.
     */
    private static List<Vm> createVms() {
        List<Vm> vmList = new ArrayList<>();
        String[] typeNames = {"Small", "Medium", "Large"};

        for (int type = 0; type < VM_TYPES.length; type++) {
            int pes = VM_TYPES[type][0];
            int ramMB = VM_TYPES[type][1];
            int mips = VM_TYPES[type][2];
            int count = VM_TYPES[type][3];

            for (int i = 0; i < count; i++) {
                Vm vm = new VmSimple(mips, pes)
                        .setRam(ramMB)
                        .setBw(250)       // 250 Mbps per VM
                        .setSize(10_000)  // 10 GB storage
                        .setCloudletScheduler(new CloudletSchedulerSpaceShared());

                vmList.add(vm);
            }

            System.out.printf("  [VM] %s: %d MIPS/PE x %d PEs, %d MB RAM x %d units%n",
                    typeNames[type], mips, pes, ramMB, count);
        }

        return vmList;
    }

    private static void printDatacenterInfo(List<Host> hostList, List<Vm> vmList) {
        long totalHostMips = hostList.stream()
                .mapToLong(h -> h.getPeList().stream().mapToLong(Pe::getCapacity).sum())
                .sum();
        long totalHostRam = hostList.stream().mapToLong(h -> h.getRam().getCapacity()).sum();

        System.out.println("\n  ================================================");
        System.out.println("           RINGKASAN DATACENTER                   ");
        System.out.println("  ================================================");
        System.out.printf("  Jumlah Host : %d unit%n", hostList.size());
        System.out.printf("  Jumlah VM   : %d unit%n", vmList.size());
        System.out.printf("  Total MIPS  : %d MIPS%n", totalHostMips);
        System.out.printf("  Total RAM   : %d MB (%.1f GB)%n", totalHostRam, totalHostRam / 1024.0);
        System.out.println("  ================================================\n");
    }

    public static record DatacenterResult(Datacenter datacenter, List<Vm> vmList) {}
}
