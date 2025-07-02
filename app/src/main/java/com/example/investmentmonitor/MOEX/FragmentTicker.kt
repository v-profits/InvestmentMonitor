package com.example.investmentmonitor.MOEX

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private lateinit var viewModel: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter
    private lateinit var itemTouchHelper: ItemTouchHelper

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

        //--- Инициализировать ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
        // Инициализировать RecyclerView
        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

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
                // Обновляем UI или делаем что-то с полученным значением
                // Обновляем цвет только для выбранного элемента
                if (currentPosition != null && backupTicker.isNotEmpty()) {
                    if (backupTicker.size > currentPosition!!)
                        backupTicker[currentPosition!!].titleItem = stringValue
                    adapter.updateTicker()
                }
                PreferencesManager.getInstance(requireActivity()).setTickerResponse(nameBoard(), backupTicker)
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
                    PreferencesManager.getInstance(requireActivity()).setTickerResponse(nameBoard(), backupTicker)
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
                delay(60000)
            }
        }
        // меняем цвет титла и иконки в меню
        val activity = requireActivity() as MainActivity
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
        if (nameBoard() != "") {
            backupTicker.clear()
            backupTicker = PreferencesManager.getInstance(requireActivity()).getTickerResponse(
                nameBoard()
            )
        }
        // Функция для изменения цвета иконок и текста меню
        if (nameFragment != "ActivityHome") {
            val activity = requireActivity() as MainActivity
            activity.updateColors(nameFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        // Сохранение данных для восстановления в новой сессии
        val zipTicker = backupTicker
        if (zipTicker.isNotEmpty()) {
            PreferencesManager.getInstance(requireActivity()).setTickerResponse(zipTicker[0].board, zipTicker)
        }
    }
}
