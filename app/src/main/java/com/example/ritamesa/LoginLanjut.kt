package com.example.ritamesa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ritamesa.network.ApiClient
import com.example.ritamesa.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginLanjut : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginLanjut"
    }

    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_lanjut)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateBasedOnRole(sessionManager.getUserRole() ?: "student", sessionManager.isClassOfficer())
            return
        }

        setupLoginButton()
    }

    private fun setupLoginButton() {
        val edtUsername = findViewById<EditText>(R.id.edtNama)
        val edtPassword = findViewById<EditText>(R.id.edtPass)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        progressBar = ProgressBar(this) // You may need to add this to layout

        btnMasuk.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Masukkan username dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        // Disable button during login
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        btnMasuk.isEnabled = false
        
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getInstance(this@LoginLanjut)
                val response = apiService.login(
                    com.example.ritamesa.network.LoginRequest(
                        email = username, // Backend expect email, tapi bisa username juga
                        password = password,
                        device_name = "Android"
                    )
                )

                withContext(Dispatchers.Main) {
                    btnMasuk.isEnabled = true
                    
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!
                        
                        // Save token and user data
                        sessionManager.saveAuthToken(loginResponse.token)
                        sessionManager.saveUserData(
                            userId = loginResponse.user.id,
                            name = loginResponse.user.name,
                            email = loginResponse.user.email,
                            role = loginResponse.user.role,
                            isClassOfficer = loginResponse.user.is_class_officer ?: false
                        )
                        
                        Toast.makeText(
                            this@LoginLanjut,
                            "Login berhasil! Selamat datang ${loginResponse.user.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Navigate based on role
                        navigateBasedOnRole(
                            loginResponse.user.role,
                            loginResponse.user.is_class_officer ?: false
                        )
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Login failed: $errorBody")
                        Toast.makeText(
                            this@LoginLanjut,
                            "Login gagal: Username atau password salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnMasuk.isEnabled = true
                    Log.e(TAG, "Login error: ${e.message}", e)
                    Toast.makeText(
                        this@LoginLanjut,
                        "Error: ${e.message ?: "Tidak dapat terhubung ke server"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun navigateBasedOnRole(role: String, isClassOfficer: Boolean) {
        when (role.lowercase()) {
            "admin" -> navigateToAdminDashboard()
            "teacher" -> {
                // Check if wali kelas (homeroom teacher)
                // For now, navigate to guru dashboard
                // TODO: Check if teacher has homeroom_class_id
                navigateToGuruDashboard()
            }
            "student" -> {
                navigateToSiswaDashboard(isClassOfficer)
            }
            else -> {
                Toast.makeText(this, "Role tidak dikenali: $role", Toast.LENGTH_SHORT).show()
                navigateToSiswaDashboard(false)
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