package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class JadwalHarianAdapter(
    private val jadwalList: List<JadwalHarianItem>,
    private val isPengurus: Boolean = false
) : RecyclerView.Adapter<JadwalHarianAdapter.JadwalViewHolder>() {

    inner class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // SESUAI DENGAN item_mapel_siswa.xml
        val tvMapel: TextView = itemView.findViewById(R.id.textmapelsiswa)
        val tvSesi: TextView = itemView.findViewById(R.id.session)
        val btnQr: ImageButton = itemView.findViewById(R.id.btn_qr_render)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mapel_siswa, parent, false)
        return JadwalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
        val jadwal = jadwalList[position]

        // SET DATA
        holder.tvMapel.text = jadwal.mataPelajaran
        holder.tvSesi.text = jadwal.sesi

        // TOMBOL QR: HANYA UNTUK PENGURUS
        if (isPengurus) {
            holder.btnQr.visibility = View.VISIBLE
            holder.btnQr.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "QR untuk ${jadwal.mataPelajaran}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            holder.btnQr.visibility = View.GONE
        }

        // CLICK LISTENER ITEM
        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "${jadwal.mataPelajaran} - ${jadwal.sesi}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int = jadwalList.size
}