package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.toolbar.AccountProductsToolbarHelper
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel

@AndroidEntryPoint
class AccountProductsMainFragment : Fragment(R.layout.account_product_landing_main_fragment) {

    val viewModel: AccountProductsHomeViewModel by activityViewModels()

    lateinit var mToolbarContainer: AccountProductsToolbarHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AccountProductLandingMainFragmentBinding.bind(view)
        requireActivity().window?.let {window -> WindowCompat.setDecorFitsSystemWindows(window, false) }
        mToolbarContainer = AccountProductsToolbarHelper(binding, this@AccountProductsMainFragment)
        setupLandingScreen()
    }

    private fun setupLandingScreen() {
            setupGraph(
                graphResId = R.navigation.nav_account_product_landing,
                startDestination =  viewModel.getStartDestinationIdScreen(),
                containerId = R.id.productNavigationContainerView,
                startDestinationArgs = arguments
            )
    }

    fun getChildNavHost(): NavHostFragment? = childFragmentManager.findFragmentById(R.id.productNavigationContainerView) as? NavHostFragment

}