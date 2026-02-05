package com.example.ritamesa

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class DashboardSiswaActivity : AppCompatActivity() {

    // Session Manager
    private lateinit var sessionManager: SessionManager

    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView
    private lateinit var recyclerJadwal: RecyclerView
    private lateinit var btnHome: ImageButton
    private lateinit var btnAssignment: ImageButton
    private lateinit var profileSiswa: ImageView
    private lateinit var profileOverlay: ImageView

    // TAMBAHKAN INI - TextView tanggal di jam layout
    private lateinit var txtTanggalDiJamLayout: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val jadwalHariIni = mutableListOf<JadwalSiswaItem>()
    private var isPengurus = false

    private val jamMasukDatabase = "07:00:00"
    private val jamPulangDatabase = "15:00:00"

    companion object {
        private const val TAG = "DashboardSiswa"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== DASHBOARD SISWA ACTIVITY START ===")

        sessionManager = SessionManager(this)

        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        try {
            // Perbaikan: gunakan cara yang lebih aman untuk mendapatkan isPengurus
            isPengurus = if (intent.hasExtra("IS_PENGURUS")) {
                intent.getBooleanExtra("IS_PENGURUS", false)
            } else {
                sessionManager.isClassOfficer()
            }
            Log.d(TAG, "isPengurus = $isPengurus")

            setContentView(R.layout.dashboard_siswa)
            Log.d(TAG, "Layout loaded successfully")

            initViews()
            setupDateTime()
            setupProfileImage()
            setupRecyclerView()
            setupButtonListeners()
            setupLogoutButton() // FUNGSI INI PERLU DITAMBAHKAN
            updateRoleUI()

            // Load data from API
            loadDashboardData() // FUNGSI INI PERLU DITAMBAHKAN

            Toast.makeText(this, "Dashboard dimuat sukses", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "ERROR in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // TAMBAHKAN FUNGSI navigateToLogin() YANG HILANG
    private fun navigateToLogin() {
        val intent = Intent(this, LoginAwal::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun initViews() {
        try {
            txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
            txtWaktuLive = findViewById(R.id.txtWaktuLive)
            txtJamMasuk = findViewById(R.id.txtJamMasuk)
            txtJamPulang = findViewById(R.id.txtJamPulang)
            recyclerJadwal = findViewById(R.id.recyclerJadwal)
            btnHome = findViewById(R.id.btnHome)
            btnAssignment = findViewById(R.id.btnAssignment)
            profileSiswa = findViewById(R.id.profile_siswa)
            profileOverlay = findViewById(R.id.profile_overlay)

            // TAMBAHKAN INI - TextView tanggal di jam layout
            txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)

            // DEBUG DETAIL VIEWS
            Log.d(TAG, "=== VIEW INITIALIZATION ===")
            Log.d(TAG, "txtTanggalSekarang: ${txtTanggalSekarang != null}")
            Log.d(TAG, "txtWaktuLive: ${txtWaktuLive != null}")
            Log.d(TAG, "txtJamMasuk: ${txtJamMasuk != null}")
            Log.d(TAG, "txtJamPulang: ${txtJamPulang != null}")
            Log.d(TAG, "profileSiswa: ${profileSiswa != null}")
            Log.d(TAG, "profileOverlay: ${profileOverlay != null}")
            Log.d(TAG, "recyclerJadwal: ${recyclerJadwal != null}")
            Log.d(TAG, "btnHome: ${btnHome != null}")
            Log.d(TAG, "btnAssignment: ${btnAssignment != null}")
            Log.d(TAG, "=== END VIEW INIT ===")

        } catch (e: Exception) {
            Log.e(TAG, "Error in initViews: ${e.message}", e)
            Toast.makeText(this, "Error finding views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDateTime() {
        try {
            // Format tanggal Indonesia dengan locale Indonesia
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val currentDate = Date()
            val tanggalHariIni = dateFormat.format(currentDate)

            // Ubah ke format: SENIN, 1 JANUARI 2025 (huruf besar sesuai UI)
            val tanggalFormatBesar = tanggalHariIni.uppercase(Locale.forLanguageTag("id-ID"))

            // UPDATE SEMUA TEXTVIEW TANGGAL
            txtTanggalSekarang.text = tanggalFormatBesar
            txtTanggalDiJamLayout.text = tanggalFormatBesar // TAMBAHKAN INI

            // Format jam pembelajaran (hilangkan detik jika ada)
            val jamMasuk = jamMasukDatabase.substring(0, 5)
            val jamPulang = jamPulangDatabase.substring(0, 5)
            txtJamMasuk.text = jamMasuk
            txtJamPulang.text = jamPulang

            Log.d(TAG, "Date setup: $tanggalFormatBesar")
            Log.d(TAG, "Jam masuk: $jamMasuk, Jam pulang: $jamPulang")

            // Setup live clock dengan timezone WIB
            runnable = object : Runnable {
                override fun run() {
                    updateLiveTime()
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(runnable)

        } catch (e: Exception) {
            Log.e(TAG, "Error in setupDateTime: ${e.message}")
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

    private fun setupProfileImage() {
        try {
            if (isPengurus) {
                profileSiswa.setImageResource(R.drawable.profile_pengurus)
                profileOverlay.setImageResource(R.drawable.profile_p)
                Log.d(TAG, "Profile set to PENGURUS")
            } else {
                profileSiswa.setImageResource(R.drawable.profile_siswa)
                profileOverlay.setImageResource(R.drawable.profile_p)
                Log.d(TAG, "Profile set to SISWA BIASA")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupProfileImage: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            jadwalHariIni.clear()
            jadwalHariIni.addAll(generateDummyJadwal())

            Log.d(TAG, "Setting up RecyclerView with ${jadwalHariIni.size} items")

            recyclerJadwal.layoutManager = LinearLayoutManager(this)
            recyclerJadwal.setHasFixedSize(true)

            val adapter = JadwalSiswaAdapter(jadwalHariIni)
            recyclerJadwal.adapter = adapter
            adapter.notifyDataSetChanged()

            Log.d(TAG, "RecyclerView setup COMPLETE")

        } catch (e: Exception) {
            Log.e(TAG, "Error in setupRecyclerView: ${e.message}", e)
            Toast.makeText(this, "Error loading schedule", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateDummyJadwal(): List<JadwalSiswaItem> {
        val jadwalList = mutableListOf<JadwalSiswaItem>()

        val dataJadwal = listOf(
            DataJadwal("Jam Ke 1-3", "B. Indonesia", "Hadir", "07:10"),
            DataJadwal("Jam Ke 4-5", "Matematika", "Hadir", "09:00"),
            DataJadwal("Jam Ke 6-7", "IPA", "Hadir", "10:40"),
            DataJadwal("Jam Ke 8-10", "Bahasa Inggris", "Hadir", "12:45"),
        )

        for ((index, data) in dataJadwal.withIndex()) {
            jadwalList.add(
                JadwalSiswaItem(
                    id = index + 1,
                    sesi = data.sesi,
                    mataPelajaran = data.mapel,
                    status = data.status,
                    jam = data.jam,
                    keterangan = "Siswa ${data.status} ${if (data.status == "Hadir") "Tepat Waktu" else ""}"
                )
            )
        }

        Log.d(TAG, "Generated ${jadwalList.size} jadwal items")
        return jadwalList
    }

    private fun setupButtonListeners() {
        try {
            Log.d(TAG, "=== SETUP BUTTON LISTENERS ===")

            if (btnHome != null) {
                Log.d(TAG, "btnHome FOUND, setting onClick...")
                btnHome.setOnClickListener {
                    Toast.makeText(this, "Anda sudah di Dashboard", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Tombol Home diklik")
                }
                Log.d(TAG, "btnHome onClick listener SET")
            } else {
                Log.e(TAG, "btnHome is NULL!")
            }

            if (btnAssignment != null) {
                Log.d(TAG, "btnAssignment FOUND, setting onClick...")
                btnAssignment.setOnClickListener {
                    Log.d(TAG, ">>> TOMBOL ASSIGNMENT DIKLIK! <<<")

                    try {
                        if (isPengurus) {
                            Toast.makeText(this, "Membuka Riwayat Kehadiran Kelas (Pengurus)...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, RiwayatKehadiranKelasPengurusActivity::class.java)
                            intent.putExtra("IS_PENGURUS", true)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasPengurusActivity BERHASIL")
                        } else {
                            Toast.makeText(this, "Membuka Riwayat Kehadiran Kelas (Siswa)...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, RiwayatKehadiranKelasSiswaActivity::class.java)
                            intent.putExtra("IS_PENGURUS", false)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasSiswaActivity BERHASIL")
                        }
                    } catch (e: ClassNotFoundException) {
                        Log.e(TAG, "CLASS NOT FOUND ERROR!", e)
                        Toast.makeText(this,
                            "Error: Activity tidak ditemukan!",
                            Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "GENERAL ERROR: ${e.message}", e)
                        Toast.makeText(this,
                            "Gagal membuka halaman: ${e.localizedMessage}",
                            Toast.LENGTH_LONG).show()
                    }
                }
                Log.d(TAG, "btnAssignment onClick listener SET")
            } else {
                Log.e(TAG, "btnAssignment is NULL!")
                Toast.makeText(this, "ERROR: Tombol Riwayat tidak ditemukan", Toast.LENGTH_LONG).show()
            }

            Log.d(TAG, "=== BUTTON LISTENERS SETUP COMPLETE ===")

        } catch (e: Exception) {
            Log.e(TAG, "Error in setupButtonListeners: ${e.message}", e)
            Toast.makeText(this, "Error setting up buttons: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // TAMBAHKAN FUNGSI loadDashboardData() YANG HILANG
    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@DashboardSiswaActivity)
                val response = apiService.getStudentDashboard()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        updateUIWithData(data)
                        Toast.makeText(this@DashboardSiswaActivity, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "API response unsuccessful: ${response.code()} - ${response.message()}")
                        Toast.makeText(this@DashboardSiswaActivity, "Menggunakan data simulasi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error loading dashboard", e)
                    Toast.makeText(this@DashboardSiswaActivity, "Gagal memuat data, menggunakan data simulasi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // TAMBAHKAN FUNGSI updateUIWithData() YANG DIPANGGIL OLEH loadDashboardData()
    private fun updateUIWithData(data: com.example.ritamesa.network.StudentDashboardResponse) {
        try {
            // Update data siswa jika diperlukan
            Log.d(TAG, "Data siswa: ${data.student.name}, Kelas: ${data.student.class_name}")

            // Update jadwal hari ini dari API
            jadwalHariIni.clear()
            if (data.schedule_today.isNotEmpty()) {
                data.schedule_today.forEachIndexed { index, schedule ->
                    jadwalHariIni.add(
                        JadwalSiswaItem(
                            id = schedule.id,
                            sesi = schedule.time_slot,
                            mataPelajaran = schedule.subject,
                            status = schedule.status,
                            jam = schedule.start_time,
                            keterangan = schedule.status_label
                        )
                    )
                }
            }

            // Refresh adapter
            recyclerJadwal.adapter?.notifyDataSetChanged()
            Log.d(TAG, "UI updated with API data, ${jadwalHariIni.size} items")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI with data", e)
            Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
        }
    }

    // TAMBAHKAN FUNGSI setupLogoutButton() YANG HILANG
    private fun setupLogoutButton() {
        try {
            // Jika ada tombol logout di layout, bisa ditambahkan
            // val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
            // btnLogout?.setOnClickListener { showLogoutDialog() }

            // Atau gunakan long press pada profile
            profileSiswa.setOnLongClickListener {
                showLogoutDialog()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up logout button", e)
        }
    }

    // TAMBAHKAN FUNGSI showLogoutDialog()
    private fun showLogoutDialog() {
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
    }

    // TAMBAHKAN FUNGSI performLogout()
    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@DashboardSiswaActivity)
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

                    Toast.makeText(this@DashboardSiswaActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateRoleUI() {
        val role = if (isPengurus) "Pengurus Kelas" else "Siswa"
        Toast.makeText(this, "Selamat datang, $role!", Toast.LENGTH_SHORT).show()
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
        try {
            handler.removeCallbacks(runnable)
            Log.d(TAG, "=== DASHBOARD ACTIVITY DESTROYED ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing handler callbacks: ${e.message}")
        }
    }

    // ========== ADAPTER CLASSES ==========
    private inner class JadwalSiswaAdapter(
        private val jadwalList: List<JadwalSiswaItem>
    ) : RecyclerView.Adapter<JadwalSiswaAdapter.JadwalViewHolder>() {

        inner class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
            val txtMapelDetail: TextView = itemView.findViewById(R.id.Mata_pelajaran)
            val txtKeterangan: TextView = itemView.findViewById(R.id.Text_keterangan_hadir)
            val txtJam: TextView = itemView.findViewById(R.id.tvJam_1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
            try {
                Log.d(TAG, "Adapter: onCreateViewHolder")
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dashboard_siswa, parent, false)
                return JadwalViewHolder(view)
            } catch (e: Exception) {
                Log.e(TAG, "Adapter ERROR in onCreateViewHolder: ${e.message}", e)
                throw e
            }
        }

        override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
            try {
                if (position >= jadwalList.size) {
                    Log.e(TAG, "Adapter: Invalid position $position")
                    return
                }

                val jadwal = jadwalList[position]
                holder.txtMataPelajaran.text = jadwal.sesi
                holder.txtMapelDetail.text = jadwal.mataPelajaran
                holder.txtKeterangan.text = jadwal.keterangan
                holder.txtJam.text = jadwal.jam

                Log.d(TAG, "Adapter: Bound position $position - ${jadwal.sesi}")

            } catch (e: Exception) {
                Log.e(TAG, "Adapter ERROR in onBindViewHolder: ${e.message}", e)
            }
        }

        override fun getItemCount(): Int {
            val count = jadwalList.size
            Log.d(TAG, "Adapter: getItemCount = $count")
            return count
        }
    }

    // ========== DATA CLASSES ==========
    data class JadwalSiswaItem(
        val id: Int,
        val sesi: String,
        val mataPelajaran: String,
        val status: String,
        val jam: String,
        val keterangan: String
    )

    data class DataJadwal(
        val sesi: String,
        val mapel: String,
        val status: String,
        val jam: String
    )
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
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageButton
//import android.widget.ImageView
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
//import java.util.*
//
//class DashboardSiswaActivity : AppCompatActivity() {
//
//    // Session Manager
//    private lateinit var sessionManager: SessionManager
//
//    private lateinit var txtTanggalSekarang: TextView
//    private lateinit var txtWaktuLive: TextView
//    private lateinit var txtJamMasuk: TextView
//    private lateinit var txtJamPulang: TextView
//    private lateinit var recyclerJadwal: RecyclerView
//    private lateinit var btnHome: ImageButton
//    private lateinit var btnAssignment: ImageButton
//    private lateinit var profileSiswa: ImageView
//    private lateinit var profileOverlay: ImageView
//
//    // TAMBAHKAN INI - TextView tanggal di jam layout
//    private lateinit var txtTanggalDiJamLayout: TextView
//
//    private val handler = Handler(Looper.getMainLooper())
//    private lateinit var runnable: Runnable
//    private val jadwalHariIni = mutableListOf<JadwalSiswaItem>()
//    private var isPengurus = false
//
//    private val jamMasukDatabase = "07:00:00"
//    private val jamPulangDatabase = "15:00:00"
//
//    companion object {
//        private const val TAG = "DashboardSiswa"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d(TAG, "=== DASHBOARD SISWA ACTIVITY START ===")
//
//        sessionManager = SessionManager(this)
//
//        // Check if logged in
//        if (!sessionManager.isLoggedIn()) {
//            navigateToLogin()
//            return
//        }
//
//        try {
//            // Perbaikan: gunakan cara yang lebih aman untuk mendapatkan isPengurus
//            isPengurus = if (intent.hasExtra("IS_PENGURUS")) {
//                intent.getBooleanExtra("IS_PENGURUS", false)
//            } else {
//                sessionManager.isClassOfficer()
//            }
//            Log.d(TAG, "isPengurus = $isPengurus")
//
//            setContentView(R.layout.dashboard_siswa)
//            Log.d(TAG, "Layout loaded successfully")
//
//            initViews()
//            setupDateTime()
//            setupProfileImage()
//            setupRecyclerView()
//            setupButtonListeners()
//            setupLogoutButton()
//            updateRoleUI()
//
//            // Load data from API
//            loadDashboardData()
//
//            Toast.makeText(this, "Dashboard dimuat sukses", Toast.LENGTH_SHORT).show()
//
//        } catch (e: Exception) {
//            Log.e(TAG, "ERROR in onCreate: ${e.message}", e)
//            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun initViews() {
//        try {
//            txtTanggalSekarang = findViewById(R.id.txtTanggalSekarang)
//            txtWaktuLive = findViewById(R.id.txtWaktuLive)
//            txtJamMasuk = findViewById(R.id.txtJamMasuk)
//            txtJamPulang = findViewById(R.id.txtJamPulang)
//            recyclerJadwal = findViewById(R.id.recyclerJadwal)
//            btnHome = findViewById(R.id.btnHome)
//            btnAssignment = findViewById(R.id.btnAssignment)
//            profileSiswa = findViewById(R.id.profile_siswa)
//            profileOverlay = findViewById(R.id.profile_overlay)
//
//            // TAMBAHKAN INI - TextView tanggal di jam layout
//            txtTanggalDiJamLayout = findViewById(R.id.txtTanggalDiJamLayout)
//
//            // DEBUG DETAIL VIEWS
//            Log.d(TAG, "=== VIEW INITIALIZATION ===")
//            Log.d(TAG, "txtTanggalSekarang: ${txtTanggalSekarang != null}")
//            Log.d(TAG, "txtWaktuLive: ${txtWaktuLive != null}")
//            Log.dTag, "txtJamMasuk: ${txtJamMasuk != null}")
//            Log.d(TAG, "txtJamPulang: ${txtJamPulang != null}")
//            Log.d(TAG, "profileSiswa: ${profileSiswa != null}")
//            Log.d(TAG, "profileOverlay: ${profileOverlay != null}")
//            Log.d(TAG, "recyclerJadwal: ${recyclerJadwal != null}")
//            Log.d(TAG, "btnHome: ${btnHome != null}")
//            Log.d(TAG, "btnAssignment: ${btnAssignment != null}")
//            Log.d(TAG, "=== END VIEW INIT ===")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in initViews: ${e.message}", e)
//            Toast.makeText(this, "Error finding views: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun setupDateTime() {
//        try {
//            // Format tanggal Indonesia dengan locale Indonesia
//            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
//            val currentDate = Date()
//            val tanggalHariIni = dateFormat.format(currentDate)
//
//            // Ubah ke format: SENIN, 1 JANUARI 2025 (huruf besar sesuai UI)
//            val tanggalFormatBesar = tanggalHariIni.uppercase(Locale.forLanguageTag("id-ID"))
//
//            // UPDATE SEMUA TEXTVIEW TANGGAL
//            txtTanggalSekarang.text = tanggalFormatBesar
//            txtTanggalDiJamLayout.text = tanggalFormatBesar // TAMBAHKAN INI
//
//            // Format jam pembelajaran (hilangkan detik jika ada)
//            val jamMasuk = jamMasukDatabase.substring(0, 5)
//            val jamPulang = jamPulangDatabase.substring(0, 5)
//            txtJamMasuk.text = jamMasuk
//            txtJamPulang.text = jamPulang
//
//            Log.d(TAG, "Date setup: $tanggalFormatBesar")
//            Log.d(TAG, "Jam masuk: $jamMasuk, Jam pulang: $jamPulang")
//
//            // Setup live clock dengan timezone WIB
//            runnable = object : Runnable {
//                override fun run() {
//                    updateLiveTime()
//                    handler.postDelayed(this, 1000)
//                }
//            }
//            handler.post(runnable)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in setupDateTime: ${e.message}")
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
//    private fun setupProfileImage() {
//        try {
//            if (isPengurus) {
//                profileSiswa.setImageResource(R.drawable.profile_pengurus)
//                profileOverlay.setImageResource(R.drawable.profile_p)
//                Log.d(TAG, "Profile set to PENGURUS")
//            } else {
//                profileSiswa.setImageResource(R.drawable.profile_siswa)
//                profileOverlay.setImageResource(R.drawable.profile_p)
//                Log.d(TAG, "Profile set to SISWA BIASA")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in setupProfileImage: ${e.message}", e)
//        }
//    }
//
//    private fun setupRecyclerView() {
//        try {
//            jadwalHariIni.clear()
//            jadwalHariIni.addAll(generateDummyJadwal())
//
//            Log.d(TAG, "Setting up RecyclerView with ${jadwalHariIni.size} items")
//
//            recyclerJadwal.layoutManager = LinearLayoutManager(this)
//            recyclerJadwal.setHasFixedSize(true)
//
//            val adapter = JadwalSiswaAdapter(jadwalHariIni)
//            recyclerJadwal.adapter = adapter
//            adapter.notifyDataSetChanged()
//
//            Log.d(TAG, "RecyclerView setup COMPLETE")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in setupRecyclerView: ${e.message}", e)
//            Toast.makeText(this, "Error loading schedule", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun generateDummyJadwal(): List<JadwalSiswaItem> {
//        val jadwalList = mutableListOf<JadwalSiswaItem>()
//
//        val dataJadwal = listOf(
//            DataJadwal("Jam Ke 1-3", "B. Indonesia", "Hadir", "07:10"),
//            DataJadwal("Jam Ke 4-5", "Matematika", "Hadir", "09:00"),
//            DataJadwal("Jam Ke 6-7", "IPA", "Hadir", "10:40"),
//            DataJadwal("Jam Ke 8-10", "Bahasa Inggris", "Hadir", "12:45"),
//        )
//
//        for ((index, data) in dataJadwal.withIndex()) {
//            jadwalList.add(
//                JadwalSiswaItem(
//                    id = index + 1,
//                    sesi = data.sesi,
//                    mataPelajaran = data.mapel,
//                    status = data.status,
//                    jam = data.jam,
//                    keterangan = "Siswa ${data.status} ${if (data.status == "Hadir") "Tepat Waktu" else ""}"
//                )
//            )
//        }
//
//        Log.d(TAG, "Generated ${jadwalList.size} jadwal items")
//        return jadwalList
//    }
//
//    private fun setupButtonListeners() {
//        try {
//            Log.d(TAG, "=== SETUP BUTTON LISTENERS ===")
//
//            if (btnHome != null) {
//                Log.d(TAG, "btnHome FOUND, setting onClick...")
//                btnHome.setOnClickListener {
//                    Toast.makeText(this, "Anda sudah di Dashboard", Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, "Tombol Home diklik")
//                }
//                Log.d(TAG, "btnHome onClick listener SET")
//            } else {
//                Log.e(TAG, "btnHome is NULL!")
//            }
//
//            if (btnAssignment != null) {
//                Log.d(TAG, "btnAssignment FOUND, setting onClick...")
//                btnAssignment.setOnClickListener {
//                    Log.d(TAG, ">>> TOMBOL ASSIGNMENT DIKLIK! <<<")
//
//                    try {
//                        if (isPengurus) {
//                            Toast.makeText(this, "Membuka Riwayat Kehadiran Kelas (Pengurus)...", Toast.LENGTH_SHORT).show()
//                            val intent = Intent(this, RiwayatKehadiranKelasPengurusActivity::class.java)
//                            intent.putExtra("IS_PENGURUS", true)
//                            startActivity(intent)
//                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                            Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasPengurusActivity BERHASIL")
//                        } else {
//                            Toast.makeText(this, "Membuka Riwayat Kehadiran Kelas (Siswa)...", Toast.LENGTH_SHORT).show()
//                            val intent = Intent(this, RiwayatKehadiranKelasSiswaActivity::class.java)
//                            intent.putExtra("IS_PENGURUS", false)
//                            startActivity(intent)
//                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                            Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasSiswaActivity BERHASIL")
//                        }
//                    } catch (e: ClassNotFoundException) {
//                        Log.e(TAG, "CLASS NOT FOUND ERROR!", e)
//                        Toast.makeText(this,
//                            "Error: Activity tidak ditemukan!",
//                            Toast.LENGTH_LONG).show()
//                    } catch (e: Exception) {
//                        Log.e(TAG, "GENERAL ERROR: ${e.message}", e)
//                        Toast.makeText(this,
//                            "Gagal membuka halaman: ${e.localizedMessage}",
//                            Toast.LENGTH_LONG).show()
//                    }
//                }
//                Log.d(TAG, "btnAssignment onClick listener SET")
//            } else {
//                Log.e(TAG, "btnAssignment is NULL!")
//                Toast.makeText(this, "ERROR: Tombol Riwayat tidak ditemukan", Toast.LENGTH_LONG).show()
//            }
//
//            Log.d(TAG, "=== BUTTON LISTENERS SETUP COMPLETE ===")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in setupButtonListeners: ${e.message}", e)
//            Toast.makeText(this, "Error setting up buttons: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun loadDashboardData() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val apiService = ApiClient.getInstance(this@DashboardSiswaActivity)
//                val response = apiService.getStudentDashboard()
//
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful && response.body() != null) {
//                        val data = response.body()!!
//                        updateUIWithData(data)
//                        Toast.makeText(this@DashboardSiswaActivity, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Log.w(TAG, "API response unsuccessful: ${response.code()} - ${response.message()}")
//                        Toast.makeText(this@DashboardSiswaActivity, "Menggunakan data simulasi", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Log.e(TAG, "Error loading dashboard", e)
//                    Toast.makeText(this@DashboardSiswaActivity, "Gagal memuat data, menggunakan data simulasi", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun updateUIWithData(data: com.example.ritamesa.network.StudentDashboardResponse) {
//        try {
//            // Update data siswa jika diperlukan
//            Log.d(TAG, "Data siswa: ${data.student.name}, Kelas: ${data.student.class_name}")
//
//            // Update jadwal hari ini dari API
//            jadwalHariIni.clear()
//            if (data.schedule_today.isNotEmpty()) {
//                data.schedule_today.forEachIndexed { index, schedule ->
//                    jadwalHariIni.add(
//                        JadwalSiswaItem(
//                            id = schedule.id,
//                            sesi = schedule.time_slot,
//                            mataPelajaran = schedule.subject,
//                            status = schedule.status,
//                            jam = schedule.start_time,
//                            keterangan = schedule.status_label
//                        )
//                    )
//                }
//            }
//
//            // Refresh adapter
//            recyclerJadwal.adapter?.notifyDataSetChanged()
//            Log.d(TAG, "UI updated with API data, ${jadwalHariIni.size} items")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating UI with data", e)
//            Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun setupLogoutButton() {
//        try {
//            // Jika ada tombol logout di layout, bisa ditambahkan
//            // val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
//            // btnLogout?.setOnClickListener { showLogoutDialog() }
//
//            // Atau gunakan long press pada profile
//            profileSiswa.setOnLongClickListener {
//                showLogoutDialog()
//                true
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up logout button", e)
//        }
//    }
//
//    private fun showLogoutDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("Logout")
//            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
//            .setPositiveButton("Ya") { _, _ ->
//                performLogout()
//            }
//            .setNegativeButton("Tidak", null)
//            .show()
//    }
//
//    private fun performLogout() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val apiService = ApiClient.getInstance(this@DashboardSiswaActivity)
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
//                    Toast.makeText(this@DashboardSiswaActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()
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
//    }
//
//    private fun updateRoleUI() {
//        val role = if (isPengurus) "Pengurus Kelas" else "Siswa"
//        Toast.makeText(this, "Selamat datang, $role!", Toast.LENGTH_SHORT).show()
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
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            handler.removeCallbacks(runnable)
//            Log.d(TAG, "=== DASHBOARD ACTIVITY DESTROYED ===")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error removing handler callbacks: ${e.message}")
//        }
//    }
//
//    // ========== ADAPTER CLASSES ==========
//    private inner class JadwalSiswaAdapter(
//        private val jadwalList: List<JadwalSiswaItem>
//    ) : RecyclerView.Adapter<JadwalSiswaAdapter.JadwalViewHolder>() {
//
//        inner class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val txtMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
//            val txtMapelDetail: TextView = itemView.findViewById(R.id.Mata_pelajaran)
//            val txtKeterangan: TextView = itemView.findViewById(R.id.Text_keterangan_hadir)
//            val txtJam: TextView = itemView.findViewById(R.id.tvJam_1)
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
//            try {
//                Log.d(TAG, "Adapter: onCreateViewHolder")
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_dashboard_siswa, parent, false)
//                return JadwalViewHolder(view)
//            } catch (e: Exception) {
//                Log.e(TAG, "Adapter ERROR in onCreateViewHolder: ${e.message}", e)
//                throw e
//            }
//        }
//
//        override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
//            try {
//                if (position >= jadwalList.size) {
//                    Log.e(TAG, "Adapter: Invalid position $position")
//                    return
//                }
//
//                val jadwal = jadwalList[position]
//                holder.txtMataPelajaran.text = jadwal.sesi
//                holder.txtMapelDetail.text = jadwal.mataPelajaran
//                holder.txtKeterangan.text = jadwal.keterangan
//                holder.txtJam.text = jadwal.jam
//
//                Log.d(TAG, "Adapter: Bound position $position - ${jadwal.sesi}")
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Adapter ERROR in onBindViewHolder: ${e.message}", e)
//            }
//        }
//
//        override fun getItemCount(): Int {
//            val count = jadwalList.size
//            Log.d(TAG, "Adapter: getItemCount = $count")
//            return count
//        }
//    }
//
//    // ========== DATA CLASSES ==========
//    data class JadwalSiswaItem(
//        val id: Int,
//        val sesi: String,
//        val mataPelajaran: String,
//        val status: String,
//        val jam: String,
//        val keterangan: String
//    )
//
//    data class DataJadwal(
//        val sesi: String,
//        val mapel: String,
//        val status: String,
//        val jam: String
//    )
//}