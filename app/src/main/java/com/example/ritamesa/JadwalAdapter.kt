package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JadwalAdapter(
    private val jadwalList: List<DashboardGuruActivity.JadwalItem>,
    private val onItemClick: (DashboardGuruActivity.JadwalItem) -> Unit
) : RecyclerView.Adapter<JadwalAdapter.JadwalViewHolder>() {

    class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMataPelajaran: TextView = itemView.findViewById(R.id.MataPelajaran)
        val tvKelasJurusan: TextView = itemView.findViewById(R.id.KelasJurusan)
        val tvJam: TextView = itemView.findViewById(R.id.DurasiPembelajaran)
        val tvWaktuPelajaran: TextView = itemView.findViewById(R.id.txtLeftBottom_1)
        val btnTampilkan: ImageButton = itemView.findViewById(R.id.btnTampilkan_1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jadwal_guru, parent, false)
        return JadwalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
        val jadwal = jadwalList[position]

        holder.tvMataPelajaran.text = "${jadwal.mataPelajaran} - "
        holder.tvKelasJurusan.text = jadwal.kelas
        holder.tvJam.text = jadwal.jam

        // PERBAIKAN: Ganti "Pukul : ${jadwal.jam}" dengan waktuPelajaran yang bermakna
        holder.tvWaktuPelajaran.text = jadwal.waktuPelajaran

        holder.btnTampilkan.setOnClickListener {
            onItemClick(jadwal)
        }

        holder.itemView.setOnClickListener {
            onItemClick(jadwal)
        }
    }

    override fun getItemCount(): Int = jadwalList.size
}