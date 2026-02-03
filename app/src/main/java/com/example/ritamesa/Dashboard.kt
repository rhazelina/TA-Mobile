package com.example.ritamesa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class Dashboard : AppCompatActivity() {

    private lateinit var tvTanggal: TextView
    private lateinit var tvJam: TextView
    private lateinit var tvTotalSiswa: TextView
    private lateinit var tvTotalGuru: TextView
    private lateinit var tvTotalJurusan: TextView
    private lateinit var tvTotalKelas: TextView
    private lateinit var tvHadir: TextView
    private lateinit var tvTerlambat: TextView
    private lateinit var tvIzin: TextView
    private lateinit var tvSakit: TextView
    private lateinit var tvAlpha: TextView
    private lateinit var tvPulang: TextView
    private lateinit var barChart: BarChart
    private lateinit var tvDateCard: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = true
    private var lastUpdateTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // ===== INISIALISASI VIEW =====
        tvTanggal = findViewById(R.id.textView9)   // tanggal header
        tvJam = findViewById(R.id.textView21)      // jam
        tvTotalSiswa = findViewById(R.id.textView12)
        tvTotalGuru = findViewById(R.id.textView16)
        tvTotalJurusan = findViewById(R.id.textView17)
        tvTotalKelas = findViewById(R.id.textView18)
        tvDateCard = findViewById(R.id.textView20) // tanggal di card riwayat

        // Inisialisasi statistik kehadiran
        tvHadir = findViewById(R.id.tvTepatWaktuDashboard)
        tvTerlambat = findViewById(R.id.tvTerlambatDashboard)
        tvIzin = findViewById(R.id.tvIzinDashboard)
        tvSakit = findViewById(R.id.tvSakitDashboard)
        tvAlpha = findViewById(R.id.tvAlphaDashboard)
        tvPulang = findViewById(R.id.tvPulang) // TextView untuk pulang

        barChart = findViewById(R.id.barChartBulanan)

        updateTanggalJam()
        updateDataStatistik()

        // ===== NAVIGASI =====
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            // HOME - sudah di dashboard, tidak perlu navigasi
        }

        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            startActivity(Intent(this, RekapKehadiranSiswa::class.java))
        }

        findViewById<ImageButton>(R.id.imageButton4).setOnClickListener {
            startActivity(Intent(this, StatistikKehadiran::class.java))
        }

        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            startActivity(Intent(this, NotifikasiSemua::class.java))
        }

        findViewById<ImageButton>(R.id.imageButton87).setOnClickListener {
            startActivity(Intent(this, RiwayatKehadiranSiswa::class.java))
        }

        findViewById<ImageButton>(R.id.imageView16).setOnClickListener {
            startActivity(Intent(this, TotalGuru::class.java))
        }

        findViewById<ImageButton>(R.id.imageView17).setOnClickListener {
            startActivity(Intent(this, TotalJurusan::class.java))
        }

        findViewById<ImageButton>(R.id.imageView18).setOnClickListener {
            startActivity(Intent(this, TotalKelas::class.java))
        }

        findViewById<ImageButton>(R.id.imageView6).setOnClickListener {
            startActivity(Intent(this, TotalSiswa::class.java))
        }
    }

    // ===== UPDATE DATA STATISTIK DINAMIS =====
    private fun updateDataStatistik() {
        // Data statistik
        val totalSiswa = Random.nextInt(80, 120)
        val totalGuru = Random.nextInt(10, 20)
        val totalJurusan = 5
        val totalKelas = Random.nextInt(8, 15)

        // Data kehadiran hari ini - sesuai dengan 5 kategori dari gambar
        val hadir = Random.nextInt(60, totalSiswa - 20)
        val izin = Random.nextInt(3, 10)
        val pulang = (hadir * 0.9).toInt() // 90% dari yang hadir
        val tidakHadir = Random.nextInt(5, 15)
        val sakit = Random.nextInt(2, 8)

        // Terlambat tidak ada dalam gambar, jadi kita masukkan ke kategori lain
        // atau bisa juga dianggap sebagai bagian dari "Tidak Hadir"

        // Update TextView
        tvTotalSiswa.text = totalSiswa.toString()
        tvTotalGuru.text = totalGuru.toString()
        tvTotalJurusan.text = totalJurusan.toString()
        tvTotalKelas.text = totalKelas.toString()

        // Update statistik kehadiran hari ini - sesuai dengan 5 kategori
        tvHadir.text = hadir.toString()
        tvIzin.text = izin.toString()
        tvPulang.text = pulang.toString()
        tvAlpha.text = tidakHadir.toString() // Alpha = Tidak Hadir
        tvSakit.text = sakit.toString()

        // Untuk terlambat, kita set 0 karena tidak ada di gambar
        tvTerlambat.text = "0"

        // Update chart dengan data baru
        setupChartHariIni()
    }

    // ===== SETUP CHART DATA HARI INI =====
    private fun setupChartHariIni() {
        // Ambil data dari statistik yang sudah ada
        val hadir = tvHadir.text.toString().toIntOrNull() ?: 0
        val izin = tvIzin.text.toString().toIntOrNull() ?: 0
        val pulang = tvPulang.text.toString().toIntOrNull() ?: 0
        val tidakHadir = tvAlpha.text.toString().toIntOrNull() ?: 0
        val sakit = tvSakit.text.toString().toIntOrNull() ?: 0

        // Labels untuk 5 bar sesuai gambar: Hadir, Izin, Pulang, Tidak Hadir, Sakit
        val labels = arrayOf("Hadir", "Izin", "Pulang", "Tidak Hadir", "Sakit")

        // Data untuk 5 bar
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0f, hadir.toFloat()))       // Bar 0: Hadir
        entries.add(BarEntry(1f, izin.toFloat()))        // Bar 1: Izin
        entries.add(BarEntry(2f, pulang.toFloat()))      // Bar 2: Pulang
        entries.add(BarEntry(3f, tidakHadir.toFloat()))  // Bar 3: Tidak Hadir
        entries.add(BarEntry(4f, sakit.toFloat()))       // Bar 4: Sakit

        // Warna sesuai gambar:
        // 1. Hadir - Hijau
        // 2. Izin - Biru
        // 3. Pulang - Cyan
        // 4. Tidak Hadir - Merah
        // 5. Sakit - Ungu
        val warnaStatus = listOf(
            Color.parseColor("#4CAF50"),  // Hijau - Hadir
            Color.parseColor("#FFEB3B"),  // Kuning - Izin
            Color.parseColor("#1976D2"),  // Biru - Pulang
            Color.parseColor("#F44336"),  // Merah - Tidak Hadir
            Color.parseColor("#FF9800")   // Oranye - Sakit
        )


        // Setup dataset dengan 1 set data saja
        val dataSet = BarDataSet(entries, "Kehadiran Hari Ini").apply {
            colors = warnaStatus
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        // Setup BarData
        val barData = BarData(dataSet).apply {
            barWidth = 0.5f  // Lebar bar disesuaikan untuk 5 bar
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
        }

        // Setup chart
        barChart.apply {
            data = barData
            description.isEnabled = false // Sembunyikan deskripsi default

            // X Axis - Label status
            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelCount = labels.size
                textSize = 10f
                axisMinimum = -0.5f  // Margin kiri
                axisMaximum = labels.size - 0.5f // Margin kanan
            }

            // Y Axis - Jumlah
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 10f
                setDrawGridLines(true)
                textSize = 10f
                val maxValue = maxOf(hadir, izin, pulang, tidakHadir, sakit)
                axisMaximum = (maxValue + 10).toFloat()
            }

            axisRight.isEnabled = false

            // Legend - Update legend untuk 5 kategori
            legend.isEnabled = true
            legend.textSize = 10f
            legend.formSize = 10f

            // Interaksi
            setTouchEnabled(true)
            setPinchZoom(false)  // Nonaktifkan pinch zoom
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)  // Tampilkan nilai di atas bar

            // Nonaktifkan animasi yang tidak perlu
            setDragEnabled(false)
            setScaleEnabled(false)
            setDoubleTapToZoomEnabled(false)

            // Animate
            animateY(1000)

            // Refresh chart
            invalidate()
        }
    }

    // ===== UPDATE JAM & TANGGAL REAL TIME =====
    private fun updateTanggalJam() {
        isRunning = true
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return

                val tanggalFormat = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale("id", "ID")
                )
                val jamFormat = SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                )

                val now = Date()
                val tanggal = tanggalFormat.format(now)
                val jam = jamFormat.format(now)

                tvTanggal.text = tanggal
                tvJam.text = jam
                tvDateCard.text = tanggal  // Update tanggal di card juga

                // Update data statistik setiap 30 detik (hanya jika belum di-update dalam 30 detik terakhir)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= 30000) {
                    updateDataStatistik()
                    lastUpdateTime = currentTime
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke dashboard
        updateDataStatistik()
    }
}