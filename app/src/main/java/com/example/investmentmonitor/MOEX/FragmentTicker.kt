package com.example.investmentmonitor.MOEX

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.investmentmonitor.*
import isFuturesTraded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections

class FragmentTicker : Fragment() {

    companion object {
        // Метод для создания нового экземпляра фрагмента с передачей данных
        fun newInstance(data: String): Fragment {
            val fragment = Fragment()
            val args = Bundle()
            args.putString("key_data", data)
            fragment.arguments = args
            return fragment
        }
    }

    private var job: Job? = null

    private lateinit var title: TextView
    private lateinit var subTitle: TextView

    private lateinit var viewModel: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var dbHelper: TickerDatabaseHelper

    private var hasMoved = false // флаг определяет движение касания по экрану
    private var isMoved = false // флаг разрешает или блокирует движение по экрану
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    private val customLongPressTimeout = 500L // 1000 миллисекунд = 1 секунда
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment, container, false)

        title = view.findViewById(R.id.title)
        title.text = tv_title
        subTitle = view.findViewById(R.id.sub_title)
        subTitle.text = tv_sub_title

        //--- Инициализировать ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
        // Инициализировать RecyclerView
        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        dbHelper = TickerDatabaseHelper(requireContext())

        adapter = Adapter(
            mutableListOf(),
            requireContext(),
            onItemClicked = { ticker ->
                onTickerClicked(ticker)
            },
            onItemLongClicked = { ticker, position ->
                onTickerLongClicked(ticker, position)
            }
        )
        recyclerView.adapter = adapter

        //--- Наблюдаем за изменениями цвета
        viewModel.colorData.observe(viewLifecycleOwner) { color ->
            if (moexTicker.isNotEmpty() && moexTicker[0].board == nameBoard()) {
                // Обновляем цвет только для выбранного элемента
                if (currentPosition != null) {
                    // сохраняем цвет в переменной
                    backupTicker[currentPosition!!].flagColor = color
                    backupTicker[currentPosition!!].archiveColor = color
                    // Передаем цвет и позицию в адаптер
                    adapter.notifyItemChanged(currentPosition!!) // Обновляем только один элемент
                }
            }
        }

        // Наблюдаем за изменениями строкового значения
        viewModel.stringValue.observe(viewLifecycleOwner) { stringValue ->
            if (moexTicker.isNotEmpty() && moexTicker[0].board == nameBoard()) {
                if (currentPosition != null && backupTicker.isNotEmpty()) {
                    val position = currentPosition!!
                    if (backupTicker.size > position) {
                        val ticker = backupTicker[position]
                        ticker.titleItem = stringValue

                        // передаём id в метод
//                        Log.e("aaa", "id: ${ticker.id}")
//                        val id = ticker.id ?: return@observe  // если id почему-то null, не продолжаем

                        val id = position
                        // Обновляем только поле titleItem в базе
                        TickerDatabaseHelper(requireContext()).updateTitleItem(ticker.board, ticker.ticker, id, stringValue)

                        // Обновляем UI
                        adapter.updateTicker()
                    }
                }
            }
        }

        // перемещаем итем фрагмента в самый верх
        viewModel.tickersUpAndDown.observe(viewLifecycleOwner) { updatedTickers ->
            recyclerView.post {
                adapter.updateTicker(false)
            }
        }
        return view
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val context = requireContext()
//        Log.e("aaa", "3---backupTicker: ${TickerDatabaseHelper(context).getTickersByBoard("")}")

        // Настройка ItemTouchHelper для перемещения итемов
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                // Меняем элементы местами в списках
                Collections.swap(backupTicker, fromPosition, toPosition)

                // Уведомляем адаптер о перемещении
                adapter.notifyItemMoved(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Свайпы отключены
            }

            // Этот метод вызывается, когда перетаскивание завершено
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                // Обновляем список только после завершения перемещения
                recyclerView.post {
                    adapter.updateTicker()

                    // Обновляем порядок тикеров в БД
//                    val dbHelper = TickerDatabaseHelper(requireContext())

                    // Удаляем старые записи по текущему board ❌❌❌❌❌❌❌❌
//                    dbHelper.deleteAllByBoard(nameBoard())

                    // Добавляем обновлённый список в БД
                    for (ticker in backupTicker) {
                        dbHelper.upsertTicker(ticker)
                    }
                }
            }
        }
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.tickers.observe(viewLifecycleOwner) { ticker ->
            if (ticker.isNotEmpty()) {
                moexTicker = ticker.toMutableList()
                adapter.updateTicker()
            }
            else {
                moexTicker.clear()
            }
        }

        // получаем данные с биржи
        viewModel.fetchTickers()

        // Обработка нажатия и движения по экрану
        recyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> hasMoved = true
                MotionEvent.ACTION_UP -> isMoved = false
            }
            isMoved // false - продолжает перемещение итемов
        }

