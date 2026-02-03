package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranKelasSiswaActivity : AppCompatActivity() {

    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var txtFilterTanggal: TextView

    // PERHATIAN: Di layout siswa, ini adalah ImageButton
    private lateinit var btnCalendar: ImageButton

    private val riwayatList = mutableListOf<RiwayatSiswaItem>()

    // Data dummy statistik
    private var totalHadir = 0
    private var totalIzin = 0
    private var totalSakit = 0
    private var totalAlpha = 0

    private var selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.riwayat_kehadiran_kelas_siswa)

        // Inisialisasi views
        initViews()

        // Setup calendar button
        setupCalendarButton()

        // Setup statistik
        setupStatistik()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup tombol navigasi
        setupButtonListeners()

        // SETUP BACK PRESS HANDLER
        setupBackPressedHandler()
    }

    private fun initViews() {
        // TextView untuk statistik
        txtHadirCount = findViewById(R.id.txt_hadir_count)
        txtIzinCount = findViewById(R.id.txt_izin_count)
        txtSakitCount = findViewById(R.id.txt_sakit_count)
        txtAlphaCount = findViewById(R.id.txt_alpha_count)

        // TextView lainnya
        val txtJumlah: TextView = findViewById(R.id.text_jumlah_siswa)
        txtFilterTanggal = findViewById(R.id.text_filter_tanggal)

        // PERHATIAN: Di layout siswa, id nya adalah icon_calendar (ImageButton)
        btnCalendar = findViewById(R.id.icon_calendar)

        // SET TEKS SEDERHANA
        txtJumlah.text = "Total Mata Pelajaran: 6"
        updateTanggalDisplay()

        // RecyclerView
        recyclerRiwayat = findViewById(R.id.recycler_riwayat)
    }

    private fun setupCalendarButton() {
        btnCalendar.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateTanggalDisplay()
                filterDataByDate()
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateTanggalDisplay() {
        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formatted = sdf.format(selectedDate.time)

            val finalDate = if (formatted.isNotEmpty()) {
                formatted[0].uppercaseChar() + formatted.substring(1)
            } else {
                formatted
            }

            txtFilterTanggal.text = finalDate
        } catch (e: Exception) {
            Toast.makeText(this, "Error format tanggal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupStatistik() {
        // Hitung statistik dari data dummy
        calculateStatistics()

        // Tampilkan statistik
        txtHadirCount.text = totalHadir.toString()
        txtIzinCount.text = totalIzin.toString()
        txtSakitCount.text = totalSakit.toString()
        txtAlphaCount.text = totalAlpha.toString()
    }

    private fun calculateStatistics() {
        // Reset counter
        totalHadir = 0
        totalIzin = 0
        totalSakit = 0
        totalAlpha = 0

        // Data dummy untuk siswa
        val dummyData = listOf(
            RiwayatSiswaItem("1", "B. Indonesia", "Hadir Tepat Waktu", "hadir"),
            RiwayatSiswaItem("2", "Matematika", "Hadir Tepat Waktu", "hadir"),
            RiwayatSiswaItem("3", "IPA", "Izin Sakit", "izin"),
            RiwayatSiswaItem("4", "Bahasa Inggris", "Hadir Tepat Waktu", "hadir"),
            RiwayatSiswaItem("5", "IPS", "Alpha", "alpha"),
            RiwayatSiswaItem("6", "Seni Budaya", "Sakit", "sakit")
        )

        // Hitung statistik
        dummyData.forEach { item ->
            when (item.status.toLowerCase()) {
                "hadir" -> totalHadir++
                "izin" -> totalIzin++
                "sakit" -> totalSakit++
                "alpha" -> totalAlpha++
            }
        }

        // Tambahkan ke list
        riwayatList.clear()
        riwayatList.addAll(dummyData)
    }

    private fun filterDataByDate() {
        Toast.makeText(this, "Memfilter data untuk tanggal terpilih...", Toast.LENGTH_SHORT).show()
        // Di sini bisa reload data berdasarkan tanggal yang dipilih
    }

    private fun setupRecyclerView() {
        try {
            recyclerRiwayat.layoutManager = LinearLayoutManager(this)
            recyclerRiwayat.setHasFixedSize(true)

            val adapter = RiwayatSiswaAdapter(riwayatList)
            recyclerRiwayat.adapter = adapter

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        val btnHome: View = findViewById(R.id.btnHome)
        val btnAssignment: View = findViewById(R.id.btnAssignment)
        val textNavigasi: TextView = findViewById(R.id.text_navigasi)

        // Tombol Home - kembali ke dashboard
        btnHome.setOnClickListener {
            navigateToDashboard()
        }

        // Tombol Assignment - sudah di halaman ini
        btnAssignment.setOnClickListener {
            Toast.makeText(this, "Anda sudah di Riwayat Kehadiran", Toast.LENGTH_SHORT).show()
        }

        // Text Navigasi - navigasi ke Jadwal Harian
        textNavigasi.setOnClickListener {
            navigateToJadwalHarian()
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToDashboard()
            }
        })
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardSiswaActivity::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Kembali ke Dashboard", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToJadwalHarian() {
        val intent = Intent(this, JadwalHarianSiswaActivity::class.java).apply {
            putExtra("IS_PENGURUS", false) // false karena ini untuk siswa
        }
        startActivity(intent)
        Toast.makeText(this, "Melihat Jadwal Harian", Toast.LENGTH_SHORT).show()
    }

    // ========== ADAPTER UNTUK SISWA ==========
    private inner class RiwayatSiswaAdapter(
        private val riwayatList: List<RiwayatSiswaItem>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<RiwayatSiswaAdapter.RiwayatViewHolder>() {

        inner class RiwayatViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val txtSession: TextView = itemView.findViewById(R.id.Session)
            val txtMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
            val txtKeterangan: TextView = itemView.findViewById(R.id.TextKeteranganAbsen)
            val imgBadge: ImageView = itemView.findViewById(R.id.BadgeKehadiran)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_riwayat_kehadiran_siswa, parent, false)
            return RiwayatViewHolder(view)
        }

        override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
            val riwayat = riwayatList[position]

            holder.txtSession.text = "Mapel ${riwayat.id}"
            holder.txtMataPelajaran.text = riwayat.mataPelajaran
            holder.txtKeterangan.text = riwayat.keterangan

            // Set badge berdasarkan status
            when (riwayat.status.toLowerCase()) {
                "hadir" -> holder.imgBadge.setImageResource(R.drawable.siswa_hadir_wakel)
                "izin" -> holder.imgBadge.setImageResource(R.drawable.siswa_izin_wakel)
                "sakit" -> holder.imgBadge.setImageResource(R.drawable.siswa_sakit_wakel)
                "alpha" -> holder.imgBadge.setImageResource(R.drawable.siswa_alpha_wakel)
            }
        }

        override fun getItemCount(): Int = riwayatList.size
    }

    // ========== DATA CLASS UNTUK SISWA ==========
    data class RiwayatSiswaItem(
        val id: String,
        val mataPelajaran: String,
        val keterangan: String,
        val status: String // hadir, izin, sakit, alpha
    )
}