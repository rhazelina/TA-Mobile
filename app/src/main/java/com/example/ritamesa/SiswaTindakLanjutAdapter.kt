package com.example.ritamesa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class SiswaTindakLanjutAdapter(
    private val dataList: List<Map<String, Any>>
) : RecyclerView.Adapter<SiswaTindakLanjutAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "SiswaTindakLanjutAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNamaSiswa: TextView = view.findViewById(R.id.text_nama_siswa)
        val txtJurusan: TextView = view.findViewById(R.id.text_jurusan)
        val txtAlpha: TextView = view.findViewById(R.id.tvalpha)
        val txtIzin: TextView = view.findViewById(R.id.tvizin)
        val txtSakit: TextView = view.findViewById(R.id.tvsakit)
        val txtBadgeStatus: TextView = view.findViewById(R.id.tvBadgeStatus)

        // Tambahkan variabel untuk layout badge
        val badgeAlphaLayout: View = view.findViewById(R.id.badge_alpha)
        val badgeIzinLayout: View = view.findViewById(R.id.badge_izin)
        val badgeSakitLayout: View = view.findViewById(R.id.badge_sakit)
        val badgeStatusLayout: View = view.findViewById(R.id.badge_sering_absensi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_siswa_ditindak, parent, false)
            return ViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Error inflating layout: ${e.message}")
            throw RuntimeException("Layout file not found: item_siswa_ditindak", e)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val data = dataList.getOrNull(position) ?: return

            // Set data utama dengan null safety
            holder.txtNamaSiswa.text = data.getOrDefault("nama", "Nama Tidak Diketahui").toString()
            holder.txtJurusan.text = data.getOrDefault("kelasJurusan", "Kelas Tidak Diketahui").toString()

            // Set jumlah absensi dengan default 0 jika null
            val alphaCount = (data.getOrDefault("alphaCount", 0) as? Int) ?: 0
            val izinCount = (data.getOrDefault("izinCount", 0) as? Int) ?: 0
            val sakitCount = (data.getOrDefault("sakitCount", 0) as? Int) ?: 0

            // Tampilkan text untuk masing-masing badge jika ada
            holder.txtAlpha.text = if (alphaCount > 0) "x$alphaCount Alpha" else ""
            holder.txtIzin.text = if (izinCount > 0) "x$izinCount Izin" else ""
            holder.txtSakit.text = if (sakitCount > 0) "x$sakitCount Sakit" else ""

            // Atur visibility badge KIRI (alpha, izin, sakit) berdasarkan jumlah
            // Jika jumlah = 0, maka badge disembunyikan
            holder.badgeAlphaLayout.visibility = if (alphaCount > 0) View.VISIBLE else View.GONE
            holder.badgeIzinLayout.visibility = if (izinCount > 0) View.VISIBLE else View.GONE
            holder.badgeSakitLayout.visibility = if (sakitCount > 0) View.VISIBLE else View.GONE

            // Set badge KANAN (status) dengan null safety
            val badgeText = data.getOrDefault("badgeText", "Status Tidak Diketahui").toString()
            holder.txtBadgeStatus.text = badgeText

            // Set background badge kanan dengan default jika tidak ada
            val badgeDrawable = try {
                data.getOrDefault("badgeDrawable", R.drawable.box_success) as Int
            } catch (e: Exception) {
                R.drawable.box_success
            }
            holder.badgeStatusLayout.setBackgroundResource(badgeDrawable)

            // Tampilkan badge kanan hanya jika perlu
            val showBadge = data.getOrDefault("showBadge", true) as? Boolean ?: true
            holder.badgeStatusLayout.visibility = if (showBadge) View.VISIBLE else View.GONE
            holder.txtBadgeStatus.visibility = if (showBadge) View.VISIBLE else View.GONE

        } catch (e: Exception) {
            Log.e(TAG, "Error binding data at position $position: ${e.message}")

            // Set default values jika error
            holder.txtNamaSiswa.text = "Error"
            holder.txtJurusan.text = "Error"
            holder.txtBadgeStatus.text = "Error"
            holder.badgeStatusLayout.setBackgroundResource(R.drawable.box_success)

            // Sembunyikan semua badge kiri jika error
            holder.badgeAlphaLayout.visibility = View.GONE
            holder.badgeIzinLayout.visibility = View.GONE
            holder.badgeSakitLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = dataList.size
}