package za.co.woolworths.financial.services.android.presentation.addtolist.request

data class CopyItemToListRequest(
        var items: List<CopyItemDetail>,
        var giftListIds: List<String>?
)
