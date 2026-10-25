package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import ind.finance.bourse.data.model.HoldingResponse
import ind.finance.bourse.databinding.ActivityAllHoldingsBinding
import ind.finance.bourse.ui.portfolio.PortfolioState
import ind.finance.bourse.ui.portfolio.PortfolioViewModel
import java.util.Locale

class AllHoldingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllHoldingsBinding

    private val viewModel: PortfolioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityAllHoldingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        viewModel.state.observe(this) { state ->

            when (state) {

                PortfolioState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is PortfolioState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    showHoldings(state.holdings)
                }

                is PortfolioState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                PortfolioState.Unauthorized -> {
                    binding.progressBar.visibility = View.GONE

                    val intent = Intent(
                        this,
                        LoginActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                    startActivity(intent)
                    finish()
                }
            }
        }

        viewModel.loadPortfolio()
    }

    private fun showHoldings(
        holdings: List<HoldingResponse>
    ) {

        binding.llHoldings.removeAllViews()

        binding.tvHoldingsCount.text =
            "${holdings.size} holding${if (holdings.size == 1) "" else "s"}"

        holdings.forEachIndexed { index, holding ->

            val row = createHoldingRow(holding)

            binding.llHoldings.addView(row)

            if (index < holdings.lastIndex) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    )

                    setBackgroundColor(
                        getColor(R.color.bourse_divider)
                    )
                }

                binding.llHoldings.addView(divider)
            }
        }
    }

    private fun createHoldingRow(
        holding: HoldingResponse
    ): View {

        val row = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = android.view.Gravity.CENTER_VERTICAL

            setPadding(
                16,
                14,
                16,
                14
            )

            setBackgroundColor(
                getColor(R.color.bourse_surface)
            )

            isClickable = true
            isFocusable = true

            setOnClickListener {

                val symbol = holding.symbol
                    ?: return@setOnClickListener

                val intent = Intent(
                    this@AllHoldingsActivity,
                    StockDetailsActivity::class.java
                ).apply {
                    putExtra("STOCK_SYMBOL", symbol)
                }

                startActivity(intent)
            }
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val symbolText = TextView(this).apply {
            text = holding.symbol ?: "—"
            setTextColor(
                getColor(R.color.bourse_text_primary)
            )
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val companyText = TextView(this).apply {
            text = holding.companyName ?: "—"
            setTextColor(
                getColor(R.color.bourse_text_secondary)
            )
            textSize = 12f
        }

        val quantityText = TextView(this).apply {
            text = holding.quantity?.let {
                "$it share${if (it == 1) "" else "s"}"
            } ?: "—"

            setTextColor(
                getColor(R.color.bourse_text_secondary)
            )

            textSize = 12f
        }

        left.addView(symbolText)
        left.addView(companyText)
        left.addView(quantityText)

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.END
        }

        val valueText = TextView(this).apply {
            text = money(holding.currentValue)

            setTextColor(
                getColor(R.color.bourse_text_primary)
            )

            textSize = 14f

            setTypeface(
                typeface,
                android.graphics.Typeface.BOLD
            )
        }

        val pnlText = TextView(this).apply {

            text = signedMoney(
                holding.profitLoss
            )

            setTextColor(
                if ((holding.profitLoss ?: 0.0) >= 0)
                    getColor(R.color.bourse_profit)
                else
                    getColor(R.color.bourse_loss)
            )

            textSize = 12f
        }

        right.addView(valueText)
        right.addView(pnlText)

        row.addView(left)
        row.addView(right)

        return row
    }

    private fun money(value: Double?): String =
        value?.let {
            String.format(
                Locale.US,
                "₹%,.2f",
                it
            )
        } ?: "—"

    private fun signedMoney(value: Double?): String {

        if (value == null) return "—"

        return String.format(
            Locale.US,
            "%s₹%,.2f",
            if (value >= 0) "+" else "",
            value
        )
    }
}