package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.databinding.ActivityApplyNowBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.account_sales_activity.*
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters.ApplyNowFragAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.ViewState
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
class ApplyNow : AppCompatActivity() {
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var binding: ActivityApplyNowBinding
    val viewModel: ApplyNowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyNowBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        with(binding) {
            viewModel.setupBottomSheetBehaviour(incBottomSheetLayout)
            bottomSheetListener()
        }
        callApplyNow("creditCardcApplyNow")

    }


    private fun callApplyNow(contentId: String) {
        lifecycleScope.launchWhenStarted {
            viewModel.applyNowResponse(contentId = contentId).collect { response ->
                when (response) {
                    is ViewState.RenderSuccess -> {
                        viewModel.applyNowResponse.value = response.output
                        binding.apply {
                            response.output.content.size.apply {
                                viewpagerApplyNow.adapter = ApplyNowFragAdapter(this@ApplyNow, this)

                                when (this > 1) {
                                    true -> {
                                        setupTablayout(tabLayoutApplyNow, viewpagerApplyNow)
                                    }
                                    false -> {
                                        tabLayoutApplyNow.visibility = GONE
                                        viewTabLayoutApplyNowSeparator.visibility = GONE
                                    }
                                }
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

    fun setupTablayout(tabLayoutApplyNow: TabLayout, viewpagerApplyNow: ViewPager2) {
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
                AnimationUtilExtension.transitionBottomSheetBackgroundColor(dimView, slideOffset)
                navigateBackImageButton.rotation = slideOffset * -90
                if (slideOffset > 0.2)
                    AnimationUtilExtension.animateButtonIn(bottomApplyNowButtonRelativeLayout)
                else
                    AnimationUtilExtension.animateButtonOut(bottomApplyNowButtonRelativeLayout)
            }
        })
    }

}