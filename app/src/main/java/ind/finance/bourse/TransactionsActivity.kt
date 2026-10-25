package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.bourse.databinding.ActivityTransactionsBinding
import ind.finance.bourse.databinding.DialogTransactionDetailBinding
import ind.finance.bourse.ui.transactions.TransactionsState
import ind.finance.bourse.ui.transactions.TransactionsViewModel

class TransactionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val viewModel: TransactionsViewModel by viewModels()
    private var allTransactions = emptyList<TransactionModel>()
    private var currentFilter = "ALL" // ALL, BUY, SELL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupFilters()
        setupListeners()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList()) { tx ->
            showTransactionDetailDialog(tx)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = transactionAdapter
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                TransactionsState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                    binding.llEmptyState.visibility = View.GONE
                }
                is TransactionsState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    allTransactions = state.transactions.mapNotNull { it.toTransactionModel() }
                    filterList()
                }
                is TransactionsState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvTransactions.visibility = View.GONE
                    binding.llEmptyState.visibility = View.GONE
                    showLoadError()
                }
                TransactionsState.Unauthorized -> {
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupFilters() {
        val buttons = listOf(binding.btnFilterAll, binding.btnFilterBuy, binding.btnFilterSell)

        binding.btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterUI(binding.btnFilterAll, buttons)
            filterList()
        }

        binding.btnFilterBuy.setOnClickListener {
            currentFilter = "BUY"
            updateFilterUI(binding.btnFilterBuy, buttons)
            filterList()
        }

        binding.btnFilterSell.setOnClickListener {
            currentFilter = "SELL"
            updateFilterUI(binding.btnFilterSell, buttons)
            filterList()
        }
    }

    private fun updateFilterUI(selectedBtn: View, allBtns: List<View>) {
        for (btn in allBtns) {
            val tv = btn as android.widget.TextView
            if (btn == selectedBtn) {
                tv.background = getDrawable(R.drawable.bg_pill_selected)
                tv.setTextColor(getColor(R.color.bourse_button_text))
            } else {
                tv.background = getDrawable(R.drawable.bg_pill_unselected)
                tv.setTextColor(getColor(R.color.bourse_text_secondary))
            }
        }
    }

    private fun filterList() {
        val filtered = when (currentFilter) {
            "BUY" -> allTransactions.filter { it.type.equals("BUY", ignoreCase = true) }
            "SELL" -> allTransactions.filter { it.type.equals("SELL", ignoreCase = true) }
            else -> allTransactions
        }

        transactionAdapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.rvTransactions.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvTransactions.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun showTransactionDetailDialog(tx: TransactionModel) {
        val detailBinding = DialogTransactionDetailBinding.inflate(LayoutInflater.from(this))
        detailBinding.tvDetailAction.text = tx.type
        val isBuy = tx.type.equals("BUY", ignoreCase = true)
        detailBinding.tvDetailAction.setTextColor(getColor(if (isBuy) R.color.bourse_buy else R.color.bourse_sell))

        detailBinding.tvDetailSymbol.text = tx.symbol
        detailBinding.tvDetailQuantity.text = "${tx.shares} share${if (tx.shares > 1) "s" else ""}"
        detailBinding.tvDetailPrice.text = String.format("₹%,.2f", tx.pricePerShare)
        detailBinding.tvDetailTotal.text = String.format("₹%,.2f", tx.totalAmount)
        detailBinding.tvDetailStatus.text = tx.status
        detailBinding.tvDetailDate.text = tx.dateTime
        detailBinding.tvDetailId.text = tx.id

        val dialog = AlertDialog.Builder(this)
            .setView(detailBinding.root)
            .setPositiveButton("Close", null)
            .create()

        dialog.show()
    }

    private fun showLoadError() {
        AlertDialog.Builder(this)
            .setMessage("Unable to load transactions")
            .setPositiveButton("Retry") { _, _ -> viewModel.loadTransactions() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun ind.finance.bourse.data.model.TransactionDto.toTransactionModel(): TransactionModel {
        return TransactionModel(
            id = tradeId.toString(),
            type = action,
            symbol = symbol,
            shares = quantity,
            pricePerShare = executionPrice,
            totalAmount = totalAmount,
            dateTime = timestamp,
            status = status
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnExploreMarket.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}
