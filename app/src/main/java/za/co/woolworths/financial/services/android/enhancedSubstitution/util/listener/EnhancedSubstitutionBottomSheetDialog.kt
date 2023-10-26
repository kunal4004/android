package za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener

import android.content.Context
import android.graphics.Paint
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TemporaryFreezeCartLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

class EnhancedSubstitutionBottomSheetDialog @Inject constructor() :
    EnhancedSubstituionManageDialogListener {
    private lateinit var listener: EnhancedSubstitutionListener
    override fun showEnhancedSubstitionBottomSheetDialog(
        fragment: Fragment,
        context: Context,
        title: String,
        description: String,
        btnText: String
    ) {
        try {
            listener = fragment as EnhancedSubstitutionListener
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val binding = TemporaryFreezeCartLayoutBinding.inflate(dialog.layoutInflater, null, false)
        binding.imageIcon.setImageDrawable(bindDrawable(R.drawable.union_row))
        binding.cancelTextView.text = context.getString(R.string.got_it_btn)
        binding.cancelTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.title.text = title
        binding.description.text = description
        binding.confirmFreezeCardButton.text = btnText
        binding.confirmFreezeCardButton.setOnClickListener {
            listener.openManageSubstituion()
            dialog.dismiss()
        }

        TextViewCompat.setTextAppearance(binding.title, R.style.style_substititon_popup_title)
        TextViewCompat.setTextAppearance(binding.description, R.style.style_substititon_popup_desc)
        TextViewCompat.setTextAppearance(binding.confirmFreezeCardButton, R.style.style_substititon_popup_button)

        binding.cancelTextView?.apply {
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }
}