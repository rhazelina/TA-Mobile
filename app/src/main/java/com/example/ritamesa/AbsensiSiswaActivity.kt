package com.example.ritamesa

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.AbsensiAdapter.SiswaData

class AbsensiSiswaActivity : AppCompatActivity() {

    private lateinit var adapter: AbsensiAdapter
    private lateinit var rvListAbsen: RecyclerView
    private lateinit var tvNamaMapel: TextView
    private lateinit var tvKelas: TextView
    private lateinit var tvTanggalWaktu: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnSimpan: ImageButton
    private lateinit var btnBatal: ImageButton

    private var mapel: String = ""
    private var kelas: String = ""
    private var tanggal: String = ""
    private var jam: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.absen_kehadiran_siswa)

        initViews()
        getDataFromIntent()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews() {
        tvNamaMapel = findViewById(R.id.text_nama_mapel)
        tvKelas = findViewById(R.id.title_kelas)
        tvTanggalWaktu = findViewById(R.id.tanggal_waktu_mulai)
        btnBack = findViewById(R.id.btn_back)
        btnSimpan = findViewById(R.id.btn_simpan_kehadiran)
        btnBatal = findViewById(R.id.btn_batal_absensi)
        rvListAbsen = findViewById(R.id.rvListAbsen)
    }

    private fun getDataFromIntent() {
        mapel = intent.getStringExtra(CameraQRActivity.EXTRA_MAPEL) ?:
                intent.getStringExtra("MATA_PELAJARAN") ?:
                "Matematika"

        kelas = intent.getStringExtra(CameraQRActivity.EXTRA_KELAS) ?:
                intent.getStringExtra("KELAS") ?:
                "XI Mekatronika 2"

        tanggal = intent.getStringExtra("tanggal") ?:
                intent.getStringExtra("TANGGAL") ?:
                getCurrentDate()

        jam = intent.getStringExtra("jam") ?:
                intent.getStringExtra("JAM") ?:
                "00:00-00:00"

        tvNamaMapel.text = mapel
        tvKelas.text = kelas
        tvTanggalWaktu.text = "$jam $tanggal"
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun setupRecyclerView() {
        val siswaList = generateDummySiswaData()

        adapter = AbsensiAdapter(siswaList)
        rvListAbsen.layoutManager = LinearLayoutManager(this)
        rvListAbsen.adapter = adapter
    }

    private fun generateDummySiswaData(): List<SiswaData> {
        val siswaList = mutableListOf<SiswaData>()

        val names = listOf(
            "Ahmad Fauzi", "Budi Santoso", "Citra Dewi", "Dian Pratama",
            "Eko Prasetyo", "Fitriani", "Gunawan", "Hendra Wijaya",
            "Indah Permata", "Joko Susilo", "Kartika Sari", "Lukman Hakim",
            "Maya Indah", "Nurhayati", "Oktaviani", "Puji Astuti",
            "Rahmat Hidayat", "Siti Aisyah", "Teguh Wijaya", "Umar Said",
            "Vina Melati", "Wahyu Ramadhan", "Yuniarti", "Zainal Abidin",
            "Agus Supriyadi", "Bayu Anggara", "Cindy Novita", "Dedi Setiawan",
            "Eka Putri", "Fajar Nugroho", "Galih Pratama", "Hesti Wulandari"
        )

        val nisnList = listOf(
            "0096785678", "0096785679", "0096785680", "0096785681",
            "0096785682", "0096785683", "0096785684", "0096785685",
            "0096785686", "0096785687", "0096785688", "0096785689",
            "0096785690", "0096785691", "0096785692", "0096785693",
            "0096785694", "0096785695", "0096785696", "0096785697",
            "0096785698", "0096785699", "0096785700", "0096785701",
            "0096785702", "0096785703", "0096785704", "0096785705",
            "0096785706", "0096785707", "0096785708", "0096785709"
        )

        for (i in names.indices) {
            siswaList.add(
                SiswaData(
                    id = i + 1,
                    nomor = i + 1,
                    nisn = nisnList[i % nisnList.size],
                    nama = names[i],
                    status = if (i % 5 == 0) "hadir" else "none"
                )
            )
        }

        return siswaList
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            simpanAbsensi()
        }

        btnBatal.setOnClickListener {
            batalAbsensi()
        }
    }

    private fun simpanAbsensi() {
        val absensiData = adapter.getAbsensiData()

        var totalHadir = 0
        var totalAlpha = 0

        absensiData.forEach { siswa ->
            when (siswa.status) {
                "hadir" -> totalHadir++
                "alpha" -> totalAlpha++
            }
        }

        val message = """
            Absensi berhasil disimpan!
            
            Mata Pelajaran: $mapel
            Kelas: $kelas
            Tanggal: $tanggal
            Jam: $jam
            
            Ringkasan:
            - Total Siswa: ${absensiData.size}
            - Hadir: $totalHadir
            - Alpha: $totalAlpha
            - Belum dipilih: ${absensiData.size - totalHadir - totalAlpha}
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        finish()
    }

    private fun batalAbsensi() {
        adapter.resetAllStatus()
        Toast.makeText(this, "Absensi dibatalkan, status direset", Toast.LENGTH_SHORT).show()
    }
}