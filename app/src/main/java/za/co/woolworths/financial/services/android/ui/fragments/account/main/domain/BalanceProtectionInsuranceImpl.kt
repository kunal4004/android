package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import android.app.Activity
import android.content.Intent
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIOverviewOverviewImpl
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap
import javax.inject.Inject

interface IBalanceProtectionInsurance {
    fun getBalanceProtectionInsuranceLead(): BpiInsuranceApplication?
    fun isInsuranceCovered(): Boolean
    fun balanceProtectionInsuranceTag(): AccountOptionsScreenUI.BalanceProtectionInsurance
    fun getDisplayLabel(): String?
    fun isBpiStatusInProgress(status: String?): Boolean
    fun setupIntent(activity: Activity?): Intent?
}

class BalanceProtectionInsuranceImpl @Inject constructor(private val accountDao: AccountProductLandingDao?) :
    IBalanceProtectionInsurance {

    override fun getBalanceProtectionInsuranceLead(): BpiInsuranceApplication? {
        return accountDao?.product?.bpiInsuranceApplication
    }

    override fun isInsuranceCovered(): Boolean {
        return accountDao?.product?.insuranceCovered ?: false
    }

    override fun balanceProtectionInsuranceTag(): AccountOptionsScreenUI.BalanceProtectionInsurance {
        val insuranceLeadGen = getBalanceProtectionInsuranceLead()
        var leadGen: BpiInsuranceApplication? = null
        val status = when {
            insuranceLeadGen != null -> when (insuranceLeadGen.status) {
                BpiInsuranceApplicationStatusType.COVERED,
                BpiInsuranceApplicationStatusType.NOT_OPTED_IN,
                BpiInsuranceApplicationStatusType.OPTED_IN -> {
                    leadGen = insuranceLeadGen
                    insuranceLeadGen.status
                }
                else -> BpiInsuranceApplicationStatusType.NOT_COVERED
            }
            else -> {
                leadGen = null
                BpiInsuranceApplicationStatusType.DISABLED
            }
        }
        return AccountOptionsScreenUI.BalanceProtectionInsurance(status, leadGen)
    }

    override fun getDisplayLabel(): String? = getBalanceProtectionInsuranceLead()?.displayLabel

    override fun isBpiStatusInProgress(status: String?): Boolean {
        return getDisplayLabel().equals(status, ignoreCase = true)
    }

    override fun setupIntent(activity: Activity?): Intent {

        val account = accountDao?.product
        val accountObjectInStringFormat = accountDao?.getAccountInStringFormat()
        val status = getBalanceProtectionInsuranceLead()?.status
        val accountsProductGroupCode = account?.productGroupCode
        val navigateToBalanceProtectionInsurance = Intent(activity, BalanceProtectionInsuranceActivity::class.java)

        val productGroupCode = when (accountsProductGroupCode) {
            AccountsProductGroupCode.STORE_CARD.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDBPI
            AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANBPI
            else -> FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDBPI
        }

        Utils.triggerFireBaseEvents(productGroupCode, activity)

        if (status == BpiInsuranceApplicationStatusType.NOT_OPTED_IN) {
                var bpiTaggingEventCode: String? = null
                val arguments: MutableMap<String, String> = HashMap()

                when (accountsProductGroupCode) {
                    AccountsProductGroupCode.CREDIT_CARD.groupCode -> {
                        bpiTaggingEventCode = FirebaseManagerAnalyticsProperties.CC_BPI_OPT_IN_START
                        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                            FirebaseManagerAnalyticsProperties.PropertyValues.CC_BPI_OPT_IN_START_VALUE
                    }
                    AccountsProductGroupCode.STORE_CARD.groupCode -> {
                        bpiTaggingEventCode = FirebaseManagerAnalyticsProperties.SC_BPI_OPT_IN_START
                        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                            FirebaseManagerAnalyticsProperties.PropertyValues.SC_BPI_OPT_IN_START_VALUE
                    }
                    AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> {
                        bpiTaggingEventCode = FirebaseManagerAnalyticsProperties.PL_BPI_OPT_IN_START
                        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                            FirebaseManagerAnalyticsProperties.PropertyValues.PL_BPI_OPT_IN_START_VALUE
                    }
                }

               Utils.triggerFireBaseEvents(bpiTaggingEventCode, arguments, activity)

                navigateToBalanceProtectionInsurance.putExtra(
                    BalanceProtectionInsuranceActivity.BPI_OPT_IN,
                    true
                )

            navigateToBalanceProtectionInsurance.putExtra(
                    BalanceProtectionInsuranceActivity.BPI_PRODUCT_GROUP_CODE,
                    accountsProductGroupCode
                )
        }

        navigateToBalanceProtectionInsurance.putExtra(BPIOverviewOverviewImpl.ACCOUNT_INFO, accountObjectInStringFormat)

        return navigateToBalanceProtectionInsurance
    }
}