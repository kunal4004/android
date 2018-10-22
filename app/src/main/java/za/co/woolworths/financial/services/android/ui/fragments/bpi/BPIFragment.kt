package za.co.woolworths.financial.services.android.ui.fragments.bpi

import android.content.Intent
import android.support.v4.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity
import java.util.*

open class BPIFragment : Fragment() {

    fun navigateToBalanceProtectionActivity() {
        if (activity == null) return
        val deathCoverIntent = Intent(activity, BalanceProtectionActivity::class.java)
        startActivity(deathCoverIntent)
        activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }


    fun createBPIList(): MutableList<BPIOverview>? {
        return Arrays.asList(BPIOverview(resources.getString(R.string.bpi_balance_protection_title), resources.getString(R.string.bpi_balance_protection_desc), R.drawable.icon_balance_protection_overview, resources.getStringArray(R.array.bpi_balance_protection_benefits), InsuranceType(), R.drawable.bg_header_balance_protection), BPIOverview(resources.getString(R.string.bpi_partner_cover_title), resources.getString(R.string.bpi_partner_cover_desc), R.drawable.icon_partner_cover, resources.getStringArray(R.array.bpi_partner_cover_benefits), InsuranceType(), R.drawable.bg_header_partner_cover), BPIOverview(resources.getString(R.string.bpi_additional_death_cover_title), resources.getString(R.string.bpi_additional_death_cover_desc), R.drawable.icon_additional_death_cover, resources.getStringArray(R.array.bpi_additional_death_cover), InsuranceType(), R.drawable.bg_header_additional_death_cover), BPIOverview(resources.getString(R.string.bpi_additional_death_cover_for_partner_title), resources.getString(R.string.bpi_additional_death_cover_for_partner_desc), R.drawable.icon_additional_death_cover_for_partner, resources.getStringArray(R.array.bpi_additional_death_cover_for_partner), InsuranceType(), R.drawable.bg_header_additional_death_cover_for_partner))
    }

    fun updateBPIList(): MutableList<BPIOverview>? {
        val bpiList = createBPIList()
        val insuranceListType = getInsuranceType()!!
        for (insuranceType in insuranceListType) {
            for (bpi in bpiList!!) {
                if (bpi.overviewTitle == insuranceType.description) {
                    val type: InsuranceType = bpi.insuranceType!!
                    type.covered = insuranceType.covered
                    type.description = insuranceType.description
                    type.effectiveDate = insuranceType.effectiveDate
                }
            }
        }
        return bpiList
    }

    fun getInsuranceType(): MutableList<InsuranceType>? {
        if (arguments != null) {
            if (arguments.containsKey("accountInfo")) {
                val account: Account? = Gson().fromJson(arguments.get("accountInfo") as String, Account::class.java)
                return account!!.insuranceTypes
            }
        }
        return null
    }
}