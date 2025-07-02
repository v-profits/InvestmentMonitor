package com.example.investmentmonitor.MOEX

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.investmentmonitor.R

class Adapter(
    private val tickers: MutableList<TickerResponse>,
    private val context: Context,
    private val onItemClicked: (TickerResponse) -> Unit,
    private val onItemLongClicked: (TickerResponse, Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticker, parent, false)
        return ViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        val ticker = tickers[position]
        val ticker = backupTicker[position]
        holder.bind(ticker)

        //--- Изменяем цвет флага, если он установлен
        // Проверяем, есть ли у элемента цвет, и применяем его
        ticker.flagColor?.let { color ->
            holder.itemView.findViewById<ImageView>(R.id.flag).setColorFilter(color, PorterDuff.Mode.SRC_IN)
        } ?: run {
            // Если цвета нет, сбрасываем фильтр
            holder.itemView.findViewById<ImageView>(R.id.flag).clearColorFilter()
        }

        //--- клики

        // Обработка короткого клика на элемент
        holder.itemView.setOnClickListener {
//            notifyItemChanged(selectedPosition)
//            selectedPosition = holder.adapterPosition
//            notifyItemChanged(selectedPosition)
            onItemClicked(ticker)
        }

        // Обработка длинного нажатия на элемент
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(ticker, position)
            true
        }

        //--- устанавливаем цвет флажков с нажатия иконки
        // Находим TextView по ID
        holder.itemView.findViewById<FrameLayout>(R.id.flag_company).apply {
            // Устанавливаем обработчик нажатия
            setOnClickListener {
                // Меняем цвет флажка иконки при нажатии
//                val imageView = findViewById<ImageView>(R.id.flag)
//                imageView.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN)

                // Обновляем цвет только для выбранного элемента
                if (backupTicker[position].flagColor == null) {
                    if (backupTicker[position].archiveColor == null) {
//                        backupTickerStocks[position].flagColor = R.color.flag1
//                        backupTickerStocks[position].flagColor = -48896
                        backupTicker[position].flagColor = -37316
                    } else {
                        backupTicker[position].flagColor = backupTicker[position].archiveColor
                    }
                } else {
                    backupTicker[position].flagColor = null
                }
                PreferencesManager.getInstance(context).setTickerResponse(nameBoard(), backupTicker)

                // Обновляем и сохраняем список
                updateTicker()
            }
        }
    }

    override fun getItemCount(): Int = backupTicker.size

    // обновляем все изменения в данных тиккеров и итемах
    @SuppressLint("NotifyDataSetChanged")
    fun updateTicker(check: Boolean = false) {

        //--- обнуляем память тиккеров
        if (isResetTickers) {
            backupTicker.clear()
            PreferencesManager.getInstance(context).setTickerResponse(nameBoard(), backupTicker)
            return
        }

        // обновляем данные цен по тиккерам (акциям)
        fun updataPrice() {
            for (i in 0 until moexTicker.size) {
                for (j in 0 until backupTicker.size) {
                    if (backupTicker[j].ticker == moexTicker[i].ticker) {
                        backupTicker[j].currentPrice = moexTicker[i].currentPrice
                        backupTicker[j].lastPrice = moexTicker[i].lastPrice
                        break
                    }
                }
            }
        }

        //--- приложение загружено, но надо показать изменения в итемах на экране
        if (check && backupTicker.isNotEmpty()) {
            updataPrice() // обновляем данные цен по акциям
            // фиксируем изменения
            notifyDataSetChanged()
                return
        }

        // Логика для обработки очередности тикеров
        // синхронизируем список данных с биржи и список сохраненных данный акции
        if (moexTicker.isNotEmpty() && moexTicker[0].board == nameBoard()) {

            // удаляем из сохраненного списока данные отсутствующие на бирже
            val iterator = backupTicker.iterator()
            while (iterator.hasNext()) {
                val backup = iterator.next()
                var flag = false

                for (newTicker in moexTicker) {
                    if (backup.ticker == newTicker.ticker && backup.board == newTicker.board || backup.ticker == "Title") {
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    iterator.remove() // безопасное удаление элемента через итератор
                }
            }

            // добавляем в сохраненный список отсутствующие данные но присутсвующие на бирже
            for (i in 0 until moexTicker.size) {
                var flag = false
                for (j in 0 until backupTicker.size)
                    if (backupTicker[j].ticker == moexTicker[i].ticker) {
                        if (backupTicker[j].name != moexTicker[i].name)
                            backupTicker[j].name = moexTicker[i].name
                        if (backupTicker[j].decimals != moexTicker[i].decimals)
                            backupTicker[j].decimals = moexTicker[i].decimals
                        if (backupTicker[j].simbolBanca != moexTicker[i].simbolBanca)
                            backupTicker[j].simbolBanca = moexTicker[i].simbolBanca
                        if (nameBoard() in listOf("TQOB", "TQCB", "PACT", "SPOB", "TQIR", "TQIY", "TQOD", "TQOE", "TQOY", "TQRD", "TQUD")) { // облигации
                            if (backupTicker[j].profit != moexTicker[i].profit)
                                backupTicker[j].profit = moexTicker[i].profit
                            if (backupTicker[j].period != moexTicker[i].period)
                                backupTicker[j].period = moexTicker[i].period
                        }



                        flag = true
                        break
                    }
                if (!flag) {
                    backupTicker.add(backupTicker.size, moexTicker[i])
                }
            }
        }

        updataPrice() // обновляем данные цен по акциям
        notifyDataSetChanged()
    }
}
