package com.example.ritamesa

data class RiwayatAbsenItem(
    val id: Int,
    val namaSiswa: String,
    val jurusan: String,
    val tanggal: String,
    val waktu: String,
    val status: String // "hadir", "izin", "sakit", "alpha"
)