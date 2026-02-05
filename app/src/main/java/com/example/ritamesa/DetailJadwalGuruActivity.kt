package com.example.ritamesa

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class DetailJadwalGuruActivity : AppCompatActivity() {

    private lateinit var currentJadwal: DashboardGuruActivity.JadwalData

    // Untuk QR Scanner result
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val isSuccess = data?.getBooleanExtra(CameraQRActivity.EXTRA_QR_RESULT, false) ?: false

            if (isSuccess) {
                val kelas = data.getStringExtra(CameraQRActivity.EXTRA_KELAS) ?: "-"
                val mapel = data.getStringExtra(CameraQRActivity.EXTRA_MAPEL) ?: "-"
                val tanggal = data.getStringExtra("tanggal") ?: "-"
                val jam = data.getStringExtra("jam") ?: "-"

                Toast.makeText(
                    this,
                    "Absensi berhasil!\n$mapel - $kelas\n$tanggal $jam",
                    Toast.LENGTH_LONG
                ).show()

                // LANGSUNG KE ABSENSI SISWA ACTIVITY SETELAH QR SCAN SUKSES
                val intent = Intent(this, AbsensiSiswaActivity::class.java).apply {
                    putExtra(CameraQRActivity.EXTRA_MAPEL, mapel)
                    putExtra(CameraQRActivity.EXTRA_KELAS, kelas)
                    putExtra("tanggal", tanggal)
                    putExtra("jam", jam)
                }
                startActivity(intent)

            } else {
                Toast.makeText(this, "Gagal scan QR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_jadwal_guru)

        val jadwalData = intent.getSerializableExtra("JADWAL_DATA") as? DashboardGuruActivity.JadwalData

        // Inisialisasi view
        val tvNamaMapel: TextView = findViewById(R.id.text_nama_mapel)
        val tvKelas: TextView = findViewById(R.id.title_kelas)
        val tvTanggalWaktu: TextView = findViewById(R.id.tanggal_waktu_mulai)
        val tvMapelDetail: TextView = findViewById(R.id.txt_end_1)
        val tvKelasDetail: TextView = findViewById(R.id.txt_end_2)
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        val btnAbsensi: ImageButton = findViewById(R.id.btn_absensi)
        val btnTidakMengajar: ImageButton = findViewById(R.id.btn_tidak_mengajar)
        val btnIzinSakit: ImageButton = findViewById(R.id.btn_izin_sakit)
        val btnAjukanDispen: ImageButton = findViewById(R.id.btn_ajukan_dispen)

        // Set data dari intent
        jadwalData?.let {
            currentJadwal = it
            tvNamaMapel.text = it.mataPelajaran
            tvKelas.text = it.kelas
            tvTanggalWaktu.text = "${formatTanggalWaktu(it.jam)}"
            tvMapelDetail.text = it.mataPelajaran
            tvKelasDetail.text = it.kelas

            // Jumlah siswa random 30-40
            val txtJumlahSiswa: TextView = findViewById(R.id.txt_end_3)
            txtJumlahSiswa.text = (30..40).random().toString()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnAbsensi.setOnClickListener {
            showAbsensiPopup()
        }

        btnTidakMengajar.setOnClickListener {
            showTidakMengajarPopup()
        }

        btnIzinSakit.setOnClickListener {
            showIzinSakitPopup()
        }

        btnAjukanDispen.setOnClickListener {
            showDispensasiPopup()
        }

        // TAMBAHAN: Long click untuk langsung ke absensi (testing tanpa QR)
        btnAbsensi.setOnLongClickListener {
            // Langsung masuk ke absensi tanpa QR scan (untuk testing)
            val intent = Intent(this, AbsensiSiswaActivity::class.java).apply {
                putExtra("MATA_PELAJARAN", currentJadwal.mataPelajaran)
                putExtra("KELAS", currentJadwal.kelas)
                putExtra("JAM", currentJadwal.jam)
                putExtra("TANGGAL", SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
            }
            startActivity(intent)
            true
        }
    }

    private fun formatTanggalWaktu(jam: String): String {
        // Format: "07:30 - 08:15"
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())

        // Parse jam untuk format yang lebih baik
        return "$jam $tanggalSekarang"
    }

    private fun showAbsensiPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_absensi)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPindaiQR: Button = dialog.findViewById(R.id.btn_pindaiqr)
        val btnKembali: Button = dialog.findViewById(R.id.btn_kembali)

        btnPindaiQR.setOnClickListener {
            dialog.dismiss()
            startQRScanner()
        }

        btnKembali.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startQRScanner() {
        val intent = Intent(this, CameraQRActivity::class.java)
        qrScannerLauncher.launch(intent)
    }

    private fun showTidakMengajarPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_tidak_mengajar)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set data otomatis dari jadwal
        val tvNamaMapel: TextView = dialog.findViewById(R.id.tv_nama_mapel)
        val tvKelas: TextView = dialog.findViewById(R.id.tv_kelas)
        val inputKeterangan: EditText = dialog.findViewById(R.id.input_keterangan)
        val inputMapel: EditText = dialog.findViewById(R.id.input_mapel)
        val inputTanggal: EditText = dialog.findViewById(R.id.input_tanggal)
        val etCatatan: EditText = dialog.findViewById(R.id.et_catatan)
        val btnKirimIzin: Button = dialog.findViewById(R.id.btn_kirim_izin)
        val btnBatalIzin: Button = dialog.findViewById(R.id.btn_batal_izin)

        // Isi data otomatis
        tvNamaMapel.text = "${currentJadwal.mataPelajaran} - "
        tvKelas.text = currentJadwal.kelas

        // Format jam
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())

        inputMapel.setText(currentJadwal.jam)
        inputTanggal.setText(tanggalSekarang)

        // Setup dropdown keterangan (simulasi)
        inputKeterangan.setOnClickListener {
            showKeteranganDropdown(inputKeterangan)
        }

        // Setup dropdown mapel (simulasi)
        inputMapel.setOnClickListener {
            showJamMapelDropdown(inputMapel)
        }

        // Setup date picker (simulasi)
        inputTanggal.setOnClickListener {
            showDatePickerDialog(inputTanggal)
        }

        btnKirimIzin.setOnClickListener {
            val keterangan = inputKeterangan.text.toString()
            val catatan = etCatatan.text.toString()

            if (keterangan.isEmpty()) {
                Toast.makeText(this, "Harap pilih keterangan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulasi pengiriman
            val message = """
                Izin berhasil dikirim!
                
                Mata Pelajaran: ${currentJadwal.mataPelajaran}
                Kelas: ${currentJadwal.kelas}
                Keterangan: $keterangan
                Tanggal: ${inputTanggal.text}
                Jam: ${inputMapel.text}
                ${if (catatan.isNotEmpty()) "Catatan: $catatan" else ""}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Sukses")
                .setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        btnBatalIzin.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showIzinSakitPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_izin)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen
        val inputKeterangan: EditText = dialog.findViewById(R.id.input_keterangan)
        val inputJam: EditText = dialog.findViewById(R.id.input_jam)
        val inputTanggal: EditText = dialog.findViewById(R.id.input_tanggal)
        val etCatatan: EditText = dialog.findViewById(R.id.et_catatan)
        val btnKirimIzin: Button = dialog.findViewById(R.id.btn_kirim_izin)
        val btnBatalIzin: Button = dialog.findViewById(R.id.btn_batal_izin)

        // TAMBAH INI: ImageButton untuk dropdown
        val btnDropdownKeterangan: ImageButton = dialog.findViewById(R.id.btn_dropdown_arrow)

        // Isi data otomatis
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())

        inputJam.setText(currentJadwal.jam)
        inputTanggal.setText(tanggalSekarang)

        // Setup dropdown jam
        inputJam.setOnClickListener {
            showJamDropdown(inputJam)
        }

        // Setup date picker
        inputTanggal.setOnClickListener {
            showDatePickerDialog(inputTanggal)
        }

        // Setup dropdown keterangan untuk EditText (sudah ada)
        inputKeterangan.setOnClickListener {
            showKeteranganIzinDropdown(inputKeterangan)
        }

        // TAMBAH INI: Setup dropdown untuk ImageButton
        btnDropdownKeterangan.setOnClickListener {
            showKeteranganIzinDropdown(inputKeterangan)
        }

        btnKirimIzin.setOnClickListener {
            val keterangan = inputKeterangan.text.toString()
            val catatan = etCatatan.text.toString()

            if (keterangan.isEmpty()) {
                Toast.makeText(this, "Harap pilih keterangan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulasi pengiriman
            val message = """
            Izin/Sakit berhasil diajukan!
            
            Mata Pelajaran: ${currentJadwal.mataPelajaran}
            Kelas: ${currentJadwal.kelas}
            Keterangan: $keterangan
            Jam: ${inputJam.text}
            Tanggal: ${inputTanggal.text}
            ${if (catatan.isNotEmpty()) "Catatan: $catatan" else ""}
        """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Sukses")
                .setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        btnBatalIzin.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDispensasiPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.pop_up_dispensasi)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen
        val inputNamaSiswa: EditText = dialog.findViewById(R.id.input_keterangan)
        val inputJam: EditText = dialog.findViewById(R.id.input_jam)
        val inputTanggal: EditText = dialog.findViewById(R.id.input_tanggal)
        val etCatatan: EditText = dialog.findViewById(R.id.et_catatan)
        val btnKirimIzin: Button = dialog.findViewById(R.id.btn_kirim_izin)
        val btnBatalIzin: Button = dialog.findViewById(R.id.btn_batal_izin)

        // Isi data otomatis
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())

        inputJam.setText(currentJadwal.jam)
        inputTanggal.setText(tanggalSekarang)

        // Setup dropdown jam
        inputJam.setOnClickListener {
            showJamDropdown(inputJam)
        }

        // Setup date picker
        inputTanggal.setOnClickListener {
            showDatePickerDialog(inputTanggal)
        }

        // Setup data dummy siswa (dropdown)
        inputNamaSiswa.setOnClickListener {
            showSiswaDropdown(inputNamaSiswa)
        }

        btnKirimIzin.setOnClickListener {
            val namaSiswa = inputNamaSiswa.text.toString()
            val catatan = etCatatan.text.toString()

            if (namaSiswa.isEmpty()) {
                Toast.makeText(this, "Harap isi nama siswa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulasi pengiriman
            val message = """
                Dispensasi berhasil diajukan!
                
                Nama Siswa: $namaSiswa
                Mata Pelajaran: ${currentJadwal.mataPelajaran}
                Kelas: ${currentJadwal.kelas}
                Jam: ${inputJam.text}
                Tanggal Berlaku: ${inputTanggal.text}
                ${if (catatan.isNotEmpty()) "Catatan: $catatan" else ""}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Sukses")
                .setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        btnBatalIzin.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showKeteranganDropdown(editText: EditText) {
        val items = arrayOf("Sakit", "Izin", "Izin Pulang")

        AlertDialog.Builder(this)
            .setTitle("Pilih Keterangan")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showKeteranganIzinDropdown(editText: EditText) {
        val items = arrayOf("Sakit", "Izin", "Izin Pulang")

        AlertDialog.Builder(this)
            .setTitle("Pilih Keterangan")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showJamMapelDropdown(editText: EditText) {
        val items = arrayOf(
            currentJadwal.jam,
            "Tukar jam dengan guru lain",
            "Jam pengganti"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Jadwal")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showJamDropdown(editText: EditText) {
        // Parse jam dari format "07:30 - 08:15"
        val jamParts = currentJadwal.jam.split(" - ")
        val items = if (jamParts.size == 2) {
            arrayOf(
                currentJadwal.jam,
                "${jamParts[0]} - ${jamParts[1]} (Full)",
                "${jamParts[0]} (Awal)",
                "${jamParts[1]} (Akhir)"
            )
        } else {
            arrayOf(currentJadwal.jam)
        }

        AlertDialog.Builder(this)
            .setTitle("Pilih Jam Mapel")
            .setItems(items) { _, which ->
                editText.setText(items[which])
            }
            .show()
    }

    private fun showSiswaDropdown(editText: EditText) {
        val siswaList = arrayOf(
            "Fahmi - XI RPL 1",
            "Rizky - XI RPL 2",
            "Siti - XI TKJ 1",
            "Ahmad - XI Mekatronika 1",
            "Dewi - XI DKV 1",
            "Budi - XI Animasi 1",
            "Citra - XI RPL 3",
            "Eko - XI TKJ 2",
            "Fitri - XI Mekatronika 2",
            "Gunawan - XI DKV 2"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Siswa")
            .setItems(siswaList) { _, which ->
                editText.setText(siswaList[which])
            }
            .show()
    }

    private fun showGuruDropdown(editText: EditText) {
        val guruList = arrayOf(
            "Budi Santoso - Matematika",
            "Siti Rahayu - Bahasa Indonesia",
            "Ahmad Hidayat - IPA",
            "Dewi Lestari - Bahasa Inggris",
            "Joko Widodo - PPKN",
            "Rina Melati - Seni Budaya",
            "Eko Prasetyo - Olahraga",
            "Maya Sari - Sejarah",
            "Fajar Nugroho - Fisika",
            "Lina Marlina - Kimia"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih Guru")
            .setItems(guruList) { _, which ->
                editText.setText(guruList[which])
            }
            .show()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(Locale.getDefault(),
                    "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }
}