package com.shoecatalog.app

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.shoecatalog.app.api.ApiService
import com.shoecatalog.app.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBrandDialog : DialogFragment() {

    private lateinit var editTextBrandName: TextInputEditText
    private lateinit var buttonSaveBrand: Button
    private lateinit var buttonCancel: Button
    private lateinit var apiService: ApiService
    private var onBrandAdded: (() -> Unit)? = null

    fun setOnBrandAddedListener(listener: () -> Unit) {
        onBrandAdded = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_brand)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        apiService = RetrofitClient.apiService

        editTextBrandName = dialog.findViewById(R.id.editTextBrandName)
        buttonSaveBrand = dialog.findViewById(R.id.buttonSaveBrand)
        buttonCancel = dialog.findViewById(R.id.buttonCancel)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSaveBrand.setOnClickListener {
            saveBrand(dialog)
        }

        return dialog
    }

    private fun saveBrand(dialog: Dialog) {
        val brandName = editTextBrandName.text?.toString()?.trim()

        if (brandName.isNullOrEmpty()) {
            editTextBrandName.error = getString(R.string.brand_name_required)
            return
        }

        // Format brand name to UPPERCASE
        val brandNameUppercase = brandName.uppercase()

        buttonSaveBrand.isEnabled = false
        buttonSaveBrand.text = getString(R.string.saving)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = mapOf("brand_name" to brandNameUppercase)
                val response = apiService.createBrand(request)

                withContext(Dispatchers.Main) {
                    buttonSaveBrand.isEnabled = true
                    buttonSaveBrand.text = getString(R.string.save_brand)

                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(
                            requireContext(),
                            R.string.brand_saved_successfully,
                            Toast.LENGTH_SHORT
                        ).show()
                        onBrandAdded?.invoke()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_saving_brand,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    buttonSaveBrand.isEnabled = true
                    buttonSaveBrand.text = getString(R.string.save_brand)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }
}

