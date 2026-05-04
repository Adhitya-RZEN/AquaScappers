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
        private const val DATABASE_VERSION = 4 // Naik versi karena skema berubah total

        // TABEL 1: tanks (Tabel Master untuk Identitas Tangki)
        private const val TABLE_TANKS = "tanks"
        private const val COLUMN_TANK_ID = "id"
        private const val COLUMN_TANK_TITLE = "title"
        private const val COLUMN_TANK_CREATED = "created_at"

        // TABEL 2: tank_logs (Tabel Transaksi untuk Historis Parameter Air)
        private const val TABLE_LOGS = "tank_logs"
        private const val COLUMN_LOG_ID = "id"
        private const val COLUMN_REF_TANK_ID = "tank_id" // Bertindak sebagai Foreign Key
        private const val COLUMN_DESC = "description"
        private const val COLUMN_PH = "ph_level"
        private const val COLUMN_NH3 = "ammonia"
        private const val COLUMN_NO2 = "nitrite"
        private const val COLUMN_NO3 = "nitrate"
        private const val COLUMN_LOG_CREATED = "created_at"
    }

    // Mengaktifkan dukungan Foreign Key di SQLite
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Buat Tabel Master (Tanks)
        val createTableTanks = ("CREATE TABLE $TABLE_TANKS ("
                + "$COLUMN_TANK_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TANK_TITLE TEXT,"
                + "$COLUMN_TANK_CREATED TEXT)")
        db.execSQL(createTableTanks)

        // 2. Buat Tabel Transaksi (Tank Logs) menggunakan tipe REAL (Desimal) dan Foreign Key
        // ON DELETE CASCADE memastikan jika tangki dihapus, seluruh riwayatnya ikut terhapus
        val createTableLogs = ("CREATE TABLE $TABLE_LOGS ("
                + "$COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_REF_TANK_ID INTEGER,"
                + "$COLUMN_DESC TEXT,"
                + "$COLUMN_PH REAL,"
                + "$COLUMN_NH3 REAL,"
                + "$COLUMN_NO2 REAL,"
                + "$COLUMN_NO3 REAL,"
                + "$COLUMN_LOG_CREATED TEXT,"
                + "FOREIGN KEY($COLUMN_REF_TANK_ID) REFERENCES $TABLE_TANKS($COLUMN_TANK_ID) ON DELETE CASCADE)")
        db.execSQL(createTableLogs)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TANKS")
        onCreate(db)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // CREATE: Menyimpan data menggunakan 2 Tabel
    fun insertLog(log: TankLog): Long {
        val db = this.writableDatabase
        val timeNow = getCurrentDateTime()
        var finalTankId = log.tankId

        // Langkah A: Jika ini tangki baru (tankId == 0), simpan dulu namanya di tabel 'tanks'
        if (finalTankId == 0) {
            val tankValues = ContentValues().apply {
                put(COLUMN_TANK_TITLE, log.title)
                put(COLUMN_TANK_CREATED, timeNow)
            }
            finalTankId = db.insert(TABLE_TANKS, null, tankValues).toInt()
        }

        // Langkah B: Simpan parameter air di tabel 'tank_logs'
        val logValues = ContentValues().apply {
            put(COLUMN_REF_TANK_ID, finalTankId)
            put(COLUMN_DESC, log.description)
            // Konversi dari String (input UI) ke REAL (Database) dengan nilai default 0.0 jika kosong
            put(COLUMN_PH, log.phLevel.toDoubleOrNull() ?: 0.0)
            put(COLUMN_NH3, log.ammonia.toDoubleOrNull() ?: 0.0)
            put(COLUMN_NO2, log.nitrite.toDoubleOrNull() ?: 0.0)
            put(COLUMN_NO3, log.nitrate.toDoubleOrNull() ?: 0.0)
            put(COLUMN_LOG_CREATED, timeNow)
        }
        db.insert(TABLE_LOGS, null, logValues)

        db.close()
        return finalTankId.toLong()
    }

    // READ: Menarik data dengan metode JOIN
    fun getAllLatestLogs(): ArrayList<TankLog> {
        val logList = ArrayList<TankLog>()
        val db = this.readableDatabase

        // Menggabungkan nama tangki (dari tabel tanks) dan parameter terbarunya (dari tabel tank_logs)
        val selectQuery = """
            SELECT t.$COLUMN_TANK_ID, t.$COLUMN_TANK_TITLE, 
                   l.$COLUMN_DESC, l.$COLUMN_PH, l.$COLUMN_NH3, l.$COLUMN_NO2, l.$COLUMN_NO3, l.$COLUMN_LOG_CREATED
            FROM $TABLE_TANKS t
            INNER JOIN $TABLE_LOGS l ON t.$COLUMN_TANK_ID = l.$COLUMN_REF_TANK_ID
            WHERE l.$COLUMN_LOG_ID IN (SELECT MAX($COLUMN_LOG_ID) FROM $TABLE_LOGS GROUP BY $COLUMN_REF_TANK_ID)
            ORDER BY l.$COLUMN_LOG_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                logList.add(
                    TankLog(
                        tankId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TANK_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANK_TITLE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC)),
                        // SQLite secara otomatis mengonversi tipe REAL kembali menjadi String untuk UI
                        phLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PH)),
                        ammonia = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NH3)),
                        nitrite = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO2)),
                        nitrate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO3)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_CREATED))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return logList
    }

    // READ HISTORY: Hanya menarik log tanpa perlu JOIN ke master
    fun getTankHistory(tankId: Int, limit: Int = 20): ArrayList<TankLog> {
        val historyList = ArrayList<TankLog>()
        val db = this.readableDatabase

        val selectQuery = """
            SELECT * FROM $TABLE_LOGS 
            WHERE $COLUMN_REF_TANK_ID = ? 
            ORDER BY $COLUMN_LOG_ID DESC 
            LIMIT ?
        """.trimIndent()

        val cursor = db.rawQuery(selectQuery, arrayOf(tankId.toString(), limit.toString()))

        if (cursor.moveToFirst()) {
            do {
                historyList.add(
                    TankLog(
                        title = "", // Kosongkan karena tidak perlu di-render ulang di list historis
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC)),
                        phLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PH)),
                        ammonia = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NH3)),
                        nitrite = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO2)),
                        nitrate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO3)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_CREATED))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return historyList
    }

    // DELETE: Otomatis menghapus seluruh riwayat berkat `ON DELETE CASCADE` di Foreign Key
    fun deleteTank(tankId: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_TANKS, "$COLUMN_TANK_ID=?", arrayOf(tankId.toString()))
        db.close()
        return success
    }
}