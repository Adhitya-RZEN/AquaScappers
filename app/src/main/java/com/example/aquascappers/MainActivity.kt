package com.example.aquascappers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aquascappers.Sqlite.DatabaseHelper
import com.example.aquascappers.Sqlite.LogAdapter
import com.example.aquascappers.Sqlite.TankLog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvLogs: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        rvLogs = findViewById(R.id.rvTankLogs)
        rvLogs.layoutManager = LinearLayoutManager(this)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddLogActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val logList = dbHelper.getAllLatestLogs() // Menggunakan metode baru
        logAdapter = LogAdapter(
            logList,
            onUpdateClick = { log ->
                val intent = Intent(this, AddLogActivity::class.java)
                intent.putExtra("TANK_ID", log.tankId) // Kirim TANK_ID, bukan ID biasa
                intent.putExtra("LOG_TITLE", log.title)
                startActivity(intent)
            },
            onDeleteClick = { log ->
                showDeleteDialog(log)
            }
        )
        rvLogs.adapter = logAdapter
    }

    private fun showDeleteDialog(log: TankLog) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setPositiveButton("Hapus") { _, _ ->
                val result = dbHelper.deleteTank(log.tankId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}