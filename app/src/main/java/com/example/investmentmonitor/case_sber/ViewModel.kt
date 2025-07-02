package com.example.investmentmonitor.case_sber

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _tickers = MutableLiveData<List<TickerResponse>>(emptyList())
    val tickers: LiveData<List<TickerResponse>> get() = _tickers


    // Метод для фрагментов
    fun fetchTickers() {
        val assetsGroup = listOf("акции","фонды","облигации","фьючерсы","индексы")
        viewModelScope.launch {
            for (asset in assetsGroup) {
                try {
                    // Объединение всех тикеров и данных
                    val allTickers = mutableListOf<List<List<String>>>()
                    val allMarketData = mutableListOf<List<List<String>>>()

                    // загружаем данные
                    val response = when(asset) {
                        "акции" -> RetrofitClient.apiService.getJsonStock() // акции
                        "фонды" -> RetrofitClient.apiService.getJsonFund() // фонды
                        "облигации" -> RetrofitClient.apiService.getJsonBond() // облигации
                        "фьючерсы" -> RetrofitClient.apiService.getJsonFutures() // фьючерсы
                        else -> RetrofitClient.apiService.getJsonIndex() // индексы
                    }

                    response.body()?.securities?.data?.let { allTickers.add(it) }
                    response.body()?.marketdata?.data?.let { allMarketData.add(it) }

                    val rawTickers = allTickers.flatten()
                    val rawMarketData = allMarketData.flatten()

                    // Создание списка TickerResponse
                    val tickersCase = rawTickers.mapIndexed { index, it ->
//                    when(nameBoard()) {
                        when(asset) {
                            // акции
                            "акции" -> {
                                TickerResponse(
                                    board = it.getOrNull(1) ?: "", // BOARDID
                                    ticker = it.getOrNull(0) ?: "N/A", // SECID
                                    name = it.getOrNull(2) ?: "", // SHORTNAME
                                    decimals = it.getOrNull(8)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                    prevPrice = it.getOrNull(3) ?: "—", // PREVPRICE
                                    openPrice = rawMarketData.getOrNull(index)?.getOrNull(9) ?: "—", // OPEN
                                    lastPrice = rawMarketData.getOrNull(index)?.getOrNull(12) ?: "—", // LAST
                                    simbolBanca = it.getOrNull(16) ?: "" // FACEUNIT
                                )
                            }
                            // фонды
                            "фонды" -> {
                                TickerResponse(
                                    board = it.getOrNull(1) ?: "", // BOARDID
                                    ticker = it.getOrNull(0) ?: "N/A", // SECID
                                    name = it.getOrNull(9) ?: "", // SECNAME
                                    decimals = it.getOrNull(8)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                    prevPrice = it.getOrNull(3) ?: "—", // PREVPRICE
                                    openPrice = rawMarketData.getOrNull(index)?.getOrNull(9) ?: "—", // OPEN
                                    lastPrice = rawMarketData.getOrNull(index)?.getOrNull(12) ?: "—", // LAST
                                    simbolBanca = it.getOrNull(23) ?: "" // CURRENCYID
                                )
                            }
                            // облигации
                            "облигации" -> {
                                 TickerResponse(
                                    board = it.getOrNull(1) ?: "", // BOARDID
                                    ticker = it.getOrNull(0) ?: "", // SECID
                                    name = it.getOrNull(2) ?: "N/A", // SHORTNAME
                                    decimals = it.getOrNull(14)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                    prevPrice = it.getOrNull(8) ?: "—", // PREVPRICE
                                    openPrice = rawMarketData.getOrNull(index)?.getOrNull(8) ?: "—", // OPEN
                                    lastPrice = rawMarketData.getOrNull(index)?.getOrNull(11) ?: "—", // LAST
                                    simbolBanca =  it.getOrNull(31) ?: "", // CURRENCYID
                                    profit = it.getOrNull(4) ?: "", // YIELDATPREVWAPRICE
                                    period = it.getOrNull(13) ?: "" // MATDATE
                                )
                            }
                            // фьючерсы
                            "фьючерсы" -> {
                                TickerResponse(
                                    board = it.getOrNull(1) ?: "", // BOARDID
                                    ticker = it.getOrNull(0) ?: "N/A", // SECID
                                    name = it.getOrNull(2) ?: "", // SHORTNAME
                                    decimals = it.getOrNull(5)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                    prevPrice = it.getOrNull(19) ?: "—", // PREVPRICE
                                    openPrice = rawMarketData.getOrNull(index)?.getOrNull(5) ?: "—", // OPEN
                                    lastPrice = rawMarketData.getOrNull(index)?.getOrNull(8) ?: "—", // LAST
                                    simbolBanca = ""
                                )
                            }
                            // индексы
                            else -> {
                                TickerResponse(
                                    board = it.getOrNull(1) ?: "", // BOARDID
                                    ticker = it.getOrNull(0) ?: "N/A", // SECID
                                    name = it.getOrNull(2) ?: "", // NAME
                                    decimals = it.getOrNull(3)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                    prevPrice = rawMarketData.getOrNull(index)?.getOrNull(4) ?: "—", // CURRENCTVALUE
                                    openPrice = rawMarketData.getOrNull(index)?.getOrNull(3) ?: "—", // OPENVALUE
                                    lastPrice = rawMarketData.getOrNull(index)?.getOrNull(2) ?: "—", // LASTVALUE
                                    simbolBanca = it.getOrNull(7) ?: "" // CURRENCYID
                                )
                            }
                        }
                    }
                    // Обновляем данные
                    _tickers.postValue(tickersCase)

//                    if (asset == "облигации")
//                        Log.e("aaa","Данные MOEX --- $tickersCase")

//                    Log.e("aaa","Данные MOEX импортированы")

                } catch (e: Exception) {
                    _tickers.postValue(listOf(TickerResponse("", "N/A", "", 0, "—", "—", "—", "")))
                }
            }
        }
    }
}

