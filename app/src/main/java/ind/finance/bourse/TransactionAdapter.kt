package ind.finance.bourse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ind.finance.bourse.databinding.ItemTransactionBinding

class TransactionAdapter(
    private var transactions: List<TransactionModel>,
    private val onItemClick: (TransactionModel) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = transactions[position]
        holder.binding.tvTxType.text = tx.type
        holder.binding.tvTxAmount.text = String.format("₹%,.2f", tx.totalAmount)
        holder.binding.tvTxSymbol.text = tx.symbol
        holder.binding.tvTxDetails.text = "${tx.shares} share${if (tx.shares > 1) "s" else ""} • ₹${String.format("%,.2f", tx.pricePerShare)}/share"
        holder.binding.tvTxDate.text = tx.dateTime
        holder.binding.tvTxStatus.text = tx.status

        val isBuy = tx.type.equals("BUY", ignoreCase = true)
        val typeColor = holder.itemView.context.getColor(if (isBuy) R.color.bourse_buy else R.color.bourse_sell)
        holder.binding.tvTxType.setTextColor(typeColor)

        holder.itemView.setOnClickListener {
            onItemClick(tx)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateList(newList: List<TransactionModel>) {
        transactions = newList
        notifyDataSetChanged()
    }
}
