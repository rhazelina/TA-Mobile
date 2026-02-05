package com.example.ritamesa

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DashboardGuruActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardGuruActivity"
        private const val UPDATE_INTERVAL_MS = 1000L
    }

    // Session and API
    private lateinit var sessionManager: SessionManager

    // Deklarasi komponen UI
    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView
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
    private lateinit var jadwalAdapter: JadwalAdapter

    // Waktu pembelajaran (TETAP - sesuai permintaan)
    private val jamMasukDatabase = "07:00:00"
    private val jamPulangDatabase = "15:00:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_guru)

        sessionManager = SessionManager(this)

        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        // Inisialisasi komponen UI
        initViews()

        // Setup tanggal dan waktu
        setupDateTime()

        // Setup RecyclerView untuk jadwal
        setupRecyclerView()

        // Load dashboard data from API
        loadDashboardData()

        // Setup footer navigation
        setupFooterNavigation()

        // Setup logout button
        setupLogoutButton()
    }

    private fun initViews() {
        try {
            // Tanggal dan waktu
            txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
            txtWaktuLive = findViewById(R.id.txtWaktuLive)
            txtJamMasuk = findViewById(R.id.txtJamMasuk)
            txtJamPulang = findViewById(R.id.txtJamPulang)
            txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)

            // Counter kehadiran
            txtHadirCount = findViewById(R.id.txt_hadir_count)
            txtIzinCount = findViewById(R.id.txt_izin_count)
            txtSakitCount = findViewById(R.id.txt_sakit_count)
            txtAlphaCount = findViewById(R.id.txt_alpha_count)

            // RecyclerView
            recyclerJadwal = findViewById(R.id.recyclerJadwal)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Terjadi kesalahan dalam memuat tampilan", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDateTime() {
        try {
            // Format tanggal Indonesia
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val currentDate = Date()
            val tanggalHariIni = dateFormat.format(currentDate)

            // Set tanggal
            txtTanggalSekarang.text = tanggalHariIni
            txtTanggalDiJamLayout.text = tanggalHariIni

            // Format jam pembelajaran (hilangkan detik jika ada)
            val jamMasuk = jamMasukDatabase.substring(0, 5)
            val jamPulang = jamPulangDatabase.substring(0, 5)

            txtJamMasuk.text = jamMasuk
            txtJamPulang.text = jamPulang

            // Setup waktu live yang berjalan terus (WIB)
            runnable = object : Runnable {
                override fun run() {
                    updateLiveTime()
                    handler.postDelayed(this, UPDATE_INTERVAL_MS)
                }
            }

            // Mulai update waktu
            handler.post(runnable)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up date time", e)
            Toast.makeText(this, "Gagal memuat waktu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLiveTime() {
        try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            val currentTime = timeFormat.format(Date())
            txtWaktuLive.text = currentTime
        } catch (e: Exception) {
            Log.e(TAG, "Error updating live time", e)
        }
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@DashboardGuruActivity)
                val response = apiService.getTeacherDashboard()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        updateUIWithData(data)
                        Toast.makeText(this@DashboardGuruActivity, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "API response unsuccessful: ${response.code()} - ${response.message()}")
                        loadDummyData()
                        Toast.makeText(this@DashboardGuruActivity, "Menggunakan data simulasi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error loading dashboard", e)
                    loadDummyData()
                    Toast.makeText(this@DashboardGuruActivity, "Gagal memuat data, menggunakan data simulasi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUIWithData(data: com.example.ritamesa.network.TeacherDashboardResponse) {
        try {
            // Update counter kehadiran
            txtHadirCount.text = data.attendance_summary.present.toString()
            txtIzinCount.text = data.attendance_summary.excused.toString()
            txtSakitCount.text = data.attendance_summary.sick.toString()
            txtAlphaCount.text = data.attendance_summary.absent.toString()

            // Update jadwal hari ini
            jadwalHariIni.clear()
            if (data.schedule_today.isNotEmpty()) {
                data.schedule_today.forEachIndexed { index, schedule ->
                    jadwalHariIni.add(
                        JadwalItem(
                            id = schedule.id,
                            mataPelajaran = schedule.subject,
                            waktuPelajaran = "${schedule.start_time} - ${schedule.end_time}",
                            kelas = schedule.class_name,
                            jam = schedule.time_slot
                        )
                    )
                }
            } else {
                // Jika tidak ada jadwal
                jadwalHariIni.add(
                    JadwalItem(
                        id = 0,
                        mataPelajaran = "Tidak ada jadwal",
                        waktuPelajaran = "--:-- - --:--",
                        kelas = "-",
                        jam = "--:-- - --:--"
                    )
                )
            }
            jadwalAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI with data", e)
            Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDummyData() {
        try {
            // Data dummy kehadiran
            txtHadirCount.text = "25"
            txtIzinCount.text = "1"
            txtSakitCount.text = "1"
            txtAlphaCount.text = "3"

            // Data dummy jadwal
            jadwalHariIni.clear()
            jadwalHariIni.addAll(generateDummyJadwal())
            jadwalAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading dummy data", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            // Setup adapter
            jadwalAdapter = JadwalAdapter(jadwalHariIni) { jadwal ->
                if (jadwal.mataPelajaran != "Tidak ada jadwal") {
                    navigateToDetailJadwalGuru(jadwal)
                } else {
                    Toast.makeText(this, "Tidak ada jadwal untuk hari ini", Toast.LENGTH_SHORT).show()
                }
            }

            // Setup layout manager
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerJadwal.layoutManager = layoutManager
            recyclerJadwal.adapter = jadwalAdapter
            recyclerJadwal.setHasFixedSize(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up recycler view", e)
            Toast.makeText(this, "Gagal memuat daftar jadwal", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateDummyJadwal(): List<JadwalItem> {
        return listOf(
            JadwalItem(1, "Matematika", "07:00 - 08:30", "XII RPL 1", "Jam ke-1-2"),
            JadwalItem(2, "Bahasa Indonesia", "08:30 - 10:00", "XII RPL 2", "Jam ke-3-4"),
            JadwalItem(3, "Fisika", "10:15 - 11:45", "XII RPL 1", "Jam ke-5-6")
        )
    }

    private fun navigateToDetailJadwalGuru(jadwal: JadwalItem) {
        try {
            val intent = Intent(this, DetailJadwalGuruActivity::class.java).apply {
                putExtra("JADWAL_DATA", JadwalData(
                    mataPelajaran = jadwal.mataPelajaran,
                    kelas = jadwal.kelas,
                    jam = jadwal.jam,
                    waktuPelajaran = jadwal.waktuPelajaran,
                    idJadwal = jadwal.id.toString()
                ))
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to detail jadwal", e)
            Toast.makeText(this, "Gagal membuka detail jadwal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFooterNavigation() {
        try {
            val btnHome: ImageButton = findViewById(R.id.btnHome)
            val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
            val btnChart: ImageButton = findViewById(R.id.btnChart)
            val btnNotif: ImageButton = findViewById(R.id.btnNotif)

            btnHome.setOnClickListener {
                // Already on home, bisa ditambahkan refresh jika diperlukan
                // loadDashboardData()
            }

            btnCalendar.setOnClickListener {
                navigateToActivity(RiwayatKehadiranGuruActivity::class.java)
            }

            btnChart.setOnClickListener {
                navigateToActivity(RekapKehadiranGuru::class.java)
            }

            btnNotif.setOnClickListener {
                navigateToActivity(NotifikasiGuruActivity::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up footer navigation", e)
        }
    }

    private fun <T : AppCompatActivity> navigateToActivity(activityClass: Class<T>) {
        try {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to activity", e)
            Toast.makeText(this, "Gagal membuka halaman", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLogoutButton() {
        try {
            // Mencari berbagai kemungkinan view untuk logout
            val profileView = findViewById<View>(R.id.group)
//            val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
            // NGGA ADA TOMBOL BUAT LOGOUT MAU DIKEMANAIN :V

            // Prioritize dedicated logout button
//            btnLogout?.setOnClickListener {
//                showLogoutDialog()
//            }

            // Fallback to profile view long press
            profileView?.setOnLongClickListener {
                showLogoutDialog()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up logout button", e)
        }
    }

    private fun showLogoutDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout dialog", e)
        }
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@DashboardGuruActivity)
                apiService.logout()
            } catch (e: Exception) {
                Log.e(TAG, "Logout API error", e)
            } finally {
                withContext(Dispatchers.Main) {
                    // Clear session
                    sessionManager.clearSession()
                    ApiClient.resetInstance()

                    // Navigate to login
                    navigateToLogin()

                    Toast.makeText(this@DashboardGuruActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginAwal::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    // Data class untuk jadwal (sederhana, tanpa idKelas dan idMapel)
    data class JadwalItem(
        val id: Int,
        val mataPelajaran: String,
        val waktuPelajaran: String,
        val kelas: String,
        val jam: String
    )

    // Data class untuk passing data ke detail
    data class JadwalData(
        val mataPelajaran: String,
        val kelas: String,
        val jam: String,
        val waktuPelajaran: String,
        val idJadwal: String = "" // Menggunakan ID jadwal dari API
    ) : java.io.Serializable
}


// LEGACY CODE

//package com.example.ritamesa
//
//import android.app.AlertDialog
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.widget.ImageButton
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.ritamesa.network.ApiClient
//import com.example.ritamesa.network.SessionManager
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//
//class DashboardGuruActivity : AppCompatActivity() {
//
//    companion object {
//        private const val TAG = "DashboardGuruActivity"
//        private const val UPDATE_INTERVAL_MS = 1000L
//    }
//
//    // Session and API
//    private lateinit var sessionManager: SessionManager
//
//    // Deklarasi komponen UI
//    private lateinit var txtTanggalSekarang: TextView
//    private lateinit var txtWaktuLive: TextView
//    private lateinit var txtJamMasuk: TextView
//    private lateinit var txtJamPulang: TextView
//    private lateinit var txtTanggalDiJamLayout: TextView
//
//    // Counter kehadiran
//    private lateinit var txtHadirCount: TextView
//    private lateinit var txtIzinCount: TextView
//    private lateinit var txtSakitCount: TextView
//    private lateinit var txtAlphaCount: TextView
//
//    // RecyclerView
//    private lateinit var recyclerJadwal: RecyclerView
//
//    // Handler untuk update waktu live
//    private val handler = Handler(Looper.getMainLooper())
//    private lateinit var runnable: Runnable
//
//    // Data jadwal
//    private val jadwalHariIni = mutableListOf<JadwalItem>()
//    private lateinit var jadwalAdapter: JadwalAdapter
//
//    // Waktu pembelajaran (TETAP - sesuai permintaan)
//    private val jamMasukDatabase = "07:00:00"
//    private val jamPulangDatabase = "15:00:00"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.dashboard_guru)
//
//        sessionManager = SessionManager(this)
//
//        // Check if logged in
//        if (!sessionManager.isLoggedIn()) {
//            navigateToLogin()
//            return
//        }
//
//        // Inisialisasi komponen UI
//        initViews()
//
//        // Setup tanggal dan waktu
//        setupDateTime()
//
//        // Setup RecyclerView untuk jadwal
//        setupRecyclerView()
//
//        // Load dashboard data from API
//        loadDashboardData()
//
//        // Setup footer navigation
//        setupFooterNavigation()
//
//        // Setup logout button
//        setupLogoutButton()
//    }
//
//    private fun initViews() {
//        try {
//            // Tanggal dan waktu
//            txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
//            txtWaktuLive = findViewById(R.id.txtWaktuLive)
//            txtJamMasuk = findViewById(R.id.txtJamMasuk)
//            txtJamPulang = findViewById(R.id.txtJamPulang)
//            txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)
//
//            // Counter kehadiran
//            txtHadirCount = findViewById(R.id.txt_hadir_count)
//            txtIzinCount = findViewById(R.id.txt_izin_count)
//            txtSakitCount = findViewById(R.id.txt_sakit_count)
//            txtAlphaCount = findViewById(R.id.txt_alpha_count)
//
//            // RecyclerView
//            recyclerJadwal = findViewById(R.id.recyclerJadwal)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error initializing views", e)
//            Toast.makeText(this, "Terjadi kesalahan dalam memuat tampilan", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun setupDateTime() {
//        try {
//            // Format tanggal Indonesia
//            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
//            val currentDate = Date()
//            val tanggalHariIni = dateFormat.format(currentDate)
//
//            // Set tanggal
//            txtTanggalSekarang.text = tanggalHariIni
//            txtTanggalDiJamLayout.text = tanggalHariIni
//
//            // Format jam pembelajaran (hilangkan detik jika ada)
//            val jamMasuk = jamMasukDatabase.substring(0, 5)
//            val jamPulang = jamPulangDatabase.substring(0, 5)
//
//            txtJamMasuk.text = jamMasuk
//            txtJamPulang.text = jamPulang
//
//            // Setup waktu live yang berjalan terus (WIB)
//            runnable = object : Runnable {
//                override fun run() {
//                    updateLiveTime()
//                    handler.postDelayed(this, UPDATE_INTERVAL_MS)
//                }
//            }
//
//            // Mulai update waktu
//            handler.post(runnable)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up date time", e)
//            Toast.makeText(this, "Gagal memuat waktu", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateLiveTime() {
//        try {
//            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
//            val currentTime = timeFormat.format(Date())
//            txtWaktuLive.text = currentTime
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating live time", e)
//        }
//    }
//
//    private fun loadDashboardData() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val apiService = ApiClient.getInstance(this@DashboardGuruActivity)
//                val response = apiService.getTeacherDashboard()
//
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful && response.body() != null) {
//                        val data = response.body()!!
//                        updateUIWithData(data)
//                        Toast.makeText(this@DashboardGuruActivity, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Log.w(TAG, "API response unsuccessful: ${response.code()} - ${response.message()}")
//                        loadDummyData()
//                        Toast.makeText(this@DashboardGuruActivity, "Menggunakan data simulasi", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Log.e(TAG, "Error loading dashboard", e)
//                    loadDummyData()
//                    Toast.makeText(this@DashboardGuruActivity, "Gagal memuat data, menggunakan data simulasi", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun updateUIWithData(data: com.example.ritamesa.network.TeacherDashboardResponse) {
//        try {
//            // Update counter kehadiran
//            txtHadirCount.text = data.attendance_summary.present.toString()
//            txtIzinCount.text = data.attendance_summary.excused.toString()
//            txtSakitCount.text = data.attendance_summary.sick.toString()
//            txtAlphaCount.text = data.attendance_summary.absent.toString()
//
//            // Update jadwal hari ini
//            jadwalHariIni.clear()
//            if (data.schedule_today.isNotEmpty()) {
//                data.schedule_today.forEachIndexed { index, schedule ->
//                    jadwalHariIni.add(
//                        JadwalItem(
//                            id = index + 1,
//                            mataPelajaran = schedule.subject ?: "Mata Pelajaran",
//                            waktuPelajaran = "${schedule.start_time ?: "--:--"} - ${schedule.end_time ?: "--:--"}",
//                            kelas = schedule.class_name ?: "Kelas",
//                            jam = "${schedule.start_time ?: "--:--"} - ${schedule.end_time ?: "--:--"}",
//                            idKelas = schedule.class_id?.toString() ?: "",
//                            idMapel = schedule.subject_id?.toString() ?: ""
//                        )
//                    )
//                }
//            } else {
//                // Jika tidak ada jadwal
//                jadwalHariIni.add(
//                    JadwalItem(
//                        id = 1,
//                        mataPelajaran = "Tidak ada jadwal",
//                        waktuPelajaran = "--:-- - --:--",
//                        kelas = "-",
//                        jam = "--:-- - --:--",
//                        idKelas = "",
//                        idMapel = ""
//                    )
//                )
//            }
//            jadwalAdapter.notifyDataSetChanged()
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating UI with data", e)
//            Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun loadDummyData() {
//        try {
//            // Data dummy kehadiran
//            txtHadirCount.text = "25"
//            txtIzinCount.text = "1"
//            txtSakitCount.text = "1"
//            txtAlphaCount.text = "3"
//
//            // Data dummy jadwal
//            jadwalHariIni.clear()
//            jadwalHariIni.addAll(generateDummyJadwal())
//            jadwalAdapter.notifyDataSetChanged()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error loading dummy data", e)
//        }
//    }
//
//    private fun setupRecyclerView() {
//        try {
//            // Setup adapter
//            jadwalAdapter = JadwalAdapter(jadwalHariIni) { jadwal ->
//                if (jadwal.mataPelajaran != "Tidak ada jadwal") {
//                    navigateToDetailJadwalGuru(jadwal)
//                } else {
//                    Toast.makeText(this, "Tidak ada jadwal untuk hari ini", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            // Setup layout manager
//            val layoutManager = LinearLayoutManager(this)
//            layoutManager.orientation = LinearLayoutManager.VERTICAL
//            recyclerJadwal.layoutManager = layoutManager
//            recyclerJadwal.adapter = jadwalAdapter
//            recyclerJadwal.setHasFixedSize(true)
//
//            // Tambahkan divider jika diperlukan
//            // recyclerJadwal.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up recycler view", e)
//            Toast.makeText(this, "Gagal memuat daftar jadwal", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun generateDummyJadwal(): List<JadwalItem> {
//        return listOf(
//            JadwalItem(1, "Matematika", "07:00 - 08:30", "XII RPL 1", "07:00 - 08:30", "1", "1"),
//            JadwalItem(2, "Bahasa Indonesia", "08:30 - 10:00", "XII RPL 2", "08:30 - 10:00", "2", "2"),
//            JadwalItem(3, "Fisika", "10:15 - 11:45", "XII RPL 1", "10:15 - 11:45", "1", "3")
//        )
//    }
//
//    private fun navigateToDetailJadwalGuru(jadwal: JadwalItem) {
//        try {
//            val intent = Intent(this, DetailJadwalGuruActivity::class.java).apply {
//                putExtra("JADWAL_DATA", JadwalData(
//                    mataPelajaran = jadwal.mataPelajaran,
//                    kelas = jadwal.kelas,
//                    jam = jadwal.jam,
//                    waktuPelajaran = jadwal.waktuPelajaran,
//                    idKelas = jadwal.idKelas,
//                    idMapel = jadwal.idMapel
//                ))
//            }
//            startActivity(intent)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error navigating to detail jadwal", e)
//            Toast.makeText(this, "Gagal membuka detail jadwal", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun setupFooterNavigation() {
//        try {
//            val btnHome: ImageButton = findViewById(R.id.btnHome)
//            val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
//            val btnChart: ImageButton = findViewById(R.id.btnChart)
//            val btnNotif: ImageButton = findViewById(R.id.btnNotif)
//
//            btnHome.setOnClickListener {
//                // Already on home, bisa ditambahkan refresh jika diperlukan
//                // loadDashboardData()
//            }
//
//            btnCalendar.setOnClickListener {
//                navigateToActivity(RiwayatKehadiranGuruActivity::class.java)
//            }
//
//            btnChart.setOnClickListener {
//                navigateToActivity(RekapKehadiranGuru::class.java)
//            }
//
//            btnNotif.setOnClickListener {
//                navigateToActivity(NotifikasiGuruActivity::class.java)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up footer navigation", e)
//        }
//    }
//
//    private fun <T : AppCompatActivity> navigateToActivity(activityClass: Class<T>) {
//        try {
//            val intent = Intent(this, activityClass)
//            startActivity(intent)
//            // Optional: tambahkan animasi transisi
//            // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error navigating to activity", e)
//            Toast.makeText(this, "Gagal membuka halaman", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun setupLogoutButton() {
//        try {
//            // Mencari berbagai kemungkinan view untuk logout
//            val profileView = findViewById<View>(R.id.group)
//            val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
//
//            // Prioritize dedicated logout button
//            btnLogout?.setOnClickListener {
//                showLogoutDialog()
//            }
//
//            // Fallback to profile view long press
//            profileView?.setOnLongClickListener {
//                showLogoutDialog()
//                true
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up logout button", e)
//        }
//    }
//
//    private fun showLogoutDialog() {
//        try {
//            AlertDialog.Builder(this)
//                .setTitle("Logout")
//                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
//                .setPositiveButton("Ya") { _, _ ->
//                    performLogout()
//                }
//                .setNegativeButton("Tidak") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setCancelable(true)
//                .show()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error showing logout dialog", e)
//        }
//    }
//
//    private fun performLogout() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val apiService = ApiClient.getInstance(this@DashboardGuruActivity)
//                apiService.logout()
//            } catch (e: Exception) {
//                Log.e(TAG, "Logout API error", e)
//            } finally {
//                withContext(Dispatchers.Main) {
//                    // Clear session
//                    sessionManager.clearSession()
//                    ApiClient.resetInstance()
//
//                    // Navigate to login
//                    navigateToLogin()
//
//                    Toast.makeText(this@DashboardGuruActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun navigateToLogin() {
//        val intent = Intent(this, LoginAwal::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        startActivity(intent)
//        finish()
//
//        // Optional: tambahkan animasi
//        // overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        handler.removeCallbacks(runnable)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        handler.post(runnable)
//        // Optional: refresh data saat kembali ke halaman
//        // loadDashboardData()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        handler.removeCallbacks(runnable)
//    }
//
//    // Data class untuk jadwal
//    data class JadwalItem(
//        val id: Int,
//        val mataPelajaran: String,
//        val waktuPelajaran: String,
//        val kelas: String,
//        val jam: String,
//        val idKelas: String,
//        val idMapel: String
//    )
//
//    // Data class untuk passing data ke detail
//    data class JadwalData(
//        val mataPelajaran: String,
//        val kelas: String,
//        val jam: String,
//        val waktuPelajaran: String,
//        val idKelas: String = "",
//        val idMapel: String = ""
//    ) : java.io.Serializable
//}