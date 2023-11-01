package za.co.woolworths.financial.services.android.shoppinglist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.ConfirmationBottomsheetDialogFragment
import za.co.woolworths.financial.services.android.shoppinglist.listener.MyShoppingListItemClickListener
import za.co.woolworths.financial.services.android.shoppinglist.model.EditOptionType
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class MoreOptionDialogFragment : WBottomSheetDialogFragment() {

    private var listId: String? = ""
    private var selectedItemCount = 0
    private var isConfirmClicked = false
    companion object {
        var listener : MyShoppingListItemClickListener? = null
        const val ITEM_COUNT = "ITEM_COUNT"
        const val COPY_LIST_ID = "COPY_LIST_ID"
        const val CONFIRM_CLICKED = "CONFIRM_CLICKED"

        fun newInstance(shoppingListItemClickListener:MyShoppingListItemClickListener, itemCount:Int, listId:String, isConfirmClick:Boolean) = MoreOptionDialogFragment().withArgs {
            listener = shoppingListItemClickListener
            putInt(ITEM_COUNT, itemCount)
            putString(COPY_LIST_ID, listId)
            putBoolean(CONFIRM_CLICKED, isConfirmClick)
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
                    dialog?.dismiss()
                    val fragment = AddToListFragment.newInstance(listener, listId)
                    fragment.show(parentFragmentManager, AddToListFragment::class.simpleName)
                }, {
                    //todo item move
                }) {
                dialog?.dismiss()
                if (isConfirmClicked) {
                     listener?.itemEditOptionsClick(EditOptionType.RemoveItemFromList)
                } else {
                    val bottomsheetConfirmationDialog = ConfirmationBottomsheetDialogFragment().also {
                        it.arguments = bundleOf(
                            AppConstant.Keys.BUNDLE_KEY to AppConstant.RESULT_DELETE_ITEM_CONFIRMED,
                            AppConstant.Keys.BUNDLE_KEY_SCREEN_NAME to AppConstant.SCREEN_NAME_DELETE_ITEM_CONFIRMATION
                        )
                    }
                    bottomsheetConfirmationDialog.show(
                        requireActivity().supportFragmentManager,
                        ConfirmationBottomsheetDialogFragment::class.java.simpleName
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.apply {

            arguments?.apply {
                selectedItemCount = getInt(ITEM_COUNT, 0)
                listId = getString(COPY_LIST_ID, "")
                isConfirmClicked = getBoolean(CONFIRM_CLICKED, false)
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