package com.shoecatalog.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shoecatalog.app.R
import com.shoecatalog.app.data.model.Brand

class BrandAdapter(
    private val brands: MutableList<Brand>,
    private val onBrandClick: (Brand) -> Unit
) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        holder.bind(brands[position])
    }

    override fun getItemCount(): Int = brands.size

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brandNameTextView: TextView = itemView.findViewById(R.id.textViewBrandName)

        fun bind(brand: Brand) {
            brandNameTextView.text = brand.brandName
            itemView.setOnClickListener {
                onBrandClick(brand)
            }
        }
    }
}

