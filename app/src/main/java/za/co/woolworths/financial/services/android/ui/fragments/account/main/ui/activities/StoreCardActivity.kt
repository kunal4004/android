package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.ACCOUNT_PRODUCT_PAYLOAD
import javax.inject.Inject

@AndroidEntryPoint
class StoreCardActivity : AppCompatActivity() {

    lateinit var binding: AccountProductLandingActivityBinding

    val viewModel: AccountProductsHomeViewModel by viewModels()

    @Inject lateinit var navigationGraph : NavigationGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AccountProductLandingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        setupView()
    }

    private fun setupView() {
        val fragmentContainer = supportFragmentManager.findFragmentById(R.id.accountProductLandingFragmentContainerView) as? NavHostFragment
        navigationGraph.setupNavigationGraph(
            fragmentContainer?.navController,
            graphResId = R.navigation.nav_account_product_landing,
            startDestinationId = R.id.accountProductsMainFragment,
            startDestinationArgs = intent.extras
        )
    }

//
//    fun actionBar() {
//        setSupportActionBar(binding.accountToolbar)
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowTitleEnabled(false)
//            setDisplayUseLogoEnabled(false)
//            setHomeAsUpIndicator(R.drawable.back24)
//        }
//        binding.accountToolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }
//    }
}