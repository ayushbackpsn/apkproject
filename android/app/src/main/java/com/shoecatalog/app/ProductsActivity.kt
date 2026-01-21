package com.shoecatalog.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shoecatalog.app.adapter.ProductAdapter
import com.shoecatalog.app.api.ApiService
import com.shoecatalog.app.api.RetrofitClient
import com.shoecatalog.app.data.model.PdfGenerateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProductsActivity : AppCompatActivity() {

    private lateinit var recyclerViewProducts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonGenerate: Button
    private lateinit var toolbar: Toolbar
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var apiService: ApiService
    private lateinit var productAdapter: ProductAdapter

    private var brandId: String = ""
    private var brandName: String = ""
    private val REQUEST_CODE_ADD_PRODUCT = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        brandId = intent.getStringExtra("brand_id") ?: ""
        brandName = intent.getStringExtra("brand_name") ?: ""

        initializeViews()
        setupRecyclerView()
        setupToolbar()
        setupButton()
        setupFab()

        apiService = RetrofitClient.apiService

        loadProducts()
    }

    private fun initializeViews() {
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts)
        progressBar = findViewById(R.id.progressBar)
        buttonGenerate = findViewById(R.id.buttonGenerate)
        toolbar = findViewById(R.id.toolbar)
        fabAddProduct = findViewById(R.id.fabAddProduct)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(mutableListOf())
        recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        recyclerViewProducts.adapter = productAdapter
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButton() {
        buttonGenerate.setOnClickListener {
            generatePdf()
        }
    }

    private fun setupFab() {
        fabAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_PRODUCT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_PRODUCT && resultCode == RESULT_OK) {
            loadProducts()
        }
    }

    private fun loadProducts() {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getProductsByBrand(brandId)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        productAdapter = ProductAdapter(response.body()!!.toMutableList())
                        recyclerViewProducts.adapter = productAdapter
                    } else {
                        Toast.makeText(
                            this@ProductsActivity,
                            R.string.error_loading,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ProductsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun generatePdf() {
        val selectedIds = productAdapter.getSelectedProductIds()

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, R.string.no_products_selected, Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        buttonGenerate.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = PdfGenerateRequest(selectedIds)
                val response = apiService.generatePdf(request)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonGenerate.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val pdfResponse = response.body()!!
                        Toast.makeText(
                            this@ProductsActivity,
                            R.string.pdf_generated,
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Download PDF and share
                        downloadAndSharePdf(pdfResponse.downloadUrl)
                    } else {
                        Toast.makeText(
                            this@ProductsActivity,
                            R.string.pdf_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonGenerate.isEnabled = true
                    Toast.makeText(
                        this@ProductsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun downloadAndSharePdf(pdfUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(pdfUrl).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body
                    if (body != null) {
                        val inputStream: InputStream = body.byteStream()

                        // Save to app's cache directory
                        val pdfDir = File(cacheDir, "pdfs")
                        if (!pdfDir.exists()) {
                            pdfDir.mkdirs()
                        }

                        val fileName = pdfUrl.substringAfterLast("/")
                        val pdfFile = File(pdfDir, fileName)

                        FileOutputStream(pdfFile).use { output ->
                            inputStream.copyTo(output)
                        }

                        withContext(Dispatchers.Main) {
                            sharePdfViaWhatsApp(pdfFile)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProductsActivity,
                        "Error downloading PDF: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sharePdfViaWhatsApp(pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Product Catalog")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Try to open WhatsApp specifically
            intent.setPackage("com.whatsapp")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                // If WhatsApp is not installed, show share dialog
                intent.setPackage(null)
                startActivity(Intent.createChooser(intent, "Share PDF via"))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}

