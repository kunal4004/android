package za.co.woolworths.financial.services.android.util

import androidx.core.text.HtmlCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
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
                if(s1?.locationId != "" && s1?.storeName?.contains(PARGO, true) == false) {
                    s1.storeName = s1?.storeName + " " + HtmlCompat.fromHtml(WoolworthsApplication.getInstance().getString(R.string.pargo),
                            HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
                if(s2?.locationId != "" && s2?.storeName?.contains(PARGO, true) == false) {
                    s2.storeName = s2?.storeName + " " + HtmlCompat.fromHtml(WoolworthsApplication.getInstance().getString(R.string.pargo),
                            HtmlCompat.FROM_HTML_MODE_LEGACY)
                }

                return@Comparator sortRoles[s2.storeDeliveryType?.lowercase()]?.let { sortRoles[s1.storeDeliveryType?.lowercase()]?.minus(it) }
                        ?: -1
            }
            val sortedStoreList = arrayListOf<Store>().apply { addAll(storeArrayList) }
            sortedStoreList.sortWith(comparator)
            return sortedStoreList
        }
    }
}