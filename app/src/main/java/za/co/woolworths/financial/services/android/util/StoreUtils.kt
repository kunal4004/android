package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.geolocation.network.model.Store

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
                    s1.storeName = PARGO + " " + s1?.storeName
                }
                if(s2?.locationId != "" && s2?.storeName?.contains(PARGO, true) == false) {
                    s2.storeName = PARGO + " " + s2?.storeName
                }

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
                when (store.storeDeliveryType?.lowercase()) {
                    StoreDeliveryType.OTHER.type.lowercase() -> {
                        if (index == 0) {
                            storeListRowsList.add(StoreListRow.Header(Constant.FASHION_BEAUTY_HOME_WARE))
                            storeListRowsList.add(StoreListRow.StoreRow(store))
                        } else {
                            storeListRowsList.add(StoreListRow.StoreRow(store))
                        }
                    }
                    StoreDeliveryType.FOOD.type.lowercase() -> {
                        if (index == 0) {
                            storeListRowsList.add(StoreListRow.Header(Constant.FOOD_ITEMS_ONLY))
                            storeListRowsList.add(StoreListRow.StoreRow(store))
                        } else {
                            val previousStore = addressList[index - 1]
                            if (!store.storeDeliveryType.equals(previousStore.storeDeliveryType)) {
                                storeListRowsList.add(StoreListRow.Header(Constant.FOOD_ITEMS_ONLY))
                                storeListRowsList.add(StoreListRow.StoreRow(store))
                            } else {
                                storeListRowsList.add(StoreListRow.StoreRow(store))
                            }
                        }
                    }
                    StoreDeliveryType.FOOD_AND_OTHER.type.lowercase() -> {
                        if (index == 0) {
                            storeListRowsList.add(StoreListRow.Header(Constant.ALL_WOOL_WORTHS_PRODUCTS_ONLY))
                            storeListRowsList.add(StoreListRow.StoreRow(store))
                        } else {
                            val previousStore = addressList.get(index - 1)
                            if (!store.storeDeliveryType.equals(previousStore.storeDeliveryType)) {
                                storeListRowsList.add(StoreListRow.Header(Constant.ALL_WOOL_WORTHS_PRODUCTS_ONLY))
                                storeListRowsList.add(StoreListRow.StoreRow(store))
                            } else {
                                storeListRowsList.add(StoreListRow.StoreRow(store))
                            }
                        }
                    }
                }
            }
            return storeListRowsList

        }


        fun sortedStoreListBasedOnDistance(address: List<Store>?): List<Store> {
            address?.stream()?.sorted { store1, store2 ->
                store2.distance?.let { store1.distance?.compareTo(it) }!!
            }
            return address!!
        }

    }
}