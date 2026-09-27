package com.kelompok8;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Simulasi Utama - Optimasi Penjadwalan Task pada Komputasi Awan
 *
 * Kelompok 8 - Strategi Optimasi Komputasi Awan
 * Departemen Teknologi Informasi, Institut Teknologi Sepuluh Nopember (ITS)
 *
 * Tugas:
 * 2. Bangun datacenter sesuai tugas sebelumnya di simulator.
 * 3. Implementasikan algoritma yang dipilih di simulator (PEFT).
 * 4. Jalankan ujicoba sesuai dengan dataset yang diajukan di minggu 3 (Sintetis & Real Trace NASA iPSC).
 *
 * Baseline:
 * - FCFS (First Come First Served)
 * - Round Robin
 */
public class MainSimulation {

    private static final int MAX_TASKS = 1000;
    private static final String DATASET_DIR = "dataset";
    private static final String SWF_FILE = "dataset/NASA-iPSC-1993-3.1-cln.swf";
    private static final String RESULTS_DIR = "results";

    public static void main(String[] args) throws Exception {
        printBanner();

        String mode = args.length > 0 ? args[0].toUpperCase() : "ALL";
        Files.createDirectories(Path.of(RESULTS_DIR));

        List<MetricsCollector.ExperimentResult> allResults = new ArrayList<>();

        // =========================================================================
        // SKENARIO 1: DATASET SINTETIS (1000 Task)
        // =========================================================================
        System.out.println("\n" + "=".repeat(75));
        System.out.println("  SKENARIO 1: DATASET SINTETIS (1000 TASKS - 3 KELAS UKURAN)");
        System.out.println("=".repeat(75));

        List<MetricsCollector.ExperimentResult> syntheticResults = new ArrayList<>();

        if (mode.equals("ALL") || mode.equals("PEFT")) {
            syntheticResults.add(runExperiment("PEFT", "Synthetic", null));
        }
        if (mode.equals("ALL") || mode.equals("FCFS")) {
            syntheticResults.add(runExperiment("FCFS", "Synthetic", null));
        }
        if (mode.equals("ALL") || mode.equals("RR") || mode.equals("ROUNDROBIN")) {
            syntheticResults.add(runExperiment("RR", "Synthetic", null));
        }

        if (syntheticResults.size() > 1) {
            MetricsCollector.printComparisonTable(syntheticResults);
            MetricsCollector.exportToCsv(syntheticResults, RESULTS_DIR + "/synthetic_results.csv");
            allResults.addAll(syntheticResults);
        }

        // =========================================================================
        // SKENARIO 2: DATASET REAL TRACE (NASA iPSC - SWF Format)
        // =========================================================================
        String swfPath = findSwfFile();
        if (swfPath != null) {
            System.out.println("\n\n" + "=".repeat(75));
            System.out.println("  SKENARIO 2: DATASET REAL TRACE (NASA iPSC - SWF FORMAT 1000 TASKS)");
            System.out.println("=".repeat(75));

            List<MetricsCollector.ExperimentResult> nasaResults = new ArrayList<>();

            if (mode.equals("ALL") || mode.equals("PEFT")) {
                nasaResults.add(runExperiment("PEFT", "NASA-iPSC", swfPath));
            }
            if (mode.equals("ALL") || mode.equals("FCFS")) {
                nasaResults.add(runExperiment("FCFS", "NASA-iPSC", swfPath));
            }
            if (mode.equals("ALL") || mode.equals("RR") || mode.equals("ROUNDROBIN")) {
                nasaResults.add(runExperiment("RR", "NASA-iPSC", swfPath));
            }

            if (nasaResults.size() > 1) {
                MetricsCollector.printComparisonTable(nasaResults);
                MetricsCollector.exportToCsv(nasaResults, RESULTS_DIR + "/nasa_ipsc_results.csv");
                allResults.addAll(nasaResults);
            }
        } else {
            System.out.println("\n  [PERINGATAN] Dataset SWF tidak ditemukan di: " + SWF_FILE);
        }

        // Simpan gabungan semua hasil
        if (!allResults.isEmpty()) {
            MetricsCollector.exportToCsv(allResults, RESULTS_DIR + "/all_results.csv");
        }

        printFooter();
    }

