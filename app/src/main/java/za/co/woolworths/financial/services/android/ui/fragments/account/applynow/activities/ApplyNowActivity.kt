package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityApplyNowBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.applynow.Content
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
            setupToolbarTopMargin()
        }
        callApplyNow(viewModel.contentID())

    }

    private fun ActivityApplyNowBinding.setupToolbarTopMargin() {
        KotlinUtils.setTransparentStatusBar(this@ApplyNowActivity)
        val params = toolbar.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight()
        toolbar.layoutParams = params
    }

    private fun ActivityApplyNowBinding.setupView(content: Content) {
        incAccountSalesFrontLayout.accountSalesCardHeader.cardFrontImageView.visibility = GONE
        incAccountSalesFrontLayout.accountSalesCardHeader.cardBackImageView.visibility = GONE
//        incAccountSalesFrontLayout.root.account.root.cardFrontImageView.setImageResource()
        incAccountSalesFrontLayout.storeCardDescriptionTextView.text = content.description
        incAccountSalesFrontLayout.storeCardTitleTextView.text = content.title
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
                                setupView(response.output.content[0])
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
                setupTablayout()
            }
            false -> {
                tabLayoutApplyNow.visibility = GONE
                viewTabLayoutApplyNowSeparator.visibility = GONE
            }
        }

    }

    private fun ActivityApplyNowBinding.setupTablayout() {
        TabLayoutMediator(tabLayoutApplyNow, viewpagerApplyNow) { tab, position ->
            viewModel.applyNowResponse.value!!.content[position].apply {
                tab.text = this.title.substringBefore(' ')
            }

        }.attach()
        tabLayoutApplyNow.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                setupView(viewModel.applyNowResponse.value!!.content[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
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
                    if (slideOffset > 0.2) {
                        bottomApplyNowButton.isEnabled = true
                        AnimationUtilExtension.animateButtonIn(bottomApplyNowButtonRelativeLayout)
                    } else {
                        bottomApplyNowButton.isEnabled = false
                        AnimationUtilExtension.animateButtonOut(bottomApplyNowButtonRelativeLayout)
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                incAccountSalesFrontLayout.storeCardApplyNowButton, bottomApplyNowButton -> {
                    viewModel.onApplyNowButtonTapped()
                        .let { url -> KotlinUtils.openUrlInPhoneBrowser(url, this@ApplyNowActivity) }
                }
                navigateBackImageButton -> onBackPressed()

                incAccountSalesFrontLayout.viewApplicationStatusTextView -> {
                    KotlinUtils.openUrlInPhoneBrowser(
                        viewModel.viewApplicationStatusLinkInExternalBrowser(),
                        this@ApplyNowActivity
                    )

                }

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