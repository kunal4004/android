package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.got_it_dialog_icon_fragment.*
import kotlinx.android.synthetic.main.root_device_info_fragment.tvDescription
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class GotItDialogIconFragment : WBottomSheetDialogFragment() {

    private var mButtonText: String? = null
    private var mDescription: String? = null
    private var mTitle: String? = null
    private var mIcon: Int? = null

    companion object {
        private const val TITLE = "TITLE"
        private const val DESCRIPTION = "DESCRIPTION"
        private const val ICON = "ICON"
        private const val BUTTON_TEXT = "BUTTON_TEXT"

        fun newInstance(title: String = "", description: String = "", icon: Int = R.drawable.icon_block_card, buttonText: String = "GOT IT") = GotItDialogIconFragment().withArgs {
            putString(TITLE, title)
            putString(DESCRIPTION, description)
            putInt(ICON, icon)
            putString(BUTTON_TEXT, buttonText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mTitle = getString(TITLE)
            mDescription = getString(DESCRIPTION)
            mButtonText = getString(BUTTON_TEXT)
            mIcon = getInt(ICON)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.got_it_dialog_icon_fragment, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDescription?.text = mDescription
        gotItButtonTapped?.setOnClickListener { dismissAllowingStateLoss() }
    }
}