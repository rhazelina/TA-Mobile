package com.example.ritamesa

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DataSiswaDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_detail_siswa)

        val id = intent.getIntExtra("id", -1)
        val nama = intent.getStringExtra("nama") ?: "Tidak ada nama"

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Detail Siswa: $nama (ID: $id)"

        // Tambahkan kode untuk menampilkan detail siswa lainnya
    }
}