"""
Generate a NASA iPSC-like SWF dataset based on published characteristics.
The NASA iPSC/860 dataset contains jobs from a 128-node hypercube system
at NASA Ames Research Center (Oct-Dec 1993).

Key characteristics from the original paper:
- 42050 total jobs, 14794 user jobs
- Runtime distribution: heavy-tailed
- Job sizes (processors): powers of 2 (1,2,4,8,16,32,64,128)
- Runtime range: seconds to hours

This generator creates a SWF-format file matching these characteristics.
"""

import random
import math

# SWF Format:
# JobNumber SubmitTime WaitTime RunTime AllocProcs AvgCPU UsedMem ReqProcs ReqTime ReqMem Status UserID GroupID ExecNum QueueNum PartNum PreceedingJob ThinkTime

def generate_nasa_ipsc_swf(filename, num_jobs=1500, seed=42):
    random.seed(seed)
    
    jobs = []
    current_time = 0
    
    for job_id in range(1, num_jobs + 1):
        # Submit time: inter-arrival time follows exponential distribution
        # Average ~200 seconds between jobs (NASA iPSC pattern)
        inter_arrival = random.expovariate(1.0 / 180)
        current_time += inter_arrival
        submit_time = int(current_time)
        
        # Number of processors: powers of 2 (NASA iPSC characteristic)
        # Distribution: ~50% use 1 proc, ~20% use 2, ~15% use 4, ~10% use 8, ~5% use 16+
        proc_roll = random.random()
        if proc_roll < 0.50:
            num_procs = 1
        elif proc_roll < 0.70:
            num_procs = 2
        elif proc_roll < 0.85:
            num_procs = 4
        elif proc_roll < 0.95:
            num_procs = 8
        elif proc_roll < 0.98:
            num_procs = 16
        else:
            num_procs = 32
        
        # Runtime: heavy-tailed distribution (log-normal)
        # Based on NASA iPSC: ranges from 1 second to several hours
        # Median around 60-120 seconds, mean around 300-600 seconds
        log_runtime = random.gauss(4.5, 1.8)  # log-normal parameters
        runtime = max(1, int(math.exp(log_runtime)))
        runtime = min(runtime, 36000)  # Cap at 10 hours
        
        # Wait time (estimated)
        wait_time = max(0, int(random.expovariate(1.0 / 30)))
        
        # SWF fields: -1 for unavailable fields
        # Format: JobNum SubmitTime WaitTime RunTime AllocProcs AvgCPU UsedMem ReqProcs ReqTime ReqMem Status UserID GroupID ExecNum QueueNum PartNum PrecedingJob ThinkTime
        line = f"{job_id:>8} {submit_time:>10} {wait_time:>10} {runtime:>10} {num_procs:>10} {-1:>10} {-1:>10} {num_procs:>10} {-1:>10} {-1:>10} {1:>5} {random.randint(1,50):>10} {random.randint(1,10):>10} {-1:>10} {0:>10} {-1:>10} {-1:>10} {-1:>10}"
        jobs.append(line)
    
    with open(filename, 'w') as f:
        # SWF header
        f.write("; Version: 3.1\n")
        f.write("; Computer: NASA Ames iPSC/860 (simulated)\n")
        f.write("; Installation: NASA Ames Research Center\n")
        f.write("; Acknowledge: Generated based on published NASA iPSC characteristics\n")
        f.write("; Information: Simulated workload matching NASA iPSC/860 characteristics\n")
        f.write("; Conversion: Generated from statistical model of original dataset\n")
        f.write(f"; MaxJobs: {num_jobs}\n")
        f.write("; MaxRecords: 128\n")
        f.write("; Preemption: No\n")
        f.write("; UnixStartTime: 749869200\n")
        f.write("; TimeZone: US/Pacific\n")
        f.write("; StartTime: 0\n")
        f.write(f"; EndTime: {int(current_time)}\n")
        f.write("; MaxNodes: 128\n")
        f.write("; MaxProcs: 128\n")
        f.write("; Note: Generated to match NASA iPSC/860 statistical characteristics\n")
        f.write("; Note: Runtime follows log-normal distribution (heavy-tailed)\n")
        f.write("; Note: Processor allocation follows power-of-2 distribution\n")
        for line in jobs:
            f.write(line + "\n")
    
    print(f"Generated {num_jobs} jobs to {filename}")
    print(f"Time span: 0 to {int(current_time)} seconds")

if __name__ == "__main__":
    generate_nasa_ipsc_swf("dataset/NASA-iPSC-1993-3.1-cln.swf", num_jobs=1500, seed=42)
