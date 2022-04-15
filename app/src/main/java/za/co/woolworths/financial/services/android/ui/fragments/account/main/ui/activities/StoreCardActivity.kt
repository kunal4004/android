package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class StoreCardActivity : AppCompatActivity() {

    lateinit var binding: AccountProductLandingActivityBinding

    val viewModel: AccountProductsHomeViewModel by viewModels()

    @Inject
    lateinit var navigationGraph: NavigationGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentStatusBar()
        binding = AccountProductLandingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setTransparentStatusBar() {
        window?.apply {
            when (Build.VERSION.SDK_INT) {
                in 22..29 -> {
                    statusBarColor = Color.TRANSPARENT
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
                else -> {
                    statusBarColor = Color.TRANSPARENT
                    // Making status bar overlaps with the activity
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    // Root ViewGroup of my activity
                    ViewCompat.setOnApplyWindowInsetsListener(window.decorView){ _, _ ->

                        // Return CONSUMED if you don't want want the window insets to keep being
                        // passed down to descendant views.
                        WindowInsetsCompat.CONSUMED
                    }
                }
            }
        }
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
}