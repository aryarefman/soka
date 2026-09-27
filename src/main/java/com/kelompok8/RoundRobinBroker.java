package com.kelompok8;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.*;

/**
 * Round Robin Broker.
 *
 * Algoritma baseline pembanding 2.
 * Mendistribusikan task secara bergiliran (Round Robin) ke seluruh
 * VM yang memenuhi batasan kapasitas (P_vm >= P_task) tanpa memperhatikan
 * panjang task, MIPS VM, maupun beban antrian saat ini.
 */
public class RoundRobinBroker extends DatacenterBrokerSimple {

    public RoundRobinBroker(final CloudSimPlus simulation) {
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

        // Pointer putaran round-robin terpisah untuk tiap kelas kebutuhan PE
        Map<Integer, Integer> roundRobinPointers = new HashMap<>();

        for (Cloudlet cloudlet : waitingList) {
            int taskPes = (int) cloudlet.getPesNumber();

            // Filter VM yang memenuhi batasan kapasitas
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
