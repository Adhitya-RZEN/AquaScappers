package com.example.aquascappers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aquascappers.Sqlite.DatabaseHelper
import com.example.aquascappers.Sqlite.TankLog

/**
 * Activity untuk menambah log baru atau memperbarui log pada tangki yang sudah ada, 
 * dilengkapi dengan tampilan riwayat log sebelumnya.
 */
class AddLogActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var currentTankId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_log)

        dbHelper = DatabaseHelper(this)

        val etTitle = findViewById<EditText>(R.id.etTankName)
        val etPh = findViewById<EditText>(R.id.etPhLevel)
        val etNH2 = findViewById<EditText>(R.id.etNH2Level)
        val etNO2 = findViewById<EditText>(R.id.etNO2Level)
        val etNO3 = findViewById<EditText>(R.id.etNO3Level)
        val etDesc = findViewById<EditText>(R.id.etDescription)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val tvHistoryHeader = findViewById<TextView>(R.id.tvHistoryHeader)
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)

        if (intent.hasExtra("TANK_ID")) {
            currentTankId = intent.getIntExtra("TANK_ID", 0)
            etTitle.setText(intent.getStringExtra("LOG_TITLE"))

            etTitle.isEnabled = false
            etTitle.alpha = 0.5f

            btnSave.text = "TAMBAH LOG BARU"

            tvHistoryHeader.visibility = View.VISIBLE
            rvHistory.visibility = View.VISIBLE
            rvHistory.layoutManager = LinearLayoutManager(this)

            val historyLogs = dbHelper.getTankHistory(currentTankId)
            rvHistory.adapter = HistoryAdapter(historyLogs)
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val ph = etPh.text.toString().trim()
            val nh2 = etNH2.text.toString().trim().ifEmpty { "0" }
            val no2 = etNO2.text.toString().trim().ifEmpty { "0" }
            val no3 = etNO3.text.toString().trim().ifEmpty { "0" }
            val desc = etDesc.text.toString().trim()

            if (title.isEmpty() || ph.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Nama, pH, dan Deskripsi wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newLog = TankLog(
                tankId = currentTankId,
                title = title,
                description = desc,
                phLevel = ph,
                ammonia = nh2,
                nitrite = no2,
                nitrate = no3
            )

            val result = dbHelper.insertLog(newLog)
            if (result > -1) {
                Toast.makeText(this, "Log berhasil dicatat!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal mencatat log", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class HistoryAdapter(private val historyList: ArrayList<TankLog>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvHistDate)
            val tvParams: TextView = itemView.findViewById(R.id.tvHistParams)
            val tvDesc: TextView = itemView.findViewById(R.id.tvHistDesc)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val log = historyList[position]
            holder.tvDate.text = log.createdAt
            holder.tvParams.text = "pH: ${log.phLevel} | NH3: ${log.ammonia} | NO2: ${log.nitrite} | NO3: ${log.nitrate}"
            holder.tvDesc.text = log.description
        }
        override fun getItemCount() = historyList.size
    }
}