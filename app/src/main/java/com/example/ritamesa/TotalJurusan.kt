package com.example.ritamesa

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TotalJurusan : AppCompatActivity() {

    // ===== DATA LIST =====
    private val listJurusanDummy = arrayListOf(
        Jurusan(1, "Mekatronika", "111"),
        Jurusan(2, "Rekayasa Perangkat Lunak", "222"),
        Jurusan(3, "Teknik Komputer Jaringan", "333"),
        Jurusan(4, "Desain Komunikasi Visual", "444"),
        Jurusan(5, "Elektronika Industri", "555"),
        Jurusan(6, "Animasi", "666"),
        Jurusan(7, "Broadcasting", "777"),
        Jurusan(8, "Audio Video", "888")
    )

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var jurusanAdapter: JurusanAdapter
    private lateinit var editTextSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_jurusan)

        initView()
        setupRecyclerView()
        setupActions()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvJurusan)
        editTextSearch = findViewById(R.id.editTextText7)
        editTextSearch.hint = "Cari jurusan"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        jurusanAdapter = JurusanAdapter(
            listJurusanDummy,
            onEditClickListener = { jurusan ->
                val position = listJurusanDummy.indexOfFirst { it.id == jurusan.id }
                if (position != -1) {
                    showEditDialog(jurusan, position)
                }
            },
            onDeleteClickListener = { jurusan ->
                val position = listJurusanDummy.indexOfFirst { it.id == jurusan.id }
                if (position != -1) {
                    showDeleteConfirmation(jurusan, position)
                }
            }
        )
        recyclerView.adapter = jurusanAdapter
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
            searchJurusan()
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchJurusan()
                true
            } else {
                false
            }
        }
    }

    private fun searchJurusan() {
        val query = editTextSearch.text.toString().trim()
        val filteredList = if (query.isEmpty()) {
            listJurusanDummy
        } else {
            listJurusanDummy.filter {
                it.KonsentrasiKeahlian.contains(query, true) ||
                        it.Kodejurusan.contains(query, true)
            }
        }

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "Tidak ditemukan jurusan dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }

        jurusanAdapter.updateData(filteredList)
    }

    private fun showAddDialog() {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_tambah_jurusan)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(true)

            // Set ukuran dialog
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            // PERBAIKAN: Gunakan ID yang benar dari XML
            val inputNama = dialog.findViewById<EditText>(R.id.et_nama_jurusan)
            val inputKode = dialog.findViewById<EditText>(R.id.et_kode_jurusan)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            btnBatal.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan.setOnClickListener {
                val namaJurusan = inputNama.text.toString().trim()
                val kodeJurusan = inputKode.text.toString().trim()

                if (namaJurusan.isEmpty() || kodeJurusan.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Cek duplikasi kode jurusan
                val isKodeExist = listJurusanDummy.any { it.Kodejurusan == kodeJurusan }
                if (isKodeExist) {
                    Toast.makeText(this, "Kode jurusan sudah digunakan!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newId = if (listJurusanDummy.isNotEmpty()) listJurusanDummy.last().id + 1 else 1
                val newJurusan = Jurusan(newId, namaJurusan, kodeJurusan)
                listJurusanDummy.add(newJurusan)
                jurusanAdapter.updateData(listJurusanDummy)
                Toast.makeText(this, "Data jurusan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showEditDialog(jurusan: Jurusan, position: Int) {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.pop_up_edit_jurusan)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(true)

            // Set ukuran dialog
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            // PERBAIKAN: Gunakan ID yang benar dari XML
            val inputNama = dialog.findViewById<EditText>(R.id.et_nama_jurusan)
            val inputKode = dialog.findViewById<EditText>(R.id.et_kode_jurusan)
            val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
            val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

            // Isi data yang akan diedit
            inputNama.setText(jurusan.KonsentrasiKeahlian)
            inputKode.setText(jurusan.Kodejurusan)

            btnBatal.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan.setOnClickListener {
                val namaJurusan = inputNama.text.toString().trim()
                val kodeJurusan = inputKode.text.toString().trim()

                if (namaJurusan.isEmpty() || kodeJurusan.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Cek duplikasi kode jurusan (kecuali untuk data yang sedang diedit)
                val isKodeExist = listJurusanDummy.any {
                    it.Kodejurusan == kodeJurusan && it.id != jurusan.id
                }
                if (isKodeExist) {
                    Toast.makeText(this, "Kode jurusan sudah digunakan!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                listJurusanDummy[position] = Jurusan(
                    jurusan.id,
                    namaJurusan,
                    kodeJurusan
                )
                jurusanAdapter.updateData(listJurusanDummy)
                Toast.makeText(this, "Data jurusan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showDeleteConfirmation(jurusan: Jurusan, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin akan menghapus data jurusan ${jurusan.KonsentrasiKeahlian}?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                listJurusanDummy.removeAt(position)
                jurusanAdapter.updateData(listJurusanDummy)
                Toast.makeText(this, "Data jurusan berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}