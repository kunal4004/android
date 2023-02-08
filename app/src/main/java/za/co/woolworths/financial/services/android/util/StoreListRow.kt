package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.geolocation.network.model.Store


sealed class StoreListRow {

    class Header(val headerName:String) : StoreListRow()
    class StoreRow(val store: Store) : StoreListRow()
}