//        val context = requireContext()
//        Log.e("aaa", "4---backupTicker: ${TickerDatabaseHelper(context).getTickersByBoard("TQBR")}")
    }

    // Обработка короткого клика по элементу
    private fun onTickerClicked(ticker: TickerResponse) {}

    // Обработка длинного клика по элементу
    private fun onTickerLongClicked(ticker: TickerResponse, position: Int) {
        currentPosition = position // запоминаем позицию для дальнейшего окрашивания флага
        hasMoved = false
        handler.postDelayed({
            if (hasMoved)
                return@postDelayed
            else {
                isMoved = true
                // создаем фрагмент меню в виде диалогового окна
                val menu = FragmentMenu.newInstance(ticker, position)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_menu, menu)
                    .addToBackStack(null)
                    .commit()
            }
            //}, ViewConfiguration.getLongPressTimeout().toLong())
            //}, longPressTimeout)
        }, customLongPressTimeout)
    }

    // для индексов получаем цену закрытия предыдущей торговой сессии
    fun loadPrices(tickersList: List<TickerResponse>) {
        lifecycleScope.launch {
            for (ticker in tickersList)
                try {
                    val secid = ticker.ticker // например, РТС
                    // Получаем диапазон доступных дат
                    val datesResponse = RetrofitClient2.api.getDates(secid)
                    val dateFrom = datesResponse.dates.data.firstOrNull()?.get(0) ?: return@launch
                    val dateTill = datesResponse.dates.data.firstOrNull()?.get(1) ?: return@launch

                    // Для примера возьмём dateTill и сделаем запрос
                    val closeResponse = RetrofitClient2.api.getClosePrice(secid, dateTill, dateTill)

                    val closePrice = closeResponse.history.data.firstOrNull()?.get(1) as? Double
                    // обновите UI с closePrice
                    ticker.lastPrice = closePrice.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Обработка ошибок (например, показать Toast)
                }
        }
    }


    override fun onResume() {
        super.onResume()

        // Запускаем корутину для повторного вызова fetchTickersStocks()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                // Вызываем функцию обновления данных с биржи
                viewModel.fetchTickers()

                // Обновляем список в нашей программе
                adapter.updateTicker(true)

                // Задержка на 1 минуту (60000 мс)
                delay(10000)
            }
        }
        // меняем цвет титла и иконки в меню
//        val activity = requireActivity() as MainActivity
//        activity.updateColors("FragmentStocks") //++++++++++++++++++++++++++++++++++++++++++++++++++++
    }

    override fun onPause() {
        super.onPause()
        // Останавливаем корутину, когда фрагмент не активен
        job?.cancel()
    }

    override fun onStart() {
        super.onStart()
        // Выгружаем сохраненные данные
        if (nameBoard().isNotEmpty()) {
//            val dbHelper = TickerDatabaseHelper(requireContext())
            backupTicker.clear()
            // Выгружаем сохраненные данные по текущему board
            backupTicker.addAll(dbHelper.getTickersByBoard(nameBoard()))
            // Сортируем список по id
            backupTicker = backupTicker.sortedBy { it.position }.toMutableList()
        }
        // Функция для изменения цвета иконок и текста меню
        if (nameFragment != "ActivityHome") {
            val activity = requireActivity() as MainActivity
            activity.updateColors(nameFragment)
        }
    }

    override fun onStop() {
        super.onStop()

        // присваиваем position по порядку позиции index
        backupTicker.forEachIndexed { index, ticker ->
            ticker.position = index
        }

        // Сохранение данных для восстановления в новой сессии
//        val dbHelper = TickerDatabaseHelper(requireContext())
        val db = dbHelper.writableDatabase

        backupTicker.forEach { ticker ->
            dbHelper.upsertTicker(ticker)
        }

//        Log.e("aaa","2------ ${ backupTicker }")
        db.close()
    }
//override fun onStop() {
//    super.onStop()
//
//    // обновляем позиции
//    backupTicker.forEachIndexed { index, ticker ->
//        ticker.position = index
//    }
//
//    // сохраняем в фоновом потоке
//    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
////        val dbHelper = TickerDatabaseHelper(requireContext())
//        val db = dbHelper.writableDatabase
//
//        db.beginTransaction()
//        try {
//            backupTicker.forEach { ticker ->
//                dbHelper.upsertTicker(ticker) // передаём уже открытый db
//            }
//            db.setTransactionSuccessful()
//        } finally {
//            db.endTransaction()
//            db.close()
//        }
//    }
//}


}
