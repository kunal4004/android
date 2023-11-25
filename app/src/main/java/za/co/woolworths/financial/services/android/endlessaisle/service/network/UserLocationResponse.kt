package za.co.woolworths.financial.services.android.endlessaisle.service.network

import za.co.woolworths.financial.services.android.models.dto.Response

/*
 * Created by Sandeep Satpute on 31,October,2023
 */
data class UserLocationResponse(
        val data: List<UserLocationData>,
        val response: Response,
        val httpCode: Int,
)

data class UserLocationData(
        val id: Long,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val address: String,
        val npcAvailable: Boolean,
        val dashEnabled: Boolean,
        val vtcEnabled: Boolean,
        val payInStore: Boolean
)
