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

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewGuru)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Data dummy untuk contoh
        val guruList = listOf(
            Guru("1", "Budi Santoso", "1987654321", "Matematika"),
            Guru("2", "Siti Aminah", "1987654322", "Bahasa Indonesia"),
            Guru("3", "Agus Wijaya", "1987654323", "IPA"),
            Guru("4", "Dewi Lestari", "1987654324", "IPS"),
            Guru("5", "Rudi Hartono", "1987654325", "PJOK")
        )

        adapter = GuruAdapter(guruList) { guru ->
            showDetailDialog(guru)
        }
        recyclerView.adapter = adapter

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

    private fun performSearch(query: String) {
        // Implementasi pencarian
        if (::adapter.isInitialized) {
            adapter.filter(query)
        }
    }

    private fun showDetailDialog(guru: Guru) {
        AlertDialog.Builder(this)
            .setTitle("Detail Guru")
            .setMessage(
                """
                Nama: ${guru.nama}
                NIP: ${guru.nip}
                Mata Pelajaran: ${guru.mataPelajaran}
                
                Kehadiran Bulan Ini:
                • Hadir: 20 hari
                • Izin: 1 hari
                • Sakit: 0 hari
                • Alpa: 0 hari
                • Terlambat: 1 hari
                • Pulang: 2 hari
                
                Persentase Kehadiran: 95.2%
                """.trimIndent()
            )
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }

            .show()
    }

    // Data class untuk guru
    data class Guru(
        val nomor: String,
        val nama: String,
        val nip: String,
        val mataPelajaran: String
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