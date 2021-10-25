package za.co.woolworths.financial.services.android.checkout.viewmodel

/**
 * Created by Kunal Uttarwar on 15/06/21.
 */
enum class AddressComponentEnum(val value: String) {
    STREET_NUMBER("street_number"),
    ROUTE("route"),
    ADMINISTRATIVE_AREA_LEVEL_1("administrative_area_level_1"),
    POSTAL_CODE("postal_code"),
    SUBLOCALITY_LEVEL_1("sublocality_level_1"),
    SUBLOCALITY_LEVEL_2("sublocality_level_2"),
    LOCALITY("locality")
}