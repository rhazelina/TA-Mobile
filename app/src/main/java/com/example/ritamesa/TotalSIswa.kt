package com.example.ritamesa

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TotalSiswa : AppCompatActivity() {

    // ===== DATA CLASS =====
    data class ModelSiswa(
        val nisn: String,
        val nama: String,
        val kelas: String,
        val jurusan: String = "RPL", // Default value
        val jk: String
    )

    // ===== ADAPTER =====
    inner class SiswaAdapter(
        private val listSiswa: List<ModelSiswa>
    ) : RecyclerView.Adapter<SiswaAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvNama: TextView = view.findViewById(R.id.tvNama)
            val tvNisn: TextView = view.findViewById(R.id.tvNisn)
            val tvKelas: TextView = view.findViewById(R.id.tvKelas)
            val tvJurusan: TextView = view.findViewById(R.id.tvKode)
            val tvJk: TextView = view.findViewById(R.id.tvJk)
            val btnEdit: LinearLayout = view.findViewById(R.id.btnEdit)
            val btnHapus: LinearLayout = view.findViewById(R.id.btnHapus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_crud_datasiswa, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val siswa = listSiswa[position]

            // Set data ke view
            holder.tvNo.text = (position + 1).toString()
            holder.tvNama.text = siswa.nama
            holder.tvNisn.text = siswa.nisn
            holder.tvKelas.text = siswa.kelas
            holder.tvJurusan.text = siswa.jurusan
            holder.tvJk.text = siswa.jk

            // Button Edit - Tampilkan popup edit
            holder.btnEdit.setOnClickListener {
                showEditDialog(siswa, position)
            }

            // Button Hapus - Tampilkan konfirmasi hapus
            holder.btnHapus.setOnClickListener {
                showDeleteDialog(siswa.nama, position)
            }
        }

        override fun getItemCount(): Int = listSiswa.size
    }

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var siswaAdapter: SiswaAdapter
    private lateinit var btnTambahContainer: View // Changed from ImageButton to View
    private lateinit var editTextSearch: EditText
    private lateinit var ivSearch: ImageView // Changed from ImageButton to ImageView

    // ===== DUMMY DATA =====
    private val listSiswaDummy = arrayListOf(
        ModelSiswa("12345", "Andi Wijaya", "X RPL 1", "RPL", "L"),
        ModelSiswa("12346", "Budi Santoso", "X RPL 2", "RPL", "L"),
        ModelSiswa("12347", "Siti Nurhaliza", "X AKL 1", "AKL", "P"),
        ModelSiswa("12348", "Dewi Lestari", "XI RPL 1", "RPL", "P"),
        ModelSiswa("12349", "Eko Prasetyo", "XI RPL 2", "RPL", "L"),
        ModelSiswa("12350", "Fajar Ramadan", "XII RPL 1", "RPL", "L"),
        ModelSiswa("12351", "Gita Gutawa", "XII AKL 1", "AKL", "P"),
        ModelSiswa("12352", "Hendra Kurniawan", "X TKJ 1", "TKJ", "L"),
        ModelSiswa("12353", "Indah Permata", "XI TKJ 1", "TKJ", "P"),
        ModelSiswa("12354", "Joko Susilo", "XII TKJ 1", "TKJ", "L"),
        ModelSiswa("12355", "Kartika Sari", "X MM 1", "MM", "P"),
        ModelSiswa("12356", "Lukman Hakim", "XI MM 1", "MM", "L"),
        ModelSiswa("12357", "Maya Indah", "XII MM 1", "MM", "P"),
        ModelSiswa("12358", "Nurul Hikmah", "X RPL 3", "RPL", "P"),
        ModelSiswa("12359", "Lely Sagita", "XII RPL 2", "RPL", "P")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.total_siswa)

        initView()
        setupRecyclerView()
        setupActions()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvSiswa)
        btnTambahContainer = findViewById(R.id.imageButton13) // ConstraintLayout, not ImageButton
        editTextSearch = findViewById(R.id.editTextText) // Changed from editTextText2 to etSearch
        ivSearch = findViewById(R.id.imageButton12) // Changed from ImageButton to ImageView

        // Set placeholder untuk search
        editTextSearch.hint = "Cari nama siswa"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        siswaAdapter = SiswaAdapter(listSiswaDummy)
        recyclerView.adapter = siswaAdapter
    }

    private fun setupActions() {
        // BUTTON BACK
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // BUTTON TAMBAH CONTAINER (clickable area)
        btnTambahContainer.setOnClickListener {
            showAddDialog()
        }

        // SEARCH ICON (ImageView, bisa diklik)
        ivSearch.setOnClickListener {
            searchSiswa()
        }

        // ENTER KEY LISTENER UNTUK SEARCH
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                searchSiswa()
                true
            } else {
                false
            }
        }
    }

    private fun searchSiswa() {
        val query = editTextSearch.text.toString().trim().lowercase()

        if (query.isEmpty()) {
            // Tampilkan semua data jika search kosong
            siswaAdapter = SiswaAdapter(listSiswaDummy)
            recyclerView.adapter = siswaAdapter
            return
        }

        val filteredList = listSiswaDummy.filter {
            it.nama.lowercase().contains(query) ||
                    it.nisn.contains(query) ||
                    it.kelas.lowercase().contains(query) ||
                    it.jurusan.lowercase().contains(query)
        }

        siswaAdapter = SiswaAdapter(filteredList)
        recyclerView.adapter = siswaAdapter

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Tidak ditemukan siswa dengan kata kunci '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_tambah_data_siswa)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        // Get views
        val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
        val inputNisn = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
        val inputJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
        val inputKelas = dialog.findViewById<EditText>(R.id.input_kelas)
        val inputJenis = dialog.findViewById<EditText>(R.id.input_jenis)
        val btnArrowJurusan = dialog.findViewById<ImageButton>(R.id.arrowJurusan)
        val btnArrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton8)
        val btnArrowJenis = dialog.findViewById<ImageButton>(R.id.imageButton9)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

        // Setup dropdown Jurusan
        btnArrowJurusan.setOnClickListener {
            showJurusanDropdown(inputJurusan)
        }

        // Setup dropdown Kelas
        btnArrowKelas.setOnClickListener {
            showKelasDropdown(inputKelas)
        }

        // Setup dropdown Jenis Kelamin
        btnArrowJenis.setOnClickListener {
            showJenisKelaminDropdown(inputJenis)
        }

        // Setup click listeners
        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnSimpan.setOnClickListener {
            val nama = inputNama.text.toString().trim()
            val nisn = inputNisn.text.toString().trim()
            val jurusan = inputJurusan.text.toString().trim()
            val kelas = inputKelas.text.toString().trim()
            val jenis = inputJenis.text.toString().trim()

            if (nama.isEmpty() || nisn.isEmpty() || jurusan.isEmpty() || kelas.isEmpty() || jenis.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tambah data baru ke list
            val newSiswa = ModelSiswa(nisn, nama, kelas, jurusan, jenis)
            listSiswaDummy.add(newSiswa)

            // Update adapter
            siswaAdapter = SiswaAdapter(listSiswaDummy)
            recyclerView.adapter = siswaAdapter

            Toast.makeText(this, "Data siswa berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditDialog(siswa: ModelSiswa, position: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_edit_data_siswa)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        // Change title
        val title = dialog.findViewById<TextView>(android.R.id.title)
        title?.text = "Edit Data Siswa"

        // Get views
        val inputNama = dialog.findViewById<EditText>(R.id.input_keterangan_nama)
        val inputNisn = dialog.findViewById<EditText>(R.id.input_keterangan_nisn)
        val inputJurusan = dialog.findViewById<EditText>(R.id.input_keterangan_jurusan)
        val inputKelas = dialog.findViewById<EditText>(R.id.input_kelas)
        val inputJenis = dialog.findViewById<EditText>(R.id.input_jenis)
        val btnArrowJurusan = dialog.findViewById<ImageButton>(R.id.arrowJurusan)
        val btnArrowKelas = dialog.findViewById<ImageButton>(R.id.imageButton8)
        val btnArrowJenis = dialog.findViewById<ImageButton>(R.id.imageButton9)
        val btnBatal = dialog.findViewById<Button>(R.id.btn_batal)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan)

        // Set current data
        inputNama.setText(siswa.nama)
        inputNisn.setText(siswa.nisn)
        inputJurusan.setText(siswa.jurusan)
        inputKelas.setText(siswa.kelas)
        inputJenis.setText(siswa.jk)

        // Change button text
        btnSimpan.text = "Update"

        // Setup dropdown Jurusan
        btnArrowJurusan.setOnClickListener {
            showJurusanDropdown(inputJurusan)
        }

        // Setup dropdown Kelas
        btnArrowKelas.setOnClickListener {
            showKelasDropdown(inputKelas)
        }

        // Setup dropdown Jenis Kelamin
        btnArrowJenis.setOnClickListener {
            showJenisKelaminDropdown(inputJenis)
        }

        // Setup click listeners
        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnSimpan.setOnClickListener {
            val nama = inputNama.text.toString().trim()
            val nisn = inputNisn.text.toString().trim()
            val jurusan = inputJurusan.text.toString().trim()
            val kelas = inputKelas.text.toString().trim()
            val jenis = inputJenis.text.toString().trim()

            if (nama.isEmpty() || nisn.isEmpty() || jurusan.isEmpty() || kelas.isEmpty() || jenis.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update data di list
            val updatedSiswa = ModelSiswa(nisn, nama, kelas, jurusan, jenis)
            listSiswaDummy[position] = updatedSiswa

            // Update adapter
            siswaAdapter = SiswaAdapter(listSiswaDummy)
            recyclerView.adapter = siswaAdapter

            Toast.makeText(this, "Data siswa berhasil diupdate", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteDialog(namaSiswa: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus data siswa:\n$namaSiswa?")
            .setPositiveButton("Hapus") { _, _ ->
                // Hapus data dari list
                listSiswaDummy.removeAt(position)

                // Update adapter
                siswaAdapter = SiswaAdapter(listSiswaDummy)
                recyclerView.adapter = siswaAdapter

                Toast.makeText(this, "Data siswa berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showJurusanDropdown(editText: EditText) {
        val jurusanList = arrayOf("RPL", "DKV", "MT", "TKJ", "AV", "EI", "BC", "ANIM")

        AlertDialog.Builder(this)
            .setTitle("Pilih Jurusan")
            .setItems(jurusanList) { _, which ->
                editText.setText(jurusanList[which])
            }
            .show()
    }

    private fun showKelasDropdown(editText: EditText) {
        val kelasList = arrayOf(
            "X RPL 1", "X RPL 2", "X RPL 3",
            "XI RPL 1", "XI RPL 2", "XI RPL 3",
            "XII RPL 1", "XII RPL 2",
            "X TKJ 1", "X TKJ 2", "X TKJ 3",
            "XI TKJ 1", "XI TKJ 2", "XI TKJ 3",
            "XII TKJ 1", "XII TKJ 2",
            "X DKV 1", "X DKV 2", "X DKV 3",
            "XI DKV 1", "XI DKV 2", "XI DKV 3",
            "XII DKV 1", "XII DKV 2", "XII DKV 3",
            "X MT 1", "X MT 2",
            "XI MT 1", "XI MT 2",
            "XII MT 1", "XII MT 2",
            "X TKJ 1", "X TKJ 2", "X TKJ 3",
            "XI TKJ 1", "XI TKJ 2", "XI TKJ 3",
            "XII TKJ 1", "XII TKJ 2",
            "X AV 1", "X AV 2",
            "XI AV 1", "XI AV 2",
            "XII AV 1", "XII AV 2",
            "X EI 1", "X EI 2",
            "XI EI 1", "XI EI 2",
            "XII EI 1", "XII EI 2",
            "X BC 1", "X BC 2",
            "XI BC 1", "XI BC 2",
            "XII BC 1", "XII BC 2",
            "X ANIM 1", "X ANIM 2",
            "XI ANIM 1", "XI ANIM 2",
            "XII ANIM 1", "XII ANIM 2"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Kelas")
            .setItems(kelasList) { _, which ->
                editText.setText(kelasList[which])
            }
            .show()
    }

    private fun showJenisKelaminDropdown(editText: EditText) {
        val jenisList = arrayOf("Laki-laki (L)", "Perempuan (P)")

        AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Kelamin")
            .setItems(jenisList) { _, which ->
                val selected = if (which == 0) "L" else "P"
                editText.setText(selected)
            }
            .show()
    }
}