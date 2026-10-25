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

        setupBottomNavigation()
        setupObservers()

        viewModel.loadHomeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHomeData()
    }

    private fun setupObservers() {
        viewModel.homeState.observe(this) { state ->
            when (state) {
                is HomeState.Loading -> {
                    // Keep layout stable
                }
                is HomeState.Success -> {
                    state.summary?.let { summary ->
                        summary.portfolioValue?.let {
                            binding.tvPortfolioValue.text = String.format("₹%,.2f", it)
                        }
                        val retAmt = summary.returnAmount ?: 0.0
                        val retPct = summary.returnPercentage ?: 0.0
                        val signAmt = if (retAmt >= 0) "+" else ""
                        val signPct = if (retPct >= 0) "+" else ""
                        binding.tvPortfolioChangeAmount.text = String.format("%s₹%,.2f", signAmt, retAmt)
                        binding.tvPortfolioChangePercent.text = String.format("%s%.2f%%", signPct, retPct)

                        val profitColor = if (retAmt >= 0) getColor(R.color.bourse_profit) else getColor(R.color.bourse_loss)
                        binding.tvPortfolioChangeAmount.setTextColor(profitColor)
                        binding.tvPortfolioChangePercent.setTextColor(profitColor)

                        summary.investedAmount?.let {
                            binding.tvInvestedValue.text = String.format("₹%,.2f", it)
                        }
                        summary.availableBalance?.let {
                            binding.tvAvailableValue.text = String.format("₹%,.2f", it)
                        }
                    }

                    // Populate recent activity (latest 1-3 transactions)
                    val txList = state.transactions
                    if (txList.isNotEmpty()) {
                        binding.cardActivity.visibility = View.VISIBLE
                        binding.activityHeader.visibility = View.VISIBLE

                        if (txList.indices.contains(0)) {
                            bindTransactionRow(
                                txList[0],
                                binding.layoutTx1,
                                binding.tvTx1Type,
                                binding.tvTx1Symbol,
                                binding.tvTx1Details,
                                binding.tvTx1Amount,
                                binding.divider1,
                                true
                            )
                        }
                        if (txList.indices.contains(1)) {
                            bindTransactionRow(
                                txList[1],
                                binding.layoutTx2,
                                binding.tvTx2Type,
                                binding.tvTx2Symbol,
                                binding.tvTx2Details,
                                binding.tvTx2Amount,
                                binding.divider2,
                                true
                            )
                        } else {
                            binding.layoutTx2.visibility = View.GONE
                            binding.divider1.visibility = View.GONE
                        }
                        if (txList.indices.contains(2)) {
                            bindTransactionRow(
                                txList[2],
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
                        binding.cardActivity.visibility = View.GONE
                        binding.activityHeader.visibility = View.GONE
                    }
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
        tvType.text = tx.type ?: "—"
        val isBuy = tx.type?.equals("BUY", ignoreCase = true) == true
        tvType.setTextColor(getColor(if (isBuy) R.color.bourse_buy else R.color.bourse_sell))

        tvSymbol.text = tx.symbol ?: "—"
        val shares = tx.shares
        tvDetails.text = shares?.let { "$it share${if (it > 1) "s" else ""}" } ?: "—"
        tvAmount.text = tx.totalAmount?.let { String.format("₹%,.2f", it) } ?: "—"

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
