//package com.example.investmentmonitor.MOEX
//
////import androidx.room.Entity
////import androidx.room.PrimaryKey
////import androidx.room.Database
////import androidx.room.Room
////import androidx.room.RoomDatabase
//import androidx.room.*
//import android.content.Context
//
//// ✅ 1. Entity TickerResponseEntity
//@Entity(tableName = "ticker_response")
//data class TickerResponseEntity(
//    @PrimaryKey(autoGenerate = true) val id: Int = 0,
//    val board: String,
//    val ticker: String,
//    var name: String,
//    var decimals: Int,
//    var currentPrice: String,
//    var lastPrice: String,
//    var simbolBanca: String,
//    var flagColor: Int? = null,
//    var archiveColor: Int? = null,
//    var visibilityTitle: Boolean = false,
//    var titleItem: String = "Заголовок",
//    var profit: String = "",
//    var period: String = ""
//)
//
//// ✅ 2. DAO — методы для сравнения, обновления, удаления
//@Dao
//interface TickerResponseDao {
//
//    @Query("SELECT * FROM ticker_response WHERE board = :board AND ticker = :ticker LIMIT 1")
//    suspend fun getByBoardAndTicker(board: String, ticker: String): TickerResponseEntity?
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(entity: TickerResponseEntity): Long
//
//    @Update
//    suspend fun update(entity: TickerResponseEntity)
//
//    @Query("DELETE FROM ticker_response WHERE board = :board AND ticker = :ticker")
//    suspend fun deleteByBoardAndTicker(board: String, ticker: String)
//
//    @Query("SELECT * FROM ticker_response")
//    suspend fun getAll(): List<TickerResponseEntity>
//}
//
//// ✅ 3. Database
//@Database(entities = [TickerResponseEntity::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun tickerResponseDao(): TickerResponseDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getInstance(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "ticker_database"
//                ).build().also { INSTANCE = it }
//            }
//        }
//    }
//}
//
//// 🔁 4. Логика сравнения и обновления
//// Вот пример функции, которая обновляет только изменившиеся поля, оставляя кастомные параметры пользователя нетронутыми:
// suspend fun updateTickerList(context: Context, newList: List<TickerResponse>) {
//    val db = AppDatabase.getInstance(context)
//    val dao = db.tickerResponseDao()
//
//    for (newItem in newList) {
//        val existing = dao.getByBoardAndTicker(newItem.board, newItem.ticker)
//        if (existing == null) {
//            // Добавляем как новый
//            dao.insert(newItem.toEntity())
//        } else {
//            // Сравниваем и обновляем только изменившиеся поля
//            var changed = false
//
//            if (existing.currentPrice != newItem.currentPrice) {
//                existing.currentPrice = newItem.currentPrice
//                changed = true
//            }
//            if (existing.lastPrice != newItem.lastPrice) {
//                existing.lastPrice = newItem.lastPrice
//                changed = true
//            }
//            if (existing.name != newItem.name) {
//                existing.name = newItem.name
//                changed = true
//            }
//            if (existing.decimals != newItem.decimals) {
//                existing.decimals = newItem.decimals
//                changed = true
//            }
//            if (existing.simbolBanca != newItem.simbolBanca) {
//                existing.simbolBanca = newItem.simbolBanca
//                changed = true
//            }
//
//            if (changed) {
//                dao.update(existing)
//            }
//        }
//    }
//}
//
//// 🔁 5. Удаление по ключу
//suspend fun deleteTicker(context: Context, board: String, ticker: String) {
//    val db = AppDatabase.getInstance(context)
//    val dao = db.tickerResponseDao()
//    dao.deleteByBoardAndTicker(board, ticker)
//}
//
//// 🔁 6. Расширения: преобразование между TickerResponse и TickerResponseEntity
//fun TickerResponse.toEntity(): TickerResponseEntity {
//    return TickerResponseEntity(
//        board = this.board,
//        ticker = this.ticker,
//        name = this.name,
//        decimals = this.decimals,
//        currentPrice = this.currentPrice,
//        lastPrice = this.lastPrice,
//        simbolBanca = this.simbolBanca,
//        flagColor = this.flagColor,
//        archiveColor = this.archiveColor,
//        visibilityTitle = this.visibilityTitle,
//        titleItem = this.titleItem,
//        profit = this.profit,
//        period = this.period
//    )
//}