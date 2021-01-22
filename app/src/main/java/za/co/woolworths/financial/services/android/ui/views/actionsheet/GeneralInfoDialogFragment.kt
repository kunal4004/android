package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.general_info_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class GeneralInfoDialogFragment : WBottomSheetDialogFragment() {

    private var mDescription: String = ""
    private var mTitle: String = ""
    private var mActionText: String = ""
    private var mInfoIcon:Int = 0

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        private const val TITLE = "TITLE"
        private const val ACTION_TEXT = "ACTION_TEXT"
        private const val INFO_ICON = "INFO_ICON"
        fun newInstance(description: String, title: String, actionText: String, infoIcon:Int? = null) = GeneralInfoDialogFragment().withArgs {
            putString(DESCRIPTION, description)
            putString(TITLE, title)
            putString(ACTION_TEXT, actionText)
            if (infoIcon != null)
                putInt(INFO_ICON, infoIcon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION, "")
            mTitle = getString(TITLE, "")
            mActionText = getString(ACTION_TEXT, "")
            mInfoIcon = getInt(INFO_ICON)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.general_info_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        description?.text = mDescription
        if (mTitle.isNotEmpty()) {
            title?.apply {
                text = mTitle
                visibility = View.VISIBLE
            }
        }
        if (mActionText.isNotEmpty()) {
            actionButton?.apply {
                text = mActionText
            }
        }

        if (mInfoIcon == 0)
            infoIcon?.visibility = View.GONE
        else {
            infoIcon?.apply {
                setBackgroundResource(mInfoIcon)
                visibility = View.VISIBLE
            }
        }
        actionButton?.setOnClickListener { dismissAllowingStateLoss() }
    }
}