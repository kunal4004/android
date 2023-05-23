package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery.SetUpDeliveryNowDialog
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ApplyNowStateMapToAccountBinNumber
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import javax.inject.Inject

interface CreditCardDeliveryNavigation{
    fun show(response: CreditCardDeliveryStatusResponse,
              applyNowStateToAccountBinNumber: ApplyNowStateMapToAccountBinNumber)
    fun redirectToCreditCardActivity(viewModel : UserAccountLandingViewModel)
}

class CreditCardDeliveryNavigationImpl @Inject constructor(private val activity : Activity?) :
    CreditCardDeliveryNavigation {

    override fun show(response: CreditCardDeliveryStatusResponse,
                      applyNowStateToAccountBinNumber: ApplyNowStateMapToAccountBinNumber) {
        activity ?: return
        val bundle = Bundle()
        bundle.putString(BundleKeysConstants.ACCOUNTBI_NNUMBER, applyNowStateToAccountBinNumber?.second)
        val supportFragmentManager = (activity as? AppCompatActivity)?.supportFragmentManager
        val setUpDeliveryNowDialog = SetUpDeliveryNowDialog(bundle)
        supportFragmentManager?.let { setUpDeliveryNowDialog.show(it, SetUpDeliveryNowDialog::class.java.simpleName) }
    }
    override fun redirectToCreditCardActivity(viewModel : UserAccountLandingViewModel) {
        activity?.apply {
            val account = viewModel.findCreditCardProduct()
            val accountNumberBin = account?.accountNumberBin
            val applyNowState = viewModel.getApplyNowState(accountNumberBin)
            val creditCardDeliveryStatusResponse = viewModel.scheduleDeliveryNetworkState.value
            val intent = Intent(this, CreditCardDeliveryActivity::class.java)
            val bundle = Bundle()
            bundle.putString(BundleKeysConstants.ENVELOPE_NUMBER, account?.cards?.get(0)?.envelopeNumber)
            bundle.putString(BundleKeysConstants.ACCOUNTBI_NNUMBER, accountNumberBin)
            bundle.putParcelable(BundleKeysConstants.STATUS_RESPONSE, creditCardDeliveryStatusResponse.data?.statusResponse)
            bundle.putString(BundleKeysConstants.PRODUCT_OFFERINGID, account?.productOfferingId?.toString())
            bundle.putBoolean("setUpDeliveryNowClicked", true)
            if (applyNowState != null)
                bundle.putSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE, applyNowState)
            intent.putExtra(BundleKeysConstants.BUNDLE, bundle)
            startActivity(intent)
        }
    }

}