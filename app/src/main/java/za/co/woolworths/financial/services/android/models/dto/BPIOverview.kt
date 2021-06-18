package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.bpi.Overview

data class BPIOverview(
        var overviewTitle: String? = "", var overviewDescription: String? = "",
        var overviewDrawable: Int?, var benfitDescription: Array<String>?,
        var insuranceType: InsuranceType? = null, var benefitHeaderDrawable: Int?)

@Parcelize
data class BalanceProtectionInsuranceOverviewFromConfig(
        var overview: Overview? = null,
        var overviewDrawable: Int,
        var insuranceType: InsuranceType? = null,
        var benefitHeaderDrawable: Int?) : Parcelable