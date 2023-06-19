package za.co.woolworths.financial.services.android.presentation.addtolist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListScreenEvents
import za.co.woolworths.financial.services.android.presentation.common.ProgressView
import za.co.woolworths.financial.services.android.presentation.createlist.CreateListScreen
import za.co.woolworths.financial.services.android.presentation.createlist.components.CreateListScreenEvent
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_FAILED
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.KEY_COUNT
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.KEY_HAS_GIFT_PRODUCT
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.KEY_LIST_DETAILS

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class AddToListFragment : WBottomSheetDialogFragment() {

    private val viewModel: AddToListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().apply {
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addedToList.collect {
                    if (it.isEmpty()) {
                        return@collect
                    }

                    val list = viewModel.getListState().selectedListItem
                    if (list.size != it.size) return@collect

                    var resultCode = arguments?.getInt(AppConstant.RESULT_CODE, -1) ?: -1
                    val successList = it.filter { listApiState -> listApiState.isSuccess }
                    if (successList.isEmpty()) {
                        resultCode = RESULT_FAILED
                    }

                    setFragmentResult(
                        requestKey = ADD_TO_SHOPPING_LIST_REQUEST_CODE.toString(),
                        result = Bundle().apply {
                            val addedListItems = viewModel.getAddedListItems()
                            val hasGift = addedListItems.any { item -> item.isGWP }
                            putInt(KEY_COUNT, addedListItems.size)
                            putBoolean(KEY_HAS_GIFT_PRODUCT, hasGift)
                            putParcelableArrayList(KEY_LIST_DETAILS, ArrayList(list))
                            putInt(AppConstant.RESULT_CODE, resultCode)
                        }
                    )
                    dismiss()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    ) {

        BackHandler(enabled = true) {
            // execute your custome logic here
            Log.e("nikesh", "Back button pressed")
        }

        OneAppTheme {

            // If `lifecycleOwner` changes, dispose and reset the effect
            DisposableEffect(viewLifecycleOwner) {
                val observer = LifecycleEventObserver { _, _ -> }

                // Add the observer to the lifecycle
                viewLifecycleOwner.lifecycle.addObserver(observer)

                // When the effect leaves the Composition, remove the observer
                onDispose {
                    viewLifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val listState = viewModel.getListState()
            val listName =
                if (listState.selectedListItem.size == 1) {
                    listState.selectedListItem.getOrNull(0)?.listName ?: ""
                } else {
                    stringResource(id = R.string.multiple_lists)
                }

            when {
                listState.showCreateList -> {
                    val createNewListState = viewModel.getCreateNewListState()
                    CreateListScreen(
                        modifier = Modifier.heightIn(max = 600.dp),
                        state = createNewListState.copy(
                            title = stringResource(id = R.string.shop_create_list),
                            cancelText = stringResource(id = R.string.cancel)
                        )
                    ) {
                        when (it) {
                            CreateListScreenEvent.BackPressed -> {
                                viewModel.onEvent(AddToListScreenEvents.CreateListBackPressed)
                            }

                            CreateListScreenEvent.CancelClick -> {
                                dismiss()
                            }

                            is CreateListScreenEvent.CreateList -> {
                                viewModel.onEvent(AddToListScreenEvents.ConfirmCreateList(it.listName))
                            }
                        }
                    }
                }

                listState.isAddToListInProgress -> {
                    ProgressView(
                        modifier = Modifier.heightIn(min = 290.dp),
                        title = stringResource(
                            id = R.string.add_to_list_progress_title,
                            listName
                        ),
                        desc = stringResource(id = R.string.processing_your_request_desc)
                    )
                }

                else -> {
                    AddToListScreen(
                        modifier = Modifier
                            .background(Color.White)
                            .wrapContentHeight()
                            .heightIn(max = 600.dp),
                        listUiState = listState
                    ) { event ->
                        when (event) {
                            AddToListScreenEvents.CancelClick -> dismiss()
                            else -> viewModel.onEvent(event)
                        }
                    }
                }
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

            setOnKeyListener { _, keyCode, _ ->
                return@setOnKeyListener when (keyCode) {
                    android.view.KeyEvent.KEYCODE_BACK -> {
                        val isCreateListShown = viewModel.getListState().showCreateList
                        if (isCreateListShown) {
                            viewModel.onEvent(AddToListScreenEvents.CreateListBackPressed)
                        }
                        isCreateListShown
                    }

                    else -> false
                }
            }
        }
    }
}