    private static MetricsCollector.ExperimentResult runExperiment(
            String algorithm, String datasetName, String swfPath) throws Exception {

        System.out.printf("%n  +-------------------------------------------------------------+%n");
        System.out.printf("  |  Algoritma : %-46s |%n", getAlgorithmName(algorithm));
        System.out.printf("  |  Dataset   : %-46s |%n", datasetName);
        System.out.printf("  |  Tasks     : %-46d |%n", MAX_TASKS);
        System.out.printf("  +-------------------------------------------------------------+%n%n");

        CloudSimPlus simulation = new CloudSimPlus();

        // 1. Bangun Datacenter (8 Host heterogen + 30 VM)
        System.out.println("  [1/4] Membangun Arsitektur Datacenter Cloud...");
        DatacenterBuilder.DatacenterResult dcResult = DatacenterBuilder.build(simulation);

        // 2. Buat Broker sesuai Algoritma
        System.out.println("  [2/4] Menginisialisasi Datacenter Broker (" + getAlgorithmName(algorithm) + ")...");
        DatacenterBroker broker = createBroker(algorithm, simulation);

        // 3. Muat Dataset (Sintetis / SWF NASA iPSC)
        System.out.println("  [3/4] Mempersiapkan Dataset (" + datasetName + ")...");
        List<Cloudlet> cloudlets;
        if (swfPath != null) {
            cloudlets = SwfParser.parse(swfPath, MAX_TASKS);
        } else {
            cloudlets = SwfParser.generateSyntheticDataset(MAX_TASKS, 42L);
        }

        // 4. Submit VM dan Cloudlet ke Broker
        broker.submitVmList(dcResult.vmList());
        broker.submitCloudletList(cloudlets);

        // 5. Jalankan Simulasi
        System.out.println("  [4/4] Menjalankan Simulasi CloudSim Plus...");
        long startTime = System.currentTimeMillis();
        simulation.start();
        long elapsedSimRealTime = System.currentTimeMillis() - startTime;

        // 6. Kumpulkan dan Evaluasi Metrik
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        List<Host> hostList = dcResult.datacenter().getHostList();

        String algoFullName = getAlgorithmName(algorithm) + " [" + datasetName + "]";
        MetricsCollector.ExperimentResult result = MetricsCollector.collectMetrics(
                algoFullName, finishedCloudlets, dcResult.vmList(), hostList, simulation.clock());

        System.out.printf("  Simulasi selesai dalam %d ms (Real-world execution time).%n", elapsedSimRealTime);
        MetricsCollector.printReport(result);
        MetricsCollector.printCloudletTable(finishedCloudlets, algorithm, 10);

        return result;
    }

    private static DatacenterBroker createBroker(String algorithm, CloudSimPlus simulation) {
        return switch (algorithm.toUpperCase()) {
            case "PEFT" -> new PeftBroker(simulation);
            case "FCFS" -> new FcfsBroker(simulation);
            case "RR", "ROUNDROBIN" -> new RoundRobinBroker(simulation);
            default -> {
                System.out.println("  Algoritma tidak dikenal: " + algorithm + ", menggunakan PEFT.");
                yield new PeftBroker(simulation);
            }
        };
    }

    private static String getAlgorithmName(String shortName) {
        return switch (shortName.toUpperCase()) {
            case "PEFT" -> "PEFT (Predict Earliest Finish Time)";
            case "FCFS" -> "FCFS (First Come First Served)";
            case "RR", "ROUNDROBIN" -> "Round Robin";
            default -> shortName;
        };
    }

    private static String findSwfFile() {
        if (Files.exists(Path.of(SWF_FILE))) return SWF_FILE;
        if (Files.exists(Path.of(SWF_FILE + ".gz"))) return SWF_FILE + ".gz";
        try {
            Path datasetDir = Path.of(DATASET_DIR);
            if (Files.exists(datasetDir)) {
                Optional<Path> swfFile = Files.list(datasetDir)
                        .filter(p -> p.toString().endsWith(".swf") || p.toString().endsWith(".swf.gz"))
                        .findFirst();
                return swfFile.map(Path::toString).orElse(null);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("====================================================================");
        System.out.println("       OPTIMASI PENJADWALAN TASK PADA KOMPUTASI AWAN                ");
        System.out.println("                                                                    ");
        System.out.println("   Kelompok 8 - Strategi Optimasi Komputasi Awan                    ");
        System.out.println("   Departemen Teknologi Informasi, ITS                              ");
        System.out.println("                                                                    ");
        System.out.println("   Algoritma Diusulkan : PEFT (Predict Earliest Finish Time)        ");
        System.out.println("   Algoritma Baseline  : FCFS, Round Robin                          ");
        System.out.println("   Simulator           : CloudSim Plus 8.0                          ");
        System.out.println("====================================================================");
    }

    private static void printFooter() {
        System.out.println("\n");
        System.out.println("====================================================================");
        System.out.println("  Seluruh eksperimen simulasi telah berhasil diselesaikan!          ");
        System.out.println("  Hasil CSV dan ringkasan metrik tersimpan di folder 'results/'.    ");
        System.out.println("====================================================================");
    }
}
