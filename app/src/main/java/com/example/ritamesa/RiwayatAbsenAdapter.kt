package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiwayatAbsenAdapter(
    private val riwayatList: List<RiwayatAbsenItem>
) : RecyclerView.Adapter<RiwayatAbsenAdapter.RiwayatViewHolder>() {

    class RiwayatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSubjudul: TextView = view.findViewById(R.id.txt_subjudul)
        val txtTanggal: TextView = view.findViewById(R.id.txt_tanggal)
        val txtWaktu: TextView = view.findViewById(R.id.txt_waktu)
        val imgStatus: ImageView = view.findViewById(R.id.img_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bar_riwayat_siswa_wakel, parent, false)
        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val riwayat = riwayatList[position]

        // Set data ke view
        holder.txtSubjudul.text = "${riwayat.namaSiswa} / ${riwayat.jurusan}"
        holder.txtTanggal.text = riwayat.tanggal
        holder.txtWaktu.text = "Pukul ${riwayat.waktu}"

        // Set image berdasarkan status
        val imageResource = when (riwayat.status.toLowerCase()) {
            "hadir" -> R.drawable.siswa_hadir_wakel
            "izin" -> R.drawable.siswa_izin_wakel
            "sakit" -> R.drawable.siswa_sakit_wakel
            "alpha" -> R.drawable.siswa_alpha_wakel
            else -> R.drawable.siswa_hadir_wakel
        }
        holder.imgStatus.setImageResource(imageResource)
    }

    override fun getItemCount(): Int {
        return riwayatList.size
    }
}