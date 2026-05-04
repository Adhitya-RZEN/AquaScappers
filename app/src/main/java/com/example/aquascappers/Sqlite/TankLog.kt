package com.example.aquascappers.Sqlite

data class TankLog(
    val id: Int = 0,
    val tankId: Int = 0, // Identitas utama tangki
    val title: String,
    val description: String,
    val phLevel: String,
    val ammonia: String = "0",
    val nitrite: String = "0",
    val nitrate: String = "0",
    val createdAt: String = "" // Tanggal & Jam
)