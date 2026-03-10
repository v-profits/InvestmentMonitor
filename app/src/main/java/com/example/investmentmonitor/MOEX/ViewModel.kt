package com.example.investmentmonitor.MOEX

//import androidx.core.content.ContentProviderCompat.requireContext
//import com.example.investmentmonitor.MOEX.TickerDatabaseHelper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isFuturesTraded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

open class ViewModel() : ViewModel() {
    private val _tickers = MutableLiveData<List<TickerResponse>>(emptyList())
    val tickers: LiveData<List<TickerResponse>> get() = _tickers

    // вводим текст заголовка итема
    private val _stringValue = MutableLiveData<String>()
    val stringValue: LiveData<String>
        get() = _stringValue

    // счетчик для кружочков фьючерсов
    var count = 0

    fun setTitle(value: String) {
        _stringValue.value = value
    }

    // вкл и выкл видимость виджета заголовка
    private val _booleanValue = MutableLiveData<Boolean>()
    val booleanValue: LiveData<Boolean>
        get() = _booleanValue

    fun setChecked(value: Boolean) {
        _booleanValue.value = value
    }

    //--- меняем цвет флажка в итеме по нажатию значка цвета в диалоговом окне (передаем цвет через ViewModel)
    val colorData = MutableLiveData<Int>()
    // меняем цвет флажка итема
    fun setColor(color: Int) {
        colorData.value = color
    }

    // для перемещения итема вверх
    val tickersUpAndDown = MutableLiveData<MutableList<TickerResponse>>(mutableListOf())
    // перемещаем итем фрагмента в самый верх
    fun swapItems(fromPosition: Int, toPosition: Int) {
        tickersUpAndDown.value = backupTicker // Здесь initialData - список с данными

        tickersUpAndDown.value?.let {
            Collections.swap(it, fromPosition, toPosition)
            tickersUpAndDown.postValue(it) // асинхронное
//            tickers.value = it  // Заменяем на синхронное обновление
        }
    }

    // Метод для фрагментов
    fun fetchTickers() {
        viewModelScope.launch {
            try {
                // Объединение всех тикеров и данных
                val allTickers = mutableListOf<List<List<String>>>()
                val allMarketData = mutableListOf<List<List<String>>>()

                // Проверяем флаги и загружаем данные только для включенных фондов
                val response = when {
                    // акции
                    flagsMap["TQBR"] == true -> RetrofitClient.apiService.getTQBR()
                    flagsMap["SMAL"] == true -> RetrofitClient.apiService.getSMAL()
                    flagsMap["SPEQ"] == true -> RetrofitClient.apiService.getSPEQ()
                    // фонды
                    flagsMap["TQTF"] == true -> RetrofitClient.apiService.getTQTF()
                    flagsMap["TQPI"] == true -> RetrofitClient.apiService.getTQPI()
                    flagsMap["TQIF"] == true -> RetrofitClient.apiService.getTQIF()
                    // облигации
                    flagsMap["TQOB"] == true -> RetrofitClient.apiService.getTQOB()
                    flagsMap["TQCB"] == true -> RetrofitClient.apiService.getTQCB()
                    flagsMap["PACT"] == true -> RetrofitClient.apiService.getPACT()
                    flagsMap["SPOB"] == true -> RetrofitClient.apiService.getSPOB()
                    flagsMap["TQIR"] == true -> RetrofitClient.apiService.getTQIR()
                    flagsMap["TQIY"] == true -> RetrofitClient.apiService.getTQIY()
                    flagsMap["TQOD"] == true -> RetrofitClient.apiService.getTQOD()
                    flagsMap["TQOE"] == true -> RetrofitClient.apiService.getTQOE()
                    flagsMap["TQOY"] == true -> RetrofitClient.apiService.getTQOY()
                    flagsMap["TQRD"] == true -> RetrofitClient.apiService.getTQRD()
                    flagsMap["TQUD"] == true -> RetrofitClient.apiService.getTQUD()
                    // индексы
                    flagsMap["SNDX"] == true -> RetrofitClient.apiService.getSNDX()
                    flagsMap["RTSI"] == true -> RetrofitClient.apiService.getRTSI()
                    flagsMap["INAV"] == true -> RetrofitClient.apiService.getINAV()
                    flagsMap["INPF"] == true -> RetrofitClient.apiService.getINPF()
                    // фьючерсы
                    flagsMap["RFUD"] == true -> RetrofitClient.apiService.getFutures()
                    // опционы
                    flagsMap["ROPD"] == true -> RetrofitClient.apiService.getOptions()
                    else  -> RetrofitClient.apiService.getINAV()
                }

                response.body()?.securities?.data?.let { allTickers.add(it) }
                response.body()?.marketdata?.data?.let { allMarketData.add(it) }

                val rawTickers = allTickers.flatten()
                val rawMarketData = allMarketData.flatten()

                // Создание списка TickerResponse
                val tickers = rawTickers.mapIndexed { index, it ->
                    when(nameBoard()) {
                        // акции
                        "TQBR", "SMAL", "SPEQ" -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(9) ?: "", // SECNAME
                                decimals = it.getOrNull(8)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(3) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(12) ?: "—", // LAST
                                simbolBanca = it.getOrNull(16) ?: "", // FACEUNIT
                                session = rawMarketData.getOrNull(index)?.getOrNull(54) ?: "—", // TRADINGSESSION "1", "2"
                            )
                        }
                        // фонды
                        "TQTF", "TQPI", "TQIF" -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(9) ?: "", // SECNAME
                                decimals = it.getOrNull(8)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(3) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(12) ?: "—", // LAST
                                simbolBanca = it.getOrNull(16) ?: "", // FACEUNIT
                                session = rawMarketData.getOrNull(index)?.getOrNull(54) ?: "—", // TRADINGSESSION "1", "2"
                            )
                        }
                        // облигации
                        "TQOB", "TQCB", "PACT", "SPOB", "TQIR", "TQIY", "TQOD", "TQOE", "TQOY", "TQRD", "TQUD" -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "", // SECID
                                name = it.getOrNull(2) ?: "N/A", // SHORTNAME
                                decimals = it.getOrNull(14)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(8) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(11) ?: "—", // LAST
                                simbolBanca =  it.getOrNull(25) ?: "", // FACEUNIT
                                profit = it.getOrNull(4) ?: "", // YIELDATPREVWAPRICE
                                period = it.getOrNull(13) ?: "", // MATDATE
                                session = rawMarketData.getOrNull(index)?.getOrNull(58) ?: "—", // TRADINGSESSION "1", "2"
                            )
                        }
                        // индексы
                        "SNDX", "RTSI", "INAV", "INPF" -> {
//                            Log.e("aaa", "index: $index")
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(2) ?: "", // NAME
                                decimals = it.getOrNull(3)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(4) ?: "—", // CURRENTVALUE
                                currentPrice = "",
                                simbolBanca = it.getOrNull(7) ?: "", // CURRENCYID
                                session = rawMarketData.getOrNull(index)?.getOrNull(25) ?: "—", // TRADINGSESSION "1", "2"
                            )
                        }
                        // фьючерсы
                        "RFUD" -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(2) ?: "", // SHORTNAME
                                decimals = it.getOrNull(5)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(19) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(8) ?: "—", // LAST
                                simbolBanca = "",
