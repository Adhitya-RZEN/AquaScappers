package com.example.aquascappers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inisialisasi CardView dari Horizontal Toolbar
        val btnMenuLogs = findViewById<CardView>(R.id.btnMenuLogs)
        val btnMenuCalculator = findViewById<CardView>(R.id.btnMenuCalculator)
        val btnMenuScheduler = findViewById<CardView>(R.id.btnMenuScheduler)
        val btnMenuDatabase = findViewById<CardView>(R.id.btnMenuDatabase)

        // Navigasi ke Halaman Tank Logs (MainActivity)
        btnMenuLogs.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Navigasi ke Halaman Smart Tools / Kalkulator
        btnMenuCalculator.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))
        }

        // Placeholder untuk fitur Scheduler
        btnMenuScheduler.setOnClickListener {
            Toast.makeText(this, "Fitur Scheduler sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        // Placeholder untuk fitur Katalog Flora/Fauna
        btnMenuDatabase.setOnClickListener {
            Toast.makeText(this, "Fitur Katalog sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }
    }
}