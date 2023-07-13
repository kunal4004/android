package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardSetupDeliveryNowBinding
import za.co.woolworths.financial.services.android.analytic.FirebaseCreditCardDeliveryEvent
import za.co.woolworths.financial.services.android.contracts.ISetupDeliveryNowListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.verifyAppInstanceId
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.saveToLocalDatabase
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class SetUpDeliveryNowDialog() : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: CreditCardSetupDeliveryNowBinding
    private var mApplyNowState: ApplyNowState? = null
    private var mFirebaseCreditCardDeliveryEvent: FirebaseCreditCardDeliveryEvent? = null
    private var deliveredToName: String? = ""
    var mSetUpDeliveryListener: ISetupDeliveryNowListener? = null
    var accountBinNumber: String? = null

    constructor(bundle: Bundle) : this() {
        accountBinNumber = bundle.getString(BundleKeysConstants.ACCOUNTBI_NNUMBER)
    }

    constructor(bundle: Bundle, mSetUpDeliveryListener: ISetupDeliveryNowListener?) : this() {
        bundle.apply {
            accountBinNumber = getString(BundleKeysConstants.ACCOUNTBI_NNUMBER)
        }
        this.mSetUpDeliveryListener = mSetUpDeliveryListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CreditCardSetupDeliveryNowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.init()
        saveToLocalDatabase(SessionDao.KEY.SCHEDULE_CREDIT_CARD_DELIVERY_ON_ACCOUNT_LANDING, "1")
    }

    private fun CreditCardSetupDeliveryNowBinding.init() {
        mApplyNowState = applyNowState()
        mFirebaseCreditCardDeliveryEvent = activity?.let { FirebaseCreditCardDeliveryEvent(mApplyNowState, it) }
        deliveredToName = SessionUtilities.getInstance()?.jwt?.name?.get(0)
        var creditCardName: String = bindString(R.string.black_credit_card_title)
        when {
            accountBinNumber.equals(Utils.GOLD_CARD, true) -> {
                cardImage?.setImageDrawable(bindDrawable(R.drawable.w_gold_credit_card))
                creditCardName = bindString(R.string.gold_credit_card_title)
            }
            accountBinNumber.equals(Utils.SILVER_CARD, true) -> {
                cardImage?.setImageDrawable(bindDrawable(R.drawable.w_silver_credit_card))
                creditCardName = bindString(R.string.silver_credit_card_title)
            }
            accountBinNumber.equals(Utils.BLACK_CARD, true) -> {
                cardImage?.setImageDrawable(bindDrawable(R.drawable.w_black_credit_card))
                creditCardName = bindString(R.string.black_credit_card_title)
            }
        }

        cancel.setOnClickListener(this@SetUpDeliveryNowDialog)
        setUpDeliveryNow.setOnClickListener(this@SetUpDeliveryNowDialog)
        val nameTitleText1 = bindString(R.string.hey_with_space)
        title?.text = nameTitleText1.plus(deliveredToName).plus(bindString(R.string.title_subdesc_setup_cc_delivery).plus(creditCardName).plus("?"))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> {
                mFirebaseCreditCardDeliveryEvent?.forLoginCreditCardDeliveryLater()
                dismiss()
            }
            R.id.setUpDeliveryNow -> {
                handleScheduleDeliveryCreditCard {
                    if (!verifyAppInstanceId())
                        navigateToScheduleOrManage()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (AccountsOptionFragment.SHOW_CREDIT_CARD_SHECULE_OR_MANAGE){
            AccountsOptionFragment.SHOW_CREDIT_CARD_SHECULE_OR_MANAGE = false
            navigateToScheduleOrManage()
        }
    }
    private fun handleScheduleDeliveryCreditCard(doScheduleOrManage: () -> Unit) {
        if (mApplyNowState != null){
            KotlinUtils.linkDeviceIfNecessary(activity,
                mApplyNowState!!,
                {
                    AccountsOptionFragment.CREDIT_CARD_SHECULE_OR_MANAGE = true
                },
                {
                    doScheduleOrManage()
                })
        }
    }
    private fun navigateToScheduleOrManage() {
        mFirebaseCreditCardDeliveryEvent?.forLoginCreditCardDelivery()
        setFragmentResult(SetUpDeliveryNowDialog::class.java.simpleName, bundleOf())
        dismiss()
    }

    private fun applyNowState(): ApplyNowState? {
        return when {
            accountBinNumber.equals(Utils.GOLD_CARD, true) -> ApplyNowState.GOLD_CREDIT_CARD
            accountBinNumber.equals(Utils.SILVER_CARD, true) -> ApplyNowState.SILVER_CREDIT_CARD
            accountBinNumber.equals(Utils.BLACK_CARD, true) -> ApplyNowState.BLACK_CREDIT_CARD
            else -> null
        }
    }
}