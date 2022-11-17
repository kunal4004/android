package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import java.util.ArrayList
import java.util.HashMap

class StoreUtils {
    companion object {
        const val PARGO : String = "Pargo"

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
    }
}