package za.co.woolworths.financial.services.android.ui.fragments.account.storecard

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.fragment.landing.AccountProductsHomeViewModel

@AndroidEntryPoint
class StoreCardActivity : AppCompatActivity() {

    lateinit var binding: AccountProductLandingActivityBinding

    val viewModel : AccountProductsHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AccountProductLandingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar()
        setupView()
    }

    @SuppressLint("ResourceType")
    private fun setupView() {
        viewModel.setupNavigationGraph(
            activity = this,
            navHostFragmentId = R.id.accountProductLandingFragmentContainerView,
            graphResId = R.navigation.nav_account_product_landing,
            startDestinationId = R.id.accountProductsMainFragment,
            startDestinationArgs = intent.extras
        )
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

    companion object {
        const val ACCOUNT_PRODUCT_PAYLOAD = "ACCOUNT_PRODUCT_PAYLOAD"
    }

}