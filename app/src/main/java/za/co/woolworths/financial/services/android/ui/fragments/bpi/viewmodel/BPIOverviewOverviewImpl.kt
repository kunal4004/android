package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import android.os.Bundle
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.models.dto.bpi.Overview
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPIOverviewInterface
import java.text.SimpleDateFormat
import java.util.Locale

class BPIOverviewOverviewImpl(private val arguments: Bundle?) :
    BPIOverviewInterface {

    companion object {
        const val ACCOUNT_INFO = "accountInfo"
    }

    override fun coveredList(): MutableList<BalanceProtectionInsuranceOverview> {

     return  mutableListOf(
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_balance_protection_title,
                    header = R.string.balance_protection_insurance,
                    description = R.string.bpi_balance_protection_desc,
                    benefits = listOf(
                        R.string.bpi_balance_protection_benefit_1,
                        R.string.bpi_balance_protection_benefit_2,
                        R.string.bpi_balance_protection_benefit_3
                    )
                ),
                R.drawable.icon_balance_protection_overview,
                InsuranceType(),
                R.drawable.bg_header_balance_protection
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_partner_cover_title,
                    description = R.string.bpi_partner_cover_desc,
                    benefits = listOf(
                        R.string.bpi_partner_cover_benefit_1,
                        R.string.bpi_partner_cover_benefit_2,
                        R.string.bpi_partner_cover_benefit_3
                )
                ),
                R.drawable.icon_partner_cover,
                InsuranceType(),
                R.drawable.bg_header_partner_cover
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_additional_death_cover_title,
                    description = R.string.bpi_additional_death_cover_desc,
                    benefits = listOf(
                        R.string.bpi_additional_death_cover_1,
                        R.string.bpi_additional_death_cover_2
                    )
                ),
                R.drawable.icon_additional_death_cover,
                InsuranceType(),
                R.drawable.bg_header_additional_death_cover
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_additional_death_cover_for_partner_title,
                    description = R.string.bpi_additional_death_cover_for_partner_desc,
                    benefits = listOf(
                        R.string.bpi_additional_death_cover_for_partner_1,
                        R.string.bpi_additional_death_cover_for_partner_2
                    )
                ),
                R.drawable.icon_additional_death_cover_for_partner,
                InsuranceType(),
                R.drawable.bg_header_additional_death_cover_for_partner
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_card_balance_protection_title,
                    description = R.string.bpi_card_balance_protection_desc,
                    benefits = listOf(
                        R.string.bpi_card_balance_protection_benefits_1,
                        R.string.bpi_card_balance_protection_benefits_2,
                        R.string.bpi_card_balance_protection_benefits_3,
                        R.string.bpi_card_balance_protection_benefits_4)
                ),
                R.drawable.bpi_card_balance_protection_icon,
                InsuranceType(),
                R.drawable.bg_header_card_balance_protection
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_loan_balance_protection_title,
                    description = R.string.bpi_loan_balance_protection_desc,
                    benefits = listOf(
                        R.string.bpi_loan_balance_protection_benefits_1,
                        R.string.bpi_loan_balance_protection_benefits_2)
                ),
                R.drawable.icon_loan_balance_protection,
                InsuranceType(),
                R.drawable.bg_header_loan_balance_protection
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_companion_care_title,
                    description = R.string.bpi_companion_care_desc,
                    benefits = listOf(
                        R.string.bpi_companion_care_benefits_1)
                ),
                R.drawable.icon_partner_cover,
                InsuranceType(),
                R.drawable.bg_header_partner_cover
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_balance_protection_65_above_title,
                    description = R.string.bpi_balance_protection_sixty_65_above_desc,
                    benefits = listOf(
                        R.string.bpi_balance_protection_65_years_benefit_1)
                ),
                R.drawable.icon_balance_protection_overview,
                InsuranceType(),
                R.drawable.bg_header_balance_protection
            ),
            BalanceProtectionInsuranceOverview(
                Overview(
                    title = R.string.bpi_partner_cover_65_above_title,
                    description = R.string.bpi_partner_cover_65_above_desc,
                    benefits = listOf(
                        R.string.bpi_partner_cover_65_years_benefit_1)
                ),
                R.drawable.icon_partner_cover,
                InsuranceType(),
                R.drawable.bg_header_partner_cover
            )
        )
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

    override fun getAccount(): Account? {
        return arguments?.let { Gson().fromJson(it.getString(ACCOUNT_INFO), Account::class.java) }
    }

    override fun navigateToOverviewDetail(): Pair<BalanceProtectionInsuranceOverview?, Boolean> {
        val insuranceType = getInsuranceType()
        val hasOneInsuranceTypeItem = insuranceType.isNotEmpty() && insuranceType.size == 1 && !insuranceType[0].covered
        val insuranceTypeItem = coveredUncoveredList()?.getOrNull(0)
        return Pair(insuranceTypeItem, hasOneInsuranceTypeItem)
    }

    override fun getInsuranceType(): MutableList<InsuranceType> = arguments?.let {
        if (it.containsKey(ACCOUNT_INFO)) {
            getAccount()?.insuranceTypes ?: mutableListOf()
        } else mutableListOf()
    } ?: mutableListOf()

    override fun coveredUncoveredList(): MutableList<BalanceProtectionInsuranceOverview> {
        val mobileConfigCoveredList: MutableList<BalanceProtectionInsuranceOverview> = coveredList()
        val coveredList = mutableListOf<BalanceProtectionInsuranceOverview>()
        val uncoveredList = mutableListOf<BalanceProtectionInsuranceOverview>()
        val insuranceListType: MutableList<InsuranceType> = getInsuranceType()

        insuranceListType.forEach { insuranceType ->
            mobileConfigCoveredList.forEach { overviewItem ->
                if (overviewItem.overview?.title?.let { bindString(it) } == insuranceType.description) {
                    overviewItem.insuranceType?.apply {
                        covered = insuranceType.covered
                        description = insuranceType.description
                        effectiveDate = insuranceType.effectiveDate
                        if (covered) {
                            coveredList.add(overviewItem)
                        } else {
                            uncoveredList.add(overviewItem)
                        }
                    }
                }
            }
        }

        return coveredList.plus(uncoveredList).toMutableList()
    }

    override fun isCovered(): Boolean =
        coveredUncoveredList().any { it.insuranceType?.covered == true }
}