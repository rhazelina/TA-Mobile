package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DashboardWaliKelasActivity : AppCompatActivity() {

    // Deklarasi komponen UI
    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView

    // TAMBAHKAN INI - TextView tanggal di jam layout
    private lateinit var txtTanggalDiJamLayout: TextView

    // Counter kehadiran
    private lateinit var txtNominalSiswa: TextView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView

    // Handler untuk update waktu live
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    // Data dummy
    private var totalSiswa = 30
    private var hadirCount = 20
    private var izinCount = 3
    private var sakitCount = 2
    private var alphaCount = 5

    // RecyclerView
    private lateinit var recyclerJadwal: RecyclerView
    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var jadwalAdapter: JadwalAdapter
    private lateinit var riwayatAdapter: RiwayatAbsenAdapter

    // Executor untuk scheduled tasks
    private val executor = Executors.newSingleThreadScheduledExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_wali_kelas)

        // Inisialisasi komponen UI
        initViews()

        // Setup tanggal dan waktu
        setupDateTime()

        // Setup data kehadiran
        setupKehadiranData()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup footer navigation (UNIVERSAL - sesuai layout)
        setupFooterNavigation()

        // Setup button listeners untuk kehadiran (bukan footer)
        setupKehadiranButtons()
    }

    private fun initViews() {
        // Tanggal dan waktu
        txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
        txtWaktuLive = findViewById(R.id.txtWaktuLive)
        txtJamMasuk = findViewById(R.id.txtJamMasuk)
        txtJamPulang = findViewById(R.id.txtJamPulang)

        // TAMBAHKAN INI - TextView tanggal di jam layout
        txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)

        // Counter kehadiran
        txtNominalSiswa = findViewById(R.id.nominal_siswa)
        txtHadirCount = findViewById(R.id.txt_hadir_count)
        txtIzinCount = findViewById(R.id.txt_izin_count)
        txtSakitCount = findViewById(R.id.txt_sakit_count)
        txtAlphaCount = findViewById(R.id.txt_alpha_count)

        // RecyclerView
        recyclerJadwal = findViewById(R.id.recyclerJadwal)
        recyclerRiwayat = findViewById(R.id.recyclerJadwal1)
    }

    private fun setupDateTime() {
        // Format tanggal Indonesia dengan locale Indonesia
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val currentDate = Date()
        val tanggalHariIni = dateFormat.format(currentDate)

        // Ubah ke format: SENIN, 1 JANUARI 2025 (huruf besar sesuai UI)
        val tanggalFormatBesar = tanggalHariIni.toUpperCase(Locale.forLanguageTag("id-ID"))

        // Set tanggal dengan format yang benar - UPDATE SEMUA
        txtTanggalSekarang.text = tanggalFormatBesar
        txtTanggalDiJamLayout.text = tanggalFormatBesar // TAMBAHKAN INI

        // Set jam pembelajaran default
        txtJamMasuk.text = "07:00:00"
        txtJamPulang.text = "15:00:00"

        // Setup waktu live yang berjalan terus (WIB - Indonesia Barat)
        runnable = object : Runnable {
            override fun run() {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                val currentTime = timeFormat.format(Date())
                txtWaktuLive.text = currentTime
                handler.postDelayed(this, 1000)
            }
        }

        // Mulai update waktu
        handler.post(runnable)
    }

    private fun setupKehadiranData() {
        // Update tampilan dengan data dummy
        updateKehadiranDisplay()

        // PERBAIKAN: Ganti scheduleAtFixedRate dengan schedule (lebih aman untuk Android)
        executor.schedule({
            runOnUiThread {
                // Simulasi perubahan kecil pada data
                hadirCount += (0..2).random()
                izinCount += (0..1).random()
                sakitCount += (0..1).random()
                alphaCount += (0..1).random()
                updateKehadiranDisplay()
            }
        }, 30, TimeUnit.SECONDS)
    }

    private fun updateKehadiranDisplay() {
        txtNominalSiswa.text = totalSiswa.toString()
        txtHadirCount.text = hadirCount.toString()
        txtIzinCount.text = izinCount.toString()
        txtSakitCount.text = sakitCount.toString()
        txtAlphaCount.text = alphaCount.toString()
    }

    private fun setupRecyclerView() {
        // Setup jadwal
        val jadwalList = generateDummyJadwal()
        jadwalAdapter = JadwalAdapter(jadwalList) { jadwal ->
            navigateToDetailJadwalWakel(jadwal)
        }

        recyclerJadwal.layoutManager = LinearLayoutManager(this)
        recyclerJadwal.adapter = jadwalAdapter
        recyclerJadwal.setHasFixedSize(true)

        // Setup riwayat absen
        val riwayatList = generateDummyRiwayat()
        riwayatAdapter = RiwayatAbsenAdapter(riwayatList)

        recyclerRiwayat.layoutManager = LinearLayoutManager(this)
        recyclerRiwayat.adapter = riwayatAdapter
        recyclerRiwayat.setHasFixedSize(true)
    }

    private fun generateDummyJadwal(): List<DashboardGuruActivity.JadwalItem> {
        val kelasList = listOf(
            "XI RPL 1", "XI RPL 2", "XI RPL 3",
            "XI Mekatronika 1", "XI Mekatronika 2",
            "XI TKJ 1", "XI TKJ 2",
            "XI DKV 1", "XI DKV 2",
            "XI Animasi 1", "XI Animasi 2"
        )

        val mapelList = listOf(
            "Matematika", "Bahasa Indonesia", "Pemrograman Dasar",
            "Basis Data", "Fisika", "Kimia", "Sejarah",
            "Seni Budaya", "PJOK", "Bahasa Inggris", "PKN"
        )

        val waktuPelajaranList = listOf(
            "Jam Pertama", "Jam Kedua", "Jam Ketiga",
            "Jam Keempat", "Jam Kelima", "Jam Keenam",
            "Jam Ketujuh", "Jam Kedelapan", "Jam Kesembilan",
            "Jam Kesepuluh", "Jam Kesebelas"
        )

        val jadwalList = mutableListOf<DashboardGuruActivity.JadwalItem>()
        val waktuMulai = listOf(
            "07:30", "08:15", "09:00", "09:45", "10:30",
            "11:15", "12:00", "12:45", "13:30", "14:15", "15:00"
        )
        val waktuSelesai = listOf(
            "08:15", "09:00", "09:45", "10:30", "11:15",
            "12:00", "12:45", "13:30", "14:15", "15:00", "15:45"
        )

        for (i in 0 until 11) {
            jadwalList.add(
                DashboardGuruActivity.JadwalItem(
                    id = i + 1,
                    mataPelajaran = mapelList[i],
                    waktuPelajaran = waktuPelajaranList[i],
                    kelas = kelasList[i],
                    jam = "${waktuMulai[i]} - ${waktuSelesai[i]}",
                    idKelas = kelasList[i].replace(" ", ""),
                    idMapel = mapelList[i].take(3).uppercase()
                )
            )
        }

        return jadwalList
    }

    private fun generateDummyRiwayat(): List<RiwayatAbsenItem> {
        val siswaList = listOf(
            "Fahmi", "Rizky", "Siti", "Ahmad", "Dewi",
            "Budi", "Citra", "Eko", "Fitri", "Gunawan"
        )

        val jurusanList = listOf(
            "XI RPL 1", "XI RPL 2", "XI TKJ 1", "XI Mekatronika 1",
            "XI DKV 1", "XI Animasi 1", "XI RPL 3", "XI TKJ 2",
            "XI Mekatronika 2", "XI DKV 2"
        )

        val statusList = listOf("hadir", "sakit", "izin", "alpha")
        val tanggalList = listOf(
            "17-Agustus-2026", "18-Agustus-2026", "19-Agustus-2026",
            "20-Agustus-2026", "21-Agustus-2026", "22-Agustus-2026",
            "23-Agustus-2026", "24-Agustus-2026", "25-Agustus-2026",
            "26-Agustus-2026"
        )

        val waktuList = listOf("07:00", "07:15", "07:30", "08:00", "08:30",
            "09:00", "09:30", "10:00", "10:30", "11:00")

        val riwayatList = mutableListOf<RiwayatAbsenItem>()

        for (i in 0 until 10) {
            riwayatList.add(
                RiwayatAbsenItem(
                    id = i + 1,
                    namaSiswa = siswaList[i],
                    jurusan = jurusanList[i],
                    tanggal = tanggalList[i],
                    waktu = waktuList[i],
                    status = statusList[i % 4]
                )
            )
        }

        return riwayatList
    }

    private fun navigateToDetailJadwalWakel(jadwal: DashboardGuruActivity.JadwalItem) {
        val intent = Intent(this, DetailJadwalWakelActivity::class.java).apply {
            putExtra("JADWAL_DATA", DashboardGuruActivity.JadwalData(
                mataPelajaran = jadwal.mataPelajaran,
                kelas = jadwal.kelas,
                jam = jadwal.jam,
                waktuPelajaran = jadwal.waktuPelajaran
            ))
        }
        startActivity(intent)
        Toast.makeText(this, "Membuka detail jadwal: ${jadwal.mataPelajaran}", Toast.LENGTH_SHORT).show()
    }

    private fun setupFooterNavigation() {
        val btnHome: ImageButton = findViewById(R.id.btnHome)
        val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
        val btnChart: ImageButton = findViewById(R.id.btnChart)
        val btnNotif: ImageButton = findViewById(R.id.btnNotif)

        // Navigasi WALI KELAS yang benar
        btnHome.setOnClickListener {
            // Sudah di dashboard wali kelas
            refreshDashboard()
            Toast.makeText(this, "Dashboard Wali Kelas direfresh", Toast.LENGTH_SHORT).show()
        }

        btnCalendar.setOnClickListener {
            // Navigasi ke Riwayat Kehadiran KELAS
            val intent = Intent(this, RiwayatKehadiranKelasActivity::class.java)
            startActivity(intent)
        }

        btnChart.setOnClickListener {
            val intent = Intent(this, TindakLanjutWaliKelasActivity::class.java)
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            // Navigasi ke Notifikasi Wali Kelas
            val intent = Intent(this, NotifikasiWaliKelasActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupKehadiranButtons() {
        // Button kehadiran (bukan bagian dari footer)
        val buttonHadir: ImageButton = findViewById(R.id.button_hadir)
        val buttonSakit: ImageButton = findViewById(R.id.button_sakit)
        val jumlahSiswaWakel: ImageButton = findViewById(R.id.jumlah_siswa_wakel)

        buttonHadir.setOnClickListener {
            Toast.makeText(this, "Menampilkan siswa hadir: $hadirCount siswa", Toast.LENGTH_SHORT).show()
        }

        buttonSakit.setOnClickListener {
            Toast.makeText(this, "Menampilkan detail kehadiran", Toast.LENGTH_SHORT).show()
        }

        jumlahSiswaWakel.setOnClickListener {
            Toast.makeText(this, "Total siswa: $totalSiswa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDashboard() {
        // Refresh data kehadiran
        totalSiswa = 30
        hadirCount = 20
        izinCount = 3
        sakitCount = 2
        alphaCount = 5
        updateKehadiranDisplay()

        // Refresh jadwal
        val jadwalList = generateDummyJadwal()
        jadwalAdapter = JadwalAdapter(jadwalList) { jadwal ->
            navigateToDetailJadwalWakel(jadwal)
        }
        recyclerJadwal.adapter = jadwalAdapter

        // Refresh riwayat
        val riwayatList = generateDummyRiwayat()
        riwayatAdapter = RiwayatAbsenAdapter(riwayatList)
        recyclerRiwayat.adapter = riwayatAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        executor.shutdownNow()
    }
}