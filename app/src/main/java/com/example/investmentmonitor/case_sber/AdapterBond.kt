package com.example.investmentmonitor.case_sber

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.investmentmonitor.R
import kotlin.math.abs

// Адаптер для RecyclerView
class AdapterBond(private val context: Context, private val dbHelper: DatabaseHelper, private var caseBonds: List<CaseAll>) : RecyclerView.Adapter<AdapterBond.AssetViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCaseBonds: List<CaseAll>) {
        caseBonds = newCaseBonds
        // Метод для сортировки элементов
        sortItemsByProfit()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asset, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {

        if (isDay) { // день
            holder.quantity.visibility = View.VISIBLE
            holder.profit_day.visibility = View.VISIBLE
            holder.price.visibility = View.GONE
            holder.profit_price.visibility = View.GONE
        } else { // все время
            holder.quantity.visibility = View.GONE
            holder.profit_day.visibility = View.GONE
            holder.price.visibility = View.VISIBLE
            holder.profit_price.visibility = View.VISIBLE
        }

        val case = caseBonds[position]
        val decimals = case.decimals
        val quantity = case.quantity
        val currency = case.currency

        var decimals_ = 1.0
        for (d in 0 until decimals)
            decimals_ *= 10

        val price = (case.price * decimals_ * 10).toInt() / decimals_ * 1.0 // умножаем на 10
        val price_prev = (case.price_prev * decimals_ * 10).toInt() / decimals_ * 1.0 // умножаем на 10
        val price_last = (case.price_last * decimals_ * 10).toInt() / decimals_ * 1.0 // умножаем на 10

        //----------------

        holder.name.text = case.name

        //----------------

        holder.quantity.text = "${formatNumberString(quantity.toString())} шт. • ${formatNumberString(price_last.toString())} ₽"

        //----------------

        val result = if (decimals_ != 0.0)
            ((price_last - price) * decimals_).toInt() / decimals_
        else 0.0

        val price_ = if (decimals_ != 0.0)
            ((price * decimals_).toInt() / decimals_ * 1.0)
        else price.toInt()

        holder.price.text = "${formatNumberString(result.toString())} ₽ • ${formatNumberString(price_.toString())} ₽"

        // Установить цвет текста из ресурса
        var color = if (result > 0.0) ContextCompat.getColor(context, R.color.color3)
        else if (result < 0.0) ContextCompat.getColor(context, R.color.color1)
        else ContextCompat.getColor(context, R.color.color2)
        holder.price.setTextColor(color)

        //----------------

        val percent: Double = if (result != 0.0) // чтобы избежать деления на 0
            abs(((100 - price * 100 / price_last) * 100).toInt() / 100.0)
        else 0.0

        holder.profit_price.text = "${formatNumberString(((result * quantity * 100).toInt() / 100.0).toString())} ₽ • ${formatNumberString(percent.toString())} %"
        holder.profit_price.setTextColor(color)

        //---------------- по profitAll надо отсортировать итемы

        val profitAll = price_last * quantity

        val procentAll: Double = if (sumAllBond != 0.0) // чтобы избежать деления на 0
                (profitAll * 10000 / sumAllBond).toInt() / 100.0
        else 0.0

        holder.profit_all.text = "${formatNumberString(((profitAll * 100).toInt() / 100.0).toString())} ₽ • ${formatNumberString(procentAll.toString())} %"

        //----------------

        val profitDay = if (price_last != 0.0)
            ((price_last - price_prev) * quantity * 100).toInt() / 100.0
        else 0.0

        val procentDay = if (price_last != 0.0) // чтобы избежать деления на 0
            abs(((100 - price_prev * 100 / price_last) * 100).toInt() / 100.0)
        else 0.0

        holder.profit_day.text = "${formatNumberString(profitDay.toString())} ₽ • ${formatNumberString(procentDay.toString())} %"

        // Установить цвет текста из ресурса
        color = if (profitDay > 0.0) ContextCompat.getColor(context, R.color.color3)
        else if (profitDay < 0.0) ContextCompat.getColor(context, R.color.color1)
        else ContextCompat.getColor(context, R.color.color2)
        holder.profit_day.setTextColor(color)

        //----------------

    }

    // Форматируем число для лучшей читаемости и заменяем точку на запятую
    fun formatNumberString(input: String): String {
        val parts = input.split(".")
        val integerPart = parts[0]
        val fractionalPart = if (parts.size > 1) parts[1] else ""

        // Форматируем целую часть числа с добавлением пробелов
        val formattedIntegerPart = integerPart.reversed().chunked(3).joinToString(" ").reversed()

        return if (fractionalPart.isNotEmpty()) {
            "$formattedIntegerPart,$fractionalPart" // Заменяем точку на запятую
        } else {
            formattedIntegerPart
        }
    }

    override fun getItemCount(): Int = caseBonds.size

    class AssetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_name)
        val quantity: TextView = view.findViewById(R.id.tv_quantity)
        val price: TextView = view.findViewById(R.id.tv_price)
        val profit_price: TextView = view.findViewById(R.id.tv_profit_price)
        val profit_day: TextView = view.findViewById(R.id.tv_profit_day)
        val profit_all: TextView = view.findViewById(R.id.tv_profit_all)
    }

    // обновляем все изменения в данных эмитентах порфеля
    @SuppressLint("NotifyDataSetChanged")
    // Обновление данных
    fun updateBond(bonds: List<TickerResponse>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction() // Начинаем транзакцию

        try {
            for (asset in caseBonds) {
                // Находим и обновляем соответствующую запись в stocks, если это необходимо
                val bond = bonds.find { it.ticker == asset.ticker }
                bond?.let {
                    // Обновляем необходимые данные
                    val pricePrev = bond.prevPrice.toDoubleOrNull() ?: 0.0
                    val priceOpen = bond.openPrice.toDoubleOrNull() ?: 0.0
                    val priceLast = bond.lastPrice.toDoubleOrNull() ?: 0.0
                    val decimals = bond.decimals

//                    // Запрос для обновления price_currency и decimals для строк, где ticker == "SBER"
//                    val query = """
//                            UPDATE Assets
//                            SET price_prev = ?, price_open = ?, price_last = ?, decimals = ?
//                            WHERE ticker = ?
//                        """
//
//                    try {
//                        val statement = db.compileStatement(query)
//                        statement.bindDouble(1, pricePrev)   // Устанавливаем цену закрытия предыдущего дня
//                        statement.bindDouble(2, priceOpen)   // Устанавливаем цену открытия дня
//                        statement.bindDouble(3, priceLast)   // Устанавливаем цену последней сделки
//                        statement.bindLong(4, decimals.toLong()) // Устанавливаем количество знаков после запятой
//                        statement.bindString(5, asset.ticker) // Устанавливаем тикер для условия WHERE
//                        statement.executeUpdateDelete()
//                    } catch (e: Exception) {
//                        Log.e("aaa", "Error updating stock data", e)
//                    }

                    // Строим запрос с учётом значений, которые не равны 0.0
                    val queryBuilder = StringBuilder("UPDATE Assets SET ")
                    val args = mutableListOf<Any>()

                    if (pricePrev != 0.0) {
                        queryBuilder.append("price_prev = ?, ")
                        args.add(pricePrev)
                    }
                    if (priceOpen != 0.0) {
                        queryBuilder.append("price_open = ?, ")
                        args.add(priceOpen)
                    }
                    if (priceLast != 0.0) {
                        queryBuilder.append("price_last = ?, ")
                        args.add(priceLast)
                    }
                    queryBuilder.append("decimals = ? WHERE ticker = ?")
                    args.add(decimals.toLong())
                    args.add(asset.ticker)

                    val query = queryBuilder.toString()

                    try {
                        val statement = db.compileStatement(query)
                        args.forEachIndexed { index, arg ->
                            when (arg) {
                                is Double -> statement.bindDouble(index + 1, arg)
                                is Long -> statement.bindLong(index + 1, arg)
                                is String -> statement.bindString(index + 1, arg)
                            }
                        }
                        statement.executeUpdateDelete()
                    } catch (e: Exception) {
                        Log.e("aaa", "Error updating stock data", e)
                    }
                }
            }

            db.setTransactionSuccessful() // Помечаем транзакцию как успешную
            sortItemsByProfit() // Метод для сортировки элементов
            notifyDataSetChanged() // Обновляем адаптер

        } catch (e: Exception) {
            Log.e("aaa", "Transaction failed", e)
        } finally {
            db.endTransaction() // Завершаем транзакцию после обработки всех записей
            db.close() // Закрываем базу данных
        }
    }

    // Метод для сортировки элементов
    @SuppressLint("NotifyDataSetChanged")
    fun sortItemsByProfit() {
        caseBonds = caseBonds.sortedByDescending { it.price_last * it.quantity }
        notifyDataSetChanged() // Обновляем адаптер для применения изменений
    }
}


//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.investmentmonitor.R
//
//// Адаптер для RecyclerView
//class AdapterBond(private val dbHelper: DatabaseHelper, private var assets: List<CaseAll>) : RecyclerView.Adapter<AdapterBond.AssetViewHolder>() {
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun updateData(newAssets: List<CaseAll>) {
//        assets = newAssets
//        notifyDataSetChanged()
//    }
//
//    class AssetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvName: TextView = view.findViewById(R.id.tv_name)
//        val tvISIN: TextView = view.findViewById(R.id.tv_quantity)
////        val tvProfitLoss: TextView = view.findViewById(R.id.tv_price)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asset, parent, false)
//        return AssetViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
//        val asset = assets[position]
//        holder.tvName.text = asset.name
//        holder.tvISIN.text = "Количество: ${asset.quantity}"
////        holder.tvProfitLoss.text = "Цена покупки: ${asset.price}"
//    }
//
//    override fun getItemCount(): Int = assets.size
//}
