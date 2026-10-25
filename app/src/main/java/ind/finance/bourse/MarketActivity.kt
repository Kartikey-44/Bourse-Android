package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import ind.finance.bourse.databinding.ActivityMarketBinding
import ind.finance.bourse.ui.market.MarketState
import ind.finance.bourse.ui.market.MarketViewModel

class MarketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarketBinding
    private lateinit var stockAdapter: StockAdapter
    private val viewModel: MarketViewModel by viewModels()

    private var currentStockList = listOf<StockModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSearch()
        setupObservers()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStocks()
    }

    private fun setupRecyclerView() {
        stockAdapter = StockAdapter(emptyList()) { stock ->
            val intent = Intent(this, StockDetailsActivity::class.java).apply {
                putExtra("STOCK_SYMBOL", stock.symbol)
                putExtra("COMPANY_NAME", stock.companyName)
                putExtra("STOCK_PRICE", stock.price)
                putExtra("STOCK_CHANGE", stock.change)
                putExtra("IS_POSITIVE", stock.isPositive)
            }
            startActivity(intent)
        }
        binding.rvStocks.layoutManager = LinearLayoutManager(this)
        binding.rvStocks.adapter = stockAdapter
    }

    private fun setupObservers() {
        viewModel.marketState.observe(this) { state ->
            when (state) {
                is MarketState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvStocks.visibility = View.GONE
                    binding.llEmptyState.visibility = View.GONE
                }
                is MarketState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    currentStockList = state.stocks.map { (dto, trend) ->
                        val changeText = when (trend?.trim()?.uppercase()) {
                            "UP" -> "▲ UP"
                            "DOWN" -> "▼ DOWN"
                            else -> "N/A"
                        }
                        StockModel(
                            symbol = dto.symbol,
                            companyName = dto.companyName,
                            price = String.format("₹%,.2f", dto.currentPrice),
                            change = changeText,
                            isPositive = trend?.trim()?.equals("UP", ignoreCase = true) == true
                        )
                    }

                    if (currentStockList.isEmpty()) {
                        binding.rvStocks.visibility = View.GONE
                        binding.llEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvStocks.visibility = View.VISIBLE
                        binding.llEmptyState.visibility = View.GONE
                    }
                    filterStocks(binding.etSearchStocks.text.toString())
                }
                is MarketState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvStocks.visibility = View.GONE
                    binding.llEmptyState.visibility = View.GONE
                    showMarketError()
                }
                is MarketState.Unauthorized -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchStocks.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStocks(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterStocks(query: String) {
        val filtered = if (query.isBlank()) {
            currentStockList
        } else {
            currentStockList.filter {
                it.symbol.contains(query, ignoreCase = true) ||
                it.companyName.contains(query, ignoreCase = true)
            }
        }

        stockAdapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.rvStocks.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
            if (currentStockList.isEmpty()) {
                binding.tvEmptyStateTitle.text = "No stocks available"
                binding.tvEmptyStateSubtitle.text = "There are no stocks to display."
            } else {
                binding.tvEmptyStateTitle.text = "No stocks found"
                binding.tvEmptyStateSubtitle.text = "Try searching for another stock."
            }
        } else {
            binding.rvStocks.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun showMarketError() {
        AlertDialog.Builder(this)
            .setMessage("Unable to load market")
            .setPositiveButton("Retry") { _, _ -> viewModel.loadStocks() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navMarket.setOnClickListener {
            // Already on Market
        }

        binding.navPortfolio.setOnClickListener {
            val intent = Intent(this, PortfolioActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}
