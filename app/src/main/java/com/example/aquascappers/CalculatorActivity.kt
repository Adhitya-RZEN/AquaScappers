package com.example.aquascappers

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val etLength = findViewById<EditText>(R.id.etLength)
        val etWidth = findViewById<EditText>(R.id.etWidth)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnCalculate.setOnClickListener {
            val lengthStr = etLength.text.toString()
            val widthStr = etWidth.text.toString()
            val heightStr = etHeight.text.toString()

            if (lengthStr.isEmpty() || widthStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "Masukkan semua ukuran dimensi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val length = lengthStr.toDouble()
            val width = widthStr.toDouble()
            val height = heightStr.toDouble()

            // Rumus volume: (P x L x T) / 1000 = Liter
            val volumeLiter = (length * width * height) / 1000

            // Asumsi kasar volume bersih: dipotong 15% untuk substrat dan hardscape
            val netVolume = volumeLiter * 0.85

            tvResult.text = "Volume Kotor: ${"%.2f".format(volumeLiter)} Liter\n\nEstimasi Volume Bersih (Air Saja): ${"%.2f".format(netVolume)} Liter"
        }
    }
}