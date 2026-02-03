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

class DashboardGuruActivity : AppCompatActivity() {

    // Deklarasi komponen UI
    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView

    // TAMBAHKAN INI - TextView tanggal di jam layout
    private lateinit var txtTanggalDiJamLayout: TextView

    // Counter kehadiran
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView

    // RecyclerView
    private lateinit var recyclerJadwal: RecyclerView

    // Handler untuk update waktu live
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    // Data jadwal
    private val jadwalHariIni = mutableListOf<JadwalItem>()

    // Waktu pembelajaran (TETAP - sesuai permintaan)
    private val jamMasukDatabase = "07:00:00"
    private val jamPulangDatabase = "15:00:00"

    // Data kehadiran (data dummy)
    private var hadirCount = 0
    private var izinCount = 0
    private var sakitCount = 0
    private var alphaCount = 0

    // Executor untuk scheduled tasks
    private val executor = Executors.newSingleThreadScheduledExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_guru)

        // Inisialisasi komponen UI
        initViews()

        // Setup tanggal dan waktu
        setupDateTime()

        // Setup kehadiran siswa
        setupKehadiran()

        // Setup RecyclerView untuk jadwal
        setupRecyclerView()

        // Setup footer navigation (UNIVERSAL - sesuai layout)
        setupFooterNavigation()

        // Setup button click listeners untuk tombol kehadiran (bukan footer)
        setupKehadiranButtons()
    }

    private fun initViews() {
        try {
            // Tanggal dan waktu
            txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
            txtWaktuLive = findViewById(R.id.txtWaktuLive)
            txtJamMasuk = findViewById(R.id.txtJamMasuk)
            txtJamPulang = findViewById(R.id.txtJamPulang)

            // TAMBAHKAN INI - TextView tanggal di jam layout
            txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout) // Beri ID di XML!

            // Counter kehadiran
            txtHadirCount = findViewById(R.id.txt_hadir_count)
            txtIzinCount = findViewById(R.id.txt_izin_count)
            txtSakitCount = findViewById(R.id.txt_sakit_count)
            txtAlphaCount = findViewById(R.id.txt_alpha_count)

            // RecyclerView - PERBAIKAN: cek ID yang benar
            recyclerJadwal = findViewById(R.id.recyclerJadwal)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initViews: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupDateTime() {
        try {
            // Format tanggal Indonesia (PERBAIKAN: gunakan Locale.forLanguageTag)
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val currentDate = Date()
            val tanggalHariIni = dateFormat.format(currentDate)

            // Set tanggal - UPDATE SEMUA TEXTVIEW TANGGAL
            txtTanggalSekarang.text = tanggalHariIni

            // TAMBAHKAN INI - Update juga tanggal di layout jam
            txtTanggalDiJamLayout.text = tanggalHariIni

            // Set jam pembelajaran - TETAP sesuai permintaan
            txtJamMasuk.text = jamMasukDatabase
            txtJamPulang.text = jamPulangDatabase

            // Setup waktu live yang berjalan terus (WIB - Indonesia Barat)
            runnable = object : Runnable {
                override fun run() {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta") // WIB timezone
                    val currentTime = timeFormat.format(Date())
                    txtWaktuLive.text = currentTime
                    handler.postDelayed(this, 1000)
                }
            }

            // Mulai update waktu
            handler.post(runnable)
        } catch (e: Exception) {
            Toast.makeText(this, "Error setupDateTime: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupKehadiran() {
        try {
            // Data dummy kehadiran
            hadirCount = 25
            izinCount = 1
            sakitCount = 1
            alphaCount = 3

            // Update tampilan
            updateKehadiranCount()

            // PERBAIKAN: Ganti scheduleAtFixedRate dengan schedule (lebih aman untuk Android)
            executor.schedule({
                runOnUiThread {
                    // Simulasi perubahan kecil pada data
                    hadirCount += (0..2).random()
                    izinCount += (0..1).random()
                    sakitCount += (0..1).random()
                    alphaCount += (0..1).random()
                    updateKehadiranCount()
                }
            }, 30, TimeUnit.SECONDS)
        } catch (e: Exception) {
            Toast.makeText(this, "Error setupKehadiran: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateKehadiranCount() {
        try {
            txtHadirCount.text = hadirCount.toString()
            txtIzinCount.text = izinCount.toString()
            txtSakitCount.text = sakitCount.toString()
            txtAlphaCount.text = alphaCount.toString()
        } catch (e: Exception) {
            // Log error
        }
    }

    private fun setupRecyclerView() {
        try {
            // Bersihkan dan isi data dummy yang terstruktur
            jadwalHariIni.clear()
            jadwalHariIni.addAll(generateDummyJadwal())

            // Setup adapter - button tampilkan ke DetailJadwalGuruActivity
            val jadwalAdapter = JadwalAdapter(jadwalHariIni) { jadwal ->
                navigateToDetailJadwalGuru(jadwal)
            }

            // Setup layout manager
            recyclerJadwal.layoutManager = LinearLayoutManager(this).apply {
                orientation = LinearLayoutManager.VERTICAL
            }

            recyclerJadwal.adapter = jadwalAdapter
            recyclerJadwal.setHasFixedSize(true)
        } catch (e: Exception) {
            Toast.makeText(this, "Error setupRecyclerView: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun generateDummyJadwal(): List<JadwalItem> {
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

        // PERBAIKAN: Waktu pelajaran yang bermakna (bukan "Jam ke X")
        val waktuPelajaranList = listOf(
            "Jam Pertama", "Jam Kedua", "Jam Ketiga",
            "Jam Keempat", "Jam Kelima", "Jam Keenam",
            "Jam Ketujuh", "Jam Kedelapan", "Jam Kesembilan",
            "Jam Kesepuluh", "Jam Kesebelas"
        )

        val jadwalList = mutableListOf<JadwalItem>()
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
                JadwalItem(
                    id = i + 1,
                    mataPelajaran = mapelList[i],
                    waktuPelajaran = waktuPelajaranList[i], // PERBAIKAN: waktu yang bermakna
                    kelas = kelasList[i],
                    jam = "${waktuMulai[i]} - ${waktuSelesai[i]}",
                    idKelas = kelasList[i].replace(" ", ""),
                    idMapel = mapelList[i].take(3).uppercase()
                )
            )
        }

        return jadwalList
    }

    private fun navigateToDetailJadwalGuru(jadwal: JadwalItem) {
        try {
            val intent = Intent(this, DetailJadwalGuruActivity::class.java).apply {
                putExtra("JADWAL_DATA", JadwalData(
                    mataPelajaran = jadwal.mataPelajaran,
                    kelas = jadwal.kelas,
                    jam = jadwal.jam,
                    waktuPelajaran = jadwal.waktuPelajaran
                ))
            }
            startActivity(intent)
            Toast.makeText(this, "Membuka detail: ${jadwal.mataPelajaran}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error navigate: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupFooterNavigation() {
        try {
            val btnHome: ImageButton = findViewById(R.id.btnHome)
            val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
            val btnChart: ImageButton = findViewById(R.id.btnChart)
            val btnNotif: ImageButton = findViewById(R.id.btnNotif)

            // Navigasi sesuai struktur untuk GURU
            btnHome.setOnClickListener {
                // Sudah di halaman Dashboard Guru, cukup refresh
                refreshDashboard()
                Toast.makeText(this, "Dashboard Guru direfresh", Toast.LENGTH_SHORT).show()
            }

            btnCalendar.setOnClickListener {
                // Navigasi ke Riwayat Kehadiran GURU
                val intent = Intent(this, RiwayatKehadiranGuruActivity::class.java)
                startActivity(intent)
            }

            btnChart.setOnClickListener {
                // Navigasi ke Tindak Lanjut GURU
                val intent = Intent(this, TindakLanjutGuruActivity::class.java)
                startActivity(intent)
            }

            btnNotif.setOnClickListener {
                // Navigasi ke Notifikasi GURU
                val intent = Intent(this, NotifikasiGuruActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error footer nav: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupKehadiranButtons() {
        try {
            // Button kehadiran siswa (bukan bagian dari footer)
            val btnHadir: ImageButton = findViewById(R.id.button_hadir)
            val btnIzin: ImageButton = findViewById(R.id.button_izin)
            val btnSakit: ImageButton = findViewById(R.id.button_sakit)
            val btnAlpha: ImageButton = findViewById(R.id.button_alpha)

            btnHadir.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Hadir", Toast.LENGTH_SHORT).show()
            }

            btnIzin.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Izin", Toast.LENGTH_SHORT).show()
            }

            btnSakit.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Sakit", Toast.LENGTH_SHORT).show()
            }

            btnAlpha.setOnClickListener {
                Toast.makeText(this, "Lihat siswa Alpha", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error kehadiran buttons: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshDashboard() {
        // Refresh data kehadiran
        hadirCount = 25
        izinCount = 1
        sakitCount = 1
        alphaCount = 3
        updateKehadiranCount()

        // Refresh jadwal
        jadwalHariIni.clear()
        jadwalHariIni.addAll(generateDummyJadwal())

        // Notify adapter
        val adapter = recyclerJadwal.adapter
        adapter?.notifyItemRangeChanged(0, jadwalHariIni.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        executor.shutdownNow()
    }

    // Data class untuk jadwal
    data class JadwalItem(
        val id: Int,
        val mataPelajaran: String,
        val waktuPelajaran: String,
        val kelas: String,
        val jam: String,
        val idKelas: String,
        val idMapel: String
    )

    // Data class untuk passing data ke detail
    data class JadwalData(
        val mataPelajaran: String,
        val kelas: String,
        val jam: String,
        val waktuPelajaran: String
    ) : java.io.Serializable
}