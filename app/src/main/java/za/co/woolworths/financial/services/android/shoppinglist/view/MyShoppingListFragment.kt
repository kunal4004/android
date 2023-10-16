package za.co.woolworths.financial.services.android.shoppinglist.view

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.presentation.common.AppToolBar
import za.co.woolworths.financial.services.android.presentation.createlist.CreateListFragment
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.viewmodel.MyListViewModel
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
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
    private var appBarShowState = mutableStateOf(String())

    companion object {
        private const val MY_LIST_SIGN_IN_REQUEST_CODE = 7878
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = contentView(
        ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
    ) {
        addObserver()

        OneAppTheme {
            Scaffold(
                topBar = {
                    Column {
                        AppToolBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .background(color = Color.White),
                            title = appBarShowState.value,
                            onClick = {
                                activity?.onBackPressed()
                            }
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(color = colorResource(id = R.color.color_D8D8D8))
                        )
                    }
                }) {
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

                        is MyLIstUIEvents.ChangeLocationClick -> {
                            locationSelectionClicked()
                        }

                        is MyLIstUIEvents.ListItemClick -> {
                            onShoppingListItemSelected(event.item)
                        }

                        is MyLIstUIEvents.ShareListClick -> {
                            navigateToShareListDialog(event.item)
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            Handler(Looper.getMainLooper()).postDelayed({
                hideActivityToolbar()
                appBarShowState.value = getString(R.string.my_shopping_lists)
            }, 2000L)
        }
    }

    override fun onResume() {
        super.onResume()
        myListviewModel.onEvent(MyLIstUIEvents.SetDeliveryLocation)
        Handler(Looper.getMainLooper()).postDelayed({
            hideActivityToolbar()
            appBarShowState.value = getString(R.string.my_shopping_lists)
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
        val fragment = ShoppingListShareDialogFragment()
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
    }
}