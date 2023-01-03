package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.GeneralInfoDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class GeneralInfoDialogFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: GeneralInfoDialogFragmentBinding
    private var mDescription: String = ""
    private var mTitle: String = ""
    private var mActionText: String = ""
    private var mInfoIcon:Int = 0
    private var mIsFromCheckoutScreen = false

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        private const val TITLE = "TITLE"
        private const val ACTION_TEXT = "ACTION_TEXT"
        private const val INFO_ICON = "INFO_ICON"
        private const val IS_FROM_CHECKOUT_SCREEN = "ISFROMCHECKOUTSCREEN"
        fun newInstance(description: String, title: String, actionText: String, infoIcon:Int? = null, isFromCheckoutScreen:Boolean = false) = GeneralInfoDialogFragment().withArgs {
            putString(DESCRIPTION, description)
            putString(TITLE, title)
            putString(ACTION_TEXT, actionText)
            if (infoIcon != null)
                putInt(INFO_ICON, infoIcon)
            putBoolean(IS_FROM_CHECKOUT_SCREEN, isFromCheckoutScreen)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION, "")
            mTitle = getString(TITLE, "")
            mActionText = getString(ACTION_TEXT, "")
            mInfoIcon = getInt(INFO_ICON)
            mIsFromCheckoutScreen = getBoolean(IS_FROM_CHECKOUT_SCREEN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GeneralInfoDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
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
            actionButton?.setOnClickListener {
                if (mIsFromCheckoutScreen) {
                    dismissAllowingStateLoss()
                    activity?.onBackPressed()
                } else {
                    dismissAllowingStateLoss()
                }
            }
        }
    }
}