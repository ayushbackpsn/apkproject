package com.shoecatalog.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shoecatalog.app.adapter.BrandAdapter
import com.shoecatalog.app.api.ApiService
import com.shoecatalog.app.api.RetrofitClient
import com.shoecatalog.app.data.model.Brand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewBrands: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private lateinit var fabAddBrand: FloatingActionButton
    private lateinit var apiService: ApiService
    private lateinit var brandAdapter: BrandAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupToolbar()
        setupFab()

        apiService = RetrofitClient.apiService

        loadBrands()
    }

    private fun initializeViews() {
        recyclerViewBrands = findViewById(R.id.recyclerViewBrands)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)
        fabAddBrand = findViewById(R.id.fabAddBrand)
    }

    private fun setupRecyclerView() {
        brandAdapter = BrandAdapter(mutableListOf()) { brand ->
            navigateToProducts(brand)
        }
        recyclerViewBrands.layoutManager = LinearLayoutManager(this)
        recyclerViewBrands.adapter = brandAdapter
    }

    private fun setupToolbar() {
        //setSupportActionBar(toolbar)
    }

    private fun setupFab() {
        fabAddBrand.setOnClickListener {
            showAddBrandDialog()
        }
    }

    private fun showAddBrandDialog() {
        val dialog = AddBrandDialog()
        dialog.setOnBrandAddedListener {
            loadBrands()
        }
        dialog.show(supportFragmentManager, "AddBrandDialog")
    }

    private fun navigateToProducts(brand: Brand) {
        val intent = Intent(this, ProductsActivity::class.java)
        intent.putExtra("brand_id", brand.id)
        intent.putExtra("brand_name", brand.brandName)
        startActivity(intent)
    }

    private fun loadBrands() {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getBrands()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        brandAdapter = BrandAdapter(response.body()!!.toMutableList()) { brand ->
                            navigateToProducts(brand)
                        }
                        recyclerViewBrands.adapter = brandAdapter
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.error_loading,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }
}

