package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RekapKehadiranGuru : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GuruAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rekap_kehadiran_guru)

        initView()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recyclerViewGuru)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        val guruList = listOf(
            Guru("1", "Budi Santoso", "1987654321", "Matematika"),
            Guru("2", "Siti Aminah", "1987654322", "Bahasa Indonesia"),
            Guru("3", "Agus Wijaya", "1987654323", "IPA"),
            Guru("4", "Dewi Lestari", "1987654324", "IPS"),
            Guru("5", "Rudi Hartono", "1987654325", "PJOK")
        )

        adapter = GuruAdapter(guruList) { guru ->
            showDetailKehadiranDialog(guru)
        }
        recyclerView.adapter = adapter
    }

    private fun setupActions() {
        // Setup tombol back
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup menu titik tiga (more_vert)
        val btnMenu = findViewById<ImageButton>(R.id.buttonmenu)
        btnMenu.setOnClickListener {
            showPopupMenu(it)
        }

        // Setup search functionality
        val editTextSearch = findViewById<EditText>(R.id.editTextText5)
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(editTextSearch.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        // Implementasi pencarian
        if (::adapter.isInitialized) {
            adapter.filter(query)
        }
    }

    private fun showDetailKehadiranDialog(guru: Guru) {
        val detailKehadiran = getGuruDetailKehadiran(guru)

        AlertDialog.Builder(this)
            .setTitle("Detail Kehadiran Guru")
            .setMessage(formatDetailMessage(detailKehadiran))
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getGuruDetailKehadiran(guru: Guru): DetailKehadiranGuru {
        // Mapping detail kehadiran berdasarkan guru
        return when (guru.nomor) {
            "1" -> DetailKehadiranGuru(
                "Senin, 7 Januari 2026",
                "Jam ke 1  07:00",
                guru.mataPelajaran,
                "Hadir",
                "12 RPL, 12 TKJ, 12 MM",
                "Mengajar sesuai jadwal"
            )
            "2" -> DetailKehadiranGuru(
                "Senin, 7 Januari 2026",
                "Jam ke 2  08:45",
                guru.mataPelajaran,
                "Hadir",
                "11 RPL, 11 TKJ",
                "Mengajar dengan baik"
            )
            "3" -> DetailKehadiranGuru(
                "Selasa, 8 Januari 2026",
                "Jam ke 1  07:00",
                guru.mataPelajaran,
                "Terlambat",
                "10 RPL, 10 MM",
                "Terlambat 15 menit"
            )
            "4" -> DetailKehadiranGuru(
                "Selasa, 8 Januari 2026",
                "Jam ke 2  08:45",
                guru.mataPelajaran,
                "Izin",
                "12 RPL, 12 TKJ",
                "Izin dinas"
            )
            "5" -> DetailKehadiranGuru(
                "Rabu, 9 Januari 2026",
                "Jam ke 3  10:30",
                guru.mataPelajaran,
                "Hadir",
                "11 MM, 10 TKJ",
                "Mengajar sesuai jadwal"
            )
            else -> DetailKehadiranGuru(
                "Kamis, 10 Januari 2026",
                "Jam ke 1  07:00",
                guru.mataPelajaran,
                "Hadir",
                "Semua kelas 12",
                "Mengajar dengan baik"
            )
        }
    }

    private fun formatDetailMessage(detail: DetailKehadiranGuru): String {
        return """
            ${detail.tanggal}
            ${detail.jam}
            
            Mata Pelajaran : ${detail.mataPelajaran}
            Status : ${detail.status}
            Jurusan/kelas: ${detail.jurusanKelas}
            Keterangan : ${detail.keterangan}
        """.trimIndent()
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_rekap_switch, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> {
                    // Sudah di halaman guru, bisa beri feedback
                    Toast.makeText(this, "Anda sudah di halaman Rekap Guru", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_siswa -> {
                    // Pindah ke halaman rekap siswa
                    val intent = Intent(this, RekapKehadiranSiswa::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun setupBottomNavigation() {
        // Home - ke Dashboard
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        // Contacts - ke Rekap Kehadiran (Siswa)
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            val intent = Intent(this, RekapKehadiranSiswa::class.java)
            startActivity(intent)
            finish()
        }

        // Bar Chart - ke Statistik
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            val intent = Intent(this, StatistikKehadiran::class.java)
            startActivity(intent)
            finish()
        }

        // Notifications - ke Notifikasi
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            val intent = Intent(this, NotifikasiSemua::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Data class untuk guru
    data class Guru(
        val nomor: String,
        val nama: String,
        val nip: String,
        val mataPelajaran: String
    )

    // Data class untuk detail kehadiran guru - DIPERBAIKI
    data class DetailKehadiranGuru(
        val tanggal: String,
        val jam: String, // Format: "Jam ke 1  07:00" dengan spasi ganda
        val mataPelajaran: String,
        val status: String,
        val jurusanKelas: String, // NAMA VARIABEL DIPERBAIKI (tidak boleh ada slash)
        val keterangan: String
    )

    // Adapter untuk RecyclerView dengan filter
    class GuruAdapter(
        private var guruList: List<Guru>,
        private val onLihatClickListener: (Guru) -> Unit
    ) : RecyclerView.Adapter<GuruAdapter.GuruViewHolder>() {

        private var filteredList: List<Guru> = guruList

        inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvTelepon: TextView = itemView.findViewById(R.id.tvTelepon)
            val tvMataPelajaran: TextView = itemView.findViewById(R.id.tvMataPelajaran)
            val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)

            init {
                btnLihat.setOnClickListener {
                    // Aksi ketika tombol lihat diklik
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onLihatClickListener(filteredList[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lihat_rekap_guru, parent, false)
            return GuruViewHolder(view)
        }

        override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
            val guru = filteredList[position]
            holder.tvNomor.text = guru.nomor
            holder.tvNama.text = guru.nama
            holder.tvTelepon.text = guru.nip
            holder.tvMataPelajaran.text = guru.mataPelajaran
        }

        override fun getItemCount(): Int = filteredList.size

        // Fungsi untuk filter data
        fun filter(query: String) {
            filteredList = if (query.isEmpty()) {
                guruList
            } else {
                guruList.filter {
                    it.nama.contains(query, ignoreCase = true) ||
                            it.nip.contains(query, ignoreCase = true) ||
                            it.mataPelajaran.contains(query, ignoreCase = true)
                }
            }
            notifyDataSetChanged()
        }
    }
}