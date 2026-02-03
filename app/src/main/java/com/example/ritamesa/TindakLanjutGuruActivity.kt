package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TindakLanjutGuruActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearchKelas: EditText
    private lateinit var adapter: SiswaTindakLanjutAdapter

    // Tombol navigasi footer
    private lateinit var btnHome: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnChart: ImageButton
    private lateinit var btnNotif: ImageButton

    // Data dummy semua siswa
    private val allSiswaData = mutableListOf<Map<String, Any>>()
    private val filteredSiswaData = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.tindak_lanjut_guru)

            initViews()
            setupFooterNavigation()
            setupRecyclerView()
            generateDummyData()
            setupSearchFilter()

            Toast.makeText(this, "Tindak Lanjut Guru dimuat", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvSiswaAbsensi)
        etSearchKelas = findViewById(R.id.etSearchKelas)

        // Inisialisasi tombol navigasi footer
        btnHome = findViewById(R.id.btnHome)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnChart = findViewById(R.id.btnChart)
        btnNotif = findViewById(R.id.btnNotif)
    }

    private fun setupFooterNavigation() {
        // Navigasi untuk GURU - Activity Tindak Lanjut

        btnHome.setOnClickListener {
            val intent = Intent(this, DashboardGuruActivity::class.java)
            startActivity(intent)
        }

        btnCalendar.setOnClickListener {
            val intent = Intent(this, RiwayatKehadiranGuruActivity::class.java)
            startActivity(intent)
        }

        btnChart.setOnClickListener {
            // Sudah di halaman Tindak Lanjut Guru, refresh saja
            refreshData()
            Toast.makeText(this, "Halaman Tindak Lanjut Guru", Toast.LENGTH_SHORT).show()
        }

        btnNotif.setOnClickListener {
            val intent = Intent(this, NotifikasiGuruActivity::class.java)
            startActivity(intent)
        }
    }

    private fun refreshData() {
        // Refresh data dengan generate ulang
        generateDummyData()
        Toast.makeText(this, "Data Tindak Lanjut direfresh", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        adapter = SiswaTindakLanjutAdapter(filteredSiswaData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchFilter() {
        etSearchKelas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }
        })
    }

    private fun filterData(query: String) {
        filteredSiswaData.clear()

        if (query.isEmpty()) {
            // Tampilkan hanya siswa yang bermasalah (tidak aman)
            filteredSiswaData.addAll(allSiswaData.filter { it["showBadge"] as Boolean })
        } else {
            val lowerQuery = query.lowercase()
            // Filter berdasarkan query dan hanya yang bermasalah
            filteredSiswaData.addAll(allSiswaData.filter {
                val showBadge = it.getOrDefault("showBadge", true) as? Boolean ?: true
                if (!showBadge) return@filter false // Lewati yang aman

                val nama = it.getOrDefault("nama", "") as String
                val kelasJurusan = it.getOrDefault("kelasJurusan", "") as String
                nama.lowercase().contains(lowerQuery) ||
                        kelasJurusan.lowercase().contains(lowerQuery)
            })
        }

        adapter.notifyDataSetChanged()
    }

    private fun generateDummyData() {
        allSiswaData.clear()

        // Data nama siswa
        val namaSiswaList = listOf(
            "Ahmad Fauzi", "Budi Santoso", "Cindy Permata", "Dedi Setiawan",
            "Eka Wulandari", "Fajar Nugroho", "Gita Maharani", "Hendra Pratama",
            "Indah Sari", "Joko Widodo", "Kartika Dewi", "Lukman Hakim",
            "Maya Puspita", "Nurhayati", "Oki Setiawan", "Putri Ayu",
            "Rizki Ramadhan", "Sari Dewi", "Toni Gunawan", "Umi Kulsum",
            "Vina Amelia", "Wahyu Kurniawan", "Xavier Tan", "Yuni Astuti",
            "Zainal Arifin", "Andi Wijaya", "Bunga Melati", "Cahyo Purnomo",
            "Dina Wulandari", "Eko Susanto"
        )

        // Data kelas/jurusan SMK
        val kelasJurusanList = listOf(
            "XII RPL 1", "XII RPL 2", "XII TKJ 1", "XII TKJ 2",
            "XII DKV 1", "XII DKV 2", "XII Mekatronika 1", "XII Mekatronika 2",
            "XII Animasi 1", "XII Animasi 2", "XII EI 1", "XII EI 2"
        )

        // KOMBINASI untuk testing - HANYA SISWA YANG BERMASALAH
        // Siswa yang TIDAK DITAMPILKAN (aman): (0, 0, 0) dan (0, 0, 1)
        val kondisiSiswa = listOf(
            // 1. Hanya alpha saja (2 alpha) - HARUS DITAMPILKAN
            Triple(2, 0, 0),
            // 2. Hanya izin > 5 (7 izin) - HARUS DITAMPILKAN
            Triple(0, 7, 0),
            // 3. Izin 3x + sakit 2x - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 3, 2),
            // 4. Hanya sakit saja (1 sakit) - TIDAK DITAMPILKAN (aman)
            Triple(0, 0, 1),
            // 5. Aman semua (0, 0, 0) - TIDAK DITAMPILKAN
            Triple(0, 0, 0),
            // 6. Hanya izin 2x - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 2, 0),
            // 7. Alpha 1x + izin 1x - HARUS DITAMPILKAN
            Triple(1, 1, 0),
            // 8. Izin 1x + sakit 1x - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 1, 1),
            // 9. Semua ada masing-masing 1x - HARUS DITAMPILKAN (karena ada alpha)
            Triple(1, 1, 1),
            // 10. Alpha banyak (3x) - HARUS DITAMPILKAN
            Triple(3, 0, 0),
            // 11. Izin banyak (10x) - HARUS DITAMPILKAN
            Triple(0, 10, 0),
            // 12. Sakit banyak (3x) - TIDAK DITAMPILKAN (masih dianggap aman)
            Triple(0, 0, 3),
            // 13. Alpha 1x + sakit 2x - HARUS DITAMPILKAN
            Triple(1, 0, 2),
            // 14. Izin sedang (4x) - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 4, 0),
            // 15. Kombinasi lengkap - HARUS DITAMPILKAN (karena ada alpha)
            Triple(2, 2, 1),
            // 16. Aman (0, 0, 0) - TIDAK DITAMPILKAN
            Triple(0, 0, 0),
            // 17. Izin borderline (6x) - HARUS DITAMPILKAN
            Triple(0, 6, 0),
            // 18. Aman (0, 0, 0) - TIDAK DITAMPILKAN
            Triple(0, 0, 0),
            // 19. Alpha 1x + izin 5x - HARUS DITAMPILKAN
            Triple(1, 5, 0),
            // 20. Hanya sakit 2x - TIDAK DITAMPILKAN
            Triple(0, 0, 2),
            // 21. Hanya izin 1x - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 1, 0),
            // 22. Alpha 4x - HARUS DITAMPILKAN
            Triple(4, 0, 0),
            // 23. Izin 8x - HARUS DITAMPILKAN
            Triple(0, 8, 0),
            // 24. Aman (0, 0, 0) - TIDAK DITAMPILKAN
            Triple(0, 0, 0),
            // 25. Alpha 2x + izin 2x - HARUS DITAMPILKAN
            Triple(2, 2, 0),
            // 26. Hanya sakit 1x - TIDAK DITAMPILKAN
            Triple(0, 0, 1),
            // 27. Izin 3x - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 3, 0),
            // 28. Alpha 1x - HARUS DITAMPILKAN
            Triple(1, 0, 0),
            // 29. Izin 1x + sakit 3x - HARUS DITAMPILKAN (karena izin ≤ 5, jadi Aman)
            Triple(0, 1, 3),
            // 30. Aman (0, 0, 0) - TIDAK DITAMPILKAN
            Triple(0, 0, 0)
        )

        // Generate data dummy untuk 30 siswa
        for (i in 0 until 30) {
            val nama = namaSiswaList.getOrNull(i) ?: "Siswa ${i+1}"
            val kelasJurusan = kelasJurusanList[i % kelasJurusanList.size]
            val (alphaCount, izinCount, sakitCount) = kondisiSiswa[i]

            // Tentukan badge info berdasarkan kondisi
            val badgeInfo = determineBadgeInfo(alphaCount, izinCount, sakitCount)

            allSiswaData.add(mapOf(
                "id" to i + 1,
                "nama" to nama,
                "kelasJurusan" to kelasJurusan,
                "alphaCount" to alphaCount,
                "izinCount" to izinCount,
                "sakitCount" to sakitCount,

                // Badge kanan (status)
                "badgeDrawable" to badgeInfo["drawable"] as Int,
                "badgeText" to badgeInfo["text"] as String,
                "showBadge" to badgeInfo["show"] as Boolean,

                // Untuk sorting
                "severityScore" to badgeInfo["severityScore"] as Int
            ))
        }

        // Urutkan berdasarkan severity score (yang bermasalah di atas)
        allSiswaData.sortByDescending { it["severityScore"] as Int }

        filteredSiswaData.clear()
        // HANYA tampilkan siswa yang bermasalah (showBadge = true)
        filteredSiswaData.addAll(allSiswaData.filter { it["showBadge"] as Boolean })
        adapter.notifyDataSetChanged()

        // Tampilkan jumlah siswa yang ditindak lanjuti
        Toast.makeText(this, "Ditemukan ${filteredSiswaData.size} siswa perlu ditindak lanjuti", Toast.LENGTH_SHORT).show()
    }

    private fun determineBadgeInfo(alpha: Int, izin: Int, sakit: Int): Map<String, Any> {
        val totalCount = alpha + izin + sakit

        return when {
            // Kondisi 1: Ada alpha (minimal 1) → Sering Absensi (MERAH/DANGER)
            // Harus ditampilkan meskipun cuma 1 alpha
            alpha >= 1 -> mapOf(
                "drawable" to R.drawable.box_danger,
                "text" to "Sering Absensi",
                "show" to true,
                "severityScore" to (alpha * 100 + izin * 10 + sakit) // Alpha bernilai tinggi
            )

            // Kondisi 2: Izin > 5 kali → Perlu Diperhatikan (KUNING/WARNING)
            // Harus ditampilkan jika izin > 5
            izin > 5 -> mapOf(
                "drawable" to R.drawable.box_warning,
                "text" to "Perlu Diperhatikan",
                "show" to true,
                "severityScore" to (alpha * 100 + izin * 10 + sakit)
            )

            // Kondisi 3: Izin ≤ 5, tidak ada alpha, tapi ada absensi → Aman (HIJAU/SUCCESS)
            // TIDAK DITAMPILKAN karena tidak bermasalah
            totalCount > 0 -> mapOf(
                "drawable" to R.drawable.box_success,
                "text" to "Aman",
                "show" to false, // TIDAK DITAMPILKAN di list tindak lanjut
                "severityScore" to (alpha * 100 + izin * 10 + sakit)
            )

            // Kondisi 4: Tidak ada absensi sama sekali → Aman (HIJAU/SUCCESS)
            // TIDAK DITAMPILKAN karena tidak bermasalah
            else -> mapOf(
                "drawable" to R.drawable.box_success,
                "text" to "Aman",
                "show" to false, // TIDAK DITAMPILKAN di list tindak lanjut
                "severityScore" to 0
            )
        }
    }

    private fun buildBadgeText(alpha: Int, izin: Int, sakit: Int): String {
        // Fungsi ini tidak digunakan lagi, tapi tetap dipertahankan untuk compatibility
        return when {
            alpha > 0 -> "Sering Absensi"
            izin > 5 -> "Perlu Diperhatikan"
            else -> "Aman"
        }
    }
}