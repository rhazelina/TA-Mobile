package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotifikasiAdapter(
    private val dataList: List<Map<String, Any>>,
    private val isGuru: Boolean // true = Guru, false = Wali Kelas
) : RecyclerView.Adapter<NotifikasiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAlert: TextView = view.findViewById(R.id.text_alert)
        val txtJurusan: TextView = view.findViewById(R.id.text_jurusan)
        val txtJam: TextView = view.findViewById(R.id.jam_notifikasi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notifguru, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        // Ambil data
        val type = data["type"] as String
        val message = data["message"] as String
        val detail = data["detail"] as String
        val time = data["time"] as String
        val date = data["date"] as String

        // Tentukan teks berdasarkan role dan type
        val finalMessage = if (isGuru) {
            // Untuk Guru
            message
        } else {
            // Untuk Wali Kelas - sesuaikan kalimat jika perlu
            when (type) {
                "alpha_siswa" -> "Ada siswa alpha di kelas Anda"
                "izin_siswa" -> "Permohonan izin dari siswa"
                "sakit_siswa" -> "Ada siswa sakit di kelas Anda"
                "terlambat_siswa" -> "Ada siswa terlambat di kelas"
                else -> message
            }
        }

        val finalDetail = if (isGuru) {
            // Untuk Guru
            detail
        } else {
            // Untuk Wali Kelas - biasanya nama siswa dan kelas
            detail
        }

        // Set data ke views - TANPA MENGUBAH WARNA
        holder.txtAlert.text = finalMessage
        holder.txtJurusan.text = finalDetail
        holder.txtJam.text = time

        // HAPUS SEMUA KODE YANG MENGUBAH WARNA TEKS!
        // Biarkan warna sesuai dengan yang diatur di XML layout
        // WARNA AKAN OTOMATIS SESUAI XML (hitam/dark gray)
    }

    override fun getItemCount(): Int = dataList.size
}