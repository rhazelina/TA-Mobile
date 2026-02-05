package com.example.ritamesa

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DashboardWaliKelasActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardWaliKelas"
        private const val UPDATE_INTERVAL_MS = 1000L
    }

    // Session and API
    private lateinit var sessionManager: com.example.ritamesa.network.SessionManager

    // Deklarasi komponen UI
    private lateinit var txtTanggalSekarang: TextView
    private lateinit var txtWaktuLive: TextView
    private lateinit var txtJamMasuk: TextView
    private lateinit var txtJamPulang: TextView
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

    // RecyclerView
    private lateinit var recyclerJadwal: RecyclerView
    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var jadwalAdapter: JadwalAdapter
    private lateinit var riwayatAdapter: RiwayatAbsenAdapter

    // Data lists
    private val jadwalList = mutableListOf<DashboardGuruActivity.JadwalItem>()
    private val riwayatList = mutableListOf<RiwayatAbsenItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_wali_kelas)

        // Initialize session manager
        sessionManager = com.example.ritamesa.network.SessionManager(this)

        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        // Inisialisasi komponen UI
        initViews()

        // Setup tanggal dan waktu
        setupDateTime()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup footer navigation
        setupFooterNavigation()

        // Setup button listeners untuk kehadiran
        setupKehadiranButtons()

        // Load data dari API
        loadDashboardData()
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
            txtNominalSiswa = findViewById(R.id.nominal_siswa)
            txtHadirCount = findViewById(R.id.txt_hadir_count)
            txtIzinCount = findViewById(R.id.txt_izin_count)
            txtSakitCount = findViewById(R.id.txt_sakit_count)
            txtAlphaCount = findViewById(R.id.txt_alpha_count)

            // RecyclerView
            recyclerJadwal = findViewById(R.id.recyclerJadwal)
            recyclerRiwayat = findViewById(R.id.recyclerJadwal1)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Terjadi kesalahan dalam memuat tampilan", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDateTime() {
        try {
            // Format tanggal Indonesia dengan locale Indonesia
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val currentDate = Date()
            val tanggalHariIni = dateFormat.format(currentDate)

            // Ubah ke format huruf besar sesuai UI
            val tanggalFormatBesar = tanggalHariIni.uppercase(Locale.forLanguageTag("id-ID"))

            // Set tanggal dengan format yang benar
            txtTanggalSekarang.text = tanggalFormatBesar
            txtTanggalDiJamLayout.text = tanggalFormatBesar

            // Set jam pembelajaran default
            txtJamMasuk.text = "07:00"
            txtJamPulang.text = "15:00"

            // Setup waktu live yang berjalan terus (WIB - Indonesia Barat)
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

    private fun setupRecyclerView() {
        try {
            // Setup adapter jadwal
            jadwalAdapter = JadwalAdapter(jadwalList) { jadwal ->
                if (jadwal.mataPelajaran != "Tidak ada jadwal") {
                    navigateToDetailJadwalWakel(jadwal)
                } else {
                    Toast.makeText(this, "Tidak ada jadwal untuk hari ini", Toast.LENGTH_SHORT).show()
                }
            }

            // Setup layout manager untuk jadwal
            recyclerJadwal.layoutManager = LinearLayoutManager(this)
            recyclerJadwal.adapter = jadwalAdapter
            recyclerJadwal.setHasFixedSize(true)

            // Setup adapter riwayat
            riwayatAdapter = RiwayatAbsenAdapter(riwayatList)

            // Setup layout manager untuk riwayat
            recyclerRiwayat.layoutManager = LinearLayoutManager(this)
            recyclerRiwayat.adapter = riwayatAdapter
            recyclerRiwayat.setHasFixedSize(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up recycler view", e)
            Toast.makeText(this, "Gagal memuat daftar", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // PERBAIKAN: Ganti dengan endpoint yang benar
                val apiService = ApiClient.getInstance(this@DashboardWaliKelasActivity)
                val response = apiService.getTeacherDashboard() // Gunakan endpoint yang ada

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        updateUIWithData(data)
                        Toast.makeText(this@DashboardWaliKelasActivity, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "API response unsuccessful: ${response.code()} - ${response.message()}")
                        loadDummyData()
                        Toast.makeText(this@DashboardWaliKelasActivity, "Menggunakan data simulasi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error loading dashboard", e)
                    loadDummyData()
                    Toast.makeText(this@DashboardWaliKelasActivity, "Gagal memuat data, menggunakan data simulasi", Toast.LENGTH_SHORT).show()
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

            // Update total siswa (gunakan data dari API jika ada, atau hitung dari kehadiran)
            val totalStudents = data.attendance_summary.present +
                    data.attendance_summary.excused +
                    data.attendance_summary.sick +
                    data.attendance_summary.absent
            txtNominalSiswa.text = totalStudents.toString()

            // Update jadwal hari ini
            jadwalList.clear()
            if (data.schedule_today.isNotEmpty()) {
                data.schedule_today.forEach { schedule ->
                    jadwalList.add(
                        DashboardGuruActivity.JadwalItem(
                            id = schedule.id,
                            mataPelajaran = schedule.subject,
                            waktuPelajaran = schedule.time_slot,
                            kelas = schedule.class_name,
                            jam = "${schedule.start_time} - ${schedule.end_time}"
                        )
                    )
                }
            } else {
                jadwalList.add(
                    DashboardGuruActivity.JadwalItem(
                        id = 0,
                        mataPelajaran = "Tidak ada jadwal",
                        waktuPelajaran = "--:-- - --:--",
                        kelas = "-",
                        jam = "--:-- - --:--"
                    )
                )
            }
            jadwalAdapter.notifyDataSetChanged()

            // Update riwayat absensi dengan data dummy (sesuaikan dengan API nanti)
            updateRiwayatWithDummyData()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI with data", e)
            Toast.makeText(this, "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRiwayatWithDummyData() {
        riwayatList.clear()
        riwayatList.addAll(generateDummyRiwayat())
        riwayatAdapter.notifyDataSetChanged()
    }

    private fun loadDummyData() {
        try {
            // Data dummy kehadiran
            txtNominalSiswa.text = "30"
            txtHadirCount.text = "20"
            txtIzinCount.text = "3"
            txtSakitCount.text = "2"
            txtAlphaCount.text = "5"

            // Data dummy jadwal
            jadwalList.clear()
            jadwalList.addAll(generateDummyJadwal())
            jadwalAdapter.notifyDataSetChanged()

            // Data dummy riwayat
            riwayatList.clear()
            riwayatList.addAll(generateDummyRiwayat())
            riwayatAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            Log.e(TAG, "Error loading dummy data", e)
        }
    }

    private fun generateDummyJadwal(): List<DashboardGuruActivity.JadwalItem> {
        return listOf(
            DashboardGuruActivity.JadwalItem(1, "Matematika", "Jam Pertama", "XI RPL 1", "07:30 - 08:15"),
            DashboardGuruActivity.JadwalItem(2, "Bahasa Indonesia", "Jam Kedua", "XI RPL 2", "08:15 - 09:00"),
            DashboardGuruActivity.JadwalItem(3, "Pemrograman Dasar", "Jam Ketiga", "XI RPL 3", "09:00 - 09:45"),
            DashboardGuruActivity.JadwalItem(4, "Basis Data", "Jam Keempat", "XI Mekatronika 1", "09:45 - 10:30"),
            DashboardGuruActivity.JadwalItem(5, "Fisika", "Jam Kelima", "XI Mekatronika 2", "10:30 - 11:15")
        )
    }

    private fun generateDummyRiwayat(): List<RiwayatAbsenItem> {
        return listOf(
            RiwayatAbsenItem(1, "Fahmi", "XI RPL 1", "26-01-2026", "07:30", "hadir"),
            RiwayatAbsenItem(2, "Rizky", "XI RPL 1", "26-01-2026", "07:45", "sakit"),
            RiwayatAbsenItem(3, "Siti", "XI RPL 1", "26-01-2026", "08:00", "izin"),
            RiwayatAbsenItem(4, "Ahmad", "XI RPL 2", "26-01-2026", "07:35", "hadir"),
            RiwayatAbsenItem(5, "Dewi", "XI RPL 2", "26-01-2026", "07:50", "alpha")
        )
    }

    private fun navigateToDetailJadwalWakel(jadwal: DashboardGuruActivity.JadwalItem) {
        try {
            val intent = Intent(this, DetailJadwalWakelActivity::class.java).apply {
                putExtra("JADWAL_DATA", DashboardGuruActivity.JadwalData(
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
                // Already on home, refresh data
                loadDashboardData()
                Toast.makeText(this, "Dashboard diperbarui", Toast.LENGTH_SHORT).show()
            }

            btnCalendar.setOnClickListener {
                navigateToActivity(RiwayatKehadiranKelasActivity::class.java)
            }

            btnChart.setOnClickListener {
                navigateToActivity(TindakLanjutWaliKelasActivity::class.java)
            }

            btnNotif.setOnClickListener {
                navigateToActivity(NotifikasiWaliKelasActivity::class.java)
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

    private fun setupKehadiranButtons() {
        try {
            // Button kehadiran (bukan bagian dari footer)
            val buttonHadir: ImageButton = findViewById(R.id.button_hadir)
            val buttonSakit: ImageButton = findViewById(R.id.button_sakit)
            val jumlahSiswaWakel: ImageButton = findViewById(R.id.jumlah_siswa_wakel)

            buttonHadir.setOnClickListener {
                Toast.makeText(this, "Menampilkan siswa hadir: ${txtHadirCount.text} siswa", Toast.LENGTH_SHORT).show()
            }

            buttonSakit.setOnClickListener {
                Toast.makeText(this, "Menampilkan detail kehadiran", Toast.LENGTH_SHORT).show()
            }

            jumlahSiswaWakel.setOnClickListener {
                Toast.makeText(this, "Total siswa: ${txtNominalSiswa.text}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up kehadiran buttons", e)
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
        // Refresh data saat kembali ke activity
        loadDashboardData()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}