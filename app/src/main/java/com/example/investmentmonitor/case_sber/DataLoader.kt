package com.example.investmentmonitor.case_sber

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// API-сервис
interface ApiService {
//    @GET("engines/stock/markets/shares/boardgroups/57/securities.json?") // это акции и фонды
//    https://iss.moex.com/iss/engines/stock/markets/shares/boardgroups/57/securities.json?iss.dp=comma&iss.meta=off&iss.only=securities&securities.columns=SECID,SECNAME

    //    https://iss.moex.com/iss/engines/stock/markets/shares/securities.json // акции
    @GET("engines/stock/markets/shares/boards/TQBR/securities.json")
    suspend fun getJsonStock(): Response<MarketDataResponse>
    //    https://iss.moex.com/iss/engines/stock/markets/shares/securities.json // фонды
    @GET("engines/stock/markets/shares/boards/TQTF/securities.json")
    suspend fun getJsonFund(): Response<MarketDataResponse>
    //    https://iss.moex.com/iss/engines/stock/markets/bonds/securities.json // облигации
    @GET("engines/stock/markets/bonds/boards/TQOB/securities.json")
    suspend fun getJsonBond(): Response<MarketDataResponse>
    //    https://iss.moex.com/iss/engines/futures/markets/forts/securities.json // фьючерсы
    @GET("engines/futures/markets/forts/securities.json")
    suspend fun getJsonFutures(): Response<MarketDataResponse>
    //    https://iss.moex.com/iss/engines/stock/markets/index/securities.json // индексы
    @GET("engines/stock/markets/index/securities.json")
    suspend fun getJsonIndex(): Response<MarketDataResponse>
    //    https://iss.moex.com/iss/engines/futures/markets/options/securities.json // опционы
}

