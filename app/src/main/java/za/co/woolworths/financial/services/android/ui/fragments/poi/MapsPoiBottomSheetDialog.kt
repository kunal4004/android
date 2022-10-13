package za.co.woolworths.financial.services.android.ui.fragments.poi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.poi_map_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment


class MapsPoiBottomSheetDialog(private val clickListner: ClickListner) :
    WBottomSheetDialogFragment(),
    View.OnClickListener {

    interface ClickListner {
        fun onConfirmClick(StreetName: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(
            R.layout.poi_map_bottom_sheet_dialog,
            container,
            false
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dismissButton?.paint?.isUnderlineText = true
        initClick()
    }

    private fun initClick() {
        confirmButton?.setOnClickListener(this)
        dismissButton?.setOnClickListener(this)
        streetNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (streetNameEditText.text.toString().length > 4) {
                    context?.let {
                        confirmButton?.isEnabled = true
                        confirmButton?.setBackgroundColor(ContextCompat.getColor(it, R.color.black))
                    }
                } else {
                    context?.let {
                        confirmButton?.isEnabled = false
                        confirmButton?.setBackgroundColor(
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
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmButton -> {
                val street: String? = streetNameEditText?.text?.toString()
                if (!street.isNullOrEmpty()) {
                    clickListner.onConfirmClick(street)
                }
                dismiss()
            }
            R.id.dismissButton -> {
                dismiss()
            }
        }
    }
}