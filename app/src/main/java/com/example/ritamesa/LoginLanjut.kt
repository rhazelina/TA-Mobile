package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginLanjut : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginLanjut"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_lanjut)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLoginButton()
    }

    private fun setupLoginButton() {
        val edtUsername = findViewById<EditText>(R.id.edtNama)
        val edtPassword = findViewById<EditText>(R.id.edtPass)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)

        btnMasuk.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Masukkan username dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Login attempt: $username")

            try {
                when {
                    // ADMIN
                    username == "admin" && password == "admin123" -> {
                        Toast.makeText(this, "Login sebagai Admin...", Toast.LENGTH_SHORT).show()
                        navigateToAdminDashboard()
                    }

                    // GURU
                    username == "guru" && password == "guru123" -> {
                        Toast.makeText(this, "Login sebagai Guru...", Toast.LENGTH_SHORT).show()
                        navigateToGuruDashboard()
                    }

                    // WALI KELAS
                    username == "wali" && password == "wali123" -> {
                        Toast.makeText(this, "Login sebagai Wali Kelas...", Toast.LENGTH_SHORT).show()
                        navigateToWaliKelasDashboard()
                    }

                    // SISWA BIASA
                    username == "siswa" && password == "siswa123" -> {
                        Toast.makeText(this, "Login sebagai Siswa...", Toast.LENGTH_SHORT).show()
                        navigateToSiswaDashboard(false) // false = bukan pengurus
                    }

                    // SISWA PENGURUS KELAS
                    username == "pengurus" && password == "pengurus123" -> {
                        Toast.makeText(this, "Login sebagai Pengurus Kelas...", Toast.LENGTH_SHORT).show()
                        navigateToSiswaDashboard(true) // true = pengurus
                    }

                    // BACKUP CREDENTIALS (opsional)
                    username == "12345" && password == "guru123" -> {
                        Toast.makeText(this, "Login sebagai Guru...", Toast.LENGTH_SHORT).show()
                        navigateToGuruDashboard()
                    }
                    username == "54321" && password == "wakel123" -> {
                        Toast.makeText(this, "Login sebagai Wali Kelas...", Toast.LENGTH_SHORT).show()
                        navigateToWaliKelasDashboard()
                    }
                    username == "99999" && password == "admin999" -> {
                        Toast.makeText(this, "Login sebagai Admin...", Toast.LENGTH_SHORT).show()
                        navigateToAdminDashboard()
                    }

                    else -> {
                        Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show()
                        edtPassword.text.clear()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Login failed: ${e.message}", e)
            }
        }
    }

    private fun navigateToAdminDashboard() {
        try {
            Log.d(TAG, "Opening DashboardAdminActivity")
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard admin", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open admin dashboard: ${e.message}", e)
        }
    }

    private fun navigateToGuruDashboard() {
        try {
            Log.d(TAG, "Opening DashboardGuruActivity")
            val intent = Intent(this, DashboardGuruActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard guru", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open guru dashboard: ${e.message}", e)
        }
    }

    private fun navigateToWaliKelasDashboard() {
        try {
            Log.d(TAG, "Opening DashboardWaliKelasActivity")
            val intent = Intent(this, DashboardWaliKelasActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard wali kelas", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open wali kelas dashboard: ${e.message}", e)
        }
    }

    private fun navigateToSiswaDashboard(isPengurus: Boolean) {
        try {
            Log.d(TAG, "Opening DashboardSiswaActivity (Pengurus: $isPengurus)")
            val intent = Intent(this, DashboardSiswaActivity::class.java)
            intent.putExtra("IS_PENGURUS", isPengurus)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka dashboard siswa", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed to open siswa dashboard: ${e.message}", e)
        }
    }
}