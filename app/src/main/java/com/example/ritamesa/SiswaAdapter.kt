package com.example.ritamesa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ritamesa.R
import com.example.ritamesa.model.ModelSiswa

class SiswaAdapter(
    private val listSiswa: List<ModelSiswa>
) : RecyclerView.Adapter<SiswaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvNis: TextView = itemView.findViewById(R.id.tvNisn)
        val tvKelas: TextView = itemView.findViewById(R.id.tvKelas)
        val tvJk: TextView = itemView.findViewById(R.id.tvJk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crud_datasiswa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siswa = listSiswa[position]
        holder.tvNama.text = siswa.nama
        holder.tvNis.text = siswa.nis
        holder.tvKelas.text = siswa.kelas
        holder.tvJk.text = siswa.jk
    }

    override fun getItemCount(): Int = listSiswa.size
}
