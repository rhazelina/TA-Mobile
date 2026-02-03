package com.example.ritamesa

data class SiswaRekap(
    val id: Int,
    val nama: String,
    val nisn: String,
    val kelas: String,
    val jurusan: String
) {
    fun getKelasJurusan(): String {
        return "$kelas $jurusan"
    }
}