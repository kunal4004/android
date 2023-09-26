package za.co.woolworths.financial.services.android.shoppinglist.view

import android.content.Context
import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.presentation.common.AppToolBar
import za.co.woolworths.financial.services.android.presentation.createlist.CreateListFragment
import za.co.woolworths.financial.services.android.shoppinglist.MyLIstUIEvents
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MyShoppingListFragment : Fragment() {

    private var mBottomNavigator: BottomNavigator? = null

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
        OneAppTheme {
            Scaffold(
                topBar = {
                    Column {
                        AppToolBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .background(color = Color.White),
                            title = LocalContext.current.getString(R.string.my_shopping_lists),
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
                ) { event ->
                    when (event) {
                        is MyLIstUIEvents.CreateListClick -> {
                            navigateToCreateListFragment()
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            hideActivityToolbar()
        }
    }

    override fun onResume() {
        super.onResume()
        hideActivityToolbar()
    }

    private fun hideActivityToolbar() {
        mBottomNavigator?.apply {
            removeToolbar()
        }
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
}