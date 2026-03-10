//package com.example.investmentmonitor.MOEX
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import isFuturesTraded
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class MoexViewModel : ViewModel() {
//
//    private val _tickerList = MutableLiveData<List<TickerResponse>>()
//    val tickerList: LiveData<List<TickerResponse>> get() = _tickerList
//
//    // для индексов получаем цену закрытия предыдущей торговой сессии
//    fun loadPricesIndexes(tickersList: List<TickerResponse>) {
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val deferredResults = tickersList.map { ticker ->
//                async {
//                    try {
//                        val secid = ticker.ticker // например, РТС
//                        // Получаем диапазон доступных дат
//                        val datesResponse = RetrofitClient2.api.getDates(secid)
//                        val dateFrom = datesResponse.dates.data.firstOrNull()?.get(0) ?: return@async ticker
//                        val dateTill = datesResponse.dates.data.firstOrNull()?.get(1) ?: return@async ticker
//
//                        // Для примера возьмём dateTill и сделаем запрос
//                        val closeResponse = RetrofitClient2.api.getClosePrice(secid, dateTill, dateTill)
//                        val closePrice = closeResponse.history.data.firstOrNull()?.get(1) as? Double
//
//                        // обновите UI с closePrice
//                        ticker.lastPrice = closePrice.toString()
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        // Обработка ошибок (например, показать Toast)
//                        ticker.lastPrice = "Ошибка"
//                    }
//                    ticker
//                }
//            }
//            val updatedList = deferredResults.awaitAll()
//
//            withContext(Dispatchers.Main) {
//                _tickerList.value = updatedList
//            }
//        }
//    }
//
//    // для фьючерсов получаем статус торговой сессии (0,1)
//    fun loadStatusFutures(tickersList: List<TickerResponse>) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val deferredResults = tickersList.map { ticker ->
//                async {
//                    try {
//                        val traded = isFuturesTraded(ticker.ticker)
//                        if (traded != null) {
//                            ticker.status = traded
////                            Log.e("aaa","ticker: ${ticker.ticker}, traded: $traded")
//                            Log.e("aaa","ticker: ${ticker.ticker}, traded: ${ticker.status}")
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        // Обработка ошибок (например, показать Toast)
//                    }
//                    ticker
//                }
//            }
//            val updatedList = deferredResults.awaitAll()
//
//            withContext(Dispatchers.Main) {
//                _tickerList.value = updatedList
//            }
//        }
//    }
//}
