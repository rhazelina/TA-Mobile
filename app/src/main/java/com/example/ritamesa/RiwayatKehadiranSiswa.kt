package com.example.ritamesa

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.network.ApiClient
import com.example.ritamesa.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RiwayatKehadiranSiswa : AppCompatActivity() {

    // ===== DATA CLASS =====
    data class KehadiranItem(
        val id: Int,
        val nama: String,
        val role: String, // "Guru", "Wali Kelas", "Siswa"
        val status: String, // "hadir", "terlambat", "izin", "sakit", "alpha"
        val waktu: String,
        val tanggal: String,
        val keterangan: String,
        val statusDetail: String // "Tepat Waktu", "Terlambat", "Izin", "Sakit", "Alpha"
    )

    // ===== ADAPTER =====
    inner class KehadiranAdapter(
        private val listKehadiran: List<KehadiranItem>
    ) : RecyclerView.Adapter<KehadiranAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textTanggal: TextView = view.findViewById(R.id.text_tanggal)
            val textWaktu: TextView = view.findViewById(R.id.text_waktu)
            val textNama: TextView = view.findViewById(R.id.text_nama)
            val textRole: TextView = view.findViewById(R.id.text_role)
            val textStatus: TextView = view.findViewById(R.id.text_status)
            val textKeterangan: TextView = view.findViewById(R.id.text_keterangan)
            val rootView: View = view.findViewById(R.id.root_item)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_kehadiran, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = listKehadiran[position]

            holder.textTanggal.text = item.tanggal
            holder.textWaktu.text = item.waktu
            holder.textNama.text = item.nama
            holder.textRole.text = item.role
            holder.textStatus.text = item.statusDetail
            holder.textKeterangan.text = item.keterangan

            // Set warna teks status
            holder.textStatus.setTextColor(resources.getColor(android.R.color.black))

            // Set onClick untuk item - TANPA POP UP
            holder.rootView.setOnClickListener {
                // Hanya tampilkan Toast singkat, tanpa pop-up
                Toast.makeText(
                    this@RiwayatKehadiranSiswa,
                    "${item.nama}: ${item.statusDetail}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemCount(): Int = listKehadiran.size
    }

    // ===== COMPONENTS =====
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotalTitle: TextView
    private lateinit var tvTotalNumber: TextView
    private lateinit var tvHadirValue: TextView
    private lateinit var tvTerlambatValue: TextView
    private lateinit var tvIzinValue: TextView
    private lateinit var tvSakitValue: TextView
    private lateinit var tvAlphaValue: TextView
    private lateinit var textStatus: TextView
    private lateinit var textRole: TextView
    private lateinit var textTanggal: TextView
    private lateinit var btnDropdownStatus: ImageButton
    private lateinit var btnDropdownRole: ImageButton
    private lateinit var btnCalendar: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenu: ImageButton

    // ===== STATE =====
    private var currentStatus = "Semua"
    private var currentRole = "Semua"
    private var currentDate = Calendar.getInstance().apply {
        set(2026, 0, 7) // 7 Januari 2026
    }

    // ===== DUMMY DATA =====
    private val semuaKehadiran = listOf(
        KehadiranItem(1, "Nama Guru", "Guru", "hadir", "07:00",
            "Senin, 7 Januari 2026", "Hadir", "Tepat Waktu"),
        KehadiranItem(2, "Nama Guru", "Guru", "terlambat", "07:50",
            "Senin, 7 Januari 2026", "Hadir", "Terlambat"),
        KehadiranItem(3, "Nama Guru", "Wali Kelas", "izin", "07:00",
            "Senin, 7 Januari 2026", "Rapat bersama", "Izin"),
        KehadiranItem(4, "Nama Guru", "Wali Kelas", "sakit", "07:00",
            "Senin, 7 Januari 2026", "Demam", "Sakit"),
        KehadiranItem(5, "Nama Guru", "Guru", "alpha", "07:00",
            "Senin, 7 Januari 2026", "-", "Alpha"),
        KehadiranItem(6, "Ahmad Rizki", "Siswa", "hadir", "07:00",
            "Senin, 7 Januari 2026", "Hadir", "Tepat Waktu"),
        KehadiranItem(7, "Budi Santoso", "Siswa", "terlambat", "07:50",
            "Senin, 7 Januari 2026", "Hadir", "Terlambat"),
        KehadiranItem(8, "Citra Dewi", "Siswa", "izin", "07:00",
            "Senin, 7 Januari 2026", "Acara keluarga", "Izin"),
        KehadiranItem(9, "Dedi Kurniawan", "Siswa", "sakit", "07:00",
            "Senin, 7 Januari 2026", "Flu", "Sakit"),
        KehadiranItem(10, "Eka Putri", "Siswa", "alpha", "07:00",
            "Senin, 7 Januari 2026", "-", "Alpha")
    // List data yang ditampilkan
    private val dataKehadiran = mutableListOf<KehadiranItem>()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.riwayat_kehadiran_siswa)
        
        sessionManager = SessionManager(this)

        initViews()
        setupRecyclerView()
        setupFilters()
        setupNavigation()
        
        // Load default filter (Bulan ini)
        filterData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvRiwayatKehadiran)
        tvTotalTitle = findViewById(R.id.tvTotalTitle)
        tvTotalNumber = findViewById(R.id.tvTotalNumber)
        tvHadirValue = findViewById(R.id.tvHadirValue)
        tvTerlambatValue = findViewById(R.id.tvTerlambatValue)
        tvIzinValue = findViewById(R.id.tvIzinValue)
        tvSakitValue = findViewById(R.id.tvSakitValue)
        tvAlphaValue = findViewById(R.id.tvAlphaValue)

        textStatus = findViewById(R.id.textStatus)
        textRole = findViewById(R.id.textRole)
        textTanggal = findViewById(R.id.textTanggal)
        btnDropdownStatus = findViewById(R.id.btnDropdownStatus)
        btnDropdownRole = findViewById(R.id.btnDropdownRole)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.btnMenu)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        textTanggal.text = dateFormat.format(currentDate.time)

        // Set role default ke "Semua"
        textRole.text = "Semua"
        tvTotalTitle.text = "Total Data"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
    }

    private fun setupFilters() {
        // 1. FILTER STATUS
        btnDropdownStatus.setOnClickListener { showStatusPopupMenu() }
        textStatus.setOnClickListener { showStatusPopupMenu() }

        // 2. FILTER ROLE
        btnDropdownRole.setOnClickListener { showRolePopupMenu() }
        textRole.setOnClickListener { showRolePopupMenu() }

        // 3. FILTER TANGGAL
        btnCalendar.setOnClickListener { showDatePickerDialog() }
        textTanggal.setOnClickListener { showDatePickerDialog() }
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener { finish() }
        btnMenu.setOnClickListener { showExportImportMenu() }
    }

    private fun showStatusPopupMenu() {
        val statusList = arrayOf("Semua", "Hadir", "Terlambat", "Izin", "Sakit", "Alpha")

        AlertDialog.Builder(this)
            .setTitle("Pilih Status Kehadiran")
            .setItems(statusList) { _, which ->
                val selected = statusList[which]
                textStatus.text = selected
                currentStatus = selected
                filterData()
                Toast.makeText(this, "Filter status: $selected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showRolePopupMenu() {
        val roleList = arrayOf("Semua", "Guru", "Siswa")

        AlertDialog.Builder(this)
            .setTitle("Pilih Peran")
            .setItems(roleList) { _, which ->
                val selected = roleList[which]
                textRole.text = selected
                currentRole = selected

                // Update judul total
                if (selected == "Semua") {
                    tvTotalTitle.text = "Total Data"
                } else {
                    tvTotalTitle.text = "Total $selected"
                }

                filterData()
                Toast.makeText(this, "Filter peran: $selected", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showDatePickerDialog() {
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                currentDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                textTanggal.text = dateFormat.format(currentDate.time)
                filterData()
                Toast.makeText(this, "Filter tanggal: ${textTanggal.text}", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showExportImportMenu() {
        val popupMenu = PopupMenu(this, btnMenu)
        val menu = popupMenu.menu

        menu.add("📤 Export ke Excel")
        menu.add("📥 Import dari Excel")
        menu.add("🖨️ Print Laporan")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "📤 Export ke Excel" ->
                    Toast.makeText(this, "Export data ke Excel", Toast.LENGTH_SHORT).show()
                "📥 Import dari Excel" ->
                    Toast.makeText(this, "Import data dari Excel", Toast.LENGTH_SHORT).show()
                "🖨️ Print Laporan" ->
                    Toast.makeText(this, "Mencetak laporan", Toast.LENGTH_SHORT).show()
            }
            true
        }
        popupMenu.show()
    }

    private fun filterData() {
        // Use API to fetch data
        val month = currentDate.get(Calendar.MONTH) + 1
        val year = currentDate.get(Calendar.YEAR)
        
        Toast.makeText(this, "Memuat data...", Toast.LENGTH_SHORT).show()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@RiwayatKehadiranSiswa)
                val response = apiService.getMyAttendance(month, year)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        
                        dataKehadiran.clear()
                        result.data.forEach { record ->
                            dataKehadiran.add(
                                KehadiranItem(
                                    id = record.id,
                                    nama = sessionManager.getUserRole() ?: "Siswa", // Self
                                    role = "Siswa",
                                    status = record.status.lowercase(),
                                    waktu = record.check_in_time ?: "-",
                                    tanggal = record.date,
                                    keterangan = record.status_label,
                                    statusDetail = record.status_label
                                )
                            )
                        }
                        
                        // Apply status filter locally if needed, or update API to support status
                        val filtered = if (currentStatus != "Semua") {
                            dataKehadiran.filter { it.statusDetail.equals(currentStatus, true) || it.status.equals(currentStatus, true) }
                        } else {
                            dataKehadiran
                        }
                        
                        recyclerView.adapter = KehadiranAdapter(filtered)
                        updateStatistics(filtered)
                        
                        Toast.makeText(this@RiwayatKehadiranSiswa, "Data dimuat", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RiwayatKehadiranSiswa, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RiwayatKehadiranSiswa, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateStatistics(filteredList: List<KehadiranItem>) {
        val total = filteredList.size
        tvTotalNumber.text = total.toString()

        val hadir = filteredList.count { it.status.equals("hadir", true) || it.status.equals("present", true) }
        val terlambat = filteredList.count { it.status.equals("terlambat", true) || it.status.equals("late", true) }
        val izin = filteredList.count { it.status.equals("izin", true) || it.status.equals("excused", true) }
        val sakit = filteredList.count { it.status.equals("sakit", true) || it.status.equals("sick", true) }
        val alpha = filteredList.count { it.status.equals("alpha", true) || it.status.equals("absent", true) }
        
        tvHadirValue.text = hadir.toString()
        tvTerlambatValue.text = terlambat.toString()
        tvIzinValue.text = izin.toString()
        tvSakitValue.text = sakit.toString()
        tvAlphaValue.text = alpha.toString()
    }
}