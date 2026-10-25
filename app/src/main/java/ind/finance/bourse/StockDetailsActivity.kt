package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import ind.finance.bourse.databinding.ActivityStockDetailsBinding
import ind.finance.bourse.databinding.DialogFailureBinding
import ind.finance.bourse.databinding.DialogLoadingBinding
import ind.finance.bourse.databinding.DialogPinBinding
import ind.finance.bourse.databinding.DialogQuantityBinding
import ind.finance.bourse.databinding.DialogSuccessBinding
import ind.finance.bourse.data.local.TransactionPinStore
import ind.finance.bourse.data.model.PriceHistoryPoint
import ind.finance.bourse.data.model.TransactionDto
import ind.finance.bourse.ui.stock.StockDetailsState
import ind.finance.bourse.ui.stock.StockDetailsViewModel
import ind.finance.bourse.ui.trading.TradingState
import ind.finance.bourse.ui.trading.TradingViewModel

class StockDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockDetailsBinding

    private var stockSymbol = ""
    private var companyName = ""
    private var currentPriceDouble = 0.0
    private var priceString = ""
    private var changeString = "—"
    private var isPositiveTrend = true
    private val viewModel: StockDetailsViewModel by viewModels()
    private val tradingViewModel: TradingViewModel by viewModels()
    private lateinit var pinStore: TransactionPinStore
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStockDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        stockSymbol = intent.getStringExtra("STOCK_SYMBOL").orEmpty()

        pinStore = TransactionPinStore(this)

        setupChart()
        setupListeners()
        setupStockObserver()
        setupTradingObserver()
    }

    override fun onResume() {
        super.onResume()
        if (stockSymbol.isBlank()) {
            showError()
        } else {
            viewModel.loadStock(stockSymbol)
        }
    }

    private fun bindStockData() {
        binding.tvHeaderSymbol.text = stockSymbol
        binding.tvCompanyName.text = companyName
        binding.tvCurrentPrice.text = priceString
        binding.tvInfoSymbol.text = stockSymbol
        binding.tvInfoCompany.text = companyName
        binding.tvInfoPrice.text = priceString

        binding.tvPriceChangeAmount.text = "—"
        binding.tvPriceChangePercent.text = changeString

        val color = if (isPositiveTrend) getColor(R.color.bourse_profit) else getColor(R.color.bourse_loss)
        binding.tvPriceChangeAmount.setTextColor(color)
        binding.tvPriceChangePercent.setTextColor(color)
    }

    private fun setupChart() {
        val lineChart = binding.lineChart
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)

        val xAxis = lineChart.xAxis
        xAxis.isEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.textColor = getColor(R.color.bourse_text_secondary)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.isEnabled = true
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = getColor(R.color.bourse_divider)
        yAxisLeft.textColor = getColor(R.color.bourse_text_secondary)

        lineChart.axisRight.isEnabled = false
        lineChart.clear()
        lineChart.setNoDataText("No price trend available")
        lineChart.invalidate()
    }

    private fun bindChartHistory(history: List<PriceHistoryPoint>) {
        val lineChart = binding.lineChart
        if (history.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("No price trend available")
            lineChart.invalidate()
            return
        }

        val sortedHistory = if (history.size > 1 && history.first().timestamp > history.last().timestamp) {
            history.reversed()
        } else {
            history
        }

        val entries = ArrayList<Entry>()
        val timeLabels = ArrayList<String>()

        sortedHistory.forEachIndexed { index, point ->
            entries.add(Entry(index.toFloat(), point.price.toFloat()))
            timeLabels.add(formatTimestamp(point.timestamp))
        }

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(timeLabels)
        xAxis.granularity = 1f
        xAxis.labelCount = Math.min(timeLabels.size, 5)
        xAxis.isGranularityEnabled = true

        val dataSet = LineDataSet(entries, "Price").apply {
            color = getColor(R.color.bourse_action_text)
            valueTextColor = getColor(R.color.bourse_text_primary)
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = getColor(R.color.bourse_action_text)
            fillAlpha = 35
        }

        lineChart.data = LineData(dataSet)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.invalidate()
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            if (timestamp.length >= 16 && timestamp.contains("T")) {
                val timePart = timestamp.substringAfter("T")
                if (timePart.length >= 5) {
                    timePart.substring(0, 5)
                } else {
                    timestamp
                }
            } else {
                timestamp
            }
        } catch (_: Exception) {
            timestamp
        }
    }

    private fun setupStockObserver() {
        viewModel.state.observe(this) { state ->
            when (state) {
                StockDetailsState.Loading -> {
                    binding.llLoadingState.visibility = View.VISIBLE
                    binding.llErrorState.visibility = View.GONE
                }
                is StockDetailsState.Success -> {
                    binding.llLoadingState.visibility = View.GONE
                    binding.llErrorState.visibility = View.GONE

                    val stock = state.stock
                    companyName = stock.companyName
                    currentPriceDouble = stock.currentPrice
                    priceString = String.format("₹%,.2f", stock.currentPrice)

                    val history = state.history
                    val sortedHistory = if (history.size > 1 && history.first().timestamp > history.last().timestamp) {
                        history.reversed()
                    } else {
                        history
                    }

                    if (sortedHistory.isNotEmpty()) {
                        val startPrice = sortedHistory.firstOrNull()?.price ?: stock.currentPrice
                        val endPrice = sortedHistory.lastOrNull()?.price ?: stock.currentPrice
                        val pctChange = if (startPrice > 0) ((endPrice - startPrice) / startPrice) * 100.0 else 0.0
                        isPositiveTrend = pctChange >= 0
                        val sign = if (pctChange >= 0) "▲ +" else "▼ "
                        changeString = String.format("%s%.2f%%", sign, pctChange)
                    } else {
                        val trend = state.trend?.trend?.trim()?.uppercase()
                        isPositiveTrend = trend != "DOWN"
                        changeString = when (trend) {
                            "UP" -> "▲ UP"
                            "DOWN" -> "▼ DOWN"
                            else -> "—"
                        }
                    }

                    bindStockData()
                    bindChartHistory(state.history)
                }
                is StockDetailsState.Error -> showError()
                StockDetailsState.Unauthorized -> {
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupTradingObserver() {
        tradingViewModel.state.observe(this) { state ->
            when (state) {
                TradingState.Loading -> showTradeLoading()
                is TradingState.Success -> {
                    dismissTradeLoading()
                    showSuccessDialog(state.transaction)
                    viewModel.loadStock(stockSymbol)
                }
                is TradingState.Failure -> {
                    dismissTradeLoading()
                    showFailureDialog(state.message, pendingIsBuy, pendingQuantity)
                }
                TradingState.Unauthorized -> {
                    dismissTradeLoading()
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                TradingState.Idle -> Unit
            }
        }
    }

    private fun showError() {
        binding.llLoadingState.visibility = View.GONE
        binding.llErrorState.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnBuy.setOnClickListener {
            showQuantityDialog(isBuy = true)
        }

        binding.btnSell.setOnClickListener {
            showQuantityDialog(isBuy = false)
        }

        binding.btnRetry.setOnClickListener {
            if (stockSymbol.isNotBlank()) {
                viewModel.loadStock(stockSymbol)
            } else {
                showError()
            }
        }

        val periodButtons = listOf(binding.btn1D, binding.btn1W, binding.btn1M, binding.btn3M)
        for (btn in periodButtons) {
            btn.setOnClickListener {
                for (b in periodButtons) {
                    b.background = getDrawable(R.drawable.bg_pill_unselected)
                    b.setTextColor(getColor(R.color.bourse_text_secondary))
                }
                btn.background = getDrawable(R.drawable.bg_pill_selected)
                btn.setTextColor(getColor(R.color.bourse_button_text))
            }
        }
    }


    private fun showQuantityDialog(isBuy: Boolean) {
        val dialogBinding = DialogQuantityBinding.inflate(LayoutInflater.from(this))
        val actionTitle = if (isBuy) "Buy $stockSymbol" else "Sell $stockSymbol"
        dialogBinding.tvDialogTitle.text = actionTitle
        dialogBinding.tvDialogPrice.text = priceString

        var qty = 1
        dialogBinding.etQuantity.setText(qty.toString())
        updateEstimatedTotal(dialogBinding, qty)

        dialogBinding.btnPlus.setOnClickListener {
            qty++
            dialogBinding.etQuantity.setText(qty.toString())
            updateEstimatedTotal(dialogBinding, qty)
        }

        dialogBinding.btnMinus.setOnClickListener {
            if (qty > 1) {
                qty--
                dialogBinding.etQuantity.setText(qty.toString())
                updateEstimatedTotal(dialogBinding, qty)
            }
        }

        dialogBinding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                val parsed = text.toIntOrNull() ?: 0
                if (parsed > 0) {
                    updateEstimatedTotal(dialogBinding, parsed)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val input = dialogBinding.etQuantity.text.toString().trim()
                val quantity = input.toIntOrNull() ?: 0
                if (quantity <= 0) {
                    dialogBinding.tvQuantityError.visibility = View.VISIBLE
                } else {
                    dialogBinding.tvQuantityError.visibility = View.GONE
                    dialog.dismiss()
                    showPinDialog(isBuy, quantity)
                }
            }
        }

        dialog.show()
    }

    private fun updateEstimatedTotal(dialogBinding: DialogQuantityBinding, quantity: Int) {
        val total = quantity * currentPriceDouble
        dialogBinding.tvEstimatedTotal.text = String.format("₹%,.2f", total)
    }

    private fun showPinDialog(isBuy: Boolean, quantity: Int) {
        if (!pinStore.hasPin()) {
            AlertDialog.Builder(this)
                .setMessage("Set a transaction PIN in Profile before trading.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        val pinBinding = DialogPinBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(pinBinding.root)
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirmButton.isEnabled = false

            pinBinding.etPin.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val pinText = s.toString()
                    confirmButton.isEnabled = pinText.length == 4
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            confirmButton.setOnClickListener {
                val enteredPin = pinBinding.etPin.text.toString()
                val correctPin = pinStore.getPin()

                if (correctPin != null && enteredPin == correctPin) {
                    pinBinding.tvPinError.visibility = View.GONE
                    dialog.dismiss()
                    executeTrade(isBuy, quantity)
                } else {
                    pinBinding.tvPinError.visibility = View.VISIBLE
                }
            }
        }

        dialog.show()
    }

    private fun executeTrade(isBuy: Boolean, quantity: Int) {
        pendingIsBuy = isBuy
        pendingQuantity = quantity
        tradingViewModel.submit(isBuy, stockSymbol, quantity)
    }

    /* Loading Dialog & Dialog handlers */
    private fun showTradeLoading() {
        if (loadingDialog?.isShowing == true) return
        val loadingBinding = DialogLoadingBinding.inflate(LayoutInflater.from(this))
        loadingDialog = AlertDialog.Builder(this)
            .setView(loadingBinding.root)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun dismissTradeLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun showSuccessDialog(transaction: TransactionDto) {
        val successBinding = DialogSuccessBinding.inflate(LayoutInflater.from(this))
        val actionText = transaction.action
        successBinding.tvSuccessAction.text = actionText
        if (actionText.equals("SELL", ignoreCase = true)) {
            successBinding.tvSuccessAction.setTextColor(getColor(R.color.bourse_sell))
        }
        successBinding.tvSuccessSymbol.text = transaction.symbol
        val quantity = transaction.quantity
        successBinding.tvSuccessQuantity.text = "$quantity share${if (quantity == 1) "" else "s"}"
        successBinding.tvSuccessTotal.text = String.format("₹%,.2f", transaction.totalAmount)

        val dialog = AlertDialog.Builder(this)
            .setView(successBinding.root)
            .setPositiveButton("Done") { _, _ ->
                // Refresh stock details or finish
            }
            .create()

        dialog.show()
    }

    private var pendingIsBuy = true
    private var pendingQuantity = 1

    private fun showFailureDialog(message: String, isBuy: Boolean, quantity: Int) {
        val failureBinding = DialogFailureBinding.inflate(LayoutInflater.from(this))
        failureBinding.tvFailureMessage.text = message
        val dialog = AlertDialog.Builder(this)
            .setView(failureBinding.root)
            .setPositiveButton("Try Again") { _, _ ->
                showPinDialog(isBuy, quantity)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}
