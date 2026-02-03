package com.example.ritamesa

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class SimpleGuruAdapter(
    private val context: Context,
    private val dataList: List<Map<String, Any>>
) : RecyclerView.Adapter<SimpleGuruAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "SimpleGuruAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMapel: TextView? = view.findViewById(R.id.text_mapel)
        val txtKelas: TextView? = view.findViewById(R.id.kelas_jurusan)
        val txtStatus: TextView? = view.findViewById(R.id.status_riwayat)
        val txtTanggal: TextView? = view.findViewById(R.id.tanggal_waktu)
        val imgBadge: ImageView? = view.findViewById(R.id.imageView4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_riwayat_guru, parent, false)
            ViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Critical: Layout file not found: ${e.message}")
            // Buat view minimal untuk menghindari crash
            val view = View(parent.context)
            view.setBackgroundColor(0xFFF0F0F0.toInt())
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (position < 0 || position >= dataList.size) {
                Log.w(TAG, "Invalid position: $position")
                return
            }

            val data = dataList[position]

            holder.txtMapel?.text = "${data.getOrDefault("mapel", "Mata Pelajaran")} - "
            holder.txtKelas?.text = data.getOrDefault("kelas", "Kelas Tidak Diketahui").toString()
            holder.txtStatus?.text = data.getOrDefault("status", "Status Tidak Diketahui").toString()
            holder.txtTanggal?.text = data.getOrDefault("tanggal", "Tanggal Tidak Diketahui").toString()

            // Handle badge dengan try-catch terpisah
            try {
                val badgeResId = when (data.getOrDefault("statusType", "sakit")) {
                    "hadir" -> R.drawable.pin_hadir
                    "sakit" -> R.drawable.pin_sakit
                    "izin" -> R.drawable.pin_izin
                    "alpha" -> R.drawable.pin_alpha
                    else -> R.drawable.pin_sakit
                }
                holder.imgBadge?.setImageResource(badgeResId)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot load badge image: ${e.message}")
                // Biarkan null/tidak set gambar
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding data at position $position: ${e.message}")
            holder.txtMapel?.text = "Error"
            holder.txtKelas?.text = "Error"
            holder.txtStatus?.text = "Error"
            holder.txtTanggal?.text = "Error"
        }
    }

    override fun getItemCount(): Int = dataList.size
}