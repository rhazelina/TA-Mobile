package com.example.ritamesa

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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

        // Setup logout button (if exists in layout)
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
            Toast.makeText(this, "Error initViews: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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

            // Set jam pembelajaran
            txtJamMasuk.text = jamMasukDatabase
            txtJamPulang.text = jamPulangDatabase

            // Setup waktu live yang berjalan terus (WIB)
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
        } catch (e: Exception) {
            Toast.makeText(this, "Error setupDateTime: ${e.message}", Toast.LENGTH_LONG).show()
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
                    } else {
                        // Fallback to dummy data
                        Log.w(TAG, "API failed, using dummy data")
                        loadDummyData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error loading dashboard: ${e.message}", e)
                    // Fallback to dummy data
                    loadDummyData()
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
            data.schedule_today.forEachIndexed { index, schedule ->
                jadwalHariIni.add(
                    JadwalItem(
                        id = index + 1,
                        mataPelajaran = schedule.subject,
                        waktuPelajaran = "${schedule.start_time} - ${schedule.end_time}",
                        kelas = schedule.class_name,
                        jam = "${schedule.start_time} - ${schedule.end_time}",
                        idKelas = schedule.class_id?.toString() ?: "",
                        idMapel = schedule.subject_id?.toString() ?: ""
                    )
                )
            }
            recyclerJadwal.adapter?.notifyDataSetChanged()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI: ${e.message}", e)
            Toast.makeText(this, "Error menampilkan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDummyData() {
        // Data dummy kehadiran
        txtHadirCount.text = "25"
        txtIzinCount.text = "1"
        txtSakitCount.text = "1"
        txtAlphaCount.text = "3"

        // Data dummy jadwal
        jadwalHariIni.clear()
        jadwalHariIni.addAll(generateDummyJadwal())
        recyclerJadwal.adapter?.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        try {
            // Setup adapter
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
        }
    }

    private fun generateDummyJadwal(): List<JadwalItem> {
        return listOf(
            JadwalItem(1, "Matematika", "07:00 - 08:30", "XII RPL 1", "07:00 - 08:30", "1", "1"),
            JadwalItem(2, "Bahasa Indonesia", "08:30 - 10:00", "XII RPL 2", "08:30 - 10:00", "2", "2"),
            JadwalItem(3, "Fisika", "10:15 - 11:45", "XII RPL 1", "10:15 - 11:45", "1", "3")
        )
    }

    private fun navigateToDetailJadwalGuru(jadwal: JadwalItem) {
        try {
            val intent = Intent(this, DetailJadwalGuruActivity::class.java)
            intent.putExtra("JADWAL_DATA", JadwalData(
                mataPelajaran = jadwal.mataPelajaran,
                kelas = jadwal.kelas,
                jam = jadwal.jam,
                waktuPelajaran = jadwal.waktuPelajaran
            ))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFooterNavigation() {
        try {
            val btnHome: ImageButton = findViewById(R.id.btnHome)
            val btnCalendar: ImageButton = findViewById(R.id.btnCalendar)
            val btnChart: ImageButton = findViewById(R.id.btnChart)
            val btnNotif: ImageButton = findViewById(R.id.btnNotif)

            btnHome.setOnClickListener {
                // Already on home
            }

            btnCalendar.setOnClickListener {
                val intent = Intent(this, RiwayatKehadiranGuruActivity::class.java)
                startActivity(intent)
            }

            btnChart.setOnClickListener {
                val intent = Intent(this, RekapKehadiranGuru::class.java)
                startActivity(intent)
            }

            btnNotif.setOnClickListener {
                val intent = Intent(this, NotifikasiGuruActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error footer nav: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLogoutButton() {
        try {
            // If you have a logout button in layout, uncomment and use this
            // val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
            // btnLogout?.setOnClickListener {
            //     showLogoutDialog()
            // }

            // For now, add long press on profile to logout
            val profileView = findViewById<android.view.View>(R.id.group)
            profileView?.setOnLongClickListener {
                showLogoutDialog()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setup logout: ${e.message}")
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@DashboardGuruActivity)
                apiService.logout()
            } catch (e: Exception) {
                Log.e(TAG, "Logout API error: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    // Clear session
                    sessionManager.clearSession()
                    ApiClient.resetInstance()

                    // Navigate to login
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginAwal::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
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