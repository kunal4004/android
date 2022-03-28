package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.databinding.AccountOptionsManageCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.CardViewPager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.ManageCardScreenSlidesAdapter
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsManageCardFragment : ViewBindingFragment<AccountOptionsManageCardFragmentBinding>(AccountOptionsManageCardFragmentBinding::inflate) {

    @Inject
    lateinit var cardSliderAdapter: ManageCardScreenSlidesAdapter

    private val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardViewPager = CardViewPager()

        lifecycleScope.launchWhenStarted {
            viewModel.queryServiceGetStoreCardCards().collect {
                with(it) {
                    renderSuccess {
                        setupCard(this.output, cardViewPager) }
                    renderFailure { Log.e("renderStatus", "renderFailure") }
                    renderEmpty { Log.e("renderStatus", "renderEmpty") }
                    renderLoading { Log.e("renderStatus", "renderLoading ${this.isLoading}") }
                }
            }
        }
    }

    private fun setupCard(storeCardsResponse: StoreCardsResponse, cardViewPager: CardViewPager) {
        val storeCardsData = storeCardsResponse.storeCardsData
        cardSliderAdapter.setItem(storeCardsData?.primaryCards)
        cardViewPager.invoke(binding.accountOptionsManageCardViewPager, binding.tab, cardSliderAdapter)
    }
}

