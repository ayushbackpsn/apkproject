package com.shoecatalog.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shoecatalog.app.R
import com.shoecatalog.app.api.RetrofitClient
import com.shoecatalog.app.data.model.Product

class ProductAdapter(
    private val products: MutableList<Product>,
    private val baseUrl: String = RetrofitClient.BASE_URL
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val selectedProducts = mutableSetOf<String>()

    fun getSelectedProductIds(): List<String> {
        return selectedProducts.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkboxProduct)
        private val productImageView: ImageView = itemView.findViewById(R.id.imageViewProduct)
        private val productNameTextView: TextView = itemView.findViewById(R.id.textViewProductName)
        private val brandNameTextView: TextView = itemView.findViewById(R.id.textViewBrandName)

        fun bind(product: Product) {
            productNameTextView.text = product.productName
            brandNameTextView.text = product.brandName
            checkbox.isChecked = selectedProducts.contains(product.id)

            // Load image using Glide
            val imageUrl = if (product.productImage.startsWith("http")) {
                product.productImage
            } else {
                "${baseUrl}uploads/${product.productImage}"
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(productImageView)

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedProducts.add(product.id)
                } else {
                    selectedProducts.remove(product.id)
                }
            }
        }
    }
}

