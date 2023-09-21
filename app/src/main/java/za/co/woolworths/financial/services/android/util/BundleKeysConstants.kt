package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants

class BundleKeysConstants {

    companion object {
        const val BUNDLE = "bundle"

        //AccountsOptionFragment Keys
        const val ENVELOPE_NUMBER = "envelopeNumber"
        const val ACCOUNTBI_NNUMBER = "accountBinNumber"
        const val STATUS_RESPONSE = "StatusResponse"
        const val PRODUCT_OFFERINGID = "productOfferingId"

        const val ACCOUNT_NUMBER = "accountNumber"
        const val CARDTYPE = "cardType"
        const val CREDITCARD_TOKEN = "creditCardToken"

        //geo location
        const val KEY_PLACE_ID = "placeId"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val ADDRESS = "address"
        const val VALIDATE_RESPONSE = "ValidateResponse"
        const val IS_COMING_FROM_CNC_SELETION = "cnc_slection"
        const val STANDARD_DELIVERY = "StandardDelivery"
        const val STANDARD = "Standard"
        const val CNC = "CnC"
        const val DASH = "OnDemand"
        const val FULLFILLMENT_REQUEST_CODE = 8765
        const val IS_COMING_CONFIRM_ADD = "conform_add"
        var IS_FROM_STORE_LOCATOR = false
        const val REQUEST_CODE = 1515
        const val DASH_SET_ADDRESS_REQUEST_CODE = 1516
        const val CNC_SET_ADDRESS_REQUEST_CODE = 1517
        const val DELIVERY_TYPE = "DELIVERY_TYPE"
        const val IS_COMING_FROM_CHECKOUT = "isComingFromCheckout"
        const val IS_COMING_FROM_SLOT_SELECTION = "isComingFromSlotSelection"
        const val IS_FROM_DASH_TAB = "isFromDashTab"
        const val SAVED_ADDRESS_RESPONSE = "savedAddressResponse"
        const val DEFAULT_ADDRESS = "defaultAddress"
        const val PLACE_ID = "placeId"
        const val IS_LIQUOR = "IS_LIQUOR"
        const val NICK_NAME = "nickname"
        const val KEY_ADDRESS2 = "ADDRESS2"
        const val IS_MIXED_BASKET = "is_mix_basket"
        const val IS_FBH_ONLY = "is_fbh_only"

        const val RECOMMENDATIONS_EVENT_DATA = "recommendations_event_data"
        const val RECOMMENDATIONS_EVENT_DATA_TYPE = "recommendations_event_data_type"
        const val RECOMMENDATIONS_DYNAMIC_TITLE_REQUIRED = "recommendations_dynamic_title_required"
    }

}