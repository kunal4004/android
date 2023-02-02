package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import java.util.ArrayList
import java.util.HashMap

class StoreUtils {
    companion object {
        enum class StoreDeliveryType(val type: String) {
            OTHER("other"),
            FOOD("food"),
            FOOD_AND_OTHER("foodAndOther")
        }

        enum class FulfillmentType(val type: String) {
            FOOD_ITEMS("01"),
            CLOTHING_ITEMS("02"),
            CRG_ITEMS("07")
        }
        fun sortedStoreList(address: List<Store>?): List<Store> {
            val storeArrayList = ArrayList(address)
            val sortRoles: HashMap<String, Int> = hashMapOf(
                    StoreDeliveryType.OTHER.type.lowercase() to 0,
                    StoreDeliveryType.FOOD.type.lowercase() to 1,
                    StoreDeliveryType.FOOD_AND_OTHER.type.lowercase() to 2
            )
            val comparator = Comparator { s1: Store, s2: Store ->
                return@Comparator sortRoles[s2.storeDeliveryType?.lowercase()]?.let { sortRoles[s1.storeDeliveryType?.lowercase()]?.minus(it) }
                        ?: -1
            }
            val sortedStoreList = arrayListOf<Store>().apply { addAll(storeArrayList) }
            sortedStoreList.sortWith(comparator)
            return sortedStoreList
        }

        fun getStoresListWithHeaders(addressList: List<Store>?): List<StoreListRow> {
            val storeListRowsList = arrayListOf<StoreListRow>()
            addressList?.forEachIndexed { index, store ->
                var type: String? = null
                when (store.storeDeliveryType?.lowercase()) {
                    StoreDeliveryType.OTHER.type.lowercase() -> {
                        type = Constant.FASHION_BEAUTY_HOME_WARE
                    }
                    StoreDeliveryType.FOOD.type.lowercase() -> {
                        type = Constant.FOOD_ITEMS_ONLY
                    }
                    StoreDeliveryType.FOOD_AND_OTHER.type.lowercase() -> {
                        type = Constant.ALL_WOOL_WORTHS_PRODUCTS_ONLY
                    }
                }
                if (index == 0) {
                    type?.let { StoreListRow.Header(it) }?.let { storeListRowsList.add(it) }
                    storeListRowsList.add(StoreListRow.StoreRow(store))
                } else if (!store?.storeDeliveryType?.equals(addressList?.get(index - 1)?.storeDeliveryType)!!) {
                    type?.let { StoreListRow.Header(it) }?.let { storeListRowsList.add(it) }
                    storeListRowsList.add(StoreListRow.StoreRow(store))
                } else {
                    storeListRowsList.add(StoreListRow.StoreRow(store))
                }
            }
            return storeListRowsList

        }

        fun sortedStoreListBasedOnDistance(address: List<Store>?): List<Store>? {
            address?.stream()?.sorted { store1, store2 ->
                store2?.distance?.let { store1?.distance?.compareTo(it) }!!
            }
            return address
        }

        fun formatDeliveryTime(time : String) : String {
            return time.split(",").toTypedArray()[1]
        }
    }
}