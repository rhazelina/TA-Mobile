package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiGuruActivity : AppCompatActivity() {

    // RecyclerView untuk notifikasi hari ini
    private lateinit var rvHariIni: RecyclerView
    private lateinit var tvHariTanggal: TextView // Tambahkan ini

    // Tombol navigasi footer
    private lateinit var btnHome: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    // Adapter
    private lateinit var adapterHariIni: NotifikasiAdapter

    // Data untuk notifikasi hari ini
    private val dataHariIni = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifikasi_guru)

        initViews()
        setupFooterNavigation()
        setupRecyclerView()
        generateDummyData()
        loadDataToAdapter()
        updateTanggalRealTime() // Tambahkan ini
    }

    private fun initViews() {
        rvHariIni = findViewById(R.id.rvNotifHariIni)
        tvHariTanggal = findViewById(R.id.tvharitanggal) // Tambahkan ini

        // Inisialisasi tombol navigasi footer
        btnHome = findViewById(R.id.btnHome)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnChart = findViewById(R.id.btnChart)
        btnNotif = findViewById(R.id.btnNotif)
    }

    private fun updateTanggalRealTime() {
        try {
            // Format Indonesia Barat (WIB) - TimeZone Asia/Jakarta
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))

            // Format: "Senin, 20 Januari 2025"
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            val formattedDate = sdf.format(calendar.time)

            // Kapital huruf pertama
            val finalDate = if (formattedDate.isNotEmpty()) {
                formattedDate[0].uppercaseChar() + formattedDate.substring(1)
            } else {
                formattedDate
            }

            tvHariTanggal.text = finalDate
        } catch (e: Exception) {
            // Fallback jika error
            val calendar = Calendar.getInstance()
            val fallback = "${calendar.get(Calendar.DAY_OF_MONTH)} ${getMonthName(calendar.get(Calendar.MONTH))} ${calendar.get(Calendar.YEAR)}"
            tvHariTanggal.text = fallback
            Toast.makeText(this, "Format tanggal error, gunakan fallback", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            0 -> "Januari"
            1 -> "Februari"
            2 -> "Maret"
            3 -> "April"
            4 -> "Mei"
            5 -> "Juni"
            6 -> "Juli"
            7 -> "Agustus"
            8 -> "September"
            9 -> "Oktober"
            10 -> "November"
            11 -> "Desember"
            else -> ""
        }
    }

    private fun setupFooterNavigation() {
        // Navigasi untuk GURU

        btnHome.setOnClickListener {
            // Ke Dashboard Guru
            val intent = Intent(this, DashboardGuruActivity::class.java)
            startActivity(intent)
        }

        btnCalendar.setOnClickListener {
            // Ke Riwayat Kehadiran Guru
            val intent = Intent(this, RiwayatKehadiranGuruActivity::class.java)
            startActivity(intent)
        }

        btnChart.setOnClickListener {
            // Ke Tindak Lanjut Guru
            val intent = Intent(this, TindakLanjutGuruActivity::class.java)
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            // Sudah di halaman notifikasi, refresh saja
            refreshNotifications()
        }
    }

    private fun refreshNotifications() {
        // Refresh data notifikasi DAN tanggal
        updateTanggalRealTime()
        generateDummyData()
        loadDataToAdapter()
        Toast.makeText(this, "Notifikasi direfresh", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        // Setup untuk Hari Ini
        adapterHariIni = NotifikasiAdapter(dataHariIni, true) // role: true = Guru
        rvHariIni.layoutManager = LinearLayoutManager(this)
        rvHariIni.adapter = adapterHariIni
    }

    private fun generateDummyData() {
        // Data Hari Ini (7 notifikasi)
        dataHariIni.clear()

        // Dapatkan tanggal saat ini untuk notifikasi
        val currentDate = getCurrentFormattedDate()

        dataHariIni.addAll(listOf(
            mapOf(
                "type" to "tepat_waktu",
                "message" to "Anda mengajar tepat waktu pada",
                "detail" to "Pelajaran Matematika-XII RPL 1",
                "time" to "07:30",
                "date" to currentDate
            ),
            mapOf(
                "type" to "terlambat",
                "message" to "Anda terlambat mengajar pada",
                "detail" to "Pelajaran Bahasa Inggris-XII TKJ 2",
                "time" to "09:15",
                "date" to currentDate
            ),
            mapOf(
                "type" to "alpha_siswa",
                "message" to "Ada siswa alpha pada kelas",
                "detail" to "XII Mekatronika 1 - Mapel Fisika",
                "time" to "11:00",
                "date" to currentDate
            ),
            mapOf(
                "type" to "tindak_lanjut",
                "message" to "Perlu tindak lanjut untuk siswa",
                "detail" to "Budi Santoso - XII DKV 1",
                "time" to "13:45",
                "date" to currentDate
            ),
            mapOf(
                "type" to "izin_siswa",
                "message" to "Ada permohonan izin dari",
                "detail" to "Cindy Permata - XII Animasi 2",
                "time" to "08:30",
                "date" to currentDate
            ),
            mapOf(
                "type" to "rapor_kehadiran",
                "message" to "Rapor kehadiran bulan ini sudah tersedia",
                "detail" to "Cek di menu laporan",
                "time" to "10:00",
                "date" to currentDate
            ),
            mapOf(
                "type" to "reminder",
                "message" to "Reminder: Anda mengajar besok pada",
                "detail" to "Pelajaran Kimia-XII EI 1 jam 07:30",
                "time" to "16:00",
                "date" to currentDate
            )
        ))
    }

    private fun getCurrentFormattedDate(): String {
        try {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            return dateFormat.format(calendar.time)
        } catch (e: Exception) {
            val calendar = Calendar.getInstance()
            return "${calendar.get(Calendar.DAY_OF_MONTH)} ${getMonthName(calendar.get(Calendar.MONTH))} ${calendar.get(Calendar.YEAR)}"
        }
    }

    private fun loadDataToAdapter() {
        adapterHariIni.notifyDataSetChanged()
    }
}