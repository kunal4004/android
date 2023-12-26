package za.co.woolworths.financial.services.android.shoppinglist.view

import android.os.Build
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
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListViewModel
import za.co.woolworths.financial.services.android.shoppinglist.listener.MyShoppingListItemClickListener
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme


@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class MoreOptionDialogFragment : WBottomSheetDialogFragment() {

    private var listId: String? = ""
    private var selectedItemCount = 0
    private var isConfirmClicked = false
    private var listOfItems:ArrayList<AddToListRequest>? = ArrayList<AddToListRequest>()
    companion object {
        var listener : MyShoppingListItemClickListener? = null
        const val ITEM_COUNT = "ITEM_COUNT"
        const val COPY_LIST_ID = "COPY_LIST_ID"
        const val COPY_ITEM_LIST = "COPY_ITEM_LIST"
        const val CONFIRM_CLICKED = "CONFIRM_CLICKED"

        fun newInstance(shoppingListItemClickListener:MyShoppingListItemClickListener,
                        itemCount:Int,
                        listId:String,
                        isConfirmClick:Boolean,
                        listOfItems:ArrayList<AddToListRequest>) = MoreOptionDialogFragment().withArgs {
            listener = shoppingListItemClickListener
            putInt(ITEM_COUNT, itemCount)
            putString(COPY_LIST_ID, listId)
            putBoolean(CONFIRM_CLICKED, isConfirmClick)
            putParcelableArrayList(AddToListViewModel.ARG_ITEMS_TO_BE_ADDED, listOfItems)
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
                    /*copy item*/
                    dialog?.dismiss()
                    val fragment =
                        listOfItems?.let {
                            AddToListFragment.newInstance(listener, listId, true,
                                it
                            )
                        }
                    fragment?.show(parentFragmentManager, AddToListFragment::class.simpleName)
                }, {
                   /*move item*/
                }) {
                 /*remove item*/
                /*dialog?.dismiss()
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
                }*/
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOfItems = getParcelableArrayList(AddToListViewModel.ARG_ITEMS_TO_BE_ADDED, AddToListRequest::class.java)
                } else {
                    listOfItems = getParcelableArrayList(AddToListViewModel.ARG_ITEMS_TO_BE_ADDED)
                }
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