package com.example.investmentmonitor.MOEX

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor
import android.util.Log

class TickerDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                position INTEGER NOT NULL,
                board TEXT NOT NULL,
                ticker TEXT NOT NULL,
                name TEXT,
                decimals INTEGER,
                currentPrice TEXT,
                lastPrice TEXT,
                simbolBanca TEXT,
                flagColor INTEGER,
                archiveColor INTEGER,
                visibilityTitle INTEGER,
                titleItem TEXT,
                profit TEXT,
                period TEXT
            );
        """
        db.execSQL(createTable)
    }

    // вставка в базу
    fun upsertTicker(ticker: TickerResponse) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("position", ticker.position)
            put("board", ticker.board)
            put("ticker", ticker.ticker)
            put("name", ticker.name)
            put("decimals", ticker.decimals)
            put("currentPrice", ticker.currentPrice)
            put("lastPrice", ticker.lastPrice)
            put("simbolBanca", ticker.simbolBanca)
            put("flagColor", ticker.flagColor)
            put("archiveColor", ticker.archiveColor)
            put("visibilityTitle", if (ticker.visibilityTitle) 1 else 0)
            put("titleItem", ticker.titleItem)
            put("profit", ticker.profit)
            put("period", ticker.period)
        }

        val selection: String
        val selectionArgs: Array<String>

        if (ticker.ticker == "Title") {
            selection = "board = ? AND ticker = ? AND id = ?"
            selectionArgs = arrayOf(ticker.board, ticker.ticker, ticker.id.toString())
        } else {
            selection = "board = ? AND ticker = ?"
            selectionArgs = arrayOf(ticker.board, ticker.ticker)
        }

        val existingCursor = db.query(
            TABLE_NAME,
            arrayOf("id"),
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (existingCursor.moveToFirst()) {
            db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            )
        } else {
            db.insert(TABLE_NAME, null, values)
        }

        existingCursor.close()
        db.close()
    }

    // получить список из базы по board
    fun getTickersByBoard(board: String): List<TickerResponse> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "board = ?",
            arrayOf(board),
            null,
            null,
            null
        )

        val list = mutableListOf<TickerResponse>()
        while (cursor.moveToNext()) {
            list.add(
                TickerResponse(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    position = cursor.getInt(cursor.getColumnIndexOrThrow("position")),
                    board = cursor.getString(cursor.getColumnIndexOrThrow("board")),
                    ticker = cursor.getString(cursor.getColumnIndexOrThrow("ticker")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    decimals = cursor.getInt(cursor.getColumnIndexOrThrow("decimals")),
                    currentPrice = cursor.getString(cursor.getColumnIndexOrThrow("currentPrice")),
                    lastPrice = cursor.getString(cursor.getColumnIndexOrThrow("lastPrice")),
                    simbolBanca = cursor.getString(cursor.getColumnIndexOrThrow("simbolBanca")),
                    flagColor = cursor.getIntOrNull("flagColor"),
                    archiveColor = cursor.getIntOrNull("archiveColor"),
                    visibilityTitle = cursor.getInt(cursor.getColumnIndexOrThrow("visibilityTitle")) == 1,
                    titleItem = cursor.getString(cursor.getColumnIndexOrThrow("titleItem")),
                    profit = cursor.getString(cursor.getColumnIndexOrThrow("profit")),
                    period = cursor.getString(cursor.getColumnIndexOrThrow("period"))
                )
            )
        }

        cursor.close()
        db.close()
        return list
    }

    // удалить все записи из таблицы базы данных, соответствующие board, но отсутствующие в списке backupTicker
    fun deleteMissingTickers(board: String, backupTicker: List<TickerResponse>) {
        val db = this.writableDatabase

        // Собираем список тикеров из backupTicker
        val tickers = backupTicker.map { it.ticker }.distinct()

        if (tickers.isEmpty()) {
            // Если список пустой — удаляем все по board
            db.delete(TABLE_NAME, "board = ?", arrayOf(board))
        } else {
            // Генерируем нужное количество знаков вопроса (?, ?, ?)
            val placeholders = tickers.joinToString(",") { "?" }

            // Условие: board совпадает и тикер НЕ в списке
            val whereClause = "board = ? AND ticker NOT IN ($placeholders)"

            // Аргументы: board + тикеры
            val whereArgs = arrayOf(board) + tickers.toTypedArray()

            db.delete(TABLE_NAME, whereClause, whereArgs)
        }
        db.close()
    }

    // обновление только flagColor по board + ticker
    fun updateFlagColor(board: String, ticker: String, flagColor: Int?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            if (flagColor == null) {
                putNull("flagColor")
            } else {
                put("flagColor", flagColor)
            }
        }
        db.update(
            TABLE_NAME,
            values,
            "board = ? AND ticker = ?",
            arrayOf(board, ticker)
        )
        db.close()
    }

    // получения id по позиции:
    fun getIdByPosition(board: String, ticker: String, position: Int): Int? {
        val db = this.readableDatabase
        var id: Int? = null

        val cursor = db.query(
            TABLE_NAME,
            arrayOf("id"),
            "board = ? AND ticker = ? AND position = ?",
            arrayOf(board, ticker, position.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        db.close()

        return id
    }

    // удаление по board кроме Title
    fun deleteAllByBoard(board: String) {
        val db = this.writableDatabase
        db.delete(
            TABLE_NAME,
            "board = ? AND ticker != ?",
            arrayOf(board, "Title")
        )
        db.close()
    }

    // удаление по board и titleItem
    fun deleteByBoardAndTicker(board: String, titleItem: String) {
        val db = this.writableDatabase
        db.delete(
            TABLE_NAME,
            "board = ? AND titleItem = ?",
            arrayOf(board, titleItem)
        )
        db.close()
    }

    // Обновляем только поле titleItem в базе
    fun updateTitleItem(board: String, ticker: String, position: Int, newTitle: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("titleItem", newTitle)
        }
        db.update(
            TABLE_NAME,
            values,
            "board = ? AND ticker = ? AND position = ?",
            arrayOf(board, ticker, position.toString())
        )
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "ticker.db"
        const val DATABASE_VERSION = 4
        const val TABLE_NAME = "ticker_response"
    }
}

fun Cursor.getIntOrNull(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    return if (index != -1 && !isNull(index)) getInt(index) else null
}
