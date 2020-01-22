package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.JsonElement

data class NutritionalInformationDetails(val headings: ArrayList<String>, val nutritionalTable: ArrayList<JsonElement>)