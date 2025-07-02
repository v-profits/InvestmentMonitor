package com.example.investmentmonitor.case_sber

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.investmentmonitor.MainActivity
import com.example.investmentmonitor.R
import com.example.investmentmonitor.listTextFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class FragmentCase : Fragment() {

    private lateinit var tvAllCase: TextView
    private lateinit var tvAllDay: TextView
    private lateinit var tvIsDay: TextView

    private lateinit var llStock: LinearLayout
    private lateinit var llBond: LinearLayout
    private lateinit var llFund: LinearLayout
    private lateinit var llFutures: LinearLayout
    private lateinit var llCash: LinearLayout

    private lateinit var imageViewStock: ImageView
    private lateinit var imageViewBond: ImageView
    private lateinit var imageViewFund: ImageView
    private lateinit var imageViewFutures: ImageView
    private lateinit var imageViewCash: ImageView

    private lateinit var allStock: TextView
    private lateinit var profitStock: TextView
    private lateinit var percentStock: TextView
    private lateinit var allBond: TextView
    private lateinit var profitBond: TextView
    private lateinit var percentBond: TextView
    private lateinit var allFund: TextView
    private lateinit var profitFund: TextView
    private lateinit var percentFund: TextView
    private lateinit var allFutures: TextView
    private lateinit var profitFutures: TextView
    private lateinit var percentFutures: TextView
//    private lateinit var allFreeCash: TextView
//    private lateinit var profitFreeCash: TextView
    private lateinit var percentFreeCash: TextView

    // База данных
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerViewStocks: RecyclerView
    private lateinit var recyclerViewBonds: RecyclerView
    private lateinit var recyclerViewFunds: RecyclerView
    private lateinit var recyclerViewFutures: RecyclerView
    private lateinit var stockAdapter: AdapterStock
    private lateinit var bondAdapter: AdapterBond
    private lateinit var fundAdapter: AdapterFund
    private lateinit var futuresAdapter: AdapterFutures
    private var assetList = listOf<Assets>()
    private lateinit var tvFreeCash: TextView
    private var job: Job? = null
    private lateinit var viewModel: ViewModel
    private lateinit var case: List<CaseAll>


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_case, container, false)

        tvAllCase = view.findViewById(R.id.tvAllCase)
        tvAllDay = view.findViewById(R.id.tvAllDay)
        tvIsDay = view.findViewById(R.id.tvIsDay)

        llStock = view.findViewById(R.id.llStock)
        llBond = view.findViewById(R.id.llBond)
        llFund = view.findViewById(R.id.llFund)
        llFutures = view.findViewById(R.id.llFutures)
        llCash = view.findViewById(R.id.llCash)

        imageViewStock = view.findViewById(R.id.imageViewStock)
        imageViewBond = view.findViewById(R.id.imageViewBond)
        imageViewFund = view.findViewById(R.id.imageViewFund)
        imageViewFutures = view.findViewById(R.id.imageViewFutures)
        imageViewCash = view.findViewById(R.id.imageViewCash)

        allStock = view.findViewById(R.id.allStock)
        profitStock = view.findViewById(R.id.profitStock)
        percentStock = view.findViewById(R.id.percentStock)
        allBond = view.findViewById(R.id.allBond)
        profitBond = view.findViewById(R.id.profitBond)
        percentBond = view.findViewById(R.id.percentBond)
        allFund = view.findViewById(R.id.allFund)
        profitFund = view.findViewById(R.id.profitFund)
        percentFund = view.findViewById(R.id.percentFund)
        allFutures = view.findViewById(R.id.allFutures)
        profitFutures = view.findViewById(R.id.profitFutures)
        percentFutures = view.findViewById(R.id.percentFutures)
