package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardUpshellMessageFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import javax.inject.Inject

@AndroidEntryPoint
class StoreCardActivateVTCUpshellMessageFragment :
    Fragment(R.layout.store_card_upshell_message_fragment) {

    private var binding: StoreCardUpshellMessageFragmentBinding? = null
    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()

    @Inject
    lateinit var router: ProductLandingRouterImpl

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            StoreCardActivateVTCUpshellMessageFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StoreCardUpshellMessageFragmentBinding.bind(view)
        binding?.storeCardImageView?.setImageResource(R.drawable.virtual_temp_activate)
        binding?.storeCardImageView?.contentDescription = context?.getString(R.string.activate_vtsc_upsell_card_image_on_overlay)
        binding?.storeCardImageView?.onClick {
            viewModel.mStoreCardUpsellMessageFlagState.activateVirtualCardFlag()
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }

}