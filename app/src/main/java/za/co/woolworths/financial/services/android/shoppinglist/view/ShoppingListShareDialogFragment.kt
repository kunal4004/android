package za.co.woolworths.financial.services.android.shoppinglist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.shoppinglist.utility.prepareUrl
import za.co.woolworths.financial.services.android.shoppinglist.utility.shareListUrl
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@AndroidEntryPoint
class ShoppingListShareDialogFragment : WBottomSheetDialogFragment() {

    private var listid: String = ""
    private var selectedUrlOption: String = ""

    companion object {
        const val LIST_ID = "LISTID"
        fun newInstance(listId: String?) =
            ShoppingListShareDialogFragment().withArgs {
                this.putString(LIST_ID, listId)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    ) {
        arguments?.apply {
            listid = getString(LIST_ID, "")
        }

        OneAppTheme {
            ShareListDialog(onShareButtonClick = { selectedOption ->
                selectedUrlOption =
                    if (selectedOption == getString(R.string.view_only_option)) {
                        getString(R.string.view_only_url_option)
                    } else {
                        getString(R.string.edit_url_option)
                    }
                dialog?.dismiss()
                activity?.let {
                    shareListUrl(
                        prepareUrl(listId = listid, selectedOption),
                        requireActivity()
                    )
                }
            }) {
                dialog?.dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.apply {

            setOnShowListener { dialog ->
                val bottomSheet =
                    (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout?
                bottomSheet?.let { sheet ->
                    BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.from(sheet).isDraggable = false
                }
            }
        }
    }
}