//        allFreeCash = view.findViewById(R.id.allFreeCash)
//        profitFreeCash = view.findViewById(R.id.profitFreeCash)
        percentFreeCash = view.findViewById(R.id.percentFreeCash)

        //--- Инициализировать ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
        // Инициализировать RecyclerView
        recyclerViewStocks = view.findViewById(R.id.recyclerViewStocks)
        recyclerViewBonds = view.findViewById(R.id.recyclerViewBonds)
        recyclerViewFunds = view.findViewById(R.id.recyclerViewFunds)
        recyclerViewFutures = view.findViewById(R.id.recyclerViewFutures)
        tvFreeCash = view.findViewById(R.id.tvFreeCash) // инициализация виджета


        val activity = requireActivity() as MainActivity
        // База данных
        dbHelper = DatabaseHelper(activity)

        // Загружаем данные
        // загружаем в виджет свободные средства портфеля
        tvFreeCash.text = dbHelper.updateTextViewFreeCash()

        // обновляем данные в адаптерах
        assetList = dbHelper.getAllAssets()
        case = convertToCaseAll(assetList, listOf(".*\\акция\\b.*")) // Копирование всех записей таблицы Assets в класс CaseAll
        stockAdapter = AdapterStock(requireContext(), dbHelper, case) // обновляем данные в адаптере
        case = convertToCaseAll(assetList, listOf(".*\\облигация\\b.*"))
        bondAdapter = AdapterBond(requireContext(), dbHelper, case)
        case = convertToCaseAll(assetList, listOf(".*\\пай\\b.*", ".*\\etf\\b.*", ".*\\фонд\\b.*"))
        fundAdapter = AdapterFund(requireContext(), dbHelper, case)
        case = convertToCaseAll(assetList, listOf(".*\\фьючерс\\b.*"))
        futuresAdapter = AdapterFutures(requireContext(), dbHelper, case)

        // по умолчанию RecyclerView оптимизирован для прокрутки, здесь мы ее останавливаем
        recyclerViewStocks.layoutManager = LinearLayoutManager(context)
        recyclerViewStocks.adapter = stockAdapter
        recyclerViewBonds.layoutManager = LinearLayoutManager(context)
        recyclerViewBonds.adapter = bondAdapter
        recyclerViewFunds.layoutManager = LinearLayoutManager(context)
        recyclerViewFunds.adapter = fundAdapter
        recyclerViewFutures.layoutManager = LinearLayoutManager(context)
        recyclerViewFutures.adapter = futuresAdapter

        // поворот стрелки и сворачивание списка итемов акций
        imageViewStock.setOnClickListener { rotateArrow(recyclerViewStocks, imageViewStock, 1) }
        imageViewBond.setOnClickListener { rotateArrow(recyclerViewBonds, imageViewBond, 2) }
        imageViewFund.setOnClickListener { rotateArrow(recyclerViewFunds, imageViewFund, 3) }
        imageViewFutures.setOnClickListener { rotateArrow(recyclerViewFutures, imageViewFutures, 4) }
        imageViewCash.setOnClickListener { rotateArrow2(tvFreeCash, imageViewCash) }

        // Настраиваем фильтр
        val etFilter = view.findViewById<EditText>(R.id.etFilter)
        etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = assetList.filter { it.name.contains(s.toString(), ignoreCase = true) }
