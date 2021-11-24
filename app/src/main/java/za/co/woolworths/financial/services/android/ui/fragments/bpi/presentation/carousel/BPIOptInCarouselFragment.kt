package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import kotlinx.android.synthetic.main.bpi_opt_in_carousel_fragment.*
import kotlinx.android.synthetic.main.on_boarding_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.InsuranceLeadCarousel

class BPIOptInCarouselFragment : Fragment() {

    private val bpiViewModel: BPIViewModel? by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_opt_in_carousel_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? BalanceProtectionInsuranceActivity)?.changeActionBarUIForCarousel()
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
                    viewPager.currentItem = tabLayout.selectedTabPosition + 1
                    tabLayout.setScrollPosition(viewPager.currentItem, 0f, false, true )
                }
            }

        }
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