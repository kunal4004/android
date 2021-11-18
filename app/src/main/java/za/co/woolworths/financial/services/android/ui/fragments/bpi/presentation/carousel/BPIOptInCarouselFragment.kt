package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import kotlinx.android.synthetic.main.bpi_more_info_fragment.*
import kotlinx.android.synthetic.main.bpi_opt_in_carousel_fragment.*
import kotlinx.android.synthetic.main.on_boarding_fragment.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.BPITermsConditionsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_MORE_INFO_HTML
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_TERMS_CONDITIONS_HTML
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.InsuranceLeadCarousel
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class BPIOptInCarouselFragment : Fragment() {

    private val bpiViewModel: BPIViewModel? by activityViewModels()
    private var moreInfoHTMLContent = ""
    private var termsConditionsHTMLContent = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_opt_in_carousel_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOptInHTMLContent()

        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }
        (activity as? BalanceProtectionInsuranceActivity)?.changeActionBarUIForBPIOptIn()

        val carouselList = bpiViewModel?.insuranceLeadGenCarouselList()
        val carouselSize = carouselList?.size ?: 0
        val insuranceLeadGenCarouselAdapter =
            object : FragmentStateAdapter(this@BPIOptInCarouselFragment) {
                override fun createFragment(position: Int): Fragment =
                    InsuranceLeadGenCarouselItemFragment.newInstance(
                        Pair(InsuranceLeadGenCarouselItemFragment::class.java.simpleName,carouselList?.get(position) ?:
                        InsuranceLeadCarousel.SLIDE_1 ))

                override fun getItemCount(): Int = carouselSize
            }

        findOutCarouselViewPager?.apply {
            adapter = insuranceLeadGenCarouselAdapter

            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = carouselSize

            configurePageIndicator(carouselList)
        }

        nextButton?.setOnClickListener {
            findOutPageIndicatorTabLayout.let { tabLayout ->
                findOutCarouselViewPager?.let { viewPager ->
                    when(nextButton?.text){
                        bindString(R.string.continueLabel) -> {
                            view.findNavController().navigate(R.id.action_BPIOptInCarouselFragment_to_BPIMoreInfoFragment,
                                bundleOf(
                                    BPI_MORE_INFO_HTML to moreInfoHTMLContent,
                                    BPI_TERMS_CONDITIONS_HTML to termsConditionsHTMLContent))
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

    private fun getOptInHTMLContent() {
        arguments?.getString(BalanceProtectionInsuranceActivity.BPI_PRODUCT_GROUP_CODE)?.let { productGroupCode ->
            OneAppService.getBPITermsAndConditionsInfo(productGroupCode).enqueue(CompletionHandler(object : IResponseListener<BPITermsConditionsResponse> {
                override fun onSuccess(response: BPITermsConditionsResponse?) {
                    when(response?.httpCode){
                        AppConstant.HTTP_OK -> {
                            moreInfoHTMLContent = extractHTMLContent(response.moreInformationHtml)
                            termsConditionsHTMLContent = extractHTMLContent(response.termsAndConditionsHtml)
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
        findOutPageIndicatorTabLayout.let { tabLayout ->
            findOutCarouselViewPager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ -> tab.text = "" }.attach()

                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        carouselList?.get(position)?.let {
                            nextButton?.text = bindString(it.buttonCaption)
                        }
                        super.onPageSelected(position)
                    }
                })
            }
        }
    }
}