//                adapter.updateData(filteredList)//////////////////////////////////////////////////////////////
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Сортировка по количеству
        view.findViewById<Button>(R.id.btnSortByProfit).setOnClickListener {
            val sortedList = assetList.sortedByDescending { it.quantity }
//            adapter.updateData(sortedList)/////////////////////////////////////////////////////////////////
        }
        // Сортировка по имени
        view.findViewById<Button>(R.id.btnSortByName).setOnClickListener {
            val sortedList = assetList.sortedBy { it.name }
//            adapter.updateData(sortedList)/////////////////////////////////////////////////////////////////
        }
        // Очистить данные
        view.findViewById<Button>(R.id.btnClear).setOnClickListener {
            // очистить таблицы
            dbHelper.clearTable()
            // считаем по таблице CashFlow свободные средства и записываем в таблицу FreeCash
            dbHelper.updateFreeCashFromCashFlow()
            tvFreeCash.text = dbHelper.updateTextViewFreeCash()

            try {
                // Обновляем список портфеля после удаления данных из других таблиц
                // обновляем данные в адаптерах
                updateCase()
            } catch (e: Exception) {
                Log.e("aaa", "-Исключение при обновлении данных: ${e.message}")
            }
            Toast.makeText(activity, "Данные удалены", Toast.LENGTH_SHORT).show()
        }
        // Импорт данных
        view.findViewById<Button>(R.id.btnImport).setOnClickListener {
            // запрашиваем разрешение на чтение файлов и сохраняем имена файлов брокера в список
            val files = listTextFiles(activity) // список моих существующих общедоступных файлов "_M.html" в памяти флешки
            if (files.isEmpty()) { // если файлов нет
                Toast.makeText(activity, "Файлы не найдены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // выходим из слушателя
            } else { // если файлы есть
                CoroutineScope(Dispatchers.IO).launch {
                    for (file in files) { // перебираем все найденные файлы
                        // Путь к вашему HTML-файлу
//                        val filePath = "/storage/emulated/0/Download/40467CZ_010124_310124_M.html"
                        val filePath =
                            "/storage/emulated/0/Download/InvestmentMonitor/Sber/$file" // Укажите путь к вашему отчету
                        //проверяем наличие этих файлов уже в таблице имен файлов для исключения дублирования
                        val result = dbHelper.checkDuplicateFiles(file)
                        if (result) {
                            // Запуск обновления данных и обновления UI
                            try {
                                // Импорт данных из файла в таблицы
                                dbHelper.importTablesFromReport(filePath)
                                // Считаем по таблице CashFlow свободные средства и записываем в таблицу FreeCash
                                dbHelper.updateFreeCashFromCashFlow()
                                // Обновляем адаптер в основном потоке
                                withContext(Dispatchers.Main) {
                                    // Обновляем адаптер
                                    updateCase()
                                }
                                // загружаем в виджет свободные средства портфеля
                                tvFreeCash.text = dbHelper.updateTextViewFreeCash()
                            } catch (e: Exception) {
                                Log.e("aaa", "---Исключение при обновлении данных: ${e.message}")
                            }
                        }
                    }
                    // вывод таблицы в лог
//                    dbHelper.logTableContent()
                    // выплаты на внешний счет
                    Log.e("aaa", "Выплаты на внешний счет: ${dbHelper.getAllPayments()}")

                    // Обновляем в основном потоке
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Данные импортированы", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dbHelper.getFilesHTML()
        }
        // данные для виджетов за день и за все время
        view.findViewById<TextView>(R.id.tvIsDay).setOnClickListener {
            isDay = !isDay
            if (isDay) tvIsDay.text = "день"
            else tvIsDay.text = "все время"
            updateCase()
        }
        return view
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)

        // Наблюдаем за изменениями данных
        viewModel.tickers.observe(viewLifecycleOwner) { ticker ->
            // Обновляем UI с новыми данными
            stockAdapter.updateStock(ticker)
            bondAdapter.updateBond(ticker)
            fundAdapter.updateFund(ticker)
            futuresAdapter.updateFutures(ticker)
            // Обновление данных
            updateCase()
        }
    }

    // поворот стрелки и сворачивание списка итемов акций
    private fun rotateArrow(recyclerView: RecyclerView, imageView: ImageView, number: Int) {
        if (recyclerView.visibility == View.VISIBLE) {
            // Поворот вниз
            isRotateArrow(false, number) // запоминаем положение стрелки
            imageView.setImageResource(R.drawable.outline_keyboard_arrow_down_24)
            recyclerView.visibility = View.GONE
        } else {
            // Поворот вверх
            isRotateArrow(true, number) // запоминаем положение стрелки
            imageView.setImageResource(R.drawable.outline_keyboard_arrow_up_24)
            recyclerView.visibility = View.VISIBLE
        }
    }

    // запоминаем поворот стрелки и сворачивание списка виджета свободных средств
    private fun isRotateArrow(flag: Boolean, number: Int) {
        when(number) {
            1 -> isStock = flag
            2 -> isBond = flag
            3 -> isFund = flag
            4 -> isFutures = flag
        }
    }

    // поворот стрелки и сворачивание списка итемов акций
    private fun rotateArrow2(textView: TextView, imageView: ImageView) {
        if (textView.visibility == View.VISIBLE) {
            // Поворот вниз
            isCase = false // запоминаем положение стрелки
            imageView.setImageResource(R.drawable.outline_keyboard_arrow_down_24)
            textView.visibility = View.GONE
        } else {
            // Поворот вверх
            isCase = true // запоминаем положение стрелки
            imageView.setImageResource(R.drawable.outline_keyboard_arrow_up_24)
            textView.visibility = View.VISIBLE
        }
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

    // считаем суммы и скрываем виджеты по стрелке
    fun sumAndVisibility(case: List<CaseAll>, list: MutableList<Double>, ll: LinearLayout, rv: RecyclerView, isFlag: Boolean) {
        for (c in case) {
            list[0] += c.price * c.quantity
            list[1] += c.price_prev * c.quantity
            list[2] += c.price_open * c.quantity
            list[3] += c.price_last * c.quantity
        }
        if (case.size == 0) {
            ll.visibility = View.GONE
            rv.visibility = View.GONE
        } else {
            ll.visibility = View.VISIBLE
            if (isFlag)
                rv.visibility = View.VISIBLE
            else
                rv.visibility = View.GONE
        }
    }

    // вставляем данные и цвет в виджеты
    fun viewDataAndColor(tvAllAsset: TextView, tvProfit: TextView, tvPercent: TextView, listAsset: MutableList<Double>, all: Double) {
        tvAllAsset.text = formatNumberString(((listAsset[3] * 100).toInt() / 100.0).toString()) + " ₽"
        val temp = if (isDay)
            listAsset[3] - listAsset[1] // день
        else listAsset[3] - listAsset[0] // все время
        tvProfit.text = formatNumberString(((temp * 100).toInt() / 100.0).toString()) + " ₽ • " +
                formatNumberString(((abs(temp) / listAsset[3] * 10000 ).toInt() / 100.0).toString()) + " %"

        tvPercent.text = formatNumberString(((listAsset[3] / all * 10000).toInt() / 100.0).toString()) + " %"
        // Установить цвет текста из ресурса
        viewColor(tvProfit, temp, R.color.color1_, R.color.white, R.color.color3_)
    }

    // вставляем цвет в виджеты
    fun viewColor(tvProfit: TextView, temp: Double, color1: Int, color2: Int, color3: Int) {
        // Установить цвет текста из ресурса
        val color = if (temp > 0.0) ContextCompat.getColor(requireContext(), color3)
        else if (temp < 0.0) ContextCompat.getColor(requireContext(), color1)
        else ContextCompat.getColor(requireContext(), color2)
        tvProfit.setTextColor(color)
    }

    // Обновляем адаптер
    @Synchronized // доступ к базе данных синхронизирован
    fun updateCase() {
        // переменные для хранения итоговых данных прибыли от акций
        val listStock = mutableListOf(0.0, 0.0, 0.0, 0.0)
        val listBond = mutableListOf(0.0, 0.0, 0.0, 0.0)
        val listFund = mutableListOf(0.0, 0.0, 0.0, 0.0)
        val listFutures = mutableListOf(0.0, 0.0, 0.0, 0.0)
        // свободные средства из таблицы
        val valueFreeCash = dbHelper.updateTextViewFreeCash().toDouble()
        // данные эмитентов из таблицы
        assetList = dbHelper.getAllAssets()

        // Здесь обновляем текстовые поля и адаптеры с новыми данными

        case = convertToCaseAll(assetList, listOf(".*\\акция\\b.*")) // Копирование всех записей таблицы Assets в класс CaseAll
        sumAndVisibility(case, listStock, llStock, recyclerViewStocks, isStock) // считаем суммы и скрываем виджеты по стрелке
//        Log.e("aaa","case --- $case")
//        Log.e("aaa","listStock --- $listStock")
        stockAdapter.updateData(case) // обновляем данные в адаптере

        case = convertToCaseAll(assetList, listOf(".*\\облигация\\b.*"))
        sumAndVisibility(case, listBond, llBond, recyclerViewBonds, isBond) // считаем суммы и скрываем виджеты по стрелке
        for (i in 0 until listBond.size) {
            listBond[i] = listBond[i] * 10.0 // все цены облигаций умножаем на 10 (потом надо найти другой способ) ////////////////////////////////////////////////////////
        }
//        for (c in case) {
//            Log.e("aaa","case --- $c")
//        }
//        Log.e("aaa","listBond --- $listBond")
        bondAdapter.updateData(case) // обновляем данные в адаптере

        case = convertToCaseAll(assetList, listOf(".*\\пай\\b.*", ".*\\etf\\b.*", ".*\\фонд\\b.*"))
        sumAndVisibility(case, listFund, llFund, recyclerViewFunds, isFund) // считаем суммы и скрываем виджеты по стрелке
//        Log.e("aaa","case --- $case")
//        Log.e("aaa","listFund --- $listFund")
        fundAdapter.updateData(case) // обновляем данные в адаптере

        case = convertToCaseAll(assetList, listOf(".*\\фьючерс\\b.*"))
        // считаем суммы и скрываем виджеты по стрелке
        sumAndVisibility(case, listFutures, llFutures, recyclerViewFutures, isFutures)
//        Log.e("aaa","case --- $case")
//        Log.e("aaa","listFutures --- $listFutures")
        futuresAdapter.updateData(case) // обновляем данные в адаптере

        sumAllStock = listStock[3]
        sumAllBond = listBond[3]
        sumAllFund = listFund[3]
        sumAllFutures = listFutures[3]

        val all = valueFreeCash + sumAllStock + sumAllBond + sumAllFund + sumAllFutures
        val allDay = if (isDay) (all - listStock[1] - listBond[1] - listFund[1] - listFutures[1])
        else (all - listStock[0] - listBond[0] - listFund[0] - listFutures[0])
        tvAllCase.text = formatNumberString(((all * 100).toInt() / 100.0).toString()) + " ₽"
        tvAllDay.text = formatNumberString((((allDay * 100).toInt() / 100.0)).toString()) + " ₽ • " +
                formatNumberString(((abs(allDay * 10000 / all).toInt()) / 100.0).toString()) + " %"
        // Установить цвет текста из ресурса
        viewColor(tvAllDay, allDay, R.color.color1, R.color.color2, R.color.color3)
        viewColor(tvIsDay, allDay, R.color.color1, R.color.color2, R.color.color3)

        if (isDay) tvIsDay.text = "день"
        else tvIsDay.text = "все время"

        viewDataAndColor(allStock, profitStock, percentStock, listStock, all) // вставляем данные и цвет в виджеты
        viewDataAndColor(allBond, profitBond, percentBond, listBond, all) // вставляем данные и цвет в виджеты
        viewDataAndColor(allFund, profitFund, percentFund, listFund, all) // вставляем данные и цвет в виджеты
        viewDataAndColor(allFutures, profitFutures, percentFutures, listFutures, all) // вставляем данные и цвет в виджеты

        // виджеты свободных средств
        tvFreeCash.text = valueFreeCash.toString()
        if (valueFreeCash == 0.0) {
            llCash.visibility = View.GONE
            tvFreeCash.visibility = View.GONE
        } else {
            llCash.visibility = View.VISIBLE
            if (isCase)
                tvFreeCash.visibility = View.VISIBLE
            else
                tvFreeCash.visibility = View.GONE
        }
        percentFreeCash.text = formatNumberString(((valueFreeCash * 10000 / all).toInt() / 100.0).toString()) + " %"

//        Log.e("aaa","valueFreeCash --- $valueFreeCash")
//        Log.e("aaa","all --- $all")
//        Log.e("aaa","---------------------------")
    }

    override fun onResume() {
        super.onResume()
        // Запускаем корутину для повторного вызова fetchTickersStocks()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
//                Log.e("aaa","Задержка на 1 минуту (60000 мс)")
                // Вызываем функцию обновления данных с биржи
                viewModel.fetchTickers()

                // Задержка на 1 минуту (60000 мс)
                delay(60000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Останавливаем корутину, когда фрагмент не активен
        job?.cancel()
    }

    override fun onStart() {
        super.onStart()

        // Функция для изменения цвета иконок и текста меню
        val activity = requireActivity() as MainActivity
        activity.updateColors("FragmentCase")
    }

    override fun onStop() {
        super.onStop()
    }
}
