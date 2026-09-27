package com.kelompok8;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.*;

/**
 * FCFS (First Come First Served) Broker.
 *
 * Algoritma baseline pembanding 1.
 * Task dilayani secara berurutan sesuai waktu kedatangan (FIFO).
 * Setiap task dialokasikan ke VM yang memenuhi batasan kapasitas
 * secara bergiliran tanpa memprediksi waktu penyelesaian ataupun beban kerja.
 */
public class FcfsBroker extends DatacenterBrokerSimple {

    public FcfsBroker(final CloudSimPlus simulation) {
        super(simulation);
    }

    @Override
    protected void requestDatacentersToCreateWaitingCloudlets() {
        final List<Cloudlet> waitingList = getCloudletWaitingList();
        final List<Vm> vmList = getVmExecList();

        if (waitingList.isEmpty() || vmList.isEmpty()) {
            super.requestDatacentersToCreateWaitingCloudlets();
            return;
        }

        // Urutkan murni berdasarkan waktu kedatangan (FCFS ordering)
        waitingList.sort(Comparator.comparingDouble(Cloudlet::getSubmissionDelay)
                .thenComparingLong(Cloudlet::getId));

        // Kelompokkan VM yang memenuhi kapasitas untuk tiap kebutuhan PE
        // Index berputar untuk masing-masing kelas PE agar beban terdistribusi merata
        Map<Integer, Integer> roundRobinPointers = new HashMap<>();

        for (Cloudlet cloudlet : waitingList) {
            int taskPes = (int) cloudlet.getPesNumber();

            // Filter VM yang memenuhi batasan kapasitas (P_vm >= P_task)
            List<Vm> eligibleVms = new ArrayList<>();
            for (Vm vm : vmList) {
                if (vm.getPesNumber() >= taskPes) {
                    eligibleVms.add(vm);
                }
            }

            if (!eligibleVms.isEmpty()) {
                int ptr = roundRobinPointers.getOrDefault(taskPes, 0);
                Vm selectedVm = eligibleVms.get(ptr % eligibleVms.size());
                cloudlet.setVm(selectedVm);
                roundRobinPointers.put(taskPes, ptr + 1);
            }
        }

        super.requestDatacentersToCreateWaitingCloudlets();
    }
}
