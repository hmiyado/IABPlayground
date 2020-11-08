package com.hmiyado.iabplayground

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.hmiyado.iabplayground.databinding.PurchaseListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PurchaseListItemAdapter(private val list: List<SkuDetails>) :
    RecyclerView.Adapter<PurchaseListItemAdapter.ViewHolder>() {
    private val _onClick = MutableStateFlow<SkuDetails?>(null)
    val onClick: Flow<SkuDetails> = _onClick.filterNotNull()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PurchaseListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
        holder.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                _onClick.emit(item)
            }.start()
        }
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(private val binding: PurchaseListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SkuDetails) {
            binding.bind(item)
        }

        fun setOnClickListener(onClick: (View) -> Unit) {
            binding.root.setOnClickListener(onClick)
        }
    }
}

fun PurchaseListItemBinding.bind(item: SkuDetails) {
    title.text = item.title
    type.text = item.type
    productId.text = item.sku
    description.text = item.description
    price.text = item.price
    priceCurrencyCode.text = item.priceCurrencyCode
}