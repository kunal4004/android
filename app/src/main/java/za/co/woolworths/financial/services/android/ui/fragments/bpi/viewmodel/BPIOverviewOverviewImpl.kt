package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import android.os.Bundle
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverviewFromConfig
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPIOverviewInterface
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class BPIOverviewOverviewImpl(private val arguments: Bundle?) :
    BPIOverviewInterface {

    companion object {
        const val ACCOUNT_INFO = "accountInfo"
    }

    override fun coveredList(): MutableList<BalanceProtectionInsuranceOverviewFromConfig> {

       val balanceProtectionInsuranceFromMobileConfig = WoolworthsApplication.getInstance()?.balanceProtectionInsurance
        val overviewList  = balanceProtectionInsuranceFromMobileConfig?.overview

      val protectionList  =  mutableListOf(
            BalanceProtectionInsuranceOverviewFromConfig(
               null,
                R.drawable.icon_balance_protection_overview,
                InsuranceType(),
                R.drawable.bg_header_balance_protection
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
             null,
                R.drawable.icon_partner_cover,
                InsuranceType(),
                R.drawable.bg_header_partner_cover
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
               null,
                R.drawable.icon_additional_death_cover,
                InsuranceType(),
                R.drawable.bg_header_additional_death_cover
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
               null,
                R.drawable.icon_additional_death_cover_for_partner,
                InsuranceType(),
                R.drawable.bg_header_additional_death_cover_for_partner
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
              null,
                R.drawable.bpi_card_balance_protection_icon,
                InsuranceType(),
                R.drawable.bg_header_card_balance_protection
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
               null,
                R.drawable.icon_loan_balance_protection,
                InsuranceType(),
                R.drawable.bg_header_loan_balance_protection
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
              null,
                R.drawable.icon_partner_cover,
                InsuranceType(),
                R.drawable.bg_header_partner_cover
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
              null,
                R.drawable.icon_balance_protection_overview,
                InsuranceType(),
                R.drawable.bg_header_balance_protection
            ),
          BalanceProtectionInsuranceOverviewFromConfig(
               null,
                R.drawable.icon_partner_cover,
                InsuranceType(),
                R.drawable.bg_header_partner_cover
            )
        )

        overviewList?.forEachIndexed { index, overview ->
            protectionList[index].overview = overview
        }

        return protectionList
    }
    override fun effectiveDate(effectiveDate: String?): String {
        return try {
            effectiveDate?.let {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
                date?.let { dt -> SimpleDateFormat("dd/MM/yyyy", Locale.US).format(dt) } ?: ""
            } ?: ""
        } catch (ex: Exception) {
            ""
        }
    }

    override fun getInsuranceType(): MutableList<InsuranceType> = arguments?.let {
        if (it.containsKey(ACCOUNT_INFO)) {
            Gson().fromJson(it.getString(ACCOUNT_INFO), Account::class.java)?.insuranceTypes ?: mutableListOf()
        } else mutableListOf() } ?: mutableListOf()

    override fun coveredUncoveredList(): MutableList<BalanceProtectionInsuranceOverviewFromConfig> {
        val mobileConfigCoveredList: MutableList<BalanceProtectionInsuranceOverviewFromConfig> = coveredList()
        val coveredList = mutableListOf<BalanceProtectionInsuranceOverviewFromConfig>()
        val uncoveredList = mutableListOf<BalanceProtectionInsuranceOverviewFromConfig>()
        val insuranceListType: MutableList<InsuranceType> = getInsuranceType()

        insuranceListType.forEach { insuranceType ->
            mobileConfigCoveredList.forEach { overviewItem ->
                if (overviewItem.overview?.title == insuranceType.description) {
                    overviewItem.insuranceType?.apply {
                        covered = insuranceType.covered
                        description = insuranceType.description
                        effectiveDate = insuranceType.effectiveDate
                        if (covered) { coveredList.add(overviewItem) } else { uncoveredList.add(overviewItem) }
                    }
                }
            }
        }

        return coveredList.plus(uncoveredList).toMutableList()
    }

    override fun isCovered(): Boolean = coveredUncoveredList().any { it.insuranceType?.covered == true }
}