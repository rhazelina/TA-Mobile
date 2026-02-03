package com.example.ritamesa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class JadwalHarianSiswaActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var txtTanggal: TextView
    private lateinit var recyclerView: RecyclerView

    // PERHATIAN: Di jadwal harian, ini adalah ImageView (bukan ImageButton)
    private lateinit var iconCalendar: ImageView

    private var isPengurus = false
    private var selectedDate = Calendar.getInstance()

    companion object {
        private const val TAG = "JadwalHarian"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== JADWAL HARIAN ACTIVITY START ===")

        try {
            isPengurus = intent.getBooleanExtra("IS_PENGURUS", false)
            Log.d(TAG, "isPengurus = $isPengurus")

            setContentView(R.layout.jadwal_harian_siswa)
            Log.d(TAG, "Layout loaded")

            initViews()
            setupCalendarButton()
            setupBackPressedHandler()

            Toast.makeText(this, "Jadwal Harian dimuat", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "ERROR: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        try {
            btnBack = findViewById(R.id.btn_back)
            txtTanggal = findViewById(R.id.TextTanggalTerkini)
            recyclerView = findViewById(R.id.recycler_jadwal_now)

            // PERHATIAN: Di jadwal harian, id nya adalah icon_calendar (ImageView)
            iconCalendar = findViewById(R.id.icon_calendar)

            // SET TANGGAL SAAT INI
            updateTanggalDisplay()

            // DATA JADWAL - SIMPEL seperti awal, tapi dengan "Mata Pelajaran X"
            val data = generateJadwalData()

            // SETUP RECYCLERVIEW
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = JadwalHarianAdapter(data, isPengurus)

            // TOMBOL BACK
            btnBack.setOnClickListener {
                Log.d(TAG, ">>> TOMBOL BACK DIKLIK! <<<")
                navigateToRiwayatKehadiran()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in initViews: ${e.message}", e)
            Toast.makeText(this, "Error finding views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateJadwalData(): List<JadwalHarianItem> {
        // Data mata pelajaran tetap simpel
        val mataPelajaranList = listOf(
            "Bahasa Indonesia",
            "Matematika",
            "IPA",
            "Bahasa Inggris",
            "PKN",
            "Seni Budaya",
            "PJOK",
            "Informatika"
        )

        val jadwalList = mutableListOf<JadwalHarianItem>()

        // Generate data dengan format "Mata Pelajaran 1, 2, 3..."
        for (i in mataPelajaranList.indices) {
            val nomor = i + 1
            jadwalList.add(
                JadwalHarianItem(
                    mataPelajaran = mataPelajaranList[i],
                    sesi = "Mata Pelajaran $nomor"  // <-- INI YANG DIUBAH
                )
            )
        }

        return jadwalList
    }

    private fun setupCalendarButton() {
        // ImageView juga bisa diklik
        iconCalendar.setOnClickListener {
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
                Toast.makeText(this, "Menampilkan jadwal untuk: ${getFormattedDate()}", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun getFormattedDate(): String {
        return try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formatted = sdf.format(selectedDate.time)
            if (formatted.isNotEmpty()) {
                formatted[0].uppercaseChar() + formatted.substring(1)
            } else {
                formatted
            }
        } catch (e: Exception) {
            "Kamis, 23 Januari 2025"
        }
    }

    private fun updateTanggalDisplay() {
        try {
            txtTanggal.text = getFormattedDate()
        } catch (e: Exception) {
            txtTanggal.text = "Kamis, 23 Januari 2025"
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, ">>> TOMBOL BACK GESTURE DIKLIK! <<<")
                navigateToRiwayatKehadiran()
            }
        })
    }

    private fun navigateToRiwayatKehadiran() {
        try {
            // Berdasarkan role pengguna (siswa atau pengurus), kembali ke halaman yang sesuai
            if (isPengurus) {
                val intent = Intent(this, RiwayatKehadiranKelasPengurusActivity::class.java).apply {
                    putExtra("IS_PENGURUS", true)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasPengurusActivity BERHASIL")
            } else {
                val intent = Intent(this, RiwayatKehadiranKelasSiswaActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                Log.d(TAG, "Navigasi ke RiwayatKehadiranKelasSiswaActivity BERHASIL")
            }
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "ERROR navigating to Riwayat: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            super.onBackPressedDispatcher.onBackPressed()
        }
    }
}