package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RekapSiswaAdapter(
    private var siswaList: List<SiswaRekap>,
    private val onLihatClickListener: (SiswaRekap) -> Unit
) : RecyclerView.Adapter<RekapSiswaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNomor)           // PERBAIKAN: tvNomor bukan tvNo
        val tvNamaSiswa: TextView = itemView.findViewById(R.id.tvNama)     // PERBAIKAN: tvNama bukan tvNamaSiswa
        val tvNisn: TextView = itemView.findViewById(R.id.tvTelepon)       // PERBAIKAN: tvTelepon untuk NISN
        val tvKelasJurusan: TextView = itemView.findViewById(R.id.tvMataPelajaran) // PERBAIKAN: tvMataPelajaran untuk kelas/jurusan
        val btnLihat: ImageButton = itemView.findViewById(R.id.btnLihat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lihat_rekap_siswa, parent, false)  // PERBAIKAN: item_lihat_rekap_siswa
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siswa = siswaList[position]

        holder.tvNo.text = (position + 1).toString()
        holder.tvNamaSiswa.text = siswa.nama
        holder.tvNisn.text = siswa.nisn
        holder.tvKelasJurusan.text = siswa.getKelasJurusan()

        holder.btnLihat.setOnClickListener {
            onLihatClickListener(siswa)
        }

        // Optional: klik pada item juga membuka detail
        holder.itemView.setOnClickListener {
            onLihatClickListener(siswa)
        }
    }

    override fun getItemCount(): Int = siswaList.size

    fun updateData(newList: List<SiswaRekap>) {
        siswaList = newList
        notifyDataSetChanged()
    }

    fun filterData(query: String, originalList: List<SiswaRekap>) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.nama.contains(query, true) ||
                        it.nisn.contains(query, true) ||
                        it.jurusan.contains(query, true) ||
                        it.kelas.contains(query, true)
            }
        }
        updateData(filteredList)
    }
}