//                                status = rawMarketData.getOrNull(index)?.getOrNull(9) ?: "—", // SECTYPE "AE", "AK"
                            )
                        }
                        // опционы
                        else -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(2) ?: "", // SHORTNAME
                                decimals = it.getOrNull(5)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(19) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(8) ?: "—", // LAST
                                simbolBanca = "",
//                                status = rawMarketData.getOrNull(index)?.getOrNull(13) ?: "—", // OPTIONTYPE "P", "C" - 14; UNDERLYINGTYPE "F" - 27
                            )
                        }
                    }
                }

                // меняем статусы
                fun sessionAll(ticker: TickerResponse) {
                    ticker.session = when(ticker.session) { // 🟢 🔴 ⚫ 🔵 ⚪ 🟡 🟣 🟠 🟤
                        "0" -> "🟠" // 0, "morning", "Утренняя сессия"
                        "1" -> "🟢" // 1, "main", "Основная сессия"
                        "2" -> "🔵" // 2, "evening", "Вечерняя сессия"
                        "3" -> "🔴" // 3, "total", "Итого"
                        "5" -> "🟡" // 5, "weekend", "Дополнительная сессия выходного дня"
                        else -> "⚫"
                    }
                }

                // меняем статусы фьючерсов
                fun sessionFutures(ticker: TickerResponse, traded: String?) {
                    ticker.session = when(traded) { // 🟢 🔴 ⚫ 🔵 ⚪ 🟡 🟣 🟠 🟤
                        "0" -> "⚫" // 0, "morning", "Утренняя сессия"
                        "1" -> "🟢" // 1, "main", "Основная сессия"
                        else -> "⚪"
                    }
                }

                // для фьючерсов получаем статус торговой сессии (0,1)
                if (nameBoard() == "RFUD") { // для фьючерсов
                    viewModelScope.launch {
                        try {
                            for (ticker in tickers) {
                                val traded = isFuturesTraded(ticker.ticker)
                                // меняем статусы фьючерсов
                                sessionFutures(ticker, traded)

//                                Log.e("aaa", "ticker: ${ticker.ticker}, traded: ${traded}, status: ${ticker.status}")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Обработка ошибок (например, показать Toast)
                        }
//                        delay(2000) // задержка 2 секунды (не блокирует UI)

                        // Обновляем данные
                        _tickers.postValue(tickers)
                    }
                }
                // для индексов получаем цену закрытия предыдущей торговой сессии
                else if (nameBoard() in listOf("SNDX", "RTSI", "INAV", "INPF")) { // для индексов
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            for (ticker in tickers) {
                                val secid = ticker.ticker // например, РТС
                                // Получаем диапазон доступных дат
                                val datesResponse = RetrofitClient2.api.getDates(secid)
                                val dateFrom = datesResponse.dates.data.firstOrNull()?.get(0) ?: return@launch
                                val dateTill = datesResponse.dates.data.firstOrNull()?.get(1) ?: return@launch

                                // Для примера возьмём dateTill и сделаем запрос
                                val closeResponse = RetrofitClient2.api.getClosePrice(secid, dateTill, dateTill)
                                val closePrice = closeResponse.history.data.firstOrNull()?.get(1) as? Double

                                // обновите UI с closePrice
                                ticker.currentPrice = closePrice.toString()

//                                if (ticker.ticker == "IMOEX2")
//                                    Log.e("aaa", "ticker: ${ticker.ticker}, currentPrice: ${ticker.currentPrice}, lastPrice: ${ticker.lastPrice}")

                                // меняем статусы
                                sessionAll(ticker)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Обработка ошибок (например, показать Toast)
                        }
                        // Обновляем данные
                        _tickers.postValue(tickers)
                    }
                }
                else { // для всех остальных боардов
                    // меняем статусы
                    for (ticker in tickers)
                        sessionAll(ticker)

                    // Обновляем данные
                    _tickers.postValue(tickers)
                }

                // Обновляем данные
//                _tickers.postValue(tickers)
            } catch (e: Exception) {
                _tickers.postValue(listOf(TickerResponse(null, null, "", "N/A", "", 0, "—", "—", "", "")))
            }
        }
    }
}

