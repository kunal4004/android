package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.ACCOUNT_PRODUCT_PAYLOAD

@AndroidEntryPoint
class StoreCardActivity : AppCompatActivity() {

    lateinit var binding: AccountProductLandingActivityBinding

    val viewModel: AccountProductsHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AccountProductLandingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        actionBar()
        setAccountObject()
        setupView()
    }

    private fun setupView() {
        val fragmentContainer = supportFragmentManager.findFragmentById(R.id.accountProductLandingFragmentContainerView) as? NavHostFragment
        viewModel.setupNavigationGraph(
            fragmentContainer?.navController,
            graphResId = R.navigation.nav_account_product_landing,
            startDestinationId = R.id.accountProductsMainFragment,
            startDestinationArgs = intent.extras
        )
    }

    /**
     * TODO:: Fetch data from room db after room implementation
     */
    private fun setAccountObject() {
        val bundle = intent.extras
        val accountStr = bundle?.getString(ACCOUNT_PRODUCT_PAYLOAD)
        viewModel.saveAccount(accountStr)
    }

    fun actionBar() {
        setSupportActionBar(binding.accountToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
        binding.accountToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}