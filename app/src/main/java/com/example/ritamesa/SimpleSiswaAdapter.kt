package com.example.ritamesa

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class SimpleSiswaAdapter(
    private val context: Context,
    private val dataList: List<Map<String, Any>>
) : RecyclerView.Adapter<SimpleSiswaAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "SimpleSiswaAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSubjudul: TextView? = view.findViewById(R.id.txt_subjudul)
        val txtMapel: TextView? = view.findViewById(R.id.txt_mata_pelajaran)
        val txtKeterangan: TextView? = view.findViewById(R.id.txt_keterangan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.riwayat_kehadiran_siswa_wakel, parent, false)
            ViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Error inflating layout: ${e.message}")
            // Fallback ke layout minimal
            val view = View(parent.context)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (position < 0 || position >= dataList.size) return

            val data = dataList[position]

            holder.txtSubjudul?.text =
                "${data.getOrDefault("nama", "N/A")}/${data.getOrDefault("jurusan", "N/A")}"
            holder.txtMapel?.text =
                data.getOrDefault("mapelGuru", "Mata Pelajaran Tidak Tersedia").toString()
            holder.txtKeterangan?.text =
                "Keterangan: ${data.getOrDefault("keterangan", "Tidak ada keterangan")}"
        } catch (e: Exception) {
            Log.e(TAG, "Error binding data at position $position: ${e.message}")
            holder.txtSubjudul?.text = "Error"
            holder.txtMapel?.text = "Error"
            holder.txtKeterangan?.text = "Error"
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: List<Map<String, Any>>) {
        (this as? RecyclerView.Adapter<ViewHolder>)?.notifyDataSetChanged()
    }
}