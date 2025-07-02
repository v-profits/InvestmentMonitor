package com.example.investmentmonitor.value_moex

import com.example.investmentmonitor.MOEX.isMarketData
import com.example.investmentmonitor.MOEX.isSecurities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Асинхронная функция для получения текстовых данных из секций Securities и MarketData
fun getValuesAndColumnNames(board: String, ticker: String, callback: (List<List<String>>) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            // Строим URL с учетом переданных аргументов
            val url = when(board) {
                // акции
                "TQBR", "SMAL", "SPEQ" -> {
                    "https://iss.moex.com/iss/engines/stock/markets/shares/boards/$board/securities/$ticker.json"
                }
                // фонды
                "TQTF", "TQPI", "TQIF" -> {
                    "https://iss.moex.com/iss/engines/stock/markets/shares/boards/$board/securities/$ticker.json"
                }
                // облигации
                "TQOB", "TQCB", "PACT", "SPOB", "TQIR", "TQIY", "TQOD", "TQOE", "TQOY", "TQRD", "TQUD" -> {
                    "https://iss.moex.com/iss/engines/stock/markets/bonds/boards/$board/securities/$ticker.json"
                }
                // индексы
                "SNDX", "RTSI", "INAV", "INPF" -> {
                    "https://iss.moex.com/iss/engines/stock/markets/index/boards/$board/securities/$ticker.json"
                }
                // фьючерсы
                else -> {
                    "https://iss.moex.com/iss/engines/futures/markets/forts/securities/$ticker.json"
                }
            }

            // Выполняем сетевой запрос на фоне (в Dispatchers.IO)
            val jsonString = withContext(Dispatchers.IO) {
                URL(url).readText()
            }

            // Парсим JSON строку
            val jsonObject = JSONObject(jsonString)

            // Извлекаем секцию `securities`
            val securities = jsonObject.getJSONObject("securities")
            val securitiesColumns = securities.getJSONArray("columns")
            val securitiesData = securities.getJSONArray("data")

            // Извлекаем секцию `marketdata`
            val marketData = jsonObject.getJSONObject("marketdata")
            val marketDataColumns = marketData.getJSONArray("columns")
            val marketDataData = marketData.getJSONArray("data")

            // Создаем двумерный список для хранения данных
            val resultData = mutableListOf<List<String>>()

            // Обрабатываем данные из секции `securities`
            if (isSecurities) // isSecurities = true, если надо получить данные без marketdata
                for (i in 0 until securitiesColumns.length()) {
                    val columnName = securitiesColumns.getString(i)
                    val value = securitiesData.getJSONArray(0).get(i).toString()
                    resultData.add(listOf(columnName, value))
                }

            // Обрабатываем данные из секции `marketdata`
            if (isMarketData) // isMarketData = true, если надо получить данные без securities
                for (i in 0 until marketDataColumns.length()) {
                    val columnName = marketDataColumns.getString(i)
                    val value = marketDataData.getJSONArray(0).get(i).toString()
                    resultData.add(listOf(columnName, value))
                }

            // Возвращаем результат через callback
            callback(resultData)

        } catch (e: Exception) {
            // В случае ошибки передаем сообщение об ошибке через callback
            callback(listOf(listOf("Ошибка", e.message ?: "Неизвестная ошибка")))
        }
    }
}
