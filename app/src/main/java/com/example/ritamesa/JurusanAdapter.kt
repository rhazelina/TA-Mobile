package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JurusanAdapter(
    private var jurusanList: List<Jurusan>,
    private val onEditClickListener: (Jurusan) -> Unit,
    private val onDeleteClickListener: (Jurusan) -> Unit
) : RecyclerView.Adapter<JurusanAdapter.JurusanViewHolder>() {

    class JurusanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvNamaJurusan: TextView = itemView.findViewById(R.id.tvKonsentrasi)  // INI SUDAH BENAR
        val tvKodeJurusan: TextView = itemView.findViewById(R.id.tvKode)         // INI SUDAH BENAR
        val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        val btnHapus: View = itemView.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JurusanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jurusan, parent, false)
        return JurusanViewHolder(view)
    }

    override fun onBindViewHolder(holder: JurusanViewHolder, position: Int) {
        val jurusan = jurusanList[position]

        holder.tvNo.text = (position + 1).toString()
        holder.tvNamaJurusan.text = jurusan.KonsentrasiKeahlian
        holder.tvKodeJurusan.text = jurusan.Kodejurusan

        holder.btnEdit.setOnClickListener {
            onEditClickListener(jurusan)
        }

        holder.btnHapus.setOnClickListener {
            onDeleteClickListener(jurusan)
        }
    }

    override fun getItemCount(): Int = jurusanList.size

    fun updateData(newList: List<Jurusan>) {
        jurusanList = newList
        notifyDataSetChanged()
    }
}