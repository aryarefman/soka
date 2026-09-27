<p align="center">
  <img src="Assets/Lambang ITS PNG v1.png" alt="Logo Institut Teknologi Sepuluh Nopember" width="150">
</p>

<h1 align="center">LAPORAN TUGAS BESAR & SIMULASI</h1>
<h2 align="center">Strategi Optimasi Komputasi Awan (SOKA) — 2026</h2>
<h3 align="center"><em>Optimasi Penjadwalan Task Menggunakan Algoritma PEFT (Predict Earliest Finish Time) pada Simulator CloudSim Plus</em></h3>
<h4 align="center">Departemen Teknologi Informasi — Institut Teknologi Sepuluh Nopember (ITS)</h4>

<p align="center">
  <img src="https://img.shields.io/badge/Simulator-CloudSim_Plus_8.0.0-007ACC?style=flat-square&logo=java&logoColor=white" alt="CloudSim Plus">
  <img src="https://img.shields.io/badge/Algorithm-PEFT_Heuristic-4CAF50?style=flat-square" alt="PEFT">
  <img src="https://img.shields.io/badge/Baselines-FCFS_&_Round_Robin-FFA000?style=flat-square" alt="Baselines">
  <img src="https://img.shields.io/badge/Workload-NASA_iPSC_SWF_&_Synthetic-9C27B0?style=flat-square" alt="Workload">
  <img src="https://img.shields.io/badge/Language-Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Build-Gradle_8.x-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle">
</p>

---

## 👥 Anggota Kelompok 8

| No. | Nama Mahasiswa | NRP | Peran / Fokus |
| :---: | :--- | :---: | :--- |
| 1 | **Arya Bisma Putra Refman** | **5027241036** | Arsitektur Datacenter & Simulasi Runner |
| 2 | **Muhammad Ziddan Habibi** | **5027241122** | Formulasi & Algoritma PEFT |
| 3 | **Prabaswara Febrian Winandika** | **5027241069** | Algoritma Baseline (FCFS & Round Robin) |
| 4 | **Muhammad Fachry Shalahuddin Rusamsi** | **5027241031** | Preprocessing Dataset (SWF & Sintetis) |
| 5 | **Gemilang Ananda** | **5027241072** | Metrik Evaluasi & Model Energi SPECpower |

---

## 📑 Daftar Isi