object RetrofitClient {
    private const val BASE_URL = "https://iss.moex.com/iss/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// Модель данных для ответа от MOEX API
data class MarketDataResponse(
    val securities: Securities,
    val marketdata: MarketData
)

data class Securities(
    val data: List<List<String>>
)

data class MarketData(
    val data: List<List<String>>
)

data class TickerResponse(
    val board: String,
    val ticker: String,
    var name: String,
    var decimals: Int,
    var prevPrice: String,
    var openPrice: String,
    var lastPrice: String,
    var simbolBanca: String,
    var profit: String = "",
    var period: String = ""
)

/////////////////////////////////////////////////////////////////////////////////////////////////////

//import com.squareup.moshi.Json
//
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//import org.apache.poi.ss.formula.functions.Log
//import java.io.IOException

//data class Security(
//    @Json(name = "SECID") val secId: String,
//    @Json(name = "SHORTNAME") val shortName: String,
//    @Json(name = "ISIN") val isin: String
//)
//
//data class MarketData(
//    @Json(name = "SECID") val secId: String,
//    @Json(name = "LAST") val last: Double,
//    @Json(name = "OPEN") val open: Double,
//    @Json(name = "HIGH") val high: Double,
//    @Json(name = "LOW") val low: Double
//)
//
//data class ResponseData(
//    @Json(name = "securities") val securities: List<Security>,
//    @Json(name = "marketdata") val marketData: List<MarketData>
//)
//
//fun fetchData() {
//    val client = OkHttpClient()
//    val request = Request.Builder()
//        .url("https://iss.moex.com/iss/engines/stock/markets/shares/securities/sber.json")
//        .build()
//
//    client.newCall(request).execute().use { response ->
//        if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//        val jsonAdapter = moshi.adapter(ResponseData::class.java)
//
//        val responseData = jsonAdapter.fromJson(response.body?.string() ?: "")
//
//        responseData?.let {
//            val securitiesList = it.securities
//            val marketDataList = it.marketData
//
//            // Теперь вы можете использовать списки securitiesList и marketDataList
//            println("Securities: $securitiesList")
//            println("Market Data: $marketDataList")
//            android.util.Log.e("aaa","securitiesList ---------- ${securitiesList}")
//            android.util.Log.e("aaa","marketDataList ---------- ${marketDataList}")
//        }
//    }
//}

/////////////////////////////////////////////////////////////////////////////////////////////////////



//import android.util.Log
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//
//// API-сервис
//
//fun fetchDataFromMoex(): Pair<List<List<Any>>, List<List<Any>>> {
//    val client = OkHttpClient()
//    val url = "https://iss.moex.com/iss/engines/stock/markets/shares/securities/sber.json"
//
//    // Создаём запрос
//    val request = Request.Builder()
//        .url(url)
//        .build()
//
//    Log.e("aaa", "1-----------")
//    client.newCall(request).execute().use { response ->
//        Log.e("aaa", "2-----------")
//        if (!response.isSuccessful) throw Exception("Ошибка при выполнении запроса: ${response.code}")
//
//        // Получаем тело ответа в виде строки
//        val responseData = response.body?.string() ?: throw Exception("Пустой ответ от сервера")
//
//        // Парсим JSON
//        val json = JSONObject(responseData)
//
//        // Инициализируем списки для данных
//        val securitiesData = mutableListOf<List<Any>>()
//        val marketData = mutableListOf<List<Any>>()
//
//        // Извлекаем таблицу securities
//        val securities = json.optJSONObject("securities")?.optJSONArray("data")
//        if (securities != null) {
//            for (i in 0 until securities.length()) {
//                val row = securities.getJSONArray(i)
//                val rowData = mutableListOf<Any>()
//                for (j in 0 until row.length()) {
//                    rowData.add(row.get(j))
//                }
//                securitiesData.add(rowData)
//            }
//        }
//
//        // Извлекаем таблицу marketdata
//        val marketdata = json.optJSONObject("marketdata")?.optJSONArray("data")
//        if (marketdata != null) {
//            for (i in 0 until marketdata.length()) {
//                val row = marketdata.getJSONArray(i)
//                val rowData = mutableListOf<Any>()
//                for (j in 0 until row.length()) {
//                    rowData.add(row.get(j))
//                }
//                marketData.add(rowData)
//            }
//        }
//
//        return Pair(securitiesData, marketData)
//    }
//}

//////////////////////////////////////////////////////////////////////////////////////////////////////



//    @GET("engines/stock/markets/shares/boardgroups/57/securities.json?") // это акции и фонды
//    https://iss.moex.com/iss/engines/stock/markets/shares/boardgroups/57/securities.json?iss.dp=comma&iss.meta=off&iss.only=securities&securities.columns=SECID,SECNAME

    // для фрагмента портфеля (индексы)
//    https://iss.moex.com/iss/engines/stock/markets/index/securities.json

    // для фрагмента акций
//    https://iss.moex.com/iss/engines/stock/markets/shares/securities.json
//    @GET("engines/stock/markets/shares/boards/TQBR/securities.json") // это акции

    // для фрагмента фондов
//    https://iss.moex.com/iss/engines/stock/markets/shares/securities.json
//    @GET("engines/stock/markets/shares/boards/TQTF/securities.json")

    // для фрагмента облигаций
//    https://iss.moex.com/iss/engines/stock/markets/bonds/securities.json
//    @GET("engines/stock/markets/bonds/boards/TQOB/securities.json")

    // для фрагмента фьючерсов
//    https://iss.moex.com/iss/engines/futures/markets/forts/securities.json
//    @GET("engines/futures/markets/forts/securities.json") // это фьючерсы

    // для фрагмента опционов
//    https://iss.moex.com/iss/engines/futures/markets/options/securities.json
//    @GET("engines/futures/markets/options/securities.json") // это опционы

    // для фрагмента индексов
//    https://iss.moex.com/iss/engines/stock/markets/index/securities.json
//    @GET("engines/stock/markets/index/boards/SNDX/securities.json") // это индексы
