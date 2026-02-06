package com.shoecatalog.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import com.shoecatalog.app.api.ApiService
import com.shoecatalog.app.api.RetrofitClient
import com.shoecatalog.app.data.model.Brand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddProductActivity : AppCompatActivity() {

    private lateinit var editTextProductName: TextInputEditText
    private lateinit var autoCompleteBrandName: AutoCompleteTextView
    private lateinit var buttonAddBrand: Button
    private lateinit var imageViewProductPreview: ImageView
    private lateinit var buttonPickFromGallery: Button
    private lateinit var buttonCaptureImage: Button
    private lateinit var buttonSaveProduct: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private lateinit var apiService: ApiService

    private var selectedImageUri: Uri? = null
    private var imageFile: File? = null
    private val brands = mutableListOf<Brand>()
    private val REQUEST_CODE_GALLERY = 1001
    private val REQUEST_CODE_CAMERA = 1002
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        initializeViews()
        setupToolbar()
        setupButtons()
        loadBrands()
    }

    private fun initializeViews() {
        editTextProductName = findViewById(R.id.editTextProductName)
        autoCompleteBrandName = findViewById(R.id.autoCompleteBrandName)
        buttonAddBrand = findViewById(R.id.buttonAddBrand)
        imageViewProductPreview = findViewById(R.id.imageViewProductPreview)
        buttonPickFromGallery = findViewById(R.id.buttonPickFromGallery)
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage)
        buttonSaveProduct = findViewById(R.id.buttonSaveProduct)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)

        apiService = RetrofitClient.apiService
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        buttonAddBrand.setOnClickListener {
            showAddBrandDialog()
        }

        buttonPickFromGallery.setOnClickListener {
            if (checkGalleryPermission()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }

        buttonCaptureImage.setOnClickListener {
            if (checkPermission(Manifest.permission.CAMERA)) {
                openCamera()
            } else {
                requestPermission(Manifest.permission.CAMERA)
            }
        }

        buttonSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun showAddBrandDialog() {
        val dialog = AddBrandDialog()
        dialog.setOnBrandAddedListener {
            loadBrands()
        }
        dialog.show(supportFragmentManager, "AddBrandDialog")
    }

    private fun checkGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                when (permissions[0]) {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_IMAGES -> openGallery()
                    Manifest.permission.CAMERA -> openCamera()
                }
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageFile = File(getExternalFilesDir(null), "product_image_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile!!
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    selectedImageUri = data.data
                    imageViewProductPreview.setImageURI(selectedImageUri)
                }
            }
            REQUEST_CODE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK && imageFile != null) {
                    selectedImageUri = Uri.fromFile(imageFile)
                    imageViewProductPreview.setImageURI(selectedImageUri)
                }
            }
        }
    }

    private fun loadBrands() {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getBrands()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        brands.clear()
                        brands.addAll(response.body()!!)
                        setupBrandDropdown()
                    } else {
                        Toast.makeText(
                            this@AddProductActivity,
                            R.string.error_loading_brands,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@AddProductActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupBrandDropdown() {
        val brandNames = brands.map { it.brandName }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brandNames)
        autoCompleteBrandName.setAdapter(adapter)
    }

    private fun saveProduct() {
        val productName = editTextProductName.text?.toString()?.trim()
        var brandName = autoCompleteBrandName.text?.toString()?.trim()

        if (productName.isNullOrEmpty()) {
            editTextProductName.error = getString(R.string.product_name_required)
            return
        }

        if (brandName.isNullOrEmpty()) {
            autoCompleteBrandName.error = getString(R.string.brand_name_required)
            return
        }

        // Format brand name to UPPERCASE
        brandName = brandName.uppercase()

        if (selectedImageUri == null) {
            Toast.makeText(this, R.string.product_image_required, Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        buttonSaveProduct.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageFile = createImageFile(selectedImageUri!!)
                if (imageFile == null) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        buttonSaveProduct.isEnabled = true
                        Toast.makeText(
                            this@AddProductActivity,
                            R.string.error_processing_image,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val productNameBody = productName.toRequestBody("text/plain".toMediaTypeOrNull())
                val brandNameBody = brandName.toRequestBody("text/plain".toMediaTypeOrNull())
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("product_image", imageFile.name, requestFile)

                val response = apiService.uploadProduct(productNameBody, brandNameBody, imagePart)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonSaveProduct.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(
                            this@AddProductActivity,
                            R.string.product_saved_successfully,
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddProductActivity,
                            R.string.error_saving_product,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    buttonSaveProduct.isEnabled = true
                    Toast.makeText(
                        this@AddProductActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createImageFile(uri: Uri): File? {
        return try {
            val file = File(cacheDir, "product_image_${System.currentTimeMillis()}.jpg")
            val inputStream: InputStream? = when (uri.scheme) {
                "file" -> java.io.FileInputStream(uri.path ?: return null)
                "content" -> contentResolver.openInputStream(uri)
                else -> {
                    // Try to handle FileProvider URIs
                    try {
                        contentResolver.openInputStream(uri)
                    } catch (e: Exception) {
                        // If that fails, try to get the file path
                        val filePath = uri.path
                        if (filePath != null) {
                            java.io.FileInputStream(filePath)
                        } else {
                            null
                        }
                    }
                }
            }
            if (inputStream == null) return null
            FileOutputStream(file).use { output ->
                inputStream.use { it.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