- [1. Ringkasan: Ini Tugasnya Ngapain Sih?](#1-ringkasan-ini-tugasnya-ngapain-sih)
- [2. Arsitektur Datacenter Cloud (Tugas No. 2)](#2-arsitektur-datacenter-cloud-tugas-no-2)
  - [2.1 Konfigurasi Host Heterogen](#21-konfigurasi-host-heterogen-8-unit)
  - [2.2 Konfigurasi Virtual Machine (VM)](#22-konfigurasi-virtual-machine-vm-30-unit)
  - [2.3 Model Konsumsi Energi (SPECpower)](#23-model-konsumsi-energi-specpower)
- [3. Implementasi Algoritma Penjadwalan (Tugas No. 3)](#3-implementasi-algoritma-penjadwalan-tugas-no-3)
  - [3.1 Algoritma Utama: PEFT (Predict Earliest Finish Time)](#31-algoritma-utama-peft-predict-earliest-finish-time)
  - [3.2 Algoritma Baseline: FCFS dan Round Robin](#32-algoritma-baseline-fcfs-dan-round-robin)
  - [3.3 Batasan Kapasitas (Constraints)](#33-batasan-kapasitas-constraints)
- [4. Dataset Ujicoba (Tugas No. 4)](#4-dataset-ujicoba-tugas-no-4)
  - [4.1 Dataset Sintetis (1.000 Task)](#41-dataset-sintetis-1000-task)
  - [4.2 Dataset Real Trace NASA iPSC SWF (1.000 Task)](#42-dataset-real-trace-nasa-ipsc-swf-1000-task)
- [5. Hasil Pengujian & Analisis Perbandingan Metrik](#5-hasil-pengujian--analisis-perbandingan-metrik)
  - [5.1 Hasil Ujicoba Dataset Sintetis](#51-hasil-ujicoba-dataset-sintetis-1000-task)
  - [5.2 Hasil Ujicoba Dataset Real Trace NASA iPSC](#52-hasil-ujicoba-dataset-real-trace-nasa-ipsc-1000-task)
  - [5.3 Kesimpulan Analisis Hasil](#53-kesimpulan-analisis-hasil)
- [6. Panduan Menjalankan Simulasi (Tutorial Rekan Kelompok)](#6-panduan-menjalankan-simulasi-tutorial-rekan-kelompok)
- [7. Struktur Direktori Repository](#7-struktur-direktori-repository)
- [8. Referensi Pendukung](#8-referensi-pendukung)

---

## 1. Ringkasan: Ini Tugasnya Ngapain Sih?

### 🎯 Latar Belakang & Masalah
Di dunia komputasi awan (*cloud computing*), penyedia layanan memiliki ribuan server (*Host*) dan menyewakan mesin virtual (*VM*) dengan kapasitas yang berbeda-beda (*heterogen*). Setiap saat, ratusan hingga ribuan program atau tugas komputasi (*tasks/cloudlets*) masuk dari berbagai pengguna dengan ukuran yang bervariasi (ada task kecil, sedang, dan besar).

Jika kita menjadwalkan task ini secara sembarangan:
1. **Antrian Menumpuk**: Server lambat bisa kelebihan beban, sementara server cepat menganggur (*idle*).
2. **Makespan Lambat**: Waktu total selesainya semua pekerjaan menjadi sangat lama.
3. **Boros Listrik**: Server menyala lama tanpa efisiensi, membuang-buang energi listrik (*Joule / kWh*).

### 💡 Solusi yang Kita Bangun
Tugas ini mengimplementasikan dan membuktikan bahwa **Algoritma Optimasi Penjadwalan Task** mampu menyelesaikan masalah tersebut. Kita memilih algoritma **PEFT (*Predict Earliest Finish Time*)**:
* Sebelum menaruh task ke VM, algoritma **memprediksi kapan task tersebut akan selesai** di setiap VM yang memenuhi syarat.
* Task diprioritaskan dan dialokasikan ke VM yang menghasilkan **waktu selesai paling cepat (*Earliest Finish Time*)**.
* Kinerja PEFT kemudian **dibandingkan secara *head-to-head*** dengan 2 metode standar industri: **FCFS (*First Come First Served*)** dan **Round Robin (RR)** pada **1.000 task**.

---

## 2. Arsitektur Datacenter Cloud (Tugas No. 2)

Sesuai dokumen rancangan [Tugas 3](Tugas_3_Kelompok_8%20(2).pdf), simulasi dibangun pada simulator **CloudSim Plus 8.0.0** dengan rincian berikut:

```
+--------------------------------------------------------------------------------+
|                             1 DATACENTER TUNGGAL                              |
+--------------------------------------------------------------------------------+
                                       |
                   +-------------------+-------------------+
                   |                                       |
          8 HOST HETEROGEN                       KEBIJAKAN ALOKASI VM
     - Tipe A (2 unit): 1.000 MIPS/core            - Best-Fit Allocation
     - Tipe B (2 unit): 1.500 MIPS/core            - Meminimalkan sisa resource
     - Tipe C (2 unit): 2.000 MIPS/core
     - Tipe D (2 unit): 3.000 MIPS/core
                   |
                   +-------------------+-------------------+
                                       |
                                30 VM HETEROGEN
                        - Small  (10 unit): 1 PE, 1.000 MIPS
                        - Medium (10 unit): 2 PE, 1.500 MIPS
                        - Large  (10 unit): 4 PE, 2.000 MIPS
                                       |
                   +-------------------+-------------------+
                                       |
                            1.000 TASK (CLOUDLETS)
                      - Model: Bag-of-Tasks (Independen)
                      - Eksekusi: Non-preemptive (Space-Shared)
```

### 2.1 Konfigurasi Host Heterogen (8 Unit)
Setiap host memiliki 8 core/PE, penyimpanan 1 TB, dan model daya SPECpower:

| Tipe Host | Core (PE) | MIPS/Core | RAM | Bandwidth | Idle Power | Max Power | Jumlah | Total MIPS |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Tipe A** | 8 | 1.000 | 8 GB | 10 Gbps | 93.7 W | 135.0 W | 2 | 16.000 |
| **Tipe B** | 8 | 1.500 | 16 GB | 10 Gbps | 105.0 W | 175.0 W | 2 | 24.000 |
| **Tipe C** | 8 | 2.000 | 24 GB | 10 Gbps | 120.0 W | 225.0 W | 2 | 32.000 |
| **Tipe D** | 8 | 3.000 | 32 GB | 10 Gbps | 140.0 W | 300.0 W | 2 | 48.000 |
| **Total** | **64 Core** | - | **160 GB** | - | - | - | **8 Unit** | **120.000 MIPS** |

### 2.2 Konfigurasi Virtual Machine (VM) (30 Unit)
VM dibagi menjadi tiga kelas ukuran dengan penjadwalan `CloudletSchedulerSpaceShared`:

| Kelas VM | PE (vCPU) | RAM | MIPS per PE | Bandwidth | Storage | Jumlah Unit | Deskripsi |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Small** | 1 | 1.024 MB (1 GB) | 1.000 MIPS | 250 Mbps | 10 GB | 10 unit | Task ringan (1 PE) |
| **Medium** | 2 | 2.048 MB (2 GB) | 1.500 MIPS | 250 Mbps | 10 GB | 10 unit | Task sedang (1–2 PE) |
| **Large** | 4 | 4.096 MB (4 GB) | 2.000 MIPS | 250 Mbps | 10 GB | 10 unit | Task berat (1–4 PE) |

### 2.3 Model Konsumsi Energi (SPECpower)
Sesuai Bagian E.1, konsumsi energi tidak dihitung asal-asalan, melainkan menggunakan model daya linier benchmark nyata **SPECpower** (Beloglazov & Buyya, 2012):
$$E_{\text{host}} = P_{\text{max}} \times t_{\text{busy}} + P_{\text{idle}} \times (Makespan - t_{\text{busy}})$$
$$\text{Total Energi (kWh)} = \frac{\sum E_{\text{host}} \text{ (Joule)}}{3.600.000}$$

---

## 3. Implementasi Algoritma Penjadwalan (Tugas No. 3)

### 3.1 Algoritma Utama: PEFT (*Predict Earliest Finish Time*)
Kode sumber: [`src/main/java/com/kelompok8/PeftBroker.java`](src/main/java/com/kelompok8/PeftBroker.java)

Cara kerja PEFT untuk *Bag-of-Tasks* multi-core:
1. **Urutkan Task**: Task diurutkan berdasarkan waktu kedatangan ($a_i$), dan jika tiba bersamaan, diprioritaskan task terpanjang (*Longest Processing Time* / LPT).
2. **Kesesuaian Kapasitas**: Hanya mengevaluasi VM yang memiliki core cukup ($P_{vm} \ge P_{task}$).
3. **Hitung EST (*Earliest Start Time*)**: Waktu tercepat saat setidaknya $P_{task}$ core pada VM tersebut telah bebas.
   $$\text{EST}(i, j) = \max(a_i, \text{Waktu Core Bebas ke-}P_{task})$$
4. **Hitung EET (*Estimated Execution Time*)**:
   $$\text{EET}(i, j) = \frac{\text{Panjang Instruksi (MI)}}{\text{MIPS Core VM}}$$
5. **Hitung EFT (*Earliest Finish Time*)**:
   $$\text{EFT}(i, j) = \text{EST}(i, j) + \text{EET}(i, j)$$
6. **Pilih VM Terbaik**: Alokasikan task ke VM dengan nilai $\text{EFT}$ paling minimum ($\min \text{EFT}$).

### 3.2 Algoritma Baseline: FCFS dan Round Robin
* **FCFS (*First Come First Served*)** ([`FcfsBroker.java`](src/main/java/com/kelompok8/FcfsBroker.java)): Task dilayani murni urut antrian kedatangan tanpa memperhatikan lama eksekusi.
* **Round Robin** ([`RoundRobinBroker.java`](src/main/java/com/kelompok8/RoundRobinBroker.java)): Task dibagikan secara bergiliran siklik merata ke seluruh VM yang memenuhi syarat kapasitas.

### 3.3 Batasan Kapasitas (*Constraints*)
Mengikuti **Batasan E.4** pada dokumen tugas:
* **Kesesuaian Alokasi**: Task 4 PE **hanya boleh** dieksekusi di VM Large (4 PE). Task 2 PE di VM Medium/Large. Task 1 PE di VM Small/Medium/Large.
* **Non-Preemptive**: Task yang sudah mulai berjalan tidak boleh dihentikan atau dipindah sampai selesai.
* **Eksklusivitas**: 1 task berjalan secara utuh pada 1 VM terpilih.

---

## 4. Dataset Ujicoba (Tugas No. 4)

Kode sumber parser: [`src/main/java/com/kelompok8/SwfParser.java`](src/main/java/com/kelompok8/SwfParser.java)

### 4.1 Dataset Sintetis (1.000 Task)
Dibangkitkan secara terkontrol sesuai karakteristik pada dokumen:
* **30% Task Kecil (300 unit)**: Panjang 500 – 5.000 MI, butuh 1 PE, waktu kedatangan $0-100$ detik.
* **40% Task Sedang (400 unit)**: Panjang 5.000 – 50.000 MI, butuh 2 PE, waktu kedatangan $0-200$ detik.
* **30% Task Besar (300 unit)**: Panjang 50.000 – 500.000 MI, butuh 4 PE, waktu kedatangan $0-300$ detik.

### 4.2 Dataset Real Trace NASA iPSC SWF (1.000 Task)
File dataset: [`dataset/NASA-iPSC-1993-3.1-cln.swf`](dataset/NASA-iPSC-1993-3.1-cln.swf)
* Berasal dari superkomputer Intel iPSC/860 NASA Ames Research Center melalui *Parallel Workloads Archive*.
* Menyajikan karakteristik beban nyata: waktu kedatangan acak hingga 48 jam ($175.941$ detik) dan distribusi panjang instruksi *heavy-tailed* (1.000 – 15.308.000 MI).

---

## 5. Hasil Pengujian & Analisis Perbandingan Metrik

Seluruh ujicoba telah selesai dijalankan dengan **1.000 dari 1.000 task sukses tereksekusi 100% tanpa error**.

### 5.1 Hasil Ujicoba Dataset Sintetis (1.000 Task)

| Metrik Evaluasi | PEFT (Diusulkan) | FCFS (Baseline 1) | Round Robin (Baseline 2) | Keunggulan PEFT vs FCFS | Keterangan |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Total Task Selesai** | **1.000** | 1.000 | 1.000 | **0,00%** | Seluruh task selesai sempurna |
| **Makespan (detik)** | **4.185,85 s** | 4.947,25 s | 4.574,99 s | **-15,39%** | **Selesai 761,4 detik lebih cepat** |
| **Turnaround Time (s)** | **679,79 s** | 699,92 s | 714,21 s | **-2,88%** | Waktu tanggap rata-rata lebih singkat |
| **Waiting Time (s)** | **631,04 s** | 652,26 s | 666,52 s | **-3,25%** | Waktu antri task berkurang |
| **Throughput (task/s)** | **0,2389** | 0,2021 | 0,2186 | **+18,21%** | **Kapasitas penyelesaian task naik 18%** |
| **Avg CPU Utilization** | **38,74%** | 31,95% | 34,57% | **+21,25%** | **Pemanfaatan prosesor lebih padat & produktif** |
| **Load Balance (Std Dev)** | **5,32** | 20,55 | 20,55 | **-74,11%** | **Beban antar VM jauh lebih seimbang** |
| **Konsumsi Energi (J)** | **6.139.056,7 J** | 7.048.601,1 J | 6.608.935,1 J | **-12,90%** | **Menghemat 909.544 Joule listrik** |
| **Konsumsi Energi (kWh)**| **1,7053 kWh** | 1,9579 kWh | 1,8358 kWh | **-12,90%** | **Efisiensi daya datacenter meningkat** |

### 5.2 Hasil Ujicoba Dataset Real Trace NASA iPSC (1.000 Task)

| Metrik Evaluasi | PEFT (Diusulkan) | FCFS (Baseline 1) | Round Robin (Baseline 2) | Keunggulan PEFT vs FCFS | Keterangan |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Total Task Selesai** | **1.000** | 1.000 | 1.000 | **0,00%** | 100% task tereksekusi tanpa gagal |
| **Makespan (detik)** | **180.250,21 s** | 180.250,21 s | 180.250,21 s | **0,00%** | Dibatasi waktu kedatangan task terakhir |
| **Turnaround Time (s)** | **198,95 s** | 262,44 s | 262,44 s | **-24,19%** | **Task selesai 63,5 detik lebih cepat per task** |
| **Waiting Time (s)** | **0,00 s** | 20,54 s | 20,54 s | **-100,00%** | **Antrean task dieliminasi total (nol detik)** |
| **Throughput (task/s)** | **0,0055** | 0,0055 | 0,0055 | **0,00%** | Sesuai laju beban trace 48 jam |
| **Konsumsi Energi (J)** | **171.177.116,6 J**| 172.363.475,9 J| 172.363.475,9 J| **-0,69%** | **Hemat 1.186.359 Joule listrik** |
| **Konsumsi Energi (kWh)**| **47,5492 kWh** | 47,8787 kWh | 47,8787 kWh | **-0,69%** | Pengurangan daya operasional datacenter |

### 5.3 Kesimpulan Analisis Hasil
1. **Kecepatan & Makespan**: Pada dataset sintetis, PEFT berhasil memangkas Makespan sebesar **15,39% (761,4 detik lebih singkat)** dibanding FCFS dan **8,51%** dibanding Round Robin karena PEFT secara cerdas memetakan task-task besar ke VM yang memiliki MIPS tinggi dan slot waktu kosong terdekat.
2. **Kualitas Layanan (QoS)**: Pada trace NASA iPSC, waktu tunggu (*waiting time*) berhasil **ditekan hingga 0,00 detik (-100%)** dan *Turnaround Time* berkurang drastis sebesar **24,19%**.
3. **Efisiensi Energi Hijau (*Green Cloud*)**: Karena waktu aktif host berkurang berkat selesainya task lebih awal, total konsumsi energi berkurang hingga **12,90% (hemat 909.544 Joule)** pada beban sintetis.
4. **Pemerataan Beban (*Load Balancing*)**: Nilai deviasi standar PEFT hanya **5,32** (dibandingkan FCFS dan RR yang mencapai **20,55**), membuktikan tidak ada VM yang menderita kelebihan antrian (*overloaded*) ataupun menganggur sia-sia (*underutilized*).

---

## 6. Panduan Menjalankan Simulasi (Tutorial Rekan Kelompok)

Halo teman-teman Kelompok 8! Kode proyek ini sudah dikemas rapi dengan Gradle Wrapper, jadi kalian **tidak perlu menginstal CloudSim atau Gradle secara manual**. Cukup pastikan komputer kalian sudah memiliki **Java 21** terpasang.

### Langkah 1: Clone Repository
```bash
git clone https://github.com/aryarefman/soka.git
cd soka
```

### Langkah 2: Menjalankan Seluruh Simulasi Sekaligus
Buka PowerShell atau Command Prompt di folder proyek, lalu ketik perintah berikut:
```powershell
.\gradlew.bat runAll
```
Perintah ini akan otomatis meng-compile kode Java, menjalankan simulasi pada Dataset Sintetis dan Dataset NASA iPSC untuk ketiga algoritma (PEFT, FCFS, Round Robin), mencetak tabel hasil di layar, dan menyimpan file CSV ke folder `results/`.

### Langkah 3: Menjalankan Algoritma Tertentu Saja (Opsional)
Jika hanya ingin melihat hasil dari algoritma tertentu:
```powershell
# Menjalankan PEFT saja
.\gradlew.bat runPEFT

# Menjalankan Baseline FCFS saja
.\gradlew.bat runFCFS

# Menjalankan Baseline Round Robin saja
.\gradlew.bat runRoundRobin
```

### Langkah 4: Membuka File Hasil CSV
Semua metrik tersimpan otomatis dalam format CSV dan dapat dibuka langsung menggunakan **Microsoft Excel** atau Google Sheets:
* `results/synthetic_results.csv` : Hasil eksperimen dataset sintetis.
* `results/nasa_ipsc_results.csv` : Hasil eksperimen dataset NASA iPSC.
* `results/all_results.csv`       : Rekapitulasi gabungan seluruh pengujian.

---

## 7. Struktur Direktori Repository

```
soka/
├── .gitignore                                 # Mengabaikan build/, cache, dan file temporary
├── build.gradle                               # Konfigurasi Gradle & pustaka CloudSim Plus 8.0.0
├── settings.gradle
├── gradlew.bat                                # Skrip eksekusi Gradle untuk sistem operasi Windows
├── Tugas_3_Kelompok_8 (2).pdf                 # Dokumen asli draft usulan proyek Kelompok 8
│
├── Assets/
│   └── Lambang ITS PNG v1.png                 # Logo resmi Institut Teknologi Sepuluh Nopember (ITS)
│
├── dataset/
│   ├── NASA-iPSC-1993-3.1-cln.swf             # Dataset asli real trace format SWF (1.500 baris)
│   └── NASA-iPSC-1993-3.1-cln.swf.gz
│
├── generate_swf.py                            # Skrip pendukung pembangkit model beban kerja
│
├── results/
│   ├── synthetic_results.csv                  # Hasil simulasi skenario sintetis
│   ├── nasa_ipsc_results.csv                  # Hasil simulasi skenario NASA iPSC
│   └── all_results.csv                        # Rekapitulasi perbandingan seluruh algoritma
│
└── src/main/
    ├── java/com/kelompok8/
    │   ├── DatacenterBuilder.java             # Builder 8 Host heterogen & 30 VM (Best-Fit)
    │   ├── PeftBroker.java                    # Implementasi Heuristik PEFT (Earliest Finish Time)
    │   ├── FcfsBroker.java                    # Implementasi Baseline First Come First Served
    │   ├── RoundRobinBroker.java              # Implementasi Baseline Round Robin
    │   ├── SwfParser.java                     # Preprocessor SWF trace & dataset sintetis
    │   ├── MetricsCollector.java              # Penghitung Makespan, Energi SPECpower, & Utilisasi
    │   └── MainSimulation.java                # Main entrypoint simulasi
    └── resources/
        └── logback.xml                        # Konfigurasi logging simulator CloudSim Plus
```

---

## 8. Referensi Pendukung

1. **Arabnejad H, Barbosa JG.** *List Scheduling on Heterogeneous Distributed Systems: Evaluation and Improvements.* IEEE Transactions on Parallel and Distributed Systems. 2014;25(12):3173–3182.
2. **Beloglazov A, Buyya R.** *Optimal online deterministic algorithms and adaptive heuristics for energy and performance efficient dynamic consolidation of virtual machines in cloud data centers.* Concurrency and Computation: Practice and Experience. 2012;24(13):1397–1420. *(Rujukan resmi model daya SPECpower CloudSim)*.
3. **Arunarani AR, Manjula D, Sugumaran V.** *Task scheduling techniques in cloud computing: A literature survey.* Future Generation Computer Systems. 2019;91:407–415.
4. **Silva Filho MC, et al.** *CloudSim Plus: A modern Java 8 framework for modeling and simulation of cloud computing infrastructures and services.* Concurrency and Computation: Practice and Experience. 2017.
5. **Parallel Workloads Archive.** *NASA Ames iPSC/860 Workload Trace.* Hebrew University of Jerusalem. [https://www.cs.huji.ac.il/labs/parallel/workload/l_nasa_ipsc/](https://www.cs.huji.ac.il/labs/parallel/workload/l_nasa_ipsc/).

---
<p align="center">
  <b>Kelompok 8 — Strategi Optimasi Komputasi Awan (SOKA)</b><br>
  Departemen Teknologi Informasi, Institut Teknologi Sepuluh Nopember (ITS)<br>
  Surabaya, Indonesia — 2026
</p>
