package com.example.investmentmonitor.MOEX

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Определите модели данных (на основе JSON, только нужные поля)
data class DatesResponse(
    val dates: DatesData
)
data class DatesData(
    val data: List<List<String>> // [["2020-06-22", "2025-08-08"]]
)
data class HistoryResponse(
    val history: HistoryData
)
data class HistoryData(
    val data: List<List<Any>> // [["2025-08-08", 2961.54]]
)

// Опишите интерфейс Retrofit API
interface MoexApi {
    @GET("iss/history/engines/stock/markets/index/securities/{secid}/dates.json")
    suspend fun getDates(
        @Path("secid") secid: String
    ): DatesResponse

    @GET("iss/history/engines/stock/markets/index/securities/{secid}.json")
    suspend fun getClosePrice(
        @Path("secid") secid: String,
        @Query("from") from: String,
        @Query("till") till: String,
        @Query("history.columns") columns: String = "TRADEDATE,CLOSE"
    ): HistoryResponse
}

// Создайте Retrofit клиент
object RetrofitClient2 {
    val api: MoexApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://iss.moex.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoexApi::class.java)
    }
}


// для индексов получаем цену закрытия предыдущей торговой сессии
suspend fun priceLastIndexes(ticker: String): Double? {
    try {
        // Получаем диапазон доступных дат
        val datesResponse = RetrofitClient2.api.getDates(ticker)
//        val dateFrom = datesResponse.dates.data.firstOrNull()?.get(0) ?: return 0.0
        val dateTill = datesResponse.dates.data.firstOrNull()?.get(1) ?: return 0.0

        // Для примера возьмём dateTill и сделаем запрос
        val closeResponse = RetrofitClient2.api.getClosePrice(ticker, dateTill, dateTill)
        val closePrice = closeResponse.history.data.firstOrNull()?.get(1) as? Double

        // обновите UI с closePrice
        return closePrice
    } catch (e: Exception) {
        e.printStackTrace()
        // Обработка ошибок (например, показать Toast)
    }
    return 0.0
}
