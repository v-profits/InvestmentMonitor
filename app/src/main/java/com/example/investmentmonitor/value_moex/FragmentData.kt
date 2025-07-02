package com.example.investmentmonitor.value_moex

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.investmentmonitor.R
import com.example.investmentmonitor.MOEX.isMarketData
import com.example.investmentmonitor.MOEX.isSecurities

// фрагмент показывает данные по конкретному эмитенту в текстовом виде
class FragmentData : Fragment() {

    private lateinit var titleTickerData: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)

        // инициализация виджетов
        titleTickerData = view.findViewById(R.id.title_ticker_data)
        if (isSecurities) {
            titleTickerData.text = "Securities"
//            isSecurities = false // отменяем установленный флаг для показа итемов Securities
        }
        if (isMarketData) {
            titleTickerData.text = "MarketData"
//            isMarketData = false // отменяем установленный флаг для показа итемов MarketData
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_data)

        // Получаем данные из arguments
        val tickerData = arguments?.getSerializable("ticker_data") as? ArrayList<List<String>>

        // Логирование полученных данных
//        Log.d("aaa", "-Received data: $tickerData")

        if (!tickerData.isNullOrEmpty()) {
            recyclerView.adapter = AdapterData(tickerData)
        } else {
            Log.e("aaa", "Data is null or empty")
        }

        if (!tickerData.isNullOrEmpty()) {
            // Проверяем, что каждый элемент списка содержит хотя бы 2 элемента
            val validData = tickerData.filter { it.size >= 2 }

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = AdapterData(validData)
        } else {
            // Логирование ошибки или установка пустого представления, если данных нет
            Log.e("aaa", "Ticker data is null or empty")
        }
    }
}





