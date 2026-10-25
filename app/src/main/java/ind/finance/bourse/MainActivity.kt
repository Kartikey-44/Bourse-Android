package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.bourse.databinding.ActivityMainBinding
import ind.finance.bourse.ui.home.HomeState
import ind.finance.bourse.ui.home.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()

    private var allTransactions = listOf<ind.finance.bourse.data.model.TransactionDto>()
    private var selectedTradingAction = "BUY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTradingActions()
        setupBottomNavigation()
        setupObservers()

        binding.tvSeeAllActivity.setOnClickListener {
            val intent = Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }

        viewModel.loadHomeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHomeData()
    }

    private fun setupTradingActions() {
        updateTradingActionUI("BUY")

        binding.btnBuy.setOnClickListener {
            updateTradingActionUI("BUY")
            filterRecentActivity("BUY")
        }

        binding.btnSell.setOnClickListener {
            updateTradingActionUI("SELL")
            filterRecentActivity("SELL")
        }
    }

    private fun updateTradingActionUI(action: String) {
        selectedTradingAction = action
        if (action == "BUY") {
            binding.btnBuy.background = getDrawable(R.drawable.bg_login_button)
            binding.btnBuy.setTextColor(getColor(R.color.bourse_button_text))

            binding.btnSell.background = getDrawable(R.drawable.bg_secondary_button)
            binding.btnSell.setTextColor(getColor(R.color.bourse_text_primary))
        } else {
            binding.btnSell.background = getDrawable(R.drawable.bg_sell_button)
            binding.btnSell.setTextColor(getColor(R.color.white))

            binding.btnBuy.background = getDrawable(R.drawable.bg_secondary_button)
            binding.btnBuy.setTextColor(getColor(R.color.bourse_text_primary))
        }
    }

    private fun filterRecentActivity(action: String) {
        val filtered = if (allTransactions.isEmpty()) {
            emptyList()
        } else {
            allTransactions.filter { (it.action ?: "BUY").equals(action, ignoreCase = true) }
        }

        if (filtered.isNotEmpty()) {
            binding.cardActivity.visibility = View.VISIBLE
            binding.activityHeader.visibility = View.VISIBLE

            if (filtered.indices.contains(0)) {
                bindTransactionRow(
                    filtered[0],
                    binding.layoutTx1,
                    binding.tvTx1Type,
                    binding.tvTx1Symbol,
                    binding.tvTx1Details,
                    binding.tvTx1Amount,
                    binding.divider1,
                    filtered.size > 1
                )
            } else {
                binding.layoutTx1.visibility = View.GONE
            }

            if (filtered.indices.contains(1)) {
                bindTransactionRow(
                    filtered[1],
                    binding.layoutTx2,
                    binding.tvTx2Type,
                    binding.tvTx2Symbol,
                    binding.tvTx2Details,
                    binding.tvTx2Amount,
                    binding.divider2,
                    filtered.size > 2
                )
            } else {
                binding.layoutTx2.visibility = View.GONE
                binding.divider1.visibility = View.GONE
            }

            if (filtered.indices.contains(2)) {
                bindTransactionRow(
                    filtered[2],
                    binding.layoutTx3,
                    binding.tvTx3Type,
                    binding.tvTx3Symbol,
                    binding.tvTx3Details,
                    binding.tvTx3Amount,
                    null,
                    false
                )
            } else {
                binding.layoutTx3.visibility = View.GONE
                binding.divider2.visibility = View.GONE
            }
        } else {
            binding.layoutTx1.visibility = View.GONE
            binding.layoutTx2.visibility = View.GONE
            binding.layoutTx3.visibility = View.GONE
            binding.divider1.visibility = View.GONE
            binding.divider2.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.homeState.observe(this) { state ->
            when (state) {
                is HomeState.Loading -> {
                    // Keep layout stable
                }
                is HomeState.Success -> {
                    if (state.userName.isNotBlank()) {
                        binding.tvGreeting.text = "Good morning, ${state.userName}"
                    }
                    state.summary?.let { summary ->
                        binding.tvPortfolioValue.text = String.format("₹%,.2f", summary.totalPortfolioValue)
                        val retAmt = summary.totalProfitLoss
                        val retPct = if (summary.totalInvestedValue > 0) {
                            (summary.totalProfitLoss / summary.totalInvestedValue) * 100.0
                        } else 0.0
                        val signAmt = if (retAmt >= 0) "+" else ""
                        val signPct = if (retPct >= 0) "+" else ""
                        binding.tvPortfolioChangeAmount.text = String.format("%s₹%,.2f", signAmt, retAmt)
                        binding.tvPortfolioChangePercent.text = String.format("%s%.2f%%", signPct, retPct)

                        val profitColor = if (retAmt >= 0) getColor(R.color.bourse_profit) else getColor(R.color.bourse_loss)
                        binding.tvPortfolioChangeAmount.setTextColor(profitColor)
                        binding.tvPortfolioChangePercent.setTextColor(profitColor)

                        binding.tvInvestedValue.text = String.format("₹%,.2f", summary.totalInvestedValue)
                        binding.tvAvailableValue.text = String.format("₹%,.2f", summary.cashBalance)
                    }

                    allTransactions = state.transactions
                    filterRecentActivity(selectedTradingAction)
                }
                is HomeState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is HomeState.Unauthorized -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun bindTransactionRow(
        tx: ind.finance.bourse.data.model.TransactionDto,
        layout: View,
        tvType: TextView,
        tvSymbol: TextView,
        tvDetails: TextView,
        tvAmount: TextView,
        divider: View?,
        showDivider: Boolean
    ) {
        layout.visibility = View.VISIBLE
        tvType.text = tx.action
        val isBuy = (tx.action ?: "BUY").equals("BUY", ignoreCase = true)
        tvType.setTextColor(getColor(if (isBuy) R.color.bourse_buy else R.color.bourse_sell))

        tvSymbol.text = tx.symbol ?: "TCS"
        val shares = tx.quantity
        tvDetails.text = "$shares share${if (shares > 1) "s" else ""}"
        tvAmount.text = String.format("₹%,.2f", tx.totalAmount)

        divider?.visibility = if (showDivider) View.VISIBLE else View.GONE
    }

    private fun setupBottomNavigation() {
        binding.navHome.setOnClickListener {
            // Already on Home
        }

        binding.navMarket.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
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
