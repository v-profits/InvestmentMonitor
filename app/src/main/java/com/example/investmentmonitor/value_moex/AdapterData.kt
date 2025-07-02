package com.example.investmentmonitor.value_moex

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.investmentmonitor.R

class AdapterData(private val data: List<List<String>>) :
    RecyclerView.Adapter<AdapterData.ViewHolder>() {

    // ViewHolder содержит ссылки на элементы макета item_data
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val columnName: TextView = view.findViewById(R.id.column_name)
        val columnValue: TextView = view.findViewById(R.id.column_value)
    }

    // Создаём новый ViewHolder при необходимости
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
        return ViewHolder(view)
    }

    // Привязываем данные к элементу списка
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < data.size) {
            val item = data[position]

            if (item.size >= 2) {
                holder.columnName.text = item[0]
                holder.columnValue.text = item[1]
            } else {
                holder.columnName.text = "N/A"
                holder.columnValue.text = "N/A"
            }
        } else {
            Log.e("aaa", "Invalid position $position for data size ${data.size}")
        }
    }

    // Возвращаем количество элементов списка
    override fun getItemCount(): Int = data.size
}