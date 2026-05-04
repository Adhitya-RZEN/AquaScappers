package com.example.aquascappers.Sqlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aquascappers.R
import android.view.ContextThemeWrapper

/**
 * Adapter untuk menampilkan daftar log tangki dalam RecyclerView, termasuk menu popup untuk opsi update dan delete.
 */
class LogAdapter(
    private val logList: ArrayList<TankLog>,
    private val onUpdateClick: (TankLog) -> Unit,
    private val onDeleteClick: (TankLog) -> Unit
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvItemDesc)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val currentLog = logList[position]
        holder.tvTitle.text = "${currentLog.title} (pH: ${currentLog.phLevel})"
        holder.tvDesc.text = currentLog.description

        holder.ivMore.setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(view.context, R.style.GacorPopupMenu)
            val popup = PopupMenu(wrapper, view)

            popup.menu.add("Update")
            popup.menu.add("Delete")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Update" -> {
                        onUpdateClick(currentLog)
                        true
                    }
                    "Delete" -> {
                        onDeleteClick(currentLog)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return logList.size
    }
}