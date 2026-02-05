package com.example.ritamesa

import android.app.AlertDialog
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
import com.example.ritamesa.network.ApiClient
import com.example.ritamesa.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranGuruActivity : AppCompatActivity() {

    // Session Manager
    private lateinit var sessionManager: SessionManager

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
        // Use API for filtering
        loadDataAsync()
    }

    private fun applyFilter(status: String) {
        filterActive = if (filterActive == status) null else status
        updateTombolAktif()
        loadDataAsync()
    }

    private fun updateTombolAktif() {
        // Reset all buttons
        val defaultColor = android.graphics.Color.parseColor("#F3F4F6") // Gray-100
        btnHadir.setBackgroundColor(defaultColor)
        btnSakit.setBackgroundColor(defaultColor)
        btnIzin.setBackgroundColor(defaultColor)
        btnAlpha.setBackgroundColor(defaultColor)

        // Reset text colors
        resetTextColors()

        // Highlight active button
        when (filterActive) {
            "hadir" -> {
                btnHadir.setBackgroundColor(android.graphics.Color.parseColor("#10B981")) // Green
                txtHadirCount.setTextColor(textColorActive)
                // findViewById<TextView>(R.id.label_hadir).setTextColor(textColorActive)
            }
            "sakit" -> {
                btnSakit.setBackgroundColor(android.graphics.Color.parseColor("#F59E0B")) // Yellow
                txtSakitCount.setTextColor(textColorActive)
            }
            "izin" -> {
                btnIzin.setBackgroundColor(android.graphics.Color.parseColor("#3B82F6")) // Blue
                txtIzinCount.setTextColor(textColorActive)
            }
            "alpha" -> {
                btnAlpha.setBackgroundColor(android.graphics.Color.parseColor("#EF4444")) // Red
                txtAlphaCount.setTextColor(textColorActive)
            }
        }
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
        // Show loading indicator if exists
        Toast.makeText(this, "Memuat data...", Toast.LENGTH_SHORT).show()
        
        // Prepare params
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateParam = if (dateFilterActive) dateFormat.format(selectedDate.time) else null
        val statusParam = filterActive

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Initialize session manager if not already
                if (!::sessionManager.isInitialized) {
                     sessionManager = SessionManager(this@RiwayatKehadiranGuruActivity)
                }
                
                val apiService = ApiClient.getInstance(this@RiwayatKehadiranGuruActivity)
                val response = apiService.getTeacherAttendance(dateParam, statusParam)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        
                        // Clear existing data
                        allData.clear()
                        filteredData.clear()
                        
                        // Map response to adapter format
                        // Expected format: mapOf("id", "mapel", "kelas", "status", "tanggal", "statusType")
                        val mappedData = result.data.map { record ->
                            mapOf(
                                "id" to record.id,
                                "mapel" to record.subject,
                                "kelas" to record.class_name,
                                "status" to record.status_label,
                                "tanggal" to "${record.date} ${record.time}",
                                "statusType" to record.status.lowercase()
                            )
                        }
                        
                        allData.addAll(mappedData)
                        filteredData.addAll(mappedData) // Assuming API returns filtered data
                        
                        adapter.notifyDataSetChanged()
                        
                        // Update summary counts if available
                        if (result.summary != null) {
                            txtHadirCount.text = result.summary.present.toString()
                            txtSakitCount.text = result.summary.sick.toString()
                            txtIzinCount.text = result.summary.excused.toString()
                            txtAlphaCount.text = result.summary.absent.toString()
                        }
                        
                        Toast.makeText(
                            this@RiwayatKehadiranGuruActivity,
                            "Data berhasil dimuat: ${filteredData.size} item",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                    } else {
                        Log.e(TAG, "API Error: ${response.code()}")
                        // Fallback to dummy if needed or just show error
                        // buatDataDummyGuru() // Uncomment if fallback needed
                        Toast.makeText(
                            this@RiwayatKehadiranGuruActivity,
                            "Gagal memuat data: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Exception: ${e.message}")
                    Toast.makeText(
                        this@RiwayatKehadiranGuruActivity,
                        "Error koneksi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
            }
        }
    }

    private fun resetTextColors() {
        txtHadirCount.setTextColor(textColorNormal)
        txtSakitCount.setTextColor(textColorNormal)
        txtIzinCount.setTextColor(textColorNormal)
        txtAlphaCount.setTextColor(textColorNormal)
    }

    private fun updateAngkaTombol() {
        // Deprecated: API returns summary
    }

    private fun setupFilterButtons() {
        try {
            btnHadir.setOnClickListener { applyFilter("hadir") }
            btnSakit.setOnClickListener { applyFilter("sakit") }
            btnIzin.setOnClickListener { applyFilter("izin") }
            btnAlpha.setOnClickListener { applyFilter("alpha") }
        } catch (e: Exception) {
            Log.e(TAG, "Error setupFilterButtons: ${e.message}")
        }
    }        }
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