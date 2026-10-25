package ind.finance.bourse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ind.finance.bourse.databinding.ItemStockBinding

class StockAdapter(
    private var stocks: List<StockModel>,
    private val onItemClick: (StockModel) -> Unit
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    inner class StockViewHolder(val binding: ItemStockBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stocks[position]
        holder.binding.tvStockSymbol.text = stock.symbol
        holder.binding.tvCompanyName.text = stock.companyName
        holder.binding.tvStockPrice.text = stock.price
        holder.binding.tvStockChange.text = stock.change

        val colorRes = if (stock.isPositive) R.color.bourse_profit else R.color.bourse_loss
        holder.binding.tvStockChange.setTextColor(holder.itemView.context.getColor(colorRes))

        holder.itemView.setOnClickListener {
            onItemClick(stock)
        }
    }

    override fun getItemCount(): Int = stocks.size

    fun updateList(newList: List<StockModel>) {
        stocks = newList
        notifyDataSetChanged()
    }
}
