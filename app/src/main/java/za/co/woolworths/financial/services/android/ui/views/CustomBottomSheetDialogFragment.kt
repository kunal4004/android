package za.co.woolworths.financial.services.android.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_custom_bottomsheet_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 13/04/22.
 */
class CustomBottomSheetDialogFragment : WBottomSheetDialogFragment(),
    View.OnClickListener {

    companion object {
        const val DIALOG_TITLE = "dialog_title"
        const val DIALOG_SUB_TITLE = "dialog_sub_title"
        const val DIALOG_BUTTON_TEXT = "dialog_button_text"
        const val DIALOG_TITLE_IMG = "dialog_title_img"
        const val DIALOG_BUTTON_CLICK_RESULT = "dialog_button_click_result"

        fun newInstance(
            title: String,
            subTitle: String,
            dialog_button_text: String,
            dialog_title_img: Int,
        ) =
            CustomBottomSheetDialogFragment().withArgs {
                putString(DIALOG_TITLE, title)
                putString(DIALOG_SUB_TITLE, subTitle)
                putString(DIALOG_BUTTON_TEXT, dialog_button_text)
                putInt(DIALOG_TITLE_IMG, dialog_title_img)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_custom_bottomsheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        arguments?.apply {
            val title = getString(DIALOG_TITLE, "")
            if (title.isNullOrEmpty()) {
                tvTitle?.visibility = View.GONE
            } else {
                tvTitle?.visibility = View.VISIBLE
                tvTitle?.text = title
            }
            val subTitle = getString(DIALOG_SUB_TITLE, "")
            if (subTitle.isNullOrEmpty()) {
                tvDescription?.visibility = View.GONE
            } else {
                tvDescription?.visibility = View.VISIBLE
                tvDescription?.text = subTitle
            }
            val buttonText = getString(DIALOG_BUTTON_TEXT, "")
            if (buttonText.isNullOrEmpty()) {
                buttonAction?.visibility = View.GONE
            } else {
                buttonAction?.visibility = View.VISIBLE
                buttonAction?.text = buttonText
            }
            val dialogImg = getInt(DIALOG_TITLE_IMG)
            if (dialogImg != null) {
                img_view.visibility = View.VISIBLE
                img_view.setImageResource(dialogImg)
            } else
                img_view.visibility = View.GONE
        }

        tvDismiss.setOnClickListener(this)
        buttonAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonAction -> {
                setFragmentResult(DIALOG_BUTTON_CLICK_RESULT, bundleOf())
                dismiss()
            }
            R.id.tvDismiss -> {
                dismiss()
            }
        }
    }
}