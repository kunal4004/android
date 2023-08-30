package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.app.Activity
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsManageCardListFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.CallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent

class ManageCardItemListener(
    private val activity: Activity,
    private val router: ProductLandingRouterImpl,
    private val includeListOptions: AccountOptionsManageCardListFragmentBinding,
) : (View?) -> Unit {
    val onClickIntentObserver = SingleLiveEvent<CallBack>()
    var isDeviceLinked :Boolean = true
    init {
        setOnClickListener()
    }

    fun setOnClickListener() {
        with(includeListOptions) {
            manageCardRelativeLayout.onClick(this@ManageCardItemListener)
            linkNewCardRelativeLayout.onClick(this@ManageCardItemListener)
            activateVirtualTempCardRelativeLayout.onClick(this@ManageCardItemListener)
            replacementCardRelativeLayout.onClick(this@ManageCardItemListener)
            blockCardRelativeLayout.onClick(this@ManageCardItemListener)
        }
    }

    override fun invoke(view: View?) {
        onClickIntentObserver.value =  when (view?.id) {
            R.id.manageCardRelativeLayout -> { router.routeToManageMyCard(activity) }
            R.id.linkNewCardRelativeLayout -> { router.routeToLinkNewCard(activity) }
            R.id.activateVirtualTempCardRelativeLayout -> { router.routeToActivateVirtualTempCard(activity, isDeviceLinked) }
            R.id.replacementCardRelativeLayout -> { router.routeToGetReplacementCard(activity) }
            R.id.blockCardRelativeLayout ->{ router.routeToBlockCard(activity, isDeviceLinked = isDeviceLinked) }
            else -> null
        }
    }

    fun navigateToActivateVirtualTempCard() {
        isDeviceLinked = false
        includeListOptions.activateVirtualTempCardRelativeLayout.performClick()
        isDeviceLinked = true
    }

    fun onBlockCardTap() {
        isDeviceLinked = false
        includeListOptions.blockCardRelativeLayout.performClick()
        isDeviceLinked = true
    }

}