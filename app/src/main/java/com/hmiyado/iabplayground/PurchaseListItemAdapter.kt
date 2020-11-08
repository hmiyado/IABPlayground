package com.hmiyado.iabplayground

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.hmiyado.iabplayground.databinding.PurchaseListItemBinding

class PurchaseListItemAdapter(private val list: List<SkuDetails>) :
    RecyclerView.Adapter<PurchaseListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PurchaseListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(private val binding: PurchaseListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SkuDetails) {
            binding.bind(item)
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