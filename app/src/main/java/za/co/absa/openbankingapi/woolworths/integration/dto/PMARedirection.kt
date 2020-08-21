package za.co.absa.openbankingapi.woolworths.integration.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PMARedirection (
		@SerializedName("created") val created : String,
		@SerializedName("merchantSiteUrl") val merchantSiteUrl : String,
		@SerializedName("url") val url : String
) : Serializable