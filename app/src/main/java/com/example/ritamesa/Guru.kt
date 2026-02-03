package com.example.ritamesa

data class Guru(
    val id: Int,
    val nama: String,
    val kode: String,
    val nip: String,
    val mapel: String,
    val keterangan: String  // JANGAN diubah jadi role, biarkan keterangan
)