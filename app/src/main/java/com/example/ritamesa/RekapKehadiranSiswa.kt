package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RekapKehadiranSiswa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rekapAdapter: RekapSiswaAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton

    // Data dummy siswa
    private val siswaList = listOf(
        SiswaRekap(1, "Andi Wijaya", "1234567890", "XII", "RPL"),
        SiswaRekap(2, "Budi Santoso", "2345678901", "XII", "TKJ"),
        SiswaRekap(3, "Citra Lestari", "3456789012", "XII", "MM"),
        SiswaRekap(4, "Dewi Anggraini", "4567890123", "XI", "RPL"),
        SiswaRekap(5, "Eko Prasetyo", "5678901234", "XI", "TKJ"),
        SiswaRekap(6, "Fitriani", "6789012345", "XI", "MM"),
        SiswaRekap(7, "Gunawan", "7890123456", "X", "RPL"),
        SiswaRekap(8, "Hendra Wijaya", "8901234567", "X", "TKJ"),
        SiswaRekap(9, "Indah Permata", "9012345678", "X", "MM"),
        SiswaRekap(10, "Joko Susilo", "0123456789", "XII", "RPL")
    )

    // Data dummy detail kehadiran per siswa - DITAMBAHKAN NAMA GURU
    private val detailKehadiranMap = mapOf(
        1 to DetailKehadiran(
            "Senin, 7 Januari 2026",
            "Jam ke 1  07:00",
            "Bahasa",
            "Hadir",
            "Hadir",
            "Ibu Siti Aminah"
        ),
        2 to DetailKehadiran(
            "Senin, 7 Januari 2026",
            "Jam ke 2  08:45",
            "Matematika",
            "Hadir",
            "Hadir tepat waktu",
            "Pak Budi Santoso"
        ),
        3 to DetailKehadiran(
            "Selasa, 8 Januari 2026",
            "Jam ke 1  07:00",
            "IPA",
            "Terlambat",
            "Terlambat 15 menit",
            "Pak Agus Wijaya"
        ),
        4 to DetailKehadiran(
            "Selasa, 8 Januari 2026",
            "Jam ke 2  08:45",
            "Bahasa Inggris",
            "Sakit",
            "Izin sakit",
            "Ibu Dewi Lestari"
        ),
        5 to DetailKehadiran(
            "Rabu, 9 Januari 2026",
            "Jam ke 3  10:30",
            "Sejarah",
            "Izin",
            "Izin keluarga",
            "Pak Rudi Hartono"
        ),
        6 to DetailKehadiran(
            "Rabu, 9 Januari 2026",
            "Jam ke 1  07:00",
            "PKN",
            "Hadir",
            "Hadir",
            "Ibu Siti Aminah"
        ),
        7 to DetailKehadiran(
            "Kamis, 10 Januari 2026",
            "Jam ke 2  08:45",
            "Fisika",
            "Alpa",
            "Tidak hadir tanpa keterangan",
            "Pak Agus Wijaya"
        ),
        8 to DetailKehadiran(
            "Kamis, 10 Januari 2026",
            "Jam ke 3  10:30",
            "Kimia",
            "Hadir",
            "Hadir",
            "Pak Agus Wijaya"
        ),
        9 to DetailKehadiran(
            "Jumat, 11 Januari 2026",
            "Jam ke 1  07:00",
            "Biologi",
            "Hadir",
            "Hadir tepat waktu",
            "Ibu Dewi Lestari"
        ),
        10 to DetailKehadiran(
            "Jumat, 11 Januari 2026",
            "Jam ke 2  08:45",
            "Seni Budaya",
            "Hadir",
            "Hadir",
            "Pak Rudi Hartono"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rekap_kehadiran_siswa)

        initView()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.rvKehadiran)
        editTextSearch = findViewById(R.id.editTextText5)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.buttonmenu)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        rekapAdapter = RekapSiswaAdapter(
            siswaList,
            onLihatClickListener = { siswa ->
                // Aksi ketika tombol lihat diklik
                showDetailKehadiranDialog(siswa)
            }
        )
        recyclerView.adapter = rekapAdapter
    }

    private fun setupActions() {
        // BUTTON BACK
        btnBack.setOnClickListener {
            finish()
        }

        // BUTTON MENU (More Vert)
        btnMenu.setOnClickListener {
            showPopupMenu(it)
        }

        // BUTTON SEARCH
        findViewById<ImageButton>(R.id.imageButton17).setOnClickListener {
            performSearch()
        }

        // SEARCH TEXT LISTENER
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch()
            }
        })

        // SEARCH ACTION LISTENER
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = editTextSearch.text.toString().trim()
        rekapAdapter.filter(query)
    }

    private fun showDetailKehadiranDialog(siswa: SiswaRekap) {
        val detailKehadiran = detailKehadiranMap[siswa.id] ?: getDefaultDetail()

        AlertDialog.Builder(this)
            .setTitle("Detail Kehadiran")
            .setMessage(formatDetailMessage(detailKehadiran))
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getDefaultDetail(): DetailKehadiran {
        return DetailKehadiran(
            "Senin, 7 Januari 2026",
            "Jam ke 1  07:00",
            "Bahasa",
            "Hadir",
            "Hadir",
            "Ibu Siti Aminah"
        )
    }

    private fun formatDetailMessage(detail: DetailKehadiran): String {
        return """
            ${detail.tanggal}
            ${detail.jam}
            
            Mata pelajaran : ${detail.mataPelajaran}
            Guru pengajar : ${detail.guruPengajar} 
            Status : ${detail.status}
            Keterangan : ${detail.keterangan}
        """.trimIndent()
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_rekap_switch, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_guru -> {
                    // Pindah ke halaman rekap guru
                    val intent = Intent(this, RekapKehadiranGuru::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.menu_siswa -> {
                    // Sudah di halaman siswa
                    Toast.makeText(this, "Anda sudah di halaman Rekap Siswa", Toast.LENGTH_SHORT)
                        .show()
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun showMenuDialog() {
        val items =
            arrayOf("Refresh Data", "Ekspor ke Excel", "Filter Berdasarkan Kelas", "Pengaturan")

        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        // Refresh
                        rekapAdapter.updateData(siswaList)
                        Toast.makeText(this, "Data direfresh", Toast.LENGTH_SHORT).show()
                    }

                    1 -> {
                        // Ekspor ke Excel
                        Toast.makeText(this, "Mengekspor data ke Excel...", Toast.LENGTH_SHORT)
                            .show()
                    }

                    2 -> {
                        // Filter berdasarkan kelas
                        showFilterDialog()
                    }

                    3 -> {
                        // Pengaturan
                        Toast.makeText(this, "Membuka pengaturan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showFilterDialog() {
        val kelasList = arrayOf("Semua Kelas", "X", "XI", "XII")
        val jurusanList = arrayOf("Semua Jurusan", "RPL", "TKJ", "MM", "Mekatronika")

        AlertDialog.Builder(this)
            .setTitle("Filter Data")
            .setMessage("Pilih filter yang diinginkan:")
            .setPositiveButton("Filter Kelas") { dialog, _ ->
                showSingleChoiceDialog("Pilih Kelas", kelasList) { selected ->
                    if (selected == 0) {
                        rekapAdapter.updateData(siswaList)
                    } else {
                        val filtered = siswaList.filter { it.kelas == kelasList[selected] }
                        rekapAdapter.updateData(filtered)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Filter Jurusan") { dialog, _ ->
                showSingleChoiceDialog("Pilih Jurusan", jurusanList) { selected ->
                    if (selected == 0) {
                        rekapAdapter.updateData(siswaList)
                    } else {
                        val filtered = siswaList.filter { it.jurusan == jurusanList[selected] }
                        rekapAdapter.updateData(filtered)
                    }
                }
                dialog.dismiss()
            }
            .setNeutralButton("Reset Filter") { dialog, _ ->
                rekapAdapter.updateData(siswaList)
                Toast.makeText(this, "Filter direset", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    private fun showSingleChoiceDialog(
        title: String,
        items: Array<String>,
        onItemSelected: (Int) -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(items, 0) { dialog, which ->
                onItemSelected(which)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupBottomNavigation() {
        // Home - ke Dashboard
        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        // Contacts (Active) - ke Data Rekap (halaman ini)
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            Toast.makeText(this, "Anda sudah di halaman Rekap Kehadiran", Toast.LENGTH_SHORT).show()
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

    // Data class untuk siswa
    data class SiswaRekap(
        val id: Int,
        val nama: String,
        val nisn: String,
        val kelas: String,
        val jurusan: String
    ) {
        fun getKelasJurusan(): String = "$kelas $jurusan"
    }

    // Data class untuk detail kehadiran - DITAMBAHKAN PARAMETER guruPengajar
    data class DetailKehadiran(
        val tanggal: String,
        val jam: String,
        val mataPelajaran: String,
        val status: String,
        val keterangan: String,
        val guruPengajar: String
    )

    // Adapter untuk RecyclerView siswa
    class RekapSiswaAdapter(
        private var dataList: List<SiswaRekap>,
        private val onLihatClickListener: (SiswaRekap) -> Unit
    ) : RecyclerView.Adapter<RekapSiswaAdapter.SiswaViewHolder>() {

        private var filteredList: List<SiswaRekap> = dataList

        inner class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
            val tvNama: TextView = itemView.findViewById(R.id.tvNama)
            val tvNisn: TextView = itemView.findViewById(R.id.tvTelepon)
            val tvKelasJurusan: TextView = itemView.findViewById(R.id.tvMataPelajaran)
            val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)

            init {
                btnLihat.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onLihatClickListener(filteredList[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lihat_rekap_guru, parent, false)
            return SiswaViewHolder(view)
        }

        override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
            val siswa = filteredList[position]
            holder.tvNomor.text = siswa.id.toString()
            holder.tvNama.text = siswa.nama
            holder.tvNisn.text = siswa.nisn
            holder.tvKelasJurusan.text = "${siswa.kelas} ${siswa.jurusan}"
        }

        override fun getItemCount(): Int = filteredList.size

        fun filter(query: String) {
            filteredList = if (query.isEmpty()) {
                dataList
            } else {
                dataList.filter {
                    it.nama.contains(query, ignoreCase = true) ||
                            it.nisn.contains(query, ignoreCase = true) ||
                            it.kelas.contains(query, ignoreCase = true) ||
                            it.jurusan.contains(query, ignoreCase = true)
                }
            }
            notifyDataSetChanged()
        }

        fun updateData(newData: List<SiswaRekap>) {
            dataList = newData
            filteredList = newData
            notifyDataSetChanged()
        }
    }
}