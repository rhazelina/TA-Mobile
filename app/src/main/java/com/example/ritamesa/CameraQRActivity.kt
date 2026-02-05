package com.example.ritamesa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ritamesa.network.ApiClient
import com.example.ritamesa.network.ScanQRRequest
import com.example.ritamesa.network.SessionManager
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CameraQRActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CameraQRActivity"
        private const val CAMERA_PERMISSION_REQUEST = 100
        const val EXTRA_QR_RESULT = "qr_result"
    }

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var btnBack: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager

    private var isFlashOn = false
    private var isScanning = true
    private lateinit var barcodeCallback: BarcodeCallback

    // Location (dummy for now)
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_qr)

        sessionManager = SessionManager(this)

        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        initViews()
        setupCallback()
        setupCamera()
        setupButtonListeners()

        // Auto-simulate for testing (remove in production)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isScanning) {
                simulateQRScan()
            }
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
                handleQRScanResult(result.text)
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
            Log.e(TAG, "Camera error: ${e.message}")
        }
    }

    private fun simulateQRScan() {
        if (!isScanning) return
        isScanning = false

        progressBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE

            // Simulate JSON QR from backend
            val simulatedQR = """
                {
                    "token": "simulated-token-${System.currentTimeMillis()}",
                    "type": "student",
                    "schedule_id": 1
                }
            """.trimIndent()

            handleQRScanResult(simulatedQR)
        }, 1500)
    }

    private fun handleQRScanResult(qrData: String) {
        try {
            // Try to parse as JSON (backend format)
            val json = JSONObject(qrData)
            val token = json.getString("token")

            Log.d(TAG, "QR Token: $token")

            // Send to backend
            scanAttendance(token)

        } catch (e: Exception) {
            // Fallback: treat as plain token or old format
            Log.w(TAG, "QR not JSON, treating as token: $qrData")

            // Check if old format (ABSENSI|...)
            if (qrData.startsWith("ABSENSI|")) {
                handleOldFormatQR(qrData)
            } else {
                // Treat as plain token
                scanAttendance(qrData)
            }
        }
    }

    private fun scanAttendance(qrcodeToken: String) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@CameraQRActivity)
                val response = apiService.scanQR(
                    ScanQRRequest(
                        qrcode_token = qrcodeToken,
                        latitude = currentLatitude,
                        longitude = currentLongitude
                    )
                )

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!

                        // Vibrate on success
                        vibrate()

                        // Show success message
                        Toast.makeText(
                            this@CameraQRActivity,
                            "✅ ${result.message}\nStatus: ${result.status}",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate back to dashboard
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 2000)

                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Scan failed: $errorBody")

                        Toast.makeText(
                            this@CameraQRActivity,
                            "❌ Scan gagal: QR tidak valid atau kadaluarsa",
                            Toast.LENGTH_LONG
                        ).show()

                        // Allow retry
                        isScanning = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    Log.e(TAG, "Scan error: ${e.message}", e)
                    Toast.makeText(
                        this@CameraQRActivity,
                        "Error: ${e.message ?: "Tidak dapat terhubung ke server"}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Allow retry
                    isScanning = true
                }
            }
        }
    }

    private fun handleOldFormatQR(qrText: String) {
        // Old format: ABSENSI|Kelas|Mapel|Tanggal|Jam
        val qrParts = qrText.split("|")

        if (qrParts.size >= 4 && qrParts[0] == "ABSENSI") {
            val kelas = qrParts.getOrNull(1) ?: "-"
            val mapel = qrParts.getOrNull(2) ?: "-"
            val tanggal = qrParts.getOrNull(3) ?: "-"
            val jam = qrParts.getOrNull(4) ?: "-"

            // Navigate to old activity (if exists)
            try {
                val intent = Intent(this, AbsensiSiswaActivity::class.java).apply {
                    putExtra(AbsensiSiswaActivity.EXTRA_KELAS, kelas)
                    putExtra(AbsensiSiswaActivity.EXTRA_MAPEL, mapel)
                    putExtra(AbsensiSiswaActivity.EXTRA_TANGGAL, tanggal)
                    putExtra(AbsensiSiswaActivity.EXTRA_JAM, jam)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                isScanning = true
            }
        } else {
            Toast.makeText(this, "QR Code tidak valid", Toast.LENGTH_LONG).show()
            isScanning = true
        }
    }

    private fun vibrate() {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibrate error: ${e.message}")
        }
    }

    private fun setupButtonListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnFlash.setOnClickListener {
            toggleFlash()
        }

        // Long press to simulate scan (for testing)
        btnFlash.setOnLongClickListener {
            if (isScanning) {
                simulateQRScan()
            }
            true
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        try {
            if (isFlashOn) {
                barcodeView.setTorchOn()
                btnFlash.setImageResource(R.drawable.ic_flash_on)
            } else {
                barcodeView.setTorchOff()
                btnFlash.setImageResource(R.drawable.ic_flash_off)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Flash error: ${e.message}")
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginAwal::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            barcodeView.resume()
        } catch (e: Exception) {
            Log.e(TAG, "Resume error: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            barcodeView.pause()
            barcodeView.setTorchOff()
        } catch (e: Exception) {
            Log.e(TAG, "Pause error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            barcodeView.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Destroy error: ${e.message}")
        }
    }
}
