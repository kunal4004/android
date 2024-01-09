package za.co.woolworths.financial.services.android.shoppinglist.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.presentation.common.AppToolBar
import za.co.woolworths.financial.services.android.presentation.common.ToolbarEvents
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.ConfirmationBottomsheetDialogFragment
import za.co.woolworths.financial.services.android.presentation.createlist.CreateListFragment
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.component.MyListScreenEvents
import za.co.woolworths.financial.services.android.shoppinglist.viewmodel.MyListViewModel
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_KEY_CONFIRMATION_DIALOG
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_DELETE_LIST_CONFIRMED
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SCREEN_NAME_DELETE_LIST_CONFIRMATION
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SCREEN_NAME_DELETE_LIST_PROGRESS_BAR
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_DONT_ASK_AGAIN_CHECKED
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_ITEM
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_POSITION
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_KEY_SCREEN_NAME
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MyShoppingListFragment : Fragment() {

    private var mBottomNavigator: BottomNavigator? = null
    private val myListviewModel: MyListViewModel by viewModels()
    private var bottomsheetConfirmationDialog: ConfirmationBottomsheetDialogFragment? = null

    companion object {
        private const val MY_LIST_SIGN_IN_REQUEST_CODE = 7878
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = contentView(
        ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
    ) {
        OneAppTheme {

            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            LaunchedEffect(context) {
                myListviewModel.onScreenEvents.collect {
                    when (it) {
                        is MyListScreenEvents.DismissDialog -> {
                            bottomsheetConfirmationDialog?.dismissAllowingStateLoss()
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.my_list_snackbar_message)
                                    .uppercase()
                            )
                        }

                        else -> {}
                    }
                }
            }

            Scaffold(
                topBar = {
                    val appbarUiState by myListviewModel.appBarUIState
                    Column {
                        AppToolBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .background(color = Color.White),
                            title = stringResource(id = appbarUiState.titleRes),
                            rightButton = stringResource(id = appbarUiState.rightButtonRes),
                            showRightButton = appbarUiState.showRightButton,
                            onClick = {
                                when (it) {
                                    ToolbarEvents.OnBackPressed -> activity?.onBackPressed()
                                    is ToolbarEvents.OnRightButtonClick -> {
                                        myListviewModel.onEvent(MyLIstUIEvents.OnToolbarEditClick(it.buttonText))
                                    }
                                    else -> {}
                                }
                            }
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(color = colorResource(id = R.color.color_D8D8D8))
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState
                    ) {
                        Snackbar(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .height(40.dp),
                            containerColor = Color(0xCC030303),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = it.visuals.message,
                                style = TextStyle(
                                    fontFamily = FuturaFontFamily,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            ) {

                MyListView(
                    Modifier
                        .padding(paddingValues = it)
                        .background(Color.White),
                    myListviewModel
                ) { event ->
                    when (event) {
                        is MyLIstUIEvents.CreateListClick -> {
                            navigateToCreateListFragment()
                        }

                        is MyLIstUIEvents.ListItemClick -> {
                            onShoppingListItemSelected(event.item)
                        }

                        is MyLIstUIEvents.ShareListClick -> {
                            navigateToShareListDialog(event.item)
                        }

                        is MyLIstUIEvents.OnSwipeDeleteAction -> {
                            if (myListviewModel.isCheckedDontAskAgain()) {
                                navigateToDeleteProgressDialog(event.item)
                                myListviewModel.onEvent(
                                    MyLIstUIEvents.OnDeleteListConfirm(event.item, event.position)
                                )
                            } else
                                navigateToDeleteConfirmationDialog(event.item, event.position)
                        }

                        is MyLIstUIEvents.SignInClick -> {
                            navigateToSignInScreen()
                        }

                        else -> {
                            myListviewModel.onEvent(event)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDeleteConfirmationDialog(item: ShoppingList, index: Int) {
        // Close previous dialog if open
        bottomsheetConfirmationDialog?.dismissAllowingStateLoss()
        bottomsheetConfirmationDialog = ConfirmationBottomsheetDialogFragment().also {
            it.arguments = bundleOf(
                BUNDLE_KEY_ITEM to item,
                BUNDLE_KEY_POSITION to index,
                BUNDLE_KEY to RESULT_DELETE_LIST_CONFIRMED,
                BUNDLE_KEY_SCREEN_NAME to SCREEN_NAME_DELETE_LIST_CONFIRMATION
            )
        }
        bottomsheetConfirmationDialog?.show(
            requireActivity().supportFragmentManager,
            ConfirmationBottomsheetDialogFragment::class.java.simpleName
        )
    }

    private fun navigateToDeleteProgressDialog(item: ShoppingList) {
        // Close previous dialog if open
        bottomsheetConfirmationDialog?.dismissAllowingStateLoss()
        bottomsheetConfirmationDialog = ConfirmationBottomsheetDialogFragment().also {
            it.arguments = bundleOf(
                BUNDLE_KEY_ITEM to item,
                BUNDLE_KEY_SCREEN_NAME to SCREEN_NAME_DELETE_LIST_PROGRESS_BAR
            )
        }
        bottomsheetConfirmationDialog?.show(
            requireActivity().supportFragmentManager,
            ConfirmationBottomsheetDialogFragment::class.java.simpleName
        )
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            Handler(Looper.getMainLooper()).postDelayed({
                hideActivityToolbar()
            }, 2000L)
        }
    }

    override fun onResume() {
        super.onResume()
        myListviewModel.onEvent(MyLIstUIEvents.SetDeliveryLocation)
        Handler(Looper.getMainLooper()).postDelayed({
            hideActivityToolbar()
        }, 2000L)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                // This is mostly after changing location from change fulfillment.
                myListviewModel.onEvent(MyLIstUIEvents.SetDeliveryLocation)
            }

            MY_LIST_SIGN_IN_REQUEST_CODE -> {
                myListviewModel.onEvent(MyLIstUIEvents.SignInClick)
            }
        }
    }

    private fun hideActivityToolbar() {
        mBottomNavigator?.apply {
            removeToolbar()
        }
    }

    private fun locationSelectionClicked() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            0,
            KotlinUtils.getPreferredDeliveryType(),
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
        )
    }

    private fun navigateToCreateListFragment() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_MY_LIST_NEW_LIST,
                this
            )
        }
        (requireActivity() as? BottomNavigationActivity)?.apply {
            pushFragmentSlideUp(CreateListFragment())
        }
    }

    private fun navigateToSignInScreen() {
        activity?.let {
            ScreenManager.presentSSOSignin(
                it,
                MY_LIST_SIGN_IN_REQUEST_CODE
            )
        }
    }

    private fun onShoppingListItemSelected(shoppingList: ShoppingList) {
        activity?.let {
            ScreenManager.presentShoppingListDetailActivity(
                it,
                shoppingList.listId,
                shoppingList.listName,
                true
            )
        }
    }

    private fun navigateToShareListDialog(shoppingList: ShoppingList) {
        val fragment = ShoppingListShareDialogFragment.newInstance(shoppingList.listId)
        fragment.show(parentFragmentManager, ShoppingListShareDialogFragment::class.simpleName)
    }

    private fun addObserver() {
        setFragmentResultListener(AppConstant.REQUEST_CODE_CREATE_LIST.toString()) { _, bundle ->
            when (bundle.getInt(AppConstant.RESULT_CODE)) {
                AppConstant.REQUEST_CODE_CREATE_LIST -> {
                    myListviewModel.onEvent(MyLIstUIEvents.OnNewListCreatedEvent)
                }

                else -> {}
            }
        }

        setFragmentResultListener(REQUEST_KEY_CONFIRMATION_DIALOG) { _, bundle ->
            val result = bundle.getString(BUNDLE_KEY)
            if (result == RESULT_DELETE_LIST_CONFIRMED) {
                val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(BUNDLE_KEY_ITEM, ShoppingList::class.java)
                } else {
                    bundle.getParcelable(BUNDLE_KEY_ITEM) as? ShoppingList
                }
                val position = bundle.getInt(BUNDLE_KEY_POSITION, -1)
                val isCheckedDontAskAgain =
                    bundle.getBoolean(BUNDLE_KEY_DONT_ASK_AGAIN_CHECKED, false)
                item?.let {
                    navigateToDeleteProgressDialog(item)
                    myListviewModel.setIsCheckedDontAskAgain(isCheckedDontAskAgain)
                    myListviewModel.onEvent(MyLIstUIEvents.OnDeleteListConfirm(item, position))
                }
            }
        }

        setFragmentResultListener(SearchResultFragment.REFRESH_SHOPPING_LIST_RESULT_CODE.toString()) { _, _ ->
            // As the items in the list has been updated so call getList API again.
            myListviewModel.onEvent(MyLIstUIEvents.OnRefreshEvent)
        }
    }
}