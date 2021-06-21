package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.bpi.Overview

@Parcelize
data class BalanceProtectionInsuranceOverview(
        var overview: Overview? = null,
        var overviewDrawable: Int,
        var insuranceType: InsuranceType? = null,
        var benefitHeaderDrawable: Int?) : Parcelable