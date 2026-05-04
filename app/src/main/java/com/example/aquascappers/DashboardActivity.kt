package com.example.aquascappers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

/**
 * Activity dashboard sebagai menu utama navigasi ke berbagai fitur aplikasi seperti Log Tangki, Kalkulator, dan lainnya.
 */
class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnMenuLogs = findViewById<CardView>(R.id.btnMenuLogs)
        val btnMenuCalculator = findViewById<CardView>(R.id.btnMenuCalculator)
        val btnMenuScheduler = findViewById<CardView>(R.id.btnMenuScheduler)
        val btnMenuDatabase = findViewById<CardView>(R.id.btnMenuDatabase)

        btnMenuLogs.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnMenuCalculator.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))
        }

        btnMenuScheduler.setOnClickListener {
            Toast.makeText(this, "Fitur Scheduler sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }

        btnMenuDatabase.setOnClickListener {
            Toast.makeText(this, "Fitur Katalog sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
        }
    }
}