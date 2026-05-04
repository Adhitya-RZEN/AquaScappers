package com.example.aquascappers.Sqlite

/**
 * Data class yang merepresentasikan entitas log tangki dalam database.
 */
data class TankLog(
    val id: Int = 0,
    val tankId: Int = 0,
    val title: String,
    val description: String,
    val phLevel: String,
    val ammonia: String = "0",
    val nitrite: String = "0",
    val nitrate: String = "0",
    val createdAt: String = ""
)