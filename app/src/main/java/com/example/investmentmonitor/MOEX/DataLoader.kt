package com.example.investmentmonitor.MOEX

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// API-сервис
interface ApiService {

//    @GET("engines/stock/markets/shares/boardgroups/57/securities.json?") // это акции и фонды
//    https://iss.moex.com/iss/engines/stock/markets/shares/boardgroups/57/securities.json?iss.dp=comma&iss.meta=off&iss.only=securities&securities.columns=SECID,SECNAME

    // для фрагмента портфеля (индексы)
//    https://iss.moex.com/iss/engines/stock/markets/index/securities.json

    // для фрагмента акций
//    https://iss.moex.com/iss/engines/stock/markets/shares/securities.json
    @GET("engines/stock/markets/shares/boards/TQBR/securities.json") // это акции
    suspend fun getTQBR(): Response<MarketDataResponse>
    @GET("engines/stock/markets/shares/boards/SMAL/securities.json") // это акции
    suspend fun getSMAL(): Response<MarketDataResponse>
    @GET("engines/stock/markets/shares/boards/SPEQ/securities.json") // это акции
    suspend fun getSPEQ(): Response<MarketDataResponse>

    // для фрагмента фондов
//    https://iss.moex.com/iss/engines/stock/markets/shares/securities.json
    @GET("engines/stock/markets/shares/boards/TQTF/securities.json")
    suspend fun getTQTF(): Response<MarketDataResponse>
    @GET("engines/stock/markets/shares/boards/TQIF/securities.json")
    suspend fun getTQIF(): Response<MarketDataResponse>
    @GET("engines/stock/markets/shares/boards/TQPI/securities.json")
    suspend fun getTQPI(): Response<MarketDataResponse>

    // для фрагмента облигаций
//    https://iss.moex.com/iss/engines/stock/markets/bonds/securities.json
    @GET("engines/stock/markets/bonds/boards/TQOB/securities.json")
    suspend fun getTQOB(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQCB/securities.json")
    suspend fun getTQCB(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/PACT/securities.json")
    suspend fun getPACT(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/SPOB/securities.json")
    suspend fun getSPOB(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQIR/securities.json")
    suspend fun getTQIR(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQIY/securities.json")
    suspend fun getTQIY(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQOD/securities.json")
    suspend fun getTQOD(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQOE/securities.json")
    suspend fun getTQOE(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQOY/securities.json")
    suspend fun getTQOY(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQRD/securities.json")
    suspend fun getTQRD(): Response<MarketDataResponse>
    @GET("engines/stock/markets/bonds/boards/TQUD/securities.json")
    suspend fun getTQUD(): Response<MarketDataResponse>

    // для фрагмента фьючерсов
//    https://iss.moex.com/iss/engines/futures/markets/forts/securities.json
    @GET("engines/futures/markets/forts/securities.json") // это фьючерсы
    suspend fun getFutures(): Response<MarketDataResponse>

    // для фрагмента опционов
//    https://iss.moex.com/iss/engines/futures/markets/options/securities.json
    @GET("engines/futures/markets/options/securities.json") // это опционы
    suspend fun getOptions(): Response<MarketDataResponse>

    // для фрагмента индексов
//    https://iss.moex.com/iss/engines/stock/markets/index/securities.json
    @GET("engines/stock/markets/index/boards/SNDX/securities.json") // это индексы
    suspend fun getSNDX(): Response<MarketDataResponse>
    @GET("engines/stock/markets/index/boards/RTSI/securities.json") // это индексы
    suspend fun getRTSI(): Response<MarketDataResponse>
    @GET("engines/stock/markets/index/boards/INAV/securities.json") // это индексы
    suspend fun getINAV(): Response<MarketDataResponse>
    @GET("engines/stock/markets/index/boards/INPF/securities.json") // это индексы
    suspend fun getINPF(): Response<MarketDataResponse>
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
    var currentPrice: String,
    var lastPrice: String,
    var simbolBanca: String,
    var flagColor: Int? = null, // Поле для цвета
    var archiveColor: Int? = null, // Поле для хранения цвета
    var visibilityTitle: Boolean = false,
    var titleItem: String = "Заголовок",
    var profit: String = "",
    var period: String = ""
)