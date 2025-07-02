package com.example.investmentmonitor.MOEX

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Collections

class ViewModel : ViewModel() {

    private val _tickers = MutableLiveData<List<TickerResponse>>(emptyList())
    val tickers: LiveData<List<TickerResponse>> get() = _tickers

    // вводим текст заголовка итема
    private val _stringValue = MutableLiveData<String>()
    val stringValue: LiveData<String>
        get() = _stringValue

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
                                name = it.getOrNull(2) ?: "", // SECNAME
                                decimals = it.getOrNull(8)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(3) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(12) ?: "—", // LAST
                                simbolBanca = it.getOrNull(16) ?: "" // FACEUNIT
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
                                simbolBanca = it.getOrNull(16) ?: "" // FACEUNIT
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
                                period = it.getOrNull(13) ?: "" // MATDATE
                            )
                        }
                        // индексы
                        "SNDX", "RTSI", "INAV", "INPF" -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(2) ?: "", // NAME
                                decimals = it.getOrNull(3)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
//                                currentPrice = rawMarketData.getOrNull(index)?.getOrNull(4) ?: "—", // CURRENTVALUE
                                currentPrice = rawMarketData.getOrNull(index)?.getOrNull(2) ?: "—", // LASTVALUE
//                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(2) ?: "—" // LASTVALUE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(4) ?: "—", // CURRENTVALUE
                                simbolBanca = it.getOrNull(7) ?: "" // CURRENCYID
                            )
                        }
                        // фьючерсы
                        else -> {
                            TickerResponse(
                                board = it.getOrNull(1) ?: "", // BOARDID
                                ticker = it.getOrNull(0) ?: "N/A", // SECID
                                name = it.getOrNull(2) ?: "", // SHORTNAME
                                decimals = it.getOrNull(5)?.toString()?.toIntOrNull() ?: 0, // DECIMALS
                                currentPrice = it.getOrNull(19) ?: "—", // PREVPRICE
                                lastPrice = rawMarketData.getOrNull(index)?.getOrNull(8) ?: "—", // LAST
                                simbolBanca = ""
                            )
                        }
                    }
                }
                // Обновляем данные
                _tickers.postValue(tickers)

            } catch (e: Exception) {
                _tickers.postValue(listOf(TickerResponse("", "N/A", "", 0, "—", "—", "")))
            }
        }
    }
}

