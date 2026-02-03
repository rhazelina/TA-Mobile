package com.example.ritamesa

import android.app.DatePickerDialog
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

class RiwayatKehadiranGuruActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
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
    private lateinit var adapter: SimpleGuruAdapter
    private var filterActive: String? = null
    private var dateFilterActive: Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    private var isLoading = false
    private var selectedDate = Calendar.getInstance()

    // Warna untuk teks statistik
    private val textColorActive = android.graphics.Color.WHITE
    private val textColorNormal = android.graphics.Color.parseColor("#4B5563") // Abu-abu tua
    private val textColorDefault = android.graphics.Color.BLACK

    companion object {
        private const val TAG = "RiwayatGuruActivity"
        private const val DATE_FORMAT = "dd-MM-yyyy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}: ${throwable.message}")
            runOnUiThread {
                Toast.makeText(
                    this@RiwayatKehadiranGuruActivity,
                    "Aplikasi mengalami error, mencoba memperbaiki...",
                    Toast.LENGTH_LONG
                ).show()
            }
            handler.postDelayed({
                finish()
                startActivity(intent)
            }, 1000)
        }

        try {
            Log.d(TAG, "=== START RiwayatKehadiranGuruActivity ===")

            try {
                setContentView(R.layout.riwayat_kehadiran_guru_fix)
                Log.d(TAG, "Layout berhasil di-set: riwayat_kehadiran_guru_fix")
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL: Layout file not found: ${e.message}")
                Toast.makeText(this, "Layout error, menggunakan fallback", Toast.LENGTH_LONG).show()
                setContentView(android.R.layout.simple_list_item_1)
                Toast.makeText(this, "Mohon update aplikasi", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views")
                Toast.makeText(this, "Gagal memuat tampilan", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            setupRecyclerView()
            setupFooterNavigation()
            setupFilterButtons()
            setupCalendarButton()

            // Set tanggal awal ke hari ini
            updateTanggalDisplay()

            // Set warna teks awal
            resetTextColors()

            // Load data
            handler.postDelayed({
                loadDataAsync()
            }, 300)

        } catch (e: Exception) {
            Log.e(TAG, "FATAL ERROR in onCreate: ${e.message}", e)
            showErrorAndExit("Gagal memuat halaman: ${e.message}")
        }
    }

    private fun initializeViews(): Boolean {
        return try {
            recyclerView = findViewById(R.id.recycler_riwayat) ?: throw NullPointerException("recycler_riwayat not found")
            txtHadirCount = findViewById(R.id.txt_hadir_count) ?: throw NullPointerException("txt_hadir_count not found")
            txtSakitCount = findViewById(R.id.txt_sakit_count) ?: throw NullPointerException("txt_sakit_count not found")
            txtIzinCount = findViewById(R.id.txt_izin_count) ?: throw NullPointerException("txt_izin_count not found")
            txtAlphaCount = findViewById(R.id.txt_alpha_count) ?: throw NullPointerException("txt_alpha_count not found")
            txtFilterTanggal = findViewById(R.id.text_filter_tanggal) ?: throw NullPointerException("text_filter_tanggal not found")

            btnHadir = findViewById(R.id.button_hadir) ?: throw NullPointerException("button_hadir not found")
            btnSakit = findViewById(R.id.button_sakit) ?: throw NullPointerException("button_sakit not found")
            btnIzin = findViewById(R.id.button_izin) ?: throw NullPointerException("button_izin not found")
            btnAlpha = findViewById(R.id.button_alpha) ?: throw NullPointerException("button_alpha not found")

            iconCalendar = findViewById(R.id.icon_calendar) ?: throw NullPointerException("icon_calendar not found")

            btnHome = findViewById(R.id.btnHome) ?: ImageButton(this).apply {
                setBackgroundColor(0xFF888888.toInt())
            }
            btnChart = findViewById(R.id.btnChart) ?: ImageButton(this).apply {
                setBackgroundColor(0xFF888888.toInt())
            }
            btnNotif = findViewById(R.id.btnNotif) ?: ImageButton(this).apply {
                setBackgroundColor(0xFF888888.toInt())
            }

            Log.d(TAG, "Semua view berhasil diinisialisasi")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeViews: ${e.message}", e)
            false
        }
    }

    private fun setupCalendarButton() {
        try {
            iconCalendar.setOnClickListener {
                showDatePicker()
            }
            Log.d(TAG, "Calendar button setup selesai")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupCalendarButton: ${e.message}")
            txtFilterTanggal.setOnClickListener {
                showDatePicker()
            }
        }
    }

    private fun showDatePicker() {
        try {
            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    dateFilterActive = true
                    updateTanggalDisplay()
                    applyDateFilter()
                    // Reset filter status ketika ganti tanggal
                    filterActive = null
                    updateTombolAktif()
                    resetTextColors()
                },
                year,
                month,
                day
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            val minCalendar = Calendar.getInstance()
            minCalendar.set(2023, 0, 1)
            datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

            datePickerDialog.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showDatePicker: ${e.message}")
            Toast.makeText(this, "Gagal membuka kalender", Toast.LENGTH_SHORT).show()
        }
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
            Log.e(TAG, "Error updateTanggalDisplay: ${e.message}")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            txtFilterTanggal.text = sdf.format(selectedDate.time)
        }
    }

    private fun applyDateFilter() {
        if (isLoading) return

        isLoading = true
        Toast.makeText(this, "Memfilter data berdasarkan tanggal...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val selectedDateStr = dateFormat.format(selectedDate.time)

                val tempFilteredData = mutableListOf<Map<String, Any>>()
                synchronized(allData) {
                    for (data in allData) {
                        val tanggal = data["tanggal"] as? String ?: ""
                        if (tanggal.contains(selectedDateStr)) {
                            tempFilteredData.add(data)
                        }
                    }
                }

                handler.post {
                    filteredData.clear()
                    filteredData.addAll(tempFilteredData)

                    if (filterActive != null) {
                        applyFilter(filterActive!!)
                    } else {
                        adapter.notifyDataSetChanged()
                    }

                    // Update statistik untuk tanggal yang dipilih
                    updateAngkaTombol()

                    isLoading = false

                    val itemCount = filteredData.size
                    Toast.makeText(
                        this@RiwayatKehadiranGuruActivity,
                        if (itemCount > 0) {
                            "Menampilkan $itemCount data untuk $selectedDateStr"
                        } else {
                            "Tidak ada data untuk $selectedDateStr"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in applyDateFilter: ${e.message}", e)
                handler.post {
                    isLoading = false
                    Toast.makeText(
                        this@RiwayatKehadiranGuruActivity,
                        "Gagal memfilter data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun setupRecyclerView() {
        try {
            adapter = SimpleGuruAdapter(this, filteredData)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            Log.d(TAG, "RecyclerView setup selesai")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupRecyclerView: ${e.message}")
        }
    }

    private fun setupFooterNavigation() {
        try {
            btnHome.setOnClickListener {
                safeNavigateTo(DashboardGuruActivity::class.java, "Dashboard Guru")
            }

            val btnAssignment = findViewById<ImageButton>(R.id.btnAssigment)
            btnAssignment?.setOnClickListener {
                if (!isLoading) {
                    // Reset semua filter
                    filterActive = null
                    dateFilterActive = false
                    selectedDate = Calendar.getInstance() // Reset ke hari ini
                    updateTanggalDisplay()
                    resetFilter()
                    updateTombolAktif()
                    resetTextColors()
                    Toast.makeText(this@RiwayatKehadiranGuruActivity,
                        "Filter direset, menampilkan semua data",
                        Toast.LENGTH_SHORT).show()
                }
            }

            btnChart.setOnClickListener {
                safeNavigateTo(TindakLanjutGuruActivity::class.java, "Tindak Lanjut")
            }

            btnNotif.setOnClickListener {
                safeNavigateTo(NotifikasiGuruActivity::class.java, "Notifikasi")
            }

            Log.d(TAG, "Footer navigation setup selesai")
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

    private fun setupFilterButtons() {
        try {
            btnHadir.setOnClickListener {
                runOnUiThread { toggleFilter("hadir") }
            }
            btnSakit.setOnClickListener {
                runOnUiThread { toggleFilter("sakit") }
            }
            btnIzin.setOnClickListener {
                runOnUiThread { toggleFilter("izin") }
            }
            btnAlpha.setOnClickListener {
                runOnUiThread { toggleFilter("alpha") }
            }
            Log.d(TAG, "Filter buttons setup selesai")
        } catch (e: Exception) {
            Log.e(TAG, "Error setupFilterButtons: ${e.message}")
        }
    }

    private fun loadDataAsync() {
        if (isLoading) return

        isLoading = true
        Toast.makeText(this, "Memuat data...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                buatDataDummyGuru()

                handler.post {
                    // Saat pertama load, langsung hitung statistik untuk tanggal yang aktif
                    updateAngkaTombol()
                    adapter.notifyDataSetChanged()
                    isLoading = false
                    Toast.makeText(
                        this@RiwayatKehadiranGuruActivity,
                        "Data riwayat guru dimuat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadDataAsync: ${e.message}", e)
                handler.post {
                    isLoading = false
                    Toast.makeText(
                        this@RiwayatKehadiranGuruActivity,
                        "Gagal memuat data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun buatDataDummyGuru() {
        try {
            allData.clear()
            filteredData.clear()

            val mapelList = listOf("Matematika", "Bahasa Indonesia", "Bahasa Inggris", "Fisika", "Kimia")
            val kelasList = listOf("XII RPL 1", "XII RPL 2", "XII TKJ 1", "XII TKJ 2")

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

            // Buat data untuk 7 hari terakhir
            for (dayOffset in 0..6) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
                val tanggal = dateFormat.format(calendar.time)

                // Buat 3-5 entri per hari
                val entriesPerDay = (3..5).random()
                for (entryIndex in 1..entriesPerDay) {
                    val id = dayOffset * 10 + entryIndex

                    // Distribusi status dengan probabilitas berbeda
                    val statusRand = (1..100).random()
                    val statusType = when {
                        statusRand <= 70 -> "hadir"      // 70% hadir
                        statusRand <= 85 -> "sakit"      // 15% sakit
                        statusRand <= 95 -> "izin"       // 10% izin
                        else -> "alpha"                  // 5% alpha
                    }

                    val statusText = when (statusType) {
                        "hadir" -> if (entryIndex % 3 == 0) "Hadir Terlambat" else "Hadir Tepat Waktu"
                        "sakit" -> "Tidak Bisa Mengajar"
                        "izin" -> "Tidak Bisa Mengajar"
                        "alpha" -> "Tanpa Keterangan"
                        else -> "Tidak Mengajar"
                    }

                    val jamList = listOf("07:30", "09:15", "11:00", "13:45")
                    val jam = jamList[entryIndex % jamList.size]

                    allData.add(mapOf(
                        "id" to id,
                        "mapel" to mapelList[entryIndex % mapelList.size],
                        "kelas" to kelasList[entryIndex % kelasList.size],
                        "status" to statusText,
                        "tanggal" to "$tanggal $jam",
                        "statusType" to statusType
                    ))
                }
            }

            // Urutkan berdasarkan tanggal (terbaru dulu)
            allData.sortWith(compareByDescending<Map<String, Any>> {
                val tanggal = it["tanggal"] as? String ?: ""
                tanggal
            })

            // Set awal: tampilkan data hari ini
            val todayStr = dateFormat.format(Date())
            filteredData.addAll(allData.filter {
                val tanggal = it["tanggal"] as? String ?: ""
                tanggal.contains(todayStr)
            })

            Log.d(TAG, "Data dummy dibuat: ${allData.size} item, hari ini: ${filteredData.size} item")
        } catch (e: Exception) {
            Log.e(TAG, "Error buatDataDummyGuru: ${e.message}", e)
            throw e
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

            if (dateFilterActive) {
                val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val selectedDateStr = dateFormat.format(selectedDate.time)

                // Filter data berdasarkan tanggal DAN status
                val filteredByDateAndStatus = allData.filter {
                    val statusMatch = it["statusType"] == status
                    val tanggal = it["tanggal"] as? String ?: ""
                    val dateMatch = tanggal.contains(selectedDateStr)
                    statusMatch && dateMatch
                }
                filteredData.addAll(filteredByDateAndStatus)
            } else {
                // Filter hanya berdasarkan status
                filteredData.addAll(allData.filter { it["statusType"] == status })
            }

            adapter.notifyDataSetChanged()

            // PERBAIKAN PENTING: Saat filter status aktif, tetap hitung statistik TOTAL untuk tanggal tersebut
            // bukan hanya yang difilter
            updateAngkaTombolForFilter(status)

        } catch (e: Exception) {
            Log.e(TAG, "Error applyFilter: ${e.message}", e)
        }
    }

    private fun resetFilter() {
        try {
            filteredData.clear()

            if (dateFilterActive) {
                val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val selectedDateStr = dateFormat.format(selectedDate.time)

                filteredData.addAll(allData.filter {
                    val tanggal = it["tanggal"] as? String ?: ""
                    tanggal.contains(selectedDateStr)
                })
            } else {
                filteredData.addAll(allData)
            }

            adapter.notifyDataSetChanged()
            // Saat reset, hitung statistik normal
            updateAngkaTombol()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetFilter: ${e.message}", e)
        }
    }

    private fun updateAngkaTombolForFilter(activeStatus: String) {
        try {
            // Hitung statistik TOTAL untuk tanggal yang aktif (jika ada filter tanggal)
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val selectedDateStr = if (dateFilterActive) {
                dateFormat.format(selectedDate.time)
            } else {
                // Jika tidak ada filter tanggal, gunakan hari ini
                dateFormat.format(Date())
            }

            var hadirTotal = 0
            var sakitTotal = 0
            var izinTotal = 0
            var alphaTotal = 0

            synchronized(allData) {
                for (data in allData) {
                    val tanggal = data["tanggal"] as? String ?: ""
                    if (tanggal.contains(selectedDateStr)) {
                        when (data["statusType"]) {
                            "hadir" -> hadirTotal++
                            "sakit" -> sakitTotal++
                            "izin" -> izinTotal++
                            "alpha" -> alphaTotal++
                        }
                    }
                }
            }

            runOnUiThread {
                // Tampilkan statistik TOTAL untuk semua status
                txtHadirCount.text = hadirTotal.toString()
                txtSakitCount.text = sakitTotal.toString()
                txtIzinCount.text = izinTotal.toString()
                txtAlphaCount.text = alphaTotal.toString()

                // Tambahkan informasi jumlah yang ditampilkan di Toast
                val itemCount = filteredData.size
                val statusText = when (activeStatus) {
                    "hadir" -> "hadir"
                    "sakit" -> "sakit"
                    "izin" -> "izin"
                    "alpha" -> "alpha"
                    else -> ""
                }
                Toast.makeText(
                    this@RiwayatKehadiranGuruActivity,
                    "Menampilkan $itemCount data $statusText untuk $selectedDateStr",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Log.d(TAG, "Statistik TOTAL untuk $selectedDateStr: H=$hadirTotal, S=$sakitTotal, I=$izinTotal, A=$alphaTotal")
        } catch (e: Exception) {
            Log.e(TAG, "Error updateAngkaTombolForFilter: ${e.message}", e)
        }
    }

    private fun updateTombolAktif() {
        try {
            // Reset semua tombol ke state normal
            btnHadir.setImageResource(R.drawable.btn_guru_hadir)
            btnSakit.setImageResource(R.drawable.btn_guru_sakit)
            btnIzin.setImageResource(R.drawable.btn_guru_izin)
            btnAlpha.setImageResource(R.drawable.btn_guru_alpha)

            // Set aktif
            when (filterActive) {
                "hadir" -> btnHadir.setImageResource(R.drawable.btn_guru_hadir_active)
                "sakit" -> btnSakit.setImageResource(R.drawable.btn_guru_sakit_active)
                "izin" -> btnIzin.setImageResource(R.drawable.btn_guru_izin_active)
                "alpha" -> btnAlpha.setImageResource(R.drawable.btn_guru_alpha_active)
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
            // Set semua teks ke warna normal (abu-abu tua)
            txtHadirCount.setTextColor(textColorNormal)
            txtSakitCount.setTextColor(textColorNormal)
            txtIzinCount.setTextColor(textColorNormal)
            txtAlphaCount.setTextColor(textColorNormal)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetTextColors: ${e.message}", e)
            // Fallback ke warna hitam
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

            // Hitung dari filteredData (yang sedang ditampilkan)
            synchronized(filteredData) {
                for (data in filteredData) {
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

            Log.d(TAG, "Jumlah status: Hadir=$hadir, Sakit=$sakit, Izin=$izin, Alpha=$alpha")
        } catch (e: Exception) {
            Log.e(TAG, "Error updateAngkaTombol: ${e.message}", e)
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
        Log.d(TAG, "=== RiwayatKehadiranGuruActivity DESTROYED ===")
    }
}