package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import ind.finance.bourse.MainActivity
import ind.finance.bourse.MarketActivity
import ind.finance.bourse.ProfileActivity
import ind.finance.bourse.TransactionsActivity
import ind.finance.bourse.databinding.ActivityPortfolioBinding
import ind.finance.bourse.ui.portfolio.PortfolioState
import ind.finance.bourse.ui.portfolio.PortfolioViewModel
import java.util.Locale


class PortfolioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioBinding
    private val viewModel: PortfolioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPortfolioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        viewModel.state.observe(this) { state ->
            when (state) {
                PortfolioState.Loading -> Unit
                is PortfolioState.Success -> bindPortfolio(state)
                is PortfolioState.Error -> showError()
                PortfolioState.Unauthorized -> navigateToLogin()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPortfolio()
    }

    private fun bindPortfolio(state: PortfolioState.Success) {
        state.summary?.let { summary ->
            binding.tvPortfolioValue.text = money(summary.portfolioValue)
            binding.tvPortfolioChangeAmount.text = signedMoney(summary.returnAmount)
            binding.tvPortfolioChangePercent.text = signedPercent(summary.returnPercentage)
        }

        val holdings = state.holdings
        binding.cardHoldings.visibility = if (holdings.isEmpty()) View.GONE else View.VISIBLE
        binding.llEmptyHoldings.visibility = if (holdings.isEmpty()) View.VISIBLE else View.GONE
        val holdingSlots = listOf(
            listOf(binding.tvHolding1Symbol, binding.tvHolding1Company, binding.tvHolding1Value, binding.tvHolding1Quantity),
            listOf(binding.tvHolding2Symbol, binding.tvHolding2Company, binding.tvHolding2Value, binding.tvHolding2Quantity),
            listOf(binding.tvHolding3Symbol, binding.tvHolding3Company, binding.tvHolding3Value, binding.tvHolding3Quantity)
        )
        holdingSlots.forEachIndexed { index, views ->
            val holding = holdings.getOrNull(index)
            views.forEach { it.visibility = if (holding == null) View.GONE else View.VISIBLE }
            if (holding != null) {
                views[0].text = holding.symbol ?: "—"
                views[1].text = holding.companyName ?: "—"
                views[2].text = money(holding.currentValue)
                views[3].text = holding.quantity?.let { "$it share${if (it == 1) "" else "s"}" } ?: "—"
            }
        }

        val recommendations = state.recommendations
        binding.cardRecommendations.visibility = if (recommendations.isEmpty()) View.GONE else View.VISIBLE
        val recommendationSlots = listOf(
            listOf(binding.tvRecommendation1Name, binding.tvRecommendation1Reason, binding.tvRecommendation1Price),
            listOf(binding.tvRecommendation2Name, binding.tvRecommendation2Reason, binding.tvRecommendation2Price)
        )
        recommendationSlots.forEachIndexed { index, views ->
            val recommendation = recommendations.getOrNull(index)
            views.forEach { it.visibility = if (recommendation == null) View.GONE else View.VISIBLE }
            if (recommendation != null) {
                views[0].text = recommendation.stockName ?: recommendation.symbol ?: "—"
                views[1].text = recommendation.reason ?: recommendation.recommendation ?: "—"
                views[2].text = money(recommendation.currentPrice)
            }
        }
    }

    private fun money(value: Double?): String =
        value?.let { String.format(Locale.US, "₹%,.2f", it) } ?: "—"

    private fun signedMoney(value: Double?): String {
        if (value == null) return "—"
        return String.format(Locale.US, "%s₹%,.2f", if (value >= 0) "+" else "", value)
    }

    private fun signedPercent(value: Double?): String {
        if (value == null) return "—"
        return String.format(Locale.US, "%s%.2f%%", if (value >= 0) "+" else "", value)
    }

    private fun showError() {
        AlertDialog.Builder(this)
            .setMessage("Unable to load portfolio")
            .setPositiveButton("Retry") { _, _ -> viewModel.loadPortfolio() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setupListeners() {
        binding.btnExploreMarket.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.cardTransactionHistory.setOnClickListener {
            val intent = Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navMarket.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navPortfolio.setOnClickListener {
            // Already on Portfolio
        }

        binding.navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}
