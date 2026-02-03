package com.example.ritamesa

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiSemua : AppCompatActivity() {

    // ===== DATA CLASS =====
    data class NotifikasiItem(
        val id: Int,
        val nama: String,
        val pesan: String,
        val detail: String,
        val waktu: String,
        val tipe: String, // tepat_waktu, terlambat, alpha, sakit, rapor
        val kategori: String, // guru, siswa
        val jamKe: Int = 0, // 0 = semua jam, 1-10 = jam ke tertentu
        val kelasJurusan: String = "" // Tambahkan field kelas/jurusan
    )

    // ===== ADAPTER KHUSUS ADMIN (UBAH NAMA) =====
    inner class NotifikasiAdminAdapter(
        private val listNotifikasi: List<NotifikasiItem>
    ) : RecyclerView.Adapter<NotifikasiAdminAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textAlert: TextView = view.findViewById(R.id.text_alert)
            val jamNotifikasi: TextView = view.findViewById(R.id.jam_notifikasi)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notif_semua, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notif = listNotifikasi[position]

            holder.textAlert.text = "${notif.nama}: ${notif.pesan}"
            holder.jamNotifikasi.text = "${notif.waktu} (Jam ke-${notif.jamKe})"

            holder.itemView.setOnClickListener {
                // Panggil fungsi untuk menampilkan popup
                showDetailPopup(notif)
            }
        }

        override fun getItemCount(): Int = listNotifikasi.size
    }

    // ===== COMPONENTS =====
    private lateinit var rvHariIni: RecyclerView
    private lateinit var rvKemarin: RecyclerView
    private lateinit var rvBulanLalu: RecyclerView
    private lateinit var layoutSemua: LinearLayout
    private lateinit var layoutJamKe: LinearLayout
    private lateinit var tvPilihKelas: TextView
    private lateinit var tvJamKe: TextView
    private lateinit var btnDropdownJamKe: ImageButton
    private lateinit var tvHariIni: TextView
    private lateinit var tvKemarin: TextView
    private lateinit var tvBulanLalu: TextView

    // ===== STATE =====
    private var filterKategori = "Semua"
    private var filterJamKe: Int = 0 // 0 = semua, 1-10 = jam ke tertentu
    private var filterKelasJurusan = "Semua Kelas"
    private var isFilterSemuaActive = true

    // ===== DUMMY DATA DENGAN KELAS/JURUSAN =====
    private val semuaNotifikasi = listOf(
        NotifikasiItem(1, "Pak Ahmad", "Hadir tepat waktu", "Matematika", "07:30", "tepat_waktu", "guru", 1, "XII RPL 1"),
        NotifikasiItem(2, "Bu Siti", "Terlambat 15 menit", "Bahasa Inggris", "09:15", "terlambat", "guru", 2, "XII TKJ 2"),
        NotifikasiItem(3, "XII Mekatronika 1", "3 siswa alpha", "Fisika", "11:00", "alpha", "siswa", 3, "XII Mekatronika 1"),
        NotifikasiItem(4, "Andi Wijaya", "Sakit dengan surat dokter", "XII RPL 2", "08:45", "sakit", "siswa", 2, "XII RPL 2"),
        NotifikasiItem(5, "Pak Budi", "Hadir tepat waktu", "Kimia", "07:30", "tepat_waktu", "guru", 1, "XII IPA 1"),
        NotifikasiItem(6, "Citra Dewi", "Izin keluarga", "XII IPS 2", "10:30", "alpha", "siswa", 4, "XII IPS 2"),
        NotifikasiItem(7, "Dewi Lestari", "Sakit panas", "XII RPL 3", "10:45", "sakit", "siswa", 4, "XII RPL 3"),
        NotifikasiItem(8, "Pak Eko", "Hadir tepat waktu", "Pemrograman", "07:30", "tepat_waktu", "guru", 1, "XII RPL 2"),
        NotifikasiItem(9, "Fajar Ramadan", "Terlambat 20 menit", "XII TKJ 1", "08:50", "terlambat", "siswa", 2, "XII TKJ 1"),
        NotifikasiItem(10, "Sistem", "Rapor kehadiran tersedia", "Bulan Desember 2024", "16:30", "rapor", "guru", 7, "Semua Kelas"),
        NotifikasiItem(11, "Pak Farhan", "Hadir tepat waktu", "Matematika", "07:30", "tepat_waktu", "guru", 1, "XI RPL 1"),
        NotifikasiItem(12, "XI RPL 2", "2 siswa terlambat", "Bahasa Indonesia", "08:15", "terlambat", "siswa", 2, "XI RPL 2"),
        NotifikasiItem(13, "Bu Gita", "Sakit", "Kimia", "09:45", "sakit", "guru", 3, "XII IPA 2"),
        NotifikasiItem(14, "X RPL 1", "1 siswa alpha", "Matematika", "10:30", "alpha", "siswa", 4, "X RPL 1"),
        NotifikasiItem(15, "Pak Hasan", "Hadir tepat waktu", "Pemrograman", "07:30", "tepat_waktu", "guru", 1, "X RPL 3")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifikasi_semua)

        initViews()
        setupRecyclerViews()
        setupFilterActions()
        setupFooterNavigation()
        setupDateFilter()
        setupJenisFilter()
        filterData()
    }

    private fun initViews() {
        // RecyclerViews
        rvHariIni = findViewById(R.id.rvNotifHariIni)
        rvKemarin = findViewById(R.id.rvNotifKemarin)
        rvBulanLalu = findViewById(R.id.rvNotifBulanLalu)

        // Filter components
        layoutSemua = findViewById(R.id.icon_notif_belum)
        layoutJamKe = findViewById(R.id.icon_notif_semua)
        tvPilihKelas = findViewById(R.id.tvPilihKelas)
        tvJamKe = findViewById(R.id.tvJamKe)
        btnDropdownJamKe = findViewById(R.id.btnDropdownJamKe)

        // Section titles
        tvHariIni = findViewById(R.id.tvHariIni)
        tvKemarin = findViewById(R.id.tvKemarin)
        tvBulanLalu = findViewById(R.id.tvBulanLalu)
    }

    private fun setupRecyclerViews() {
        rvHariIni.layoutManager = LinearLayoutManager(this)
        rvKemarin.layoutManager = LinearLayoutManager(this)
        rvBulanLalu.layoutManager = LinearLayoutManager(this)

        // Set minimal height agar tidak crash
        rvHariIni.minimumHeight = 300
        rvKemarin.minimumHeight = 200
        rvBulanLalu.minimumHeight = 120
    }

    // ===== FUNGSI UNTUK MENAMPILKAN POPUP DETAIL =====
    private fun showDetailPopup(notif: NotifikasiItem) {
        // Inflate layout popup
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_detail_notif_tepat_waktu, null)

        // Setup komponen popup
        val textViewNama = dialogView.findViewById<TextView>(R.id.textViewNama)
        val textViewTanggal = dialogView.findViewById<TextView>(R.id.textViewTanggal)
        val textViewDetail = dialogView.findViewById<TextView>(R.id.textViewDetail)
        val btnTutup = dialogView.findViewById<Button>(R.id.btnTutup)

        // Buat dialog dengan AlertDialog.Builder
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        val dialog = dialogBuilder.create()

        // Set dialog agar transparan dan full screen
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Set tanggal sesuai dengan data
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val today = Calendar.getInstance().time
        val formattedDate = dateFormat.format(today)

        // Atur data sesuai tipe notifikasi
        when (notif.tipe) {
            "tepat_waktu" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Tepat Waktu
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Mata Pelajaran: ${notif.detail}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "terlambat" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Terlambat
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Detail: ${notif.pesan}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "alpha" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Alpha/Tidak Hadir
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Detail: ${notif.pesan}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "sakit" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Sakit
                    Waktu: ${notif.waktu}
                    Role: ${if (notif.kategori == "guru") "Guru" else "Siswa"}
                    Kelas/Jurusan: ${notif.kelasJurusan}
                    Detail: ${notif.pesan}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
            "rapor" -> {
                textViewNama.text = notif.nama
                textViewTanggal.text = formattedDate
                textViewDetail.text = """
                    Status: Rapor Kehadiran
                    Waktu: ${notif.waktu}
                    Keterangan: Rapor Tersedia
                    Detail: ${notif.detail}
                    Jam ke: ${notif.jamKe}
                """.trimIndent()
            }
        }

        // Setup tombol tutup
        btnTutup.setOnClickListener {
            dialog.dismiss()
        }

        // Tampilkan dialog
        dialog.show()
    }

    private fun setupFilterActions() {
        // FILTER: SEMUA vs JAM KE
        layoutSemua.setOnClickListener {
            activateSemuaFilter()
        }

        layoutJamKe.setOnClickListener {
            activateJamKeFilter()
        }

        // Setup dropdown Jam Ke
        btnDropdownJamKe.setOnClickListener {
            showJamKePopupMenu()
        }

        tvJamKe.setOnClickListener {
            showJamKePopupMenu()
        }

        // Set awal: "Semua" aktif
        activateSemuaFilter()

        // FILTER: Pilih Kelas/Jurusan
        tvPilihKelas.setOnClickListener {
            showKelasJurusanDialog()
        }

        // Arrow kelas
        findViewById<ImageButton>(R.id.btnArrowKelas).setOnClickListener {
            showKelasJurusanDialog()
        }
    }

    private fun activateSemuaFilter() {
        isFilterSemuaActive = true
        layoutSemua.setBackgroundResource(R.drawable.rectangle_86)
        layoutJamKe.background = null
        tvJamKe.text = "Jam ke-"
        filterJamKe = 0
        updateSectionTitles()
        filterData()
    }

    private fun activateJamKeFilter() {
        isFilterSemuaActive = false
        layoutJamKe.setBackgroundResource(R.drawable.rectangle_86)
        layoutSemua.background = null

        // Jika belum memilih jam ke, tampilkan popup
        if (tvJamKe.text == "Jam ke-" || tvJamKe.text == "Jam ke-") {
            showJamKePopupMenu()
        } else {
            filterData()
        }
    }

    private fun showJamKePopupMenu() {
        val popupMenu = PopupMenu(this, btnDropdownJamKe)
        val menu = popupMenu.menu

        // Tambahkan pilihan Jam ke 1 sampai 10
        for (i in 1..10) {
            menu.add(0, i, 0, "Jam ke $i")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            val jamKe = item.itemId
            filterJamKe = jamKe
            tvJamKe.text = "Jam ke $jamKe"

            // Aktifkan filter Jam Ke
            activateJamKeFilter()

            true
        }

        popupMenu.show()
    }

    private fun setupDateFilter() {
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggal)

        // Set tanggal hari ini
        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        tvTanggal.text = dateFormat.format(currentDate.time)

        btnCalendar.setOnClickListener {
            showDatePickerDialog(tvTanggal)
        }

        tvTanggal.setOnClickListener {
            showDatePickerDialog(tvTanggal)
        }
    }

    private fun showDatePickerDialog(tvTanggal: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                tvTanggal.text = dateFormat.format(selectedDate.time)

                // Filter data berdasarkan tanggal
                filterData()
                Toast.makeText(this, "Filter tanggal: ${tvTanggal.text}", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun setupJenisFilter() {
        val btnArrowFilter = findViewById<ImageButton>(R.id.btnArrowFilter)
        val tvFilter = findViewById<TextView>(R.id.tvFilter)

        btnArrowFilter.setOnClickListener {
            showJenisPopupMenu(tvFilter)
        }

        tvFilter.setOnClickListener {
            showJenisPopupMenu(tvFilter)
        }
    }

    private fun showJenisPopupMenu(tvFilter: TextView) {
        val jenisList = arrayOf("Semua", "Guru", "Siswa", "Tepat Waktu", "Terlambat", "Alpha", "Sakit", "Rapor")

        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Notifikasi")
            .setItems(jenisList) { _, which ->
                val selected = jenisList[which]
                tvFilter.text = selected
                filterKategori = selected
                filterData()
                Toast.makeText(this, "Filter: $selected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupFooterNavigation() {
        try {
            // HOME
            findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }

            // CONTACTS
            findViewById<ImageButton>(R.id.btnContacts).setOnClickListener {
                startActivity(Intent(this, TotalSiswa::class.java))
                finish()
            }

            // CHART
            findViewById<ImageButton>(R.id.btnChart).setOnClickListener {
                startActivity(Intent(this, RiwayatKehadiranSiswa::class.java))
                finish()
            }

            // NOTIFICATIONS (sudah di halaman ini)
            findViewById<ImageButton>(R.id.btnNotif).setOnClickListener {
                // Refresh data
                filterData()
                Toast.makeText(this, "Notifikasi direfresh", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun filterData() {
        var filteredList = semuaNotifikasi

        // 1. Filter berdasarkan kategori/jenis
        filteredList = when (filterKategori) {
            "Guru" -> filteredList.filter { it.kategori == "guru" }
            "Siswa" -> filteredList.filter { it.kategori == "siswa" }
            "Tepat Waktu" -> filteredList.filter { it.tipe == "tepat_waktu" }
            "Terlambat" -> filteredList.filter { it.tipe == "terlambat" }
            "Alpha" -> filteredList.filter { it.tipe == "alpha" }
            "Sakit" -> filteredList.filter { it.tipe == "sakit" }
            "Rapor" -> filteredList.filter { it.tipe == "rapor" }
            else -> filteredList
        }

        // 2. Filter berdasarkan jam ke jika aktif
        if (!isFilterSemuaActive && filterJamKe > 0) {
            filteredList = filteredList.filter { it.jamKe == filterJamKe }
        }

        // 3. Filter berdasarkan kelas/jurusan (TAMBAHAN BARU)
        if (filterKelasJurusan != "Semua Kelas") {
            filteredList = filteredList.filter { it.kelasJurusan == filterKelasJurusan }
        }

        // Pisahkan data berdasarkan waktu (sederhana untuk contoh)
        val hariIniList = filteredList.take(5)
        val kemarinList = filteredList.drop(5).take(5)
        val bulanLaluList = filteredList.drop(10)

        // Update adapters (GUNAKAN NotifikasiAdminAdapter)
        rvHariIni.adapter = NotifikasiAdminAdapter(hariIniList)
        rvKemarin.adapter = NotifikasiAdminAdapter(kemarinList)
        rvBulanLalu.adapter = NotifikasiAdminAdapter(bulanLaluList)

        // Update visibility berdasarkan data
        updateSectionVisibility(hariIniList, kemarinList, bulanLaluList)

        // Update section titles dengan info filter
        updateSectionTitlesWithFilter()
    }

    private fun updateSectionVisibility(
        hariIniList: List<NotifikasiItem>,
        kemarinList: List<NotifikasiItem>,
        bulanLaluList: List<NotifikasiItem>
    ) {
        tvHariIni.visibility = if (hariIniList.isEmpty()) View.GONE else View.VISIBLE
        rvHariIni.visibility = if (hariIniList.isEmpty()) View.GONE else View.VISIBLE

        tvKemarin.visibility = if (kemarinList.isEmpty()) View.GONE else View.VISIBLE
        rvKemarin.visibility = if (kemarinList.isEmpty()) View.GONE else View.VISIBLE

        tvBulanLalu.visibility = if (bulanLaluList.isEmpty()) View.GONE else View.VISIBLE
        rvBulanLalu.visibility = if (bulanLaluList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateSectionTitles() {
        if (filterJamKe > 0 && !isFilterSemuaActive) {
            tvHariIni.text = "Jam ke $filterJamKe (Hari Ini)"
            tvKemarin.text = "Jam ke $filterJamKe (Kemarin)"
            tvBulanLalu.text = "Jam ke $filterJamKe (Bulan Lalu)"
        } else {
            tvHariIni.text = "Hari Ini"
            tvKemarin.text = "Kemarin"
            tvBulanLalu.text = "Bulan Lalu"
        }
    }

    private fun updateSectionTitlesWithFilter() {
        // Tambahkan info filter pada judul section
        var additionalInfo = ""

        if (filterKelasJurusan != "Semua Kelas") {
            additionalInfo = " - $filterKelasJurusan"
        }

        if (filterJamKe > 0 && !isFilterSemuaActive) {
            tvHariIni.text = "Jam ke $filterJamKe (Hari Ini)$additionalInfo"
            tvKemarin.text = "Jam ke $filterJamKe (Kemarin)$additionalInfo"
            tvBulanLalu.text = "Jam ke $filterJamKe (Bulan Lalu)$additionalInfo"
        } else {
            tvHariIni.text = "Hari Ini$additionalInfo"
            tvKemarin.text = "Kemarin$additionalInfo"
            tvBulanLalu.text = "Bulan Lalu$additionalInfo"
        }

        // Jika filter kategori bukan "Semua", tambahkan juga
        if (filterKategori != "Semua") {
            tvHariIni.text = "${tvHariIni.text} - $filterKategori"
            tvKemarin.text = "${tvKemarin.text} - $filterKategori"
            tvBulanLalu.text = "${tvBulanLalu.text} - $filterKategori"
        }
    }

    private fun showKelasJurusanDialog() {
        val kelasJurusan = arrayOf(
            "Semua Kelas",
            "X RPL 1", "X RPL 2", "X RPL 3",
            "XI RPL 1", "XI RPL 2", "XI RPL 3",
            "XII RPL 1", "XII RPL 2", "XII RPL 3",
            "XII TKJ 1", "XII TKJ 2",
            "XII IPA 1", "XII IPA 2",
            "XII IPS 1", "XII IPS 2",
            "XII Mekatronika 1"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas/Jurusan")
            .setItems(kelasJurusan) { _, which ->
                val selected = kelasJurusan[which]
                filterKelasJurusan = selected
                tvPilihKelas.text = selected
                filterData() // PENTING: Panggil filterData() setelah memilih
                Toast.makeText(this, "Filter: $selected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}