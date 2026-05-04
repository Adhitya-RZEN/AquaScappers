package com.example.aquascappers.Sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AquaStock.db"
        private const val DATABASE_VERSION = 3 // Versi dinaikkan
        private const val TABLE_NAME = "tank_logs"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TANK_ID = "tank_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESC = "description"
        private const val COLUMN_PH = "ph_level"
        private const val COLUMN_NH3 = "ammonia"
        private const val COLUMN_NO2 = "nitrite"
        private const val COLUMN_NO3 = "nitrate"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TANK_ID INTEGER,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_DESC TEXT,"
                + "$COLUMN_PH TEXT,"
                + "$COLUMN_NH3 TEXT,"
                + "$COLUMN_NO2 TEXT,"
                + "$COLUMN_NO3 TEXT,"
                + "$COLUMN_CREATED_AT TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // CREATE (Append-Only Logic)
    fun insertLog(log: TankLog): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, log.title)
            put(COLUMN_DESC, log.description)
            put(COLUMN_PH, log.phLevel)
            put(COLUMN_NH3, log.ammonia)
            put(COLUMN_NO2, log.nitrite)
            put(COLUMN_NO3, log.nitrate)
            put(COLUMN_CREATED_AT, getCurrentDateTime())
        }

        // Simpan log baru
        val insertedId = db.insert(TABLE_NAME, null, values)

        // Jika ini tangki baru (tankId == 0), jadikan ID row-nya sebagai tankId
        // Jika ini update (tankId > 0), gunakan tankId dari tangki lama
        val finalTankId = if (log.tankId == 0) insertedId.toInt() else log.tankId

        val updateValues = ContentValues().apply { put(COLUMN_TANK_ID, finalTankId) }
        db.update(TABLE_NAME, updateValues, "$COLUMN_ID=?", arrayOf(insertedId.toString()))

        db.close()
        return insertedId
    }

    // READ (HANYA MENGAMBIL LOG TERBARU UNTUK SETIAP TANGKI)
    fun getAllLatestLogs(): ArrayList<TankLog> {
        val logList = ArrayList<TankLog>()
        // Optimisasi: Menggunakan MAX(id) yang dikelompokkan berdasarkan tank_id
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID IN (SELECT MAX($COLUMN_ID) FROM $TABLE_NAME GROUP BY $COLUMN_TANK_ID) ORDER BY $COLUMN_ID DESC"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                logList.add(
                    TankLog(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        tankId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TANK_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC)),
                        phLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PH)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return logList
    }

    // READ HISTORY (OPTIMISASI: Dibatasi maksimal 20 data historis terakhir)
    fun getTankHistory(tankId: Int, limit: Int = 20): ArrayList<TankLog> {
        val historyList = ArrayList<TankLog>()
        val db = this.readableDatabase
        // Ambil data yang tank_id nya sama, urutkan dari terbaru, batasi (LIMIT)
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_TANK_ID = ? ORDER BY $COLUMN_ID DESC LIMIT ?", arrayOf(tankId.toString(), limit.toString()))

        if (cursor.moveToFirst()) {
            do {
                historyList.add(
                    TankLog(
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC)),
                        phLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PH)),
                        ammonia = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NH3)),
                        nitrite = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO2)),
                        nitrate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO3)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return historyList
    }

    // DELETE: Menghapus seluruh riwayat tangki tersebut
    fun deleteTank(tankId: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_NAME, "$COLUMN_TANK_ID=?", arrayOf(tankId.toString()))
        db.close()
        return success
    }
}