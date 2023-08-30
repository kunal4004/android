package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiOptInCarouselFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.BPITermsConditionsResponse
import za.co.woolworths.financial.services.android.models.dto.bpi.BPITermsConditions
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.InsuranceLeadCarousel
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class BPIOptInCarouselFragment : BaseFragmentBinding<BpiOptInCarouselFragmentBinding>(BpiOptInCarouselFragmentBinding::inflate) {

    private val bpiViewModel: BPIViewModel? by activityViewModels()
    private var productGroupCode: String? = null

    companion object{
        var htmlContent: BPITermsConditions? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productGroupCode = arguments?.getString(BPI_PRODUCT_GROUP_CODE)
        getOptInHTMLContent()

        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        val carouselList = bpiViewModel?.insuranceLeadGenCarouselList()
        val carouselSize = carouselList?.size ?: 0
        val insuranceLeadGenCarouselAdapter =
            object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                override fun createFragment(position: Int): Fragment =
                    InsuranceLeadGenCarouselItemFragment.newInstance(
                        Pair(
                            InsuranceLeadGenCarouselItemFragment::class.java.simpleName,
                            carouselList?.get(position) ?: InsuranceLeadCarousel.SLIDE_1
                        )
                    )

                override fun getItemCount(): Int = carouselSize

                override fun containsItem(itemId: Long): Boolean {
                    return carouselList?.let { itemId < it.size } ?: false
                }
            }

        binding.findOutCarouselViewPager?.apply {
            adapter = insuranceLeadGenCarouselAdapter

            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = carouselSize

            configurePageIndicator(carouselList)
        }

        binding.nextButton?.onClick {
            binding.findOutPageIndicatorTabLayout.let { tabLayout ->
                binding.findOutCarouselViewPager?.let { viewPager ->
                    when(binding.nextButton?.text){
                        bindString(R.string.continueLabel) -> {
                            if(htmlContent == null){
                                getOptInHTMLContent()
                            }
                            view.findNavController().navigate(R.id.action_BPIOptInCarouselFragment_to_BPIMoreInfoFragment,
                                bundleOf(BPI_PRODUCT_GROUP_CODE to productGroupCode))
                        }
                        else -> {
                            viewPager.currentItem = tabLayout.selectedTabPosition + 1
                            tabLayout.setScrollPosition(viewPager.currentItem, 0f, false, true )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        (activity as? BalanceProtectionInsuranceActivity)?.changeActionBarUIForBPIOptIn()
        super.onResume()
    }

    private fun getOptInHTMLContent() {
        productGroupCode?.let { productGroupCode ->
            OneAppService().getBPITermsAndConditionsInfo(productGroupCode).enqueue(CompletionHandler(object : IResponseListener<BPITermsConditionsResponse> {
                override fun onSuccess(response: BPITermsConditionsResponse?) {
                    when(response?.httpCode){
                        AppConstant.HTTP_OK -> {
                            htmlContent = BPITermsConditions(
                                extractHTMLContent(response.moreInformationHtml),
                                extractHTMLContent(response.termsAndConditionsHtml))
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    //do nothing
                }
            }, BPITermsConditionsResponse::class.java))
        }
    }

    private fun extractHTMLContent(response: String?): String{
        response?.
        replace("#", "%23")?.
        replace("\n", "")
            .let {
                if (it != null) {
                    return it
                }
            }
        return ""
    }

    private fun configurePageIndicator(carouselList: Array<InsuranceLeadCarousel>?) {
        binding.findOutPageIndicatorTabLayout.let { tabLayout ->
            binding.findOutCarouselViewPager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ -> tab.text = "" }.attach()

                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        carouselList?.get(position)?.let {
                            binding.nextButton?.text = bindString(it.buttonCaption)
                        }
                        super.onPageSelected(position)
                    }
                })
            }
        }
    }
}