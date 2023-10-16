package za.co.woolworths.financial.services.android.shoppinglist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.shoppinglist.listener.MyShoppingListItemClickListener
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class ConfirmationDialogFragment : WBottomSheetDialogFragment() {


    companion object {
        var listener : MyShoppingListItemClickListener? = null
        fun newInstance(
            shoppingListItemClickListener: MyShoppingListItemClickListener?): ConfirmationDialogFragment {
            return ConfirmationDialogFragment().withArgs {
                listener = shoppingListItemClickListener
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    ) {

        OneAppTheme {
            Confirmationdialog(
                title = getString(R.string.are_you_sure),
                desc = getString(R.string.remove_desc) , {
                    dialog?.dismiss()
                    listener?.itemRemoveClick()
                }, {
                   dialog?.dismiss()
                }
            )
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