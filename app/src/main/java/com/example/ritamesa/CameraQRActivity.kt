package com.example.ritamesa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import android.widget.TextView

class CameraQRActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var btnBack: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var progressBar: ProgressBar

    private var isFlashOn = false
    private var isScanning = true

    private lateinit var barcodeCallback: BarcodeCallback

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        const val EXTRA_QR_RESULT = "qr_result"
        const val EXTRA_JADWAL_ID = "jadwal_id"
        const val EXTRA_MAPEL = "mata_pelajaran"
        const val EXTRA_KELAS = "kelas"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_qr)

        initViews()
        setupCallback()
        setupCamera()
        setupButtonListeners()

        Handler(Looper.getMainLooper()).postDelayed({
            simulateQRScan()
        }, 3000)
    }

    private fun initViews() {
        barcodeView = findViewById(R.id.barcode_scanner)
        btnBack = findViewById(R.id.btn_back_camera)
        btnFlash = findViewById(R.id.btn_flash)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupCallback() {
        barcodeCallback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (!isScanning) return
                isScanning = false
                handleQRResult(result.text)
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            }
        }
    }

    private fun setupCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        try {
            barcodeView.decodeContinuous(barcodeCallback)
            barcodeView.resume()
        } catch (e: Exception) {
            simulateQRScan()
        }
    }

    private fun simulateQRScan() {
        if (!isScanning) return
        isScanning = false

        progressBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE

            val simulatedQRData = "ABSENSI|${getRandomKelas()}|${getRandomMapel()}|${getCurrentDate()}|${getCurrentTime()}"
            handleQRResult(simulatedQRData)
        }, 1500)
    }

    private fun handleQRResult(qrText: String) {
        progressBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE

            val qrParts = qrText.split("|")

            if (qrParts.size >= 4 && qrParts[0] == "ABSENSI") {

                val kelas = qrParts.getOrNull(1) ?: "-"
                val mapel = qrParts.getOrNull(2) ?: "-"
                val tanggal = qrParts.getOrNull(3) ?: "-"
                val jam = qrParts.getOrNull(4) ?: "-"

                val intent = Intent(this@CameraQRActivity, AbsensiSiswaActivity::class.java).apply {
                    putExtra(EXTRA_KELAS, kelas)
                    putExtra(EXTRA_MAPEL, mapel)
                    putExtra("tanggal", tanggal)
                    putExtra("jam", jam)
                }
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "QR Code tidak valid: $qrText", Toast.LENGTH_LONG).show()
                isScanning = true
                Handler(Looper.getMainLooper()).postDelayed({
                    simulateQRScan()
                }, 2000)
            }
        }, 1500)
    }

    private fun setupButtonListeners() {
        btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnFlash.setOnClickListener {
            toggleFlash()
        }

        btnFlash.setOnLongClickListener {
            simulateQRScan()
            true
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        if (isFlashOn) {
            try {
                barcodeView.setTorchOn()
            } catch (e: Exception) {
            }
            btnFlash.setImageResource(R.drawable.ic_flash_on)
        } else {
            try {
                barcodeView.setTorchOff()
            } catch (e: Exception) {
            }
            btnFlash.setImageResource(R.drawable.ic_flash_off)
        }
    }

    private fun getRandomKelas(): String {
        val kelasList = listOf(
            "XI RPL 1", "XI RPL 2", "XI RPL 3",
            "XI Mekatronika 1", "XI Mekatronika 2",
            "XI TKJ 1", "XI TKJ 2",
            "XI DKV 1", "XI DKV 2",
            "XI Animasi 1", "XI Animasi 2"
        )
        return kelasList.random()
    }

    private fun getRandomMapel(): String {
        val mapelList = listOf(
            "Matematika", "Bahasa Indonesia", "Pemrograman Dasar",
            "Basis Data", "Fisika", "Kimia", "Sejarah",
            "Seni Budaya", "PJOK", "Bahasa Inggris", "PKN"
        )
        return mapelList.random()
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_LONG).show()
            simulateQRScan()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            barcodeView.resume()
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            barcodeView.pause()
            barcodeView.setTorchOff()
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            barcodeView.pause()
        } catch (e: Exception) {
        }
    }
}
