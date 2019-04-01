package za.co.woolworths.financial.services.android.contracts

import com.google.gson.JsonElement

interface IToastInterface {
    fun onToastButtonClicked(jsonElement: JsonElement?)
}