package com.example.ritamesa

object DataSource {
    fun getJurusanList(): List<Jurusan> {
        return listOf(
            Jurusan(1, "Mekatronika", "111"),
            Jurusan(2, "Rekaysa perangkat lunak", "222"),
            Jurusan(3, "Teknik komputer jaringan", "333"),
            Jurusan(4, "Desain komunikasi visual", "444"),
            Jurusan(5, "Elektronika industri", "555"),
            Jurusan(6, "Animasi", "666"),
            Jurusan(7, "Broadcasting", "777"),
            Jurusan(8, "Audio video", "Multimedia"),
        )
    }
}