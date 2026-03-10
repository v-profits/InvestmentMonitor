import android.util.Log
import com.example.investmentmonitor.MOEX.RetrofitClient2
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Модели данных (пример для futures securities)
data class FuturesSecuritiesResponse(
    val series: SeriesData
)
data class SeriesData(
    val columns: List<String>,
    val data: List<List<Any>>
)

// Интерфейс API
interface MoexFuturesApi {
    // Получить список фьючерсов с данными
    @GET("iss/statistics/engines/futures/markets/forts/series.json")
    suspend fun getFuturesSecurities(): FuturesSecuritiesResponse
}

// Retrofit клиент
object RetrofitClientFutures {
    val api: MoexFuturesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://iss.moex.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoexFuturesApi::class.java)
    }
}

// Функция для получения is_traded по secid, например "AKZ5"
suspend fun isFuturesTraded(secid: String): String? {
    val response = RetrofitClientFutures.api.getFuturesSecurities()

    val secidIndex = response.series.columns.indexOf("secid")
    val isTradedIndex = response.series.columns.indexOf("is_traded")

    if (secidIndex == -1 || isTradedIndex == -1) return null

    val item = response.series.data.firstOrNull {
        (it[secidIndex] as? String)?.equals(secid, ignoreCase = true) == true
    } ?: return null

    val isTradedValue = item[isTradedIndex]
    if (isTradedValue is Double)
        return isTradedValue.toInt().toString()
    else
        return isTradedValue.toString()
}

