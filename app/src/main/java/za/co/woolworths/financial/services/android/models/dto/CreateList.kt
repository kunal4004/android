package za.co.woolworths.financial.services.android.models.dto

data class CreateList(val name: String, val items: MutableList<AddToListRequest> = mutableListOf())