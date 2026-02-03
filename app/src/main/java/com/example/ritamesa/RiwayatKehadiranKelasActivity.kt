package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranKelasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtJumlahSiswa: TextView
    private lateinit var txtHadirCount: TextView
    private lateinit var txtSakitCount: TextView
    private lateinit var txtIzinCount: TextView
    private lateinit var txtAlphaCount: TextView
    private lateinit var txtFilterTanggal: TextView

    private lateinit var btnHadir: ImageButton
    private lateinit var btnSakit: ImageButton
    private lateinit var btnIzin: ImageButton
    private lateinit var btnAlpha: ImageButton
    private lateinit var iconCalendar: ImageView

    // Tombol navigasi footer
    private lateinit var btnHome: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    // Data untuk adapter
    private val allData = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
    private val filteredData = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
    private lateinit var adapter: SimpleSiswaAdapter
    private var filterActive: String? = null

    private val TOTAL_SISWA = 25
    private val handler = Handler(Looper.getMainLooper())
    private var isLoading = false

    // Warna untuk teks statistik
    private val textColorActive = android.graphics.Color.WHITE
    private val textColorNormal = android.graphics.Color.parseColor("#4B5563") // Abu-abu tua
    private val textColorDefault = android.graphics.Color.BLACK

    companion object {
        private const val TAG = "RiwayatKelasActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup global exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in ${thread.name}: ${throwable.message}")
            runOnUiThread {
                Toast.makeText(this, "Error aplikasi, restart...", Toast.LENGTH_SHORT).show()
            }
            handler.postDelayed({
                finish()
                startActivity(intent)
            }, 1500)
        }

        try {
            Log.d(TAG, "=== START RiwayatKehadiranKelasActivity ===")

            // Set layout dengan error handling
            try {
                setContentView(R.layout.riwayat_kehadiran_kelas)
                Log.d(TAG, "Layout berhasil di-set")
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL: Layout not found: ${e.message}")
                Toast.makeText(this, "Error layout", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Inisialisasi view
            if (!initializeViews()) {
                Toast.makeText(this, "Gagal memuat tampilan", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Setup komponen
            setupRecyclerView()
            setupFooterNavigation()
            setupFilterButtons()
            setupCalendarButton()

            // Set tanggal hari ini
            updateTanggalDisplay()

            // Set warna teks awal
            resetTextColors()

            // Load data
            handler.postDelayed({
                loadDataAsync()
            }, 300)

        } catch (e: Exception) {
            Log.e(TAG, "FATAL ERROR in onCreate: ${e.message}", e)
            showErrorAndExit("Gagal memuat halaman kelas")
        }
    }

    private fun initializeViews(): Boolean {
        return try {
            recyclerView = findViewById(R.id.recycler_riwayat)
                ?: throw NullPointerException("recycler_riwayat not found")
            txtJumlahSiswa = findViewById(R.id.text_jumlah_siswa)
                ?: throw NullPointerException("text_jumlah_siswa not found")
            txtHadirCount = findViewById(R.id.txt_hadir_count)
                ?: throw NullPointerException("txt_hadir_count not found")
            txtSakitCount = findViewById(R.id.txt_sakit_count)
                ?: throw NullPointerException("txt_sakit_count not found")
            txtIzinCount = findViewById(R.id.txt_izin_count)
                ?: throw NullPointerException("txt_izin_count not found")
            txtAlphaCount = findViewById(R.id.txt_alpha_count)
                ?: throw NullPointerException("txt_alpha_count not found")
            txtFilterTanggal = findViewById(R.id.text_filter_tanggal)
                ?: throw NullPointerException("text_filter_tanggal not found")

            btnHadir = findViewById(R.id.button_hadir)
                ?: throw NullPointerException("button_hadir not found")
            btnSakit = findViewById(R.id.button_sakit)
                ?: throw NullPointerException("button_sakit not found")
            btnIzin = findViewById(R.id.button_izin)
                ?: throw NullPointerException("button_izin not found")
            btnAlpha = findViewById(R.id.button_alpha)
                ?: throw NullPointerException("button_alpha not found")

            iconCalendar = findViewById(R.id.icon_calendar)
                ?: throw NullPointerException("icon_calendar not found")

            // Tombol footer dengan fallback
            btnHome = findViewById(R.id.btnHome) ?: ImageButton(this)
            btnChart = findViewById(R.id.btnChart) ?: ImageButton(this)
            btnNotif = findViewById(R.id.btnNotif) ?: ImageButton(this)

            Log.d(TAG, "Views initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializeViews: ${e.message}", e)
            false
        }
    }

    private fun setupCalendarButton() {
        try {
            // ImageView hanya untuk display
            iconCalendar.setOnClickListener {
                Toast.makeText(
                    this,
                    "Riwayat kehadiran siswa hari ini",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d(TAG, "Calendar button setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupCalendarButton: ${e.message}")
        }
    }

    private fun updateTanggalDisplay() {
        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formatted = sdf.format(Date())

            val finalDate = if (formatted.isNotEmpty()) {
                formatted[0].uppercaseChar() + formatted.substring(1)
            } else {
                formatted
            }

            txtFilterTanggal.text = finalDate
        } catch (e: Exception) {
            Log.e(TAG, "Error updateTanggalDisplay: ${e.message}")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            txtFilterTanggal.text = sdf.format(Date())
        }
    }

    private fun setupRecyclerView() {
        try {
            adapter = SimpleSiswaAdapter(this, filteredData)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            Log.d(TAG, "RecyclerView setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupRecyclerView: ${e.message}")
        }
    }

    private fun setupFooterNavigation() {
        try {
            btnHome.setOnClickListener {
                safeNavigateTo(DashboardWaliKelasActivity::class.java, "Dashboard Wali Kelas")
            }

            // btnAssigment untuk refresh data dan reset filter
            val btnAssignment = findViewById<ImageButton>(R.id.btnAssigment)
            btnAssignment?.setOnClickListener {
                if (!isLoading) {
                    // Reset filter
                    filterActive = null
                    resetFilter()
                    updateTombolAktif()
                    resetTextColors()
                    Toast.makeText(
                        this@RiwayatKehadiranKelasActivity,
                        "Filter direset, menampilkan semua data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnChart.setOnClickListener {
                val intent = Intent(this, TindakLanjutWaliKelasActivity::class.java)
                startActivity(intent)
            }

            btnNotif.setOnClickListener {
                safeNavigateTo(NotifikasiWaliKelasActivity::class.java, "Notifikasi Wali Kelas")
            }

            Log.d(TAG, "Footer navigation setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupFooterNavigation: ${e.message}")
        }
    }

    private fun safeNavigateTo(activityClass: Class<*>, screenName: String) {
        try {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot navigate to $screenName: ${e.message}")
            Toast.makeText(this, "Tidak dapat membuka $screenName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDataAsync() {
        if (isLoading) return

        isLoading = true
        Toast.makeText(this, "Memuat data siswa...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                buatDataDummySiswa()

                handler.post {
                    // Saat pertama load, langsung hitung statistik
                    updateAngkaTombol()
                    updateTotalSiswa()
                    adapter.notifyDataSetChanged()
                    isLoading = false
                    Toast.makeText(
                        this@RiwayatKehadiranKelasActivity,
                        "Data kelas dimuat ($TOTAL_SISWA siswa)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadDataAsync: ${e.message}", e)
                handler.post {
                    isLoading = false
                    Toast.makeText(
                        this@RiwayatKehadiranKelasActivity,
                        "Gagal memuat data siswa",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun buatDataDummySiswa() {
        try {
            allData.clear()
            filteredData.clear()

            val namaSiswa = listOf(
                "Ahmad Fauzi", "Budi Santoso", "Cindy Permata", "Dedi Setiawan", "Eka Wulandari",
                "Fajar Nugroho", "Gita Maharani", "Hendra Pratama", "Indah Sari", "Joko Widodo",
                "Kartika Dewi", "Lukman Hakim", "Maya Puspita", "Nurhayati", "Oki Setiawan",
                "Putri Ayu", "Rizki Ramadhan", "Sari Dewi", "Toni Gunawan", "Umi Kulsum",
                "Vina Oktaviani", "Wahyu Adi", "Xena Alexandra", "Yoga Pratama", "Zahra Fitri"
            )

            val jurusanList = listOf("RPL", "TKJ", "DKV", "Mekatronika", "Animasi", "EI")
            val mapelList = listOf("Matematika", "Bahasa Inggris", "Pemrograman Dasar", "Sejarah")
            val guruList = listOf("Pak Budi", "Ibu Sari", "Pak Andi", "Bu Maya")

            // Distribusi tetap: 15 hadir, 4 sakit, 3 izin, 3 alpha
            val statusDistribution = mutableListOf<String>().apply {
                repeat(15) { add("hadir") }
                repeat(4) { add("sakit") }
                repeat(3) { add("izin") }
                repeat(3) { add("alpha") }
            }.shuffled()

            for (i in 0 until TOTAL_SISWA) {
                val nama = namaSiswa.getOrElse(i) { "Siswa ${i+1}" }
                val jurusan = "${jurusanList[i % jurusanList.size]} ${(i % 2) + 1}"
                val mapel = mapelList[i % mapelList.size]
                val guru = guruList[i % guruList.size]
                val statusType = statusDistribution.getOrElse(i) { "hadir" }

                val keterangan = when (statusType) {
                    "hadir" -> "Siswa Hadir di Kelas"
                    "sakit" -> "Siswa Tidak Hadir (Sakit)"
                    "izin" -> "Siswa Izin Tidak Hadir"
                    "alpha" -> "Siswa Tidak Hadir Tanpa Keterangan"
                    else -> "Status Tidak Diketahui"
                }

                allData.add(mapOf(
                    "id" to i + 1,
                    "nama" to nama,
                    "jurusan" to jurusan,
                    "mapelGuru" to "$mapel,[$guru]",
                    "keterangan" to keterangan,
                    "statusType" to statusType
                ))
            }

            filteredData.addAll(allData)
            Log.d(TAG, "Data siswa dibuat: ${allData.size} item")
        } catch (e: Exception) {
            Log.e(TAG, "Error buatDataDummySiswa: ${e.message}", e)
            throw e
        }
    }

    private fun setupFilterButtons() {
        try {
            btnHadir.setOnClickListener { runOnUiThread { toggleFilter("hadir") } }
            btnSakit.setOnClickListener { runOnUiThread { toggleFilter("sakit") } }
            btnIzin.setOnClickListener { runOnUiThread { toggleFilter("izin") } }
            btnAlpha.setOnClickListener { runOnUiThread { toggleFilter("alpha") } }
            Log.d(TAG, "Filter buttons setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupFilterButtons: ${e.message}")
        }
    }

    private fun toggleFilter(status: String) {
        try {
            if (filterActive == status) {
                filterActive = null
                resetFilter()
                resetTextColors()
            } else {
                filterActive = status
                applyFilter(status)
                updateTextColors(status)
            }
            updateTombolAktif()
        } catch (e: Exception) {
            Log.e(TAG, "Error toggleFilter: ${e.message}", e)
        }
    }

    private fun applyFilter(status: String) {
        try {
            filteredData.clear()
            filteredData.addAll(allData.filter { it["statusType"] == status })
            adapter.notifyDataSetChanged()
            // Jumlah total siswa TETAP TOTAL_SISWA, tidak berubah
            updateTotalSiswa()
        } catch (e: Exception) {
            Log.e(TAG, "Error applyFilter: ${e.message}", e)
        }
    }

    private fun resetFilter() {
        try {
            filteredData.clear()
            filteredData.addAll(allData)
            adapter.notifyDataSetChanged()
            // Saat reset, hitung ulang statistik
            updateAngkaTombol()
            updateTotalSiswa()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetFilter: ${e.message}", e)
        }
    }

    private fun updateTombolAktif() {
        try {
            // Reset semua tombol ke state normal
            try {
                btnHadir.setImageResource(R.drawable.btn_guru_hadir)
                btnSakit.setImageResource(R.drawable.btn_guru_sakit)
                btnIzin.setImageResource(R.drawable.btn_guru_izin)
                btnAlpha.setImageResource(R.drawable.btn_guru_alpha)
            } catch (e: Exception) {
                Log.w(TAG, "Using default button images: ${e.message}")
                val defaultDrawable = android.R.drawable.ic_menu_save
                btnHadir.setImageResource(defaultDrawable)
                btnSakit.setImageResource(defaultDrawable)
                btnIzin.setImageResource(defaultDrawable)
                btnAlpha.setImageResource(defaultDrawable)
            }

            // Set aktif
            try {
                when (filterActive) {
                    "hadir" -> btnHadir.setImageResource(R.drawable.btn_guru_hadir_active)
                    "sakit" -> btnSakit.setImageResource(R.drawable.btn_guru_sakit_active)
                    "izin" -> btnIzin.setImageResource(R.drawable.btn_guru_izin_active)
                    "alpha" -> btnAlpha.setImageResource(R.drawable.btn_guru_alpha_active)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cannot set active button images: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updateTombolAktif: ${e.message}", e)
        }
    }

    private fun updateTextColors(activeStatus: String) {
        try {
            // Reset semua teks ke warna normal
            resetTextColors()

            // Set teks aktif menjadi putih
            when (activeStatus) {
                "hadir" -> txtHadirCount.setTextColor(textColorActive)
                "sakit" -> txtSakitCount.setTextColor(textColorActive)
                "izin" -> txtIzinCount.setTextColor(textColorActive)
                "alpha" -> txtAlphaCount.setTextColor(textColorActive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updateTextColors: ${e.message}", e)
        }
    }

    private fun resetTextColors() {
        try {
            // Set semua teks ke warna normal
            txtHadirCount.setTextColor(textColorNormal)
            txtSakitCount.setTextColor(textColorNormal)
            txtIzinCount.setTextColor(textColorNormal)
            txtAlphaCount.setTextColor(textColorNormal)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetTextColors: ${e.message}", e)
            // Fallback ke warna default
            txtHadirCount.setTextColor(textColorDefault)
            txtSakitCount.setTextColor(textColorDefault)
            txtIzinCount.setTextColor(textColorDefault)
            txtAlphaCount.setTextColor(textColorDefault)
        }
    }

    private fun updateAngkaTombol() {
        try {
            var hadir = 0
            var sakit = 0
            var izin = 0
            var alpha = 0

            // Hitung dari allData, karena ini adalah statistik total hari ini
            synchronized(allData) {
                for (data in allData) {
                    when (data["statusType"]) {
                        "hadir" -> hadir++
                        "sakit" -> sakit++
                        "izin" -> izin++
                        "alpha" -> alpha++
                    }
                }
            }

            runOnUiThread {
                txtHadirCount.text = hadir.toString()
                txtSakitCount.text = sakit.toString()
                txtIzinCount.text = izin.toString()
                txtAlphaCount.text = alpha.toString()
            }

            Log.d(TAG, "Status counts: Hadir=$hadir, Sakit=$sakit, Izin=$izin, Alpha=$alpha")
        } catch (e: Exception) {
            Log.e(TAG, "Error updateAngkaTombol: ${e.message}", e)
        }
    }

    private fun updateTotalSiswa() {
        try {
            // Total siswa TETAP 25, tidak berubah meskipun difilter
            txtJumlahSiswa.text = "Total Jumlah Siswa : $TOTAL_SISWA"
        } catch (e: Exception) {
            Log.e(TAG, "Error updateTotalSiswa: ${e.message}", e)
        }
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        handler.postDelayed({
            finish()
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "=== RiwayatKehadiranKelasActivity DESTROYED ===")
    }
}