package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuruAdapter(
    private var guruList: List<Guru>,
    private val onEditClick: (Guru, Int) -> Unit,
    private val onDeleteClick: (Guru, Int) -> Unit
) : RecyclerView.Adapter<GuruAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.tvNo)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvKode: TextView = itemView.findViewById(R.id.tvKode)
        val tvNIP: TextView = itemView.findViewById(R.id.tvNIP)
        val tvMapel: TextView = itemView.findViewById(R.id.tvMapel)
        val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)

        // Tambahkan referensi ke tombol edit dan hapus
        val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        val btnHapus: View = itemView.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crud_guru, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guru = guruList[position]

        holder.tvNo.text = (position + 1).toString()
        holder.tvNama.text = guru.nama
        holder.tvKode.text = guru.kode
        holder.tvNIP.text = guru.nip
        holder.tvMapel.text = guru.mapel
        holder.tvKeterangan.text = guru.keterangan

        // Klik untuk edit pada tombol edit
        holder.btnEdit.setOnClickListener {
            onEditClick(guru, position)
        }

        // Klik untuk hapus pada tombol hapus
        holder.btnHapus.setOnClickListener {
            onDeleteClick(guru, position)
        }

        // Opsional: klik pada item untuk edit juga
        holder.itemView.setOnClickListener {
            onEditClick(guru, position)
        }
    }

    override fun getItemCount(): Int = guruList.size

    fun updateData(newList: List<Guru>) {
        guruList = newList
        notifyDataSetChanged()
    }
}