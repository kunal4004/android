package za.co.woolworths.financial.services.android.ui.fragments.poi

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PoiMapBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class PoiBottomSheetDialog(private val clickListener: ClickListener, private val isPoiAddress:Boolean) :
    WBottomSheetDialogFragment(),
    View.OnClickListener {

    interface ClickListener {
        fun onConfirmClick(StreetName: String)
    }

    private lateinit var binding: PoiMapBottomSheetDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PoiMapBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            dismissButton.paint?.isUnderlineText = true
            initClick()
        }
    }

    private fun PoiMapBottomSheetDialogBinding.initClick() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ViewCompat.setOnApplyWindowInsetsListener(dialog?.window?.decorView!!) { _, insets ->
                    val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val navigationBarHeight =
                        insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    root.setPadding(0, 0, 0, imeHeight - navigationBarHeight)
                    insets
                }
            } else {
                @Suppress("DEPRECATION")
                dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        confirmButton.setOnClickListener(this@PoiBottomSheetDialog)
        dismissButton.setOnClickListener(this@PoiBottomSheetDialog)
        streetNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (streetNameEditText.text.toString().trim().length > 4) {
                    context?.let {
                        confirmButton.isEnabled = true
                        confirmButton.setBackgroundColor(ContextCompat.getColor(it, R.color.black))
                    }
                } else {
                    context?.let {
                        confirmButton.isEnabled = false
                        confirmButton.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                R.color.button_disable
                            )
                        )
                    }

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        if (!isPoiAddress) {
            binding.weNeedMoreTv.text = getString(R.string.un_Indexed_address_popup_title)
            binding.enterStreetNumber.text =
                getString(R.string.un_Indexed_address_popup_additional_info_placeholder)
        } else {
            binding.weNeedMoreTv.text = getString(R.string.we_need_more_info)
            binding.enterStreetNumber.text =
                getString(R.string.enter_street_name)

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmButton -> {
                val street: String? = binding.streetNameEditText.text.trim().toString()
                if (!street.isNullOrEmpty()) {
                    clickListener.onConfirmClick(street)
                }
                dismiss()
            }
            R.id.dismissButton -> {
                dismiss()
            }
        }
    }
}