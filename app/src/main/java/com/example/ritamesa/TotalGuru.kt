package com.example.ritamesa

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TotalGuru : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listGuruDummy = arrayListOf(
        Guru(1, "Budi Santoso", "G001", "198011152001011001", "Matematika", "Guru"),
        Guru(2, "Siti Aminah", "G002", "197505202000032002", "Bahasa Indonesia", "Waka"),
        Guru(3, "Ahmad Rizki", "G003", "198512102005011003", "IPA", "Guru"),
        Guru(4, "Dewi Lestari", "G004", "197802152002032004", "IPS", "Guru"),
        Guru(5, "Joko Widodo", "G005", "196107211991011005", "PKN", "Kepsek"),
        Guru(6, "Rina Melati", "G006", "198304122008032006", "Seni Budaya", "Guru"),
        Guru(7, "Agus Salim", "G007", "197611081999011007", "Olahraga", "Guru"),
        Guru(8, "Maya Sari", "G008", "198709052010032008", "Bahasa Inggris", "Waka"),
        Guru(9, "Rudi Hartono", "G009", "198201182006011009", "TIK", "Guru"),
        Guru(10, "Linda Wijaya", "G010", "198812302012032010", "BK", "Guru")
    )

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var guruAdapter: GuruAdapter
    private lateinit var editTextSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_guru)

        initView()
        setupRecyclerView()
        setupActions()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvGuru)
        editTextSearch = findViewById(R.id.editTextText7)
        editTextSearch.hint = "Cari nama guru"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        guruAdapter = GuruAdapter(listGuruDummy,
            onEditClick = { guru, position ->
                showEditDialog(guru, position)
            },
            onDeleteClick = { guru, position ->
                showDeleteConfirmation(guru, position)
            }
        )
        recyclerView.adapter = guruAdapter
    }

    private fun setupActions() {
        // BUTTON BACK
        findViewById<ImageButton>(R.id.imageView36).setOnClickListener {
            finish()
        }

        // BUTTON TAMBAH - Ini LinearLayout, bukan ImageView
        val btnTambah = findViewById<LinearLayout>(R.id.imageButton23)
        btnTambah.setOnClickListener {
            showAddDialog()
        }

        // BUTTON SEARCH
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            searchGuru()
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchGuru()
                true
            } else {
                false
            }
        }
    }

    private fun searchGuru() {
        val query = editTextSearch.text.toString().trim()
        val filteredList = if (query.isEmpty()) {
            listGuruDummy
        } else {
            listGuruDummy.filter {
                it.nama.contains(query, true) ||
                        it.nip.contains(query, true) ||
                        it.mapel.contains(query, true) ||
                        it.kode.contains(query, true) ||
                        it.keterangan.contains(query, true)
            }
        }

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "Tidak ditemukan guru dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }

        guruAdapter.updateData(filteredList)
    }

    private fun showAddDialog() {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_tambah_data_guru)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
            val inputNip = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
            val inputKode = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
            val inputMapel = dialog.findViewById<EditText>(R.id.input_kelas)
            val inputKeterangan = dialog.findViewById<EditText>(R.id.input_jenis)
            val btnArrowMapel = dialog.findViewById<ImageButton>(R.id.imageButton8)
            val btnArrowRole = dialog.findViewById<ImageButton>(R.id.imageButton9)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            btnArrowMapel?.setOnClickListener {
                showMapelDropdown(inputMapel)
            }

            btnArrowRole?.setOnClickListener {
                showKeteranganDropdown(inputKeterangan)
            }

            btnBatal?.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan?.setOnClickListener {
                val nama = inputNama?.text?.toString()?.trim() ?: ""
                val nip = inputNip?.text?.toString()?.trim() ?: ""
                val kode = inputKode?.text?.toString()?.trim() ?: ""
                val mapel = inputMapel?.text?.toString()?.trim() ?: ""
                val keterangan = inputKeterangan?.text?.toString()?.trim() ?: ""

                if (nama.isEmpty() || nip.isEmpty() || kode.isEmpty() || mapel.isEmpty() || keterangan.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                showSaveConfirmation("Tambah") {
                    val newId = if (listGuruDummy.isNotEmpty()) listGuruDummy.last().id + 1 else 1
                    val newGuru = Guru(newId, nama, kode, nip, mapel, keterangan)
                    listGuruDummy.add(newGuru)
                    guruAdapter.updateData(listGuruDummy)
                    Toast.makeText(this, "Data guru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showEditDialog(guru: Guru, position: Int) {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_tambah_data_guru) // Gunakan layout yang sama
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
            val inputNip = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
            val inputKode = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
            val inputMapel = dialog.findViewById<EditText>(R.id.input_kelas)
            val inputKeterangan = dialog.findViewById<EditText>(R.id.input_jenis)
            val btnArrowMapel = dialog.findViewById<ImageButton>(R.id.imageButton8)
            val btnArrowRole = dialog.findViewById<ImageButton>(R.id.imageButton9)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            // Set judul dialog
            dialog.setTitle("Edit Data Guru")

            // Isi data yang akan diedit
            inputNama?.setText(guru.nama)
            inputNip?.setText(guru.nip)
            inputKode?.setText(guru.kode)
            inputMapel?.setText(guru.mapel)
            inputKeterangan?.setText(guru.keterangan)

            btnArrowMapel?.setOnClickListener {
                showMapelDropdown(inputMapel)
            }

            btnArrowRole?.setOnClickListener {
                showKeteranganDropdown(inputKeterangan)
            }

            btnBatal?.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan?.setOnClickListener {
                val nama = inputNama?.text?.toString()?.trim() ?: ""
                val nip = inputNip?.text?.toString()?.trim() ?: ""
                val kode = inputKode?.text?.toString()?.trim() ?: ""
                val mapel = inputMapel?.text?.toString()?.trim() ?: ""
                val keterangan = inputKeterangan?.text?.toString()?.trim() ?: ""

                if (nama.isEmpty() || nip.isEmpty() || kode.isEmpty() || mapel.isEmpty() || keterangan.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                showSaveConfirmation("Edit") {
                    // Update data di list
                    listGuruDummy[position] = Guru(guru.id, nama, kode, nip, mapel, keterangan)
                    guruAdapter.updateData(listGuruDummy)
                    Toast.makeText(this, "Data guru berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showDeleteConfirmation(guru: Guru, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin akan menghapus data ${guru.nama}?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                listGuruDummy.removeAt(position)
                guruAdapter.updateData(listGuruDummy)
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showSaveConfirmation(action: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Yakin ${action.lowercase()} data?")
            .setPositiveButton("Ya, Simpan") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showMapelDropdown(editText: EditText?) {
        val mapelList = arrayOf(
            "Matematika", "Bahasa Indonesia", "Bahasa Inggris",
            "IPAS", "Bahasa Jawa", "PKN", "PAI", "Olahraga",
            "Informatika", "BK", "MPP", "MPKK", "PKDK",
            "Sejarah", "Bahasa Jepang"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Mapel")
            .setItems(mapelList) { _, which ->
                editText?.setText(mapelList[which])
            }
            .show()
    }

    private fun showKeteranganDropdown(editText: EditText?) {
        val keteranganList = arrayOf("Guru", "Waka", "Admin", "Kepsek")

        AlertDialog.Builder(this)
            .setTitle("Pilih Keterangan")
            .setItems(keteranganList) { _, which ->
                editText?.setText(keteranganList[which])
            }
            .show()
    }
}