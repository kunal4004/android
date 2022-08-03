package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityApplyNowBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters.ApplyNowFragAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.ViewState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
class ApplyNowActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityApplyNowBinding
    val viewModel: ApplyNowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyNowBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel.applyNowState =
            intent.extras?.get(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as ApplyNowState
        with(binding) {
            viewModel.setupBottomSheetBehaviour(incBottomSheetLayout)
            bottomSheetListener()
            clickListeners()
        }
        callApplyNow(viewModel.contentID())

    }


    private fun ActivityApplyNowBinding.clickListeners() {
        incAccountSalesFrontLayout.storeCardApplyNowButton.setOnClickListener(this@ApplyNowActivity)
        incAccountSalesFrontLayout.viewApplicationStatusTextView.setOnClickListener(this@ApplyNowActivity)
        bottomApplyNowButton.setOnClickListener(this@ApplyNowActivity)
        navigateBackImageButton.setOnClickListener(this@ApplyNowActivity)
    }


    private fun callApplyNow(contentId: String) {
        lifecycleScope.launchWhenStarted {
            viewModel.applyNowResponse(contentId = contentId).collect { response ->
                when (response) {
                    is ViewState.RenderSuccess -> {
                        viewModel.applyNowResponse.value = response.output
                        binding.apply {
                            response.output.content.size.apply {
                                viewpagerApplyNow.adapter =
                                    ApplyNowFragAdapter(this@ApplyNowActivity, this)
                                handleTabLayoutVisibility(response.output.content.size)
                            }

                        }
                    }
                    is ViewState.RenderFailure -> {
                    }
                    is ViewState.Loading -> {
                    }
                    ViewState.RenderEmpty -> {
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun ActivityApplyNowBinding.handleTabLayoutVisibility(size: Int) {
        when (size > 1) {
            true -> {
                setupTablayout(tabLayoutApplyNow, viewpagerApplyNow)
            }
            false -> {
                tabLayoutApplyNow.visibility = GONE
                viewTabLayoutApplyNowSeparator.visibility = GONE
            }
        }

    }

    private fun setupTablayout(tabLayoutApplyNow: TabLayout, viewpagerApplyNow: ViewPager2) {
        TabLayoutMediator(tabLayoutApplyNow, viewpagerApplyNow) { tab, position ->
            tab.text = viewModel.applyNowResponse.value!!.content[position].title
        }.attach()
    }

    private fun bottomSheetListener() {
        viewModel.sheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.apply {
                    AnimationUtilExtension.transitionBottomSheetBackgroundColor(
                        dimView,
                        slideOffset
                    )
                    navigateBackImageButton.rotation = slideOffset * -90
                    if (slideOffset > 0.2)
                        AnimationUtilExtension.animateButtonIn(bottomApplyNowButtonRelativeLayout)
                    else
                        AnimationUtilExtension.animateButtonOut(bottomApplyNowButtonRelativeLayout)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.storeCardApplyNowButton, R.id.bottomApplyNowButton -> {
                viewModel.onApplyNowButtonTapped()
                    .let { url -> KotlinUtils.openUrlInPhoneBrowser(url, this) }
            }
            R.id.navigateBackImageButton -> onBackPressed()

            R.id.viewApplicationStatusTextView -> {
                KotlinUtils.openUrlInPhoneBrowser(
                    viewModel.viewApplicationStatusLinkInExternalBrowser(),
                    this
                )

            }

        }
    }

    override fun onBackPressed() {
        // Collapse overlay view if view is opened, else navigate to previous screen
        viewModel.sheetBehavior?.apply {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
                return
            }
        }
        KotlinUtils.onBackPressed(this)
    }
}