package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityApplyNowBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowSectionReference
import za.co.woolworths.financial.services.android.models.dto.account.applynow.Content
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters.ApplyNowFragAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ToastFactory
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.showErrorDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
class ApplyNowActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityApplyNowBinding
    val viewModel: ApplyNowViewModel by viewModels()
    private var wasApplicationInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyNowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.applyNowState = intent.extras?.get(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as ApplyNowState
        with(binding) {
            viewModel.setupBottomSheetBehaviour(incBottomSheetLayout)
            bottomSheetListener()
            clickListeners()
            setupToolbarTopMargin()
            setHeader()
            setHeaderTitleAndDesc()
        }
        callApplyNow(viewModel.contentID())
    }

    private fun ActivityApplyNowBinding.setupToolbarTopMargin() {
        KotlinUtils.setTransparentStatusBar(this@ApplyNowActivity)
        val params = toolbar.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight()
        toolbar.layoutParams = params
    }
    private fun ActivityApplyNowBinding.setHeaderTitleAndDesc(){
        viewModel.getApplyNowResourcesData().apply{
            incAccountSalesFrontLayout.let {
                it.titleTextView.text = this.cardHeader.title
                it.descriptionTextView.text = this.cardHeader.description
            }
        }
    }

    private fun ActivityApplyNowBinding.setHeader(){
        viewModel.getApplyNowResourcesData().apply{
            incAccountSalesFrontLayout.constraintLayoutSignOut.background = AppCompatResources.getDrawable(this@ApplyNowActivity,this.cardHeader.drawables[0])
            incAccountSalesFrontLayout.accountSalesCardHeader.cardFrontImageView.let {
                it.visibility = if(viewModel.isBlackCreditCard())  INVISIBLE else VISIBLE
                AnimationUtilExtension.animateViewPushDown(it)
                it.setImageResource(this.cardHeader.drawables[1])
            }
            incAccountSalesFrontLayout.accountSalesCardHeader.cardFrontBlackImageView.let {
                it.visibility = if(viewModel.isBlackCreditCard())  VISIBLE else GONE
                it.setImageResource(this.cardHeader.drawables[1])
            }
            incAccountSalesFrontLayout.accountSalesCardHeader.cardBackImageView.let {
                AnimationUtilExtension.animateViewPushDown(it)
                it.setImageResource(this.cardHeader.drawables[2])
            }
        }
    }
    private fun ActivityApplyNowBinding.setupView(content: Content) {
        incAccountSalesFrontLayout.descriptionTextView.text = content.description
        incAccountSalesFrontLayout.titleTextView.text = content.title
    }

    private fun ActivityApplyNowBinding.clickListeners() {
        incAccountSalesFrontLayout.applyNowHeaderButton.setOnClickListener(this@ApplyNowActivity)
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
                            hideShimmer()
                            response.output.content.apply {
                                viewpagerApplyNow.adapter = ApplyNowFragAdapter(this@ApplyNowActivity, this.size)
                                handleTabLayoutVisibility(this.size)
                                setupView(this[0])
                                viewModel.setApplyNowStateForCC(ApplyNowSectionReference.valueOf(this[0].reference))
                                setHeader()
                                tabLayoutApplyNow.selectTab(tabLayoutApplyNow.getTabAt(1))
                            }
                        }
                    }
                    is ViewState.RenderFailure,
                    is ViewState.RenderErrorFromResponse-> {errorDialog()}
                    is ViewState.Loading,
                    is ViewState.RenderEmpty -> {}
                    is ViewState.RenderNoConnection->{ ToastFactory.showNoConnectionFound(this@ApplyNowActivity) }
                }
            }
        }
    }
    private fun errorDialog(){
        runOnUiThread{
            val serverErrorResponse = ServerErrorResponse()
            serverErrorResponse.desc = getString(R.string.general_error_desc) ?: ""
            showErrorDialog(this@ApplyNowActivity, serverErrorResponse)
        }
    }

    private fun ActivityApplyNowBinding.hideShimmer() {
        shimmerApplyNow.apply {
            visibility = GONE
            stopShimmer()
        }
    }
    private fun ActivityApplyNowBinding.handleTabLayoutVisibility(size: Int) {
        when (size > 1) {
            true -> { setupTablayout() }
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
                viewModel.applyNowResponse.value!!.content[tab.position].apply {
                    viewModel.setApplyNowStateForCC(ApplyNowSectionReference.valueOf(this.reference))
                    setupView(this)
                }
                setHeader()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    private fun bottomSheetListener() {
        viewModel.sheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
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
                incAccountSalesFrontLayout.applyNowHeaderButton, bottomApplyNowButton -> {
                    viewModel.onApplyNowButtonTapped()
                        .let { url ->
                            KotlinUtils.openUrlInPhoneBrowser(
                                url,
                                this@ApplyNowActivity
                            )
                        }
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
        if(wasApplicationInBackground) redirectToMyAccounts() else KotlinUtils.onBackPressed(this)
    }

    private fun redirectToMyAccounts() {
        startActivity(Intent(this, BottomNavigationActivity::class.java))
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onStart() {
        super.onStart()
        wasApplicationInBackground = !WoolworthsApplication.isApplicationInForeground()
    }
}