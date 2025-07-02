package com.example.investmentmonitor.MOEX

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.investmentmonitor.R
import kotlin.math.roundToInt

class ViewHolder(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {

    private val tickerTextView: TextView = itemView.findViewById(R.id.ticker)
    private val nameTextView: TextView = itemView.findViewById(R.id.name)
    private val priceMarketDataTextView: TextView = itemView.findViewById(R.id.price)
    private val valueTextView: TextView = itemView.findViewById(R.id.value)
    private val logoImageView: ImageView = itemView.findViewById(R.id.companyLogo)
//    private val profitTextView: TextView = itemView.findViewById(R.id.profit)
    private val titleItem: TextView = itemView.findViewById(R.id.title_item)

    private val ContainerItem: LinearLayout = itemView.findViewById(R.id.container_item)
    private val ContainerTitleItem: LinearLayout = itemView.findViewById(R.id.container_title_item)

    fun bind(ticker: TickerResponse) {
        if (ticker.board in listOf("TQOB", "TQCB", "PACT", "SPOB", "TQIR", "TQIY", "TQOD", "TQOE", "TQOY", "TQRD", "TQUD")) {
            tickerTextView.text = ticker.name
            nameTextView.text = "${ticker.profit}%  -  ${bondsPeriod(ticker.period)}" // для облигаций, купонный процент и дата погашения
            titleItem.text = ticker.titleItem
        } else {
            tickerTextView.text = ticker.ticker
            nameTextView.text = ticker.name
            titleItem.text = ticker.titleItem
        }

        if (ticker.visibilityTitle) {
            ContainerItem.visibility = View.GONE
            ContainerTitleItem.visibility = View.VISIBLE
        }
        else {
            ContainerItem.visibility = View.VISIBLE
            ContainerTitleItem.visibility = View.GONE
        }

//            priceMarketDataTextView.text = formatNumber(ticker.lastPrice)
//            valueTextView.text = ticker.value
        val result1 = ticker.currentPrice.toDoubleOrNull()
        val result2 = ticker.lastPrice.toDoubleOrNull()

        var simbol = ""
        if (ticker.simbolBanca != null) {
            simbol = when(ticker.simbolBanca) {
                "RUB" -> "₽"
                "SUR" -> "₽"
                "USD" -> "$"
                "EUR" -> "€"
                "CNY" -> "¥"
                else -> "₽"
            }
        }


//        if (result1 != null && result2 != null) {
        if (result1 != null && result2 != null && result1 != 0.0) {

            // Подсчет количества знаков после запятой
            val decimalPlaces = ticker.decimals

            var result = result2 - result1

            // Округляем результат до нужного количества знаков после запятой, убирая лишние нули
            var digits = 1
            for (i in 1..decimalPlaces)
                digits *= 10
            var numDouble = result*digits
            var numInt = numDouble.roundToInt()
            result = numInt.toDouble()/digits

            // Процентное изменение
            var percent = (result2 - result1) / result1 * 100

            numDouble = percent*100
            try {
                numInt = numDouble.roundToInt() // ошибка
            } catch (_: Exception) { }
            percent = numInt.toDouble()/100

            // Получаем кастомные цвета из ресурсов
            val customGreen = ContextCompat.getColor(context, R.color.green)
            val customRed = ContextCompat.getColor(context, R.color.red)
            val customGrey = ContextCompat.getColor(context, R.color.gray)

            // Устанавливаем цвет текста для result и для percent
            val resultColor = if (result > 0) customGreen // зелёный для положительных значений
            else if (result < 0) customRed // красный для отрицательных значений
            else customGrey // серый

            // Проверяем знак result
            val formattedResult =
                if (digits > 1) {
                    if (result > 0) "+$result" // добавляем + перед положительным значением
                    else "$result" // оставляем отрицательное значение как есть
                } else {
                    if (result > 0) "+${result.toInt()}" // добавляем + перед положительным значением
                    else "${result.toInt()}" // оставляем отрицательное значение как есть
                }

            // Проверяем знак percent
            val formattedPercent = if (percent > 0) "+$percent%" // добавляем + перед положительным значением процента
            else "$percent%" // оставляем отрицательное значение как есть

            // Устанавливаем цвет текста и текст для valueTextView
            valueTextView.setTextColor(resultColor)

            // Выводим результат и процент вместе в нужном формате
            valueTextView.text = "$formattedResult  $formattedPercent"

            // если идут торги, ставим цену последней сделки
            if (formatNumber(ticker.lastPrice) != "—")
                priceMarketDataTextView.text = "${formatNumber(ticker.lastPrice)} $simbol"
            else
                priceMarketDataTextView.text = formatNumber(ticker.lastPrice)
        }
        else {
            val customGrey = ContextCompat.getColor(context, R.color.gray)
            valueTextView.setTextColor(customGrey)
            valueTextView.text = "—"

            // если торгов нет, ставим цену последней сделки предыдущего торгового дня
            if (formatNumber(ticker.currentPrice) != "—")
                priceMarketDataTextView.text = "${formatNumber(ticker.currentPrice)} $simbol"
            else
                priceMarketDataTextView.text = formatNumber(ticker.currentPrice)
        }

        // Получаем идентификатор ресурса на основе имени тикера
        val tickerName = ticker.ticker.toLowerCase()
        val xmlResId = context.resources.getIdentifier(tickerName, "drawable", context.packageName)
        val xmlResIdDefault = when (ticker.board) {
            "TQTF" -> // мои фонды
                context.resources.getIdentifier("rus_flag", "drawable", context.packageName)
            "TQOB" -> // мои облигации
                context.resources.getIdentifier("rus_4", "drawable", context.packageName)
            else ->
                context.resources.getIdentifier("img", "drawable", context.packageName)
        }

        // загрузка изображения и обрезки до круга
        if (xmlResId != 0) {
            try {
//                    logoImageView.setImageResource(xmlResId)
                // Используем Glide для загрузки изображения и обрезки его до круглой формы
                Glide.with(context)
                    .load(xmlResId)
                    .apply(RequestOptions.circleCropTransform()) // Обрезка до круга
                    .into(logoImageView)
//                Log.d("aaa", "XML loaded successfully for $tickerName")
            } catch (e: Exception) {
//                    logoImageView.setImageResource(R.drawable.img) // Установите изображение по умолчанию
                // Используем Glide для загрузки изображения и обрезки его до круглой формы
                Glide.with(context)
                    .load(xmlResIdDefault)
                    .apply(RequestOptions.circleCropTransform()) // Обрезка до круга
                    .into(logoImageView)
//                Log.e("aaa", "Ошибка при загрузке XML: ${e.message}")
            }
        } else {
//                logoImageView.setImageResource(R.drawable.img) // Установите изображение по умолчанию
            // Используем Glide для загрузки изображения и обрезки его до круглой формы
            Glide.with(context)
                .load(xmlResIdDefault)
                .apply(RequestOptions.circleCropTransform()) // Обрезка до круга
                .into(logoImageView)
//            Log.e("aaa", "Resource not found for $tickerName")
        }
    }
}