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
class MoreOptionDialogFragment : WBottomSheetDialogFragment() {

    private var selectedItemCount = 0
    companion object {
        var listener : MyShoppingListItemClickListener? = null
        var ITEM_COUNT = "ITEM_COUNT"
        fun newInstance(shoppingListItemClickListener:MyShoppingListItemClickListener, itemCount:Int) = MoreOptionDialogFragment().withArgs {
            listener = shoppingListItemClickListener
            putInt(ITEM_COUNT, itemCount)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    ) {
        OneAppTheme {
            MoreOptionDialog(
                selectedItemCount,
                {
                    //todo item copy
                }, {
                    //todo item move
                }) {
                dialog?.dismiss()
                val fragment = ConfirmationDialogFragment.newInstance(listener)
                fragment.show(parentFragmentManager, ConfirmationDialogFragment::class.simpleName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.apply {

            arguments?.apply {
                selectedItemCount = getInt(ITEM_COUNT, 0)
            }

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