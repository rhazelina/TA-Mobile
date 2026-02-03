package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AbsensiAdapter(
    private var siswaList: List<AbsensiAdapter.SiswaData>
) : RecyclerView.Adapter<AbsensiAdapter.ViewHolder>() {

    data class SiswaData(
        val id: Int,
        val nomor: Int,
        val nisn: String,
        val nama: String,
        var status: String
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.nomor_absen)
        val tvNISN: TextView = itemView.findViewById(R.id.nisn)
        val tvNama: TextView = itemView.findViewById(R.id.nama_siswa)
        val btnHadir: ImageButton = itemView.findViewById(R.id.radio_hadir)
        val btnAlpha: ImageButton = itemView.findViewById(R.id.radio_alpha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_absen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siswa = siswaList[position]

        holder.tvNomor.text = siswa.nomor.toString()
        holder.tvNISN.text = siswa.nisn
        holder.tvNama.text = siswa.nama

        updateRadioButtons(holder, siswa.status)

        holder.btnHadir.setOnClickListener {
            siswa.status = "hadir"
            updateRadioButtons(holder, "hadir")
            notifyItemChanged(position)
        }

        holder.btnAlpha.setOnClickListener {
            siswa.status = "alpha"
            updateRadioButtons(holder, "alpha")
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = siswaList.size

    private fun updateRadioButtons(holder: ViewHolder, status: String) {
        when (status) {
            "hadir" -> {
                holder.btnHadir.setImageResource(R.drawable.radiobutton_on)
                holder.btnAlpha.setImageResource(R.drawable.radiobutton_off)
            }
            "alpha" -> {
                holder.btnHadir.setImageResource(R.drawable.radiobutton_off)
                holder.btnAlpha.setImageResource(R.drawable.radiobutton_on)
            }
            else -> {
                holder.btnHadir.setImageResource(R.drawable.radiobutton_off)
                holder.btnAlpha.setImageResource(R.drawable.radiobutton_off)
            }
        }
    }

    fun getAbsensiData(): List<SiswaData> {
        return siswaList
    }

    fun resetAllStatus() {
        siswaList.forEach { it.status = "none" }
        notifyDataSetChanged()
    }
}
