package com.example.ritamesa

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TotalKelas : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listKelasDummy = arrayListOf(
        Kelas(1, "RPL", "XII RPL 1", "Budi Santoso"),
        Kelas(2, "TKJ", "XII TKJ 1", "Siti Aminah"),
        Kelas(3, "MM", "XII MM 1", "Ahmad Rizki"),
        Kelas(4, "TKR", "XII TKR 1", "Dewi Lestari"),
        Kelas(5, "TSM", "XII TSM 1", "Joko Widodo"),
        Kelas(6, "TITL", "XII TITL 1", "Rina Melati"),
        Kelas(7, "RPL", "XI RPL 1", "Agus Salim"),
        Kelas(8, "TKJ", "XI TKJ 1", "Maya Sari"),
        Kelas(9, "MM", "XI MM 1", "Rudi Hartono"),
        Kelas(10, "TKR", "XI TKR 1", "Linda Wijaya")
    )

    // ===== DATA DROPDOWN =====
    private val listJurusan = listOf("RPL", "TKJ", "MM", "TKR", "TSM", "TITL", "AK", "AP", "PH")
    private val listTingkatan = listOf("X", "XI", "XII")
    private val listRombel = listOf("1", "2", "3", "4", "5", "6")

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var kelasAdapter: KelasAdapter
    private lateinit var editTextSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_kelas)

        initView()
        setupRecyclerView()
        setupActions()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvKelas)
        editTextSearch = findViewById(R.id.editTextText7)
        editTextSearch.hint = "Cari nama kelas"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        kelasAdapter = KelasAdapter(
            listKelasDummy,
            onEditClickListener = { kelas ->
                val position = listKelasDummy.indexOf(kelas)
                showEditDialog(kelas, position)
            },
            onDeleteClickListener = { kelas ->
                val position = listKelasDummy.indexOf(kelas)
                showDeleteConfirmation(kelas, position)
            }
        )
        recyclerView.adapter = kelasAdapter
    }

    private fun setupActions() {
        // BUTTON BACK
        findViewById<ImageButton>(R.id.imageView36).setOnClickListener {
            finish()
        }

        // BUTTON TAMBAH
        val btnTambah = findViewById<LinearLayout>(R.id.imageButton23)
        btnTambah.setOnClickListener {
            showAddDialog()
        }

        // BUTTON SEARCH
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            searchKelas()
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchKelas()
                true
            } else {
                false
            }
        }
    }

    private fun searchKelas() {
        val query = editTextSearch.text.toString().trim()
        val filteredList = if (query.isEmpty()) {
            listKelasDummy
        } else {
            listKelasDummy.filter {
                it.namaJurusan.contains(query, true) ||
                        it.namaKelas.contains(query, true) ||
                        it.waliKelas.contains(query, true)
            }
        }

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "Tidak ditemukan kelas dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }

        kelasAdapter.updateData(filteredList)
    }

    private fun showAddDialog() {
        try {
            // Buat dialog dengan layout XML
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_tambah_kelas)
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)

            // Initialize views
            val etJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
            val etKelas = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
            val etWaliKelas = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
            val btnArrowJurusan = dialog.findViewById<ImageButton>(R.id.arrowJurusan)
            val btnArrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton9)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            // Setup dropdown jurusan
            btnArrowJurusan.setOnClickListener {
                showJurusanDropdown(dialog, etJurusan)
            }

            // Setup dropdown kelas
            btnArrowKelas.setOnClickListener {
                showKelasDropdown(dialog, etKelas)
            }

            // Click listener untuk EditText juga bisa membuka dropdown
            etJurusan.setOnClickListener {
                showJurusanDropdown(dialog, etJurusan)
            }

            etKelas.setOnClickListener {
                showKelasDropdown(dialog, etKelas)
            }

            // Tombol Batal
            btnBatal.setOnClickListener {
                dialog.dismiss()
            }

            // Tombol Simpan
            btnSimpan.setOnClickListener {
                val jurusan = etJurusan.text.toString().trim()
                val kelas = etKelas.text.toString().trim()
                val waliKelas = etWaliKelas.text.toString().trim()

                if (jurusan.isEmpty() || kelas.isEmpty() || waliKelas.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newId = if (listKelasDummy.isNotEmpty()) listKelasDummy.last().id + 1 else 1
                val newKelas = Kelas(newId, jurusan, kelas, waliKelas)
                listKelasDummy.add(newKelas)
                kelasAdapter.updateData(listKelasDummy)

                Toast.makeText(this, "Data kelas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showJurusanDropdown(dialog: Dialog, etJurusan: EditText) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listJurusan)

        AlertDialog.Builder(this)
            .setTitle("Pilih Jurusan")
            .setAdapter(adapter) { _, which ->
                val selectedJurusan = listJurusan[which]
                etJurusan.setText(selectedJurusan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showKelasDropdown(dialog: Dialog, etKelas: EditText) {
        // Buat kombinasi kelas dari tingkatan dan rombel
        val kelasOptions = mutableListOf<String>()

        for (tingkat in listTingkatan) {
            for (rombel in listRombel) {
                kelasOptions.add("$tingkat $rombel")
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kelasOptions)

        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas")
            .setAdapter(adapter) { _, which ->
                val selectedKelas = kelasOptions[which]
                etKelas.setText(selectedKelas)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(kelas: Kelas, position: Int) {
        try {
            // Buat dialog dengan layout XML untuk edit
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_edit_kelas)
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)

            // Initialize views
            val etJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
            val etKelas = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
            val etWaliKelas = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
            val btnArrowJurusan = dialog.findViewById<ImageButton>(R.id.arrowJurusan)
            val btnArrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton9)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            // Set nilai awal
            etJurusan.setText(kelas.namaJurusan)
            etKelas.setText(kelas.namaKelas)
            etWaliKelas.setText(kelas.waliKelas)

            // Setup dropdown jurusan
            btnArrowJurusan.setOnClickListener {
                showJurusanDropdown(dialog, etJurusan)
            }

            // Setup dropdown kelas
            btnArrowKelas.setOnClickListener {
                showKelasDropdown(dialog, etKelas)
            }

            // Click listener untuk EditText juga bisa membuka dropdown
            etJurusan.setOnClickListener {
                showJurusanDropdown(dialog, etJurusan)
            }

            etKelas.setOnClickListener {
                showKelasDropdown(dialog, etKelas)
            }

            // Tombol Batal
            btnBatal.setOnClickListener {
                dialog.dismiss()
            }

            // Tombol Simpan
            btnSimpan.setOnClickListener {
                val jurusan = etJurusan.text.toString().trim()
                val kelasText = etKelas.text.toString().trim()
                val waliKelas = etWaliKelas.text.toString().trim()

                if (jurusan.isEmpty() || kelasText.isEmpty() || waliKelas.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                listKelasDummy[position] = Kelas(
                    kelas.id,
                    jurusan,
                    kelasText,
                    waliKelas
                )
                kelasAdapter.updateData(listKelasDummy)

                Toast.makeText(this, "Data kelas berhasil diperbarui", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showDeleteConfirmation(kelas: Kelas, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin akan menghapus data kelas ${kelas.namaKelas}?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                listKelasDummy.removeAt(position)
                kelasAdapter.updateData(listKelasDummy)
                Toast.makeText(this, "Data kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
