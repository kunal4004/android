package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.geolocation.network.model.Store

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

        /*
        Filtering  the List based on  Delivery Type
        Under Fbh stores section Woolies stores adding first and after adding Pargo Stores.
         */

        fun sortedStoreList(address: List<Store>?): List<Store>? {
            val storeArrayList = ArrayList<Store>()
            val wwFbhStoreFilterList: List<Store>? =
                address?.filter { s -> s.storeDeliveryType.equals(StoreDeliveryType.OTHER.type) && s.storeName?.lowercase()?.contains(Constant.PARGO)==false}
                    ?.sortedBy { it.distance }
            val pargoStoreFilterList: List<Store>? =
                address?.filter { s -> s.storeDeliveryType.equals(StoreDeliveryType.OTHER.type) && s.storeName?.lowercase()?.contains(Constant.PARGO)==true}
                    ?.sortedBy { it.distance }
            val foodStoreFilterList: List<Store>? =
                address?.filter { s -> s.storeDeliveryType.equals(StoreDeliveryType.FOOD.type) }
                    ?.sortedBy { it.distance }
            val mixedBasketFilterList: List<Store>? =
                address?.filter { s -> s.storeDeliveryType.equals(StoreDeliveryType.FOOD_AND_OTHER.type) }
                    ?.sortedBy { it.distance }
            if (!wwFbhStoreFilterList.isNullOrEmpty()) {
                storeArrayList.addAll(wwFbhStoreFilterList)
            }

            if (!pargoStoreFilterList.isNullOrEmpty()) {
                storeArrayList.addAll(pargoStoreFilterList)
            }
            if (!foodStoreFilterList.isNullOrEmpty()) {
                storeArrayList.addAll(foodStoreFilterList)
            }
            if (!mixedBasketFilterList.isNullOrEmpty()) {
                storeArrayList.addAll(mixedBasketFilterList)
            }
            return storeArrayList
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

    }
}