package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.*
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.common.reflect.TypeToken
import com.google.firebase.installations.FirebaseInstallations
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import org.json.JSONObject
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment.Companion.KEY_COLLECTING_DETAILS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.accountOptions
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.liquor
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dao.SessionDao.KEY
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigTradingHours
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListViewModel
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.webview.activities.WInternalWebPageActivity
import za.co.woolworths.financial.services.android.ui.extension.*
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.verifyAppInstanceId
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.IS_PET_INSURANCE
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.PetInsurancePendingFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler
import za.co.woolworths.financial.services.android.ui.fragments.onboarding.OnBoardingFragment.Companion.ON_BOARDING_SCREEN_TYPE
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GeneralInfoDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.CLIErrorMessageButtonDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ErrorMessageDialog
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.fragment.UserAccountsLandingFragment.Companion.PET_INSURANCE_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_WISHLIST_EVENT_DATA
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.KEY_HAS_GIFT_PRODUCT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SWITCH_SCREEN_CNC
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FBH_ONLY
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_DASH_TAB
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_MIXED_BASKET
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.LOCATION_UPDATE_REQUEST
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.NEED_STORE_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.NEW_DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logEvent
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import java.io.*
import java.net.SocketException
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt


class KotlinUtils {
    companion object {

        var placeId: String? = null
        var isLocationPlaceIdSame: Boolean? = false
        var isNickNameChanged: Boolean? = false
        var isComingFromCncTab: Boolean? = false
        var browsingDeliveryType: Delivery? = getPreferredDeliveryType()

        @JvmStatic
        var browsingCncStore: Store? = null
        const val collectionsIdUrl = "woolworths.wfs.co.za/CustomerCollections/IdVerification"
        const val COLLECTIONS_EXIT_URL = "collectionsExitUrl"
        const val TREATMENT_PLAN = "treamentPlan"
        const val RESULT_CODE_CLOSE_VIEW = 2203

        const val REVIEW_DATA = "reviewData"
        const val PROD_ID = "prod_id"
        const val REVIEW_REPORT: String = "reviewReport"
        const val REWIEW = "review"
        const val HELPFULNESS = "helpfulness"
        const val POSITIVE = "Positive"

        fun highlightTextInDesc(
            context: Context?,
            spannableTitle: SpannableString,
            searchTerm: String,
            textIsClickable: Boolean = true,
        ): SpannableString {
            var start = spannableTitle.indexOf(searchTerm)
            if (start == -1) {
                start = 0
            }

            val end = start + searchTerm.length
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Utils.makeCall(searchTerm)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }

            val typeface: Typeface? =
                context?.let { ResourcesCompat.getFont(it, R.font.opensans_semi_bold) }
            if (textIsClickable) spannableTitle.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val dimenPix =
                context?.resources?.getDimension(R.dimen.store_card_spannable_text_17_sp_bold)
            typeface?.style?.let { style ->
                spannableTitle.setSpan(
                    StyleSpan(style),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            spannableTitle.setSpan(
                AbsoluteSizeSpan(
                    dimenPix?.toInt()
                        ?: 0
                ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTitle.setSpan(
                AbsoluteSizeSpan(
                    dimenPix?.toInt()
                        ?: 0
                ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableTitle
        }

        fun setTransparentStatusBar(appCompatActivity: AppCompatActivity?) {

            if (Build.VERSION.SDK_INT >= 19) {
                appCompatActivity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 21) {
                appCompatActivity?.setWindowFlag(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    false
                )
                appCompatActivity?.window?.statusBarColor = Color.TRANSPARENT
            }
        }

        fun AppCompatActivity.setWindowFlag(bits: Int, on: Boolean) {
            val winParams = window?.attributes
            winParams?.apply {
                flags = if (on) {
                    flags or bits
                } else {
                    flags and bits.inv()
                }
                window?.attributes = winParams
            }
        }

        fun getStatusBarHeight(actionBarHeight: Int): Int {
            val resources = WoolworthsApplication.getAppContext().resources
            val resId: Int = resources?.getIdentifier("status_bar_height", "dimen", "android")
                ?: -1
            var statusBarHeight = 0
            if (resId > 0) {
                statusBarHeight = resources?.getDimensionPixelSize(resId) ?: 0
            }
            return statusBarHeight + actionBarHeight
        }

        fun getStatusBarHeight(): Int {
            var result = 0
            val resources = WoolworthsApplication.getAppContext().resources
            val resourceId = resources?.getIdentifier("status_bar_height", "dimen", "android") ?: 0
            if (resourceId > 0) {
                result = resources?.getDimensionPixelSize(resourceId) ?: 0
            }
            return result
        }

        fun onBackPressed(activity: Activity?) {
            activity?.apply {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
        }

        fun interpolateColor(fraction: Float, startValue: Int, endValue: Int): Int {
            val startA = startValue shr 24 and 0xff
            val startR = startValue shr 16 and 0xff
            val startG = startValue shr 8 and 0xff
            val startB = startValue and 0xff
            val endA = endValue shr 24 and 0xff
            val endR = endValue shr 16 and 0xff
            val endG = endValue shr 8 and 0xff
            val endB = endValue and 0xff
            return startA + (fraction * (endA - startA)).toInt() shl 24 or
                    (startR + (fraction * (endR - startR)).toInt() shl 16) or
                    (startG + (fraction * (endG - startG)).toInt() shl 8) or
                    startB + (fraction * (endB - startB)).toInt()
        }

        fun roundCornerDrawable(view: View?, color: String?) {
            if (view == null || TextUtils.isEmpty(color)) return
            val paddingDp: Float = (12 * view.context.resources.displayMetrics.density)
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadii =
                floatArrayOf(
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp
                )
            shape.setColor(Color.parseColor(color))
            view.background = shape
        }

        fun getCardHolderNameSurname(): String? {
            val jwtDecoded = SessionUtilities.getInstance()?.jwt
            val name = jwtDecoded?.name?.get(0)
            val familyName = jwtDecoded?.family_name?.get(0)
            return "$name $familyName"
        }

        fun dpToPxConverter(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun pxToDpConverter(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

        fun convertFromDateToDate(date: String?): String? {
            date?.apply {
                val fromDateFormat = SimpleDateFormat("yyyy-MM-dd")
                val toDateFormat = SimpleDateFormat("MMMM yyyy")
                val fromDate = fromDateFormat.parse(date)
                return toDateFormat.format(fromDate)
            }
            return ""
        }

        fun capitaliseFirstLetter(str: String?): CharSequence? {
            if (str.isNullOrEmpty())
                return str
            val value = str.lowercase()
            val words = value.split(" ").toMutableList()
            var output = ""
            for (word in words) {
                output += word.replaceFirstChar { it.titlecase() } + " "
            }
            return output.trim()
        }


        fun capitaliseFirstWordAndLetters(str: String): CharSequence? {
            val value = str.lowercase()
            val words = value.split(" ").toMutableList()

            var output = words[0].uppercase() + " "
            words.removeAt(0)
            for (word in words) {
                output += word.uppercase() + " "
            }
            return output.trim()
        }

        fun isNumberPositive(i: Float): Boolean {
            return when {
                i < 0 -> true
                i > 0 -> false
                else -> false
            }
        }

        fun getToolbarHeight(): Int {
            val tv = TypedValue()
            var actionBarHeight = 0
            val appCompat = WoolworthsApplication.getAppContext()
            if (appCompat?.theme?.resolveAttribute(android.R.attr.actionBarSize, tv, true)!!) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    appCompat.resources?.displayMetrics
                )
            }
            return actionBarHeight
        }

        fun addSpaceBeforeUppercase(word: String?): String {
            var newWord = ""
            word?.forEach { alphabet -> newWord += if (alphabet.isUpperCase()) " $alphabet" else alphabet }
            return newWord
        }

        fun setAccountNavigationGraph(
            navigationController: NavController,
            screenType: OnBoardingScreenType,
        ) {
            val bundle = Bundle()
            bundle.putSerializable(ON_BOARDING_SCREEN_TYPE, screenType)
            navigationController.setGraph(navigationController.graph, bundle)
        }

        /***
         * Convert response to a list of transactions with TransactionHeader()
         * and TransactionItem() that inherits Transaction
         */
        @SuppressLint("SimpleDateFormat")
        fun getListOfTransaction(transactionItemList: MutableList<TransactionItem>?): MutableList<Transaction> {

            val inputFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormat = SimpleDateFormat("MMMM yyyy")

            //Setting Date to the format dd/MM/yyyy
            val timeFormat = SimpleDateFormat("dd / MM / yyyy")

            transactionItemList?.forEach { transactionItem ->
                val transactionDate = transactionItem.date
                val inputDate = transactionDate?.let { date -> inputFormat.parse(date) }
                val outputMonthYear = inputDate?.let { date -> outputFormat.format(date) }
                transactionItem.month = outputMonthYear

                val formattedDate = timeFormat.format(inputDate)
                transactionItem.date = formattedDate
            }

            val groupTransactionsByMonth = transactionItemList?.groupBy { it.month }

            val transactionList: MutableList<Transaction> = mutableListOf()

            groupTransactionsByMonth?.forEach { transactionMap ->
                transactionList.add(TransactionHeader(transactionMap.key))
                transactionMap.value.forEach { transactionItem ->
                    transactionList.add(
                        transactionItem
                    )
                }
            }

            return transactionList
        }

        fun getDateDaysAfter(daysAfter: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, daysAfter)
            return calendar.time
        }

        fun removeRandFromAmount(amount: String?): String {
            if (amount?.contains("R") == true) {
                return amount.substring(1)
            }
            return amount ?: "0.0"
        }

        fun toShipByDateFormat(date: Date?): String {
            return SimpleDateFormat("dd-MM-yyy").format(date)
        }

        fun presentEditDeliveryGeoLocationActivity(
            activity: Activity?,
            requestCode: Int,
            delivery: Delivery? = Delivery.STANDARD,
            placeId: String? = null,
            isFromDashTab: Boolean = false,
            isComingFromCheckout: Boolean = false,
            isMixedBasket: Boolean = false,
            isFBHOnly: Boolean = false,
            isComingFromSlotSelection: Boolean = false,
            isLocationUpdateRequest: Boolean = false,
            savedAddressResponse: SavedAddressResponse? = null,
            defaultAddress: Address? = null,
            whoISCollecting: String? = null,
            liquorCompliance: LiquorCompliance? = null,
            cartItemList: ArrayList<CommerceItem>? = null,
            isFromNewToggleFulfilmentScreen: Boolean = false,
            isFromNewToggleFulfilmentScreenSwitchCnc: Boolean = false,
            needStoreSelection: Boolean = false,
            newDelivery: Delivery? = null,
            validateLocationResponse: ValidateLocationResponse? = null
        ) {

            activity?.apply {
                val mIntent = Intent(this, EditDeliveryLocationActivity::class.java)
                val mBundle = Bundle()
                // todo Change this logic to add everything in bundle as this is exceeding 1mb limit of bundle.
                if (liquorCompliance != null && liquorCompliance.isLiquorOrder && liquor != null && liquor!!.noLiquorImgUrl != null && !liquor!!.noLiquorImgUrl.isEmpty()) {
                    mBundle.putBoolean(Constant.LIQUOR_ORDER, liquorCompliance.isLiquorOrder)
                    mBundle.putString(Constant.NO_LIQUOR_IMAGE_URL, liquor!!.noLiquorImgUrl)
                }
                mBundle.putSerializable(BundleKeysConstants.VALIDATE_RESPONSE, validateLocationResponse)
                mBundle.putString(DELIVERY_TYPE, delivery.toString())
                mBundle.putString(NEW_DELIVERY_TYPE, newDelivery?.type)
                mBundle.putString(PLACE_ID, placeId)
                mBundle.putBoolean(IS_FROM_DASH_TAB, isFromDashTab)
                mBundle.putBoolean(IS_COMING_FROM_CHECKOUT, isComingFromCheckout)
                mBundle.putBoolean(IS_COMING_FROM_CNC_SELETION, isComingFromCheckout)
                mBundle.putBoolean(IS_MIXED_BASKET, isMixedBasket)
                mBundle.putBoolean(IS_FBH_ONLY, isFBHOnly)
                mBundle.putBoolean(IS_COMING_FROM_SLOT_SELECTION, isComingFromSlotSelection)
                mBundle.putBoolean(IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN, isFromNewToggleFulfilmentScreen)
                mBundle.putBoolean(IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SWITCH_SCREEN_CNC, isFromNewToggleFulfilmentScreenSwitchCnc)
                mBundle.putBoolean(LOCATION_UPDATE_REQUEST, isLocationUpdateRequest)
                mBundle.putBoolean(NEED_STORE_SELECTION, needStoreSelection)
                mBundle.putSerializable(SAVED_ADDRESS_RESPONSE, savedAddressResponse)
                mBundle.putSerializable(DEFAULT_ADDRESS, defaultAddress)
                mBundle.putString(KEY_COLLECTING_DETAILS, whoISCollecting)
                mBundle.putSerializable(
                    CheckoutAddressManagementBaseFragment.CART_ITEM_LIST,
                    cartItemList
                )
                mIntent.putExtra(BUNDLE, mBundle)
                startActivityForResult(mIntent, requestCode)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }

        fun presentEditDeliveryGeoLocationActivity2(
            activity: Activity?,
            requestCode: Int,
            delivery: Delivery? = Delivery.STANDARD,
            placeId: String? = null,
            isFromDashTab: Boolean = false,
            isComingFromCheckout: Boolean = false,
            isMixedBasket: Boolean = false,
            isFBHOnly: Boolean = false,
            isComingFromSlotSelection: Boolean = false,
            isLocationUpdateRequest: Boolean = false,
            savedAddressResponse: SavedAddressResponse? = null,
            defaultAddress: Address? = null,
            whoISCollecting: String? = null,
            isLiquorOrder: Boolean? = null,
            liquorImageUrl: String? = null,
            cartItemList: ArrayList<CommerceItem>? = null,
            isFromNewToggleFulfilmentScreen: Boolean = false,
            needStoreSelection: Boolean = false,
            newDelivery: Delivery? = null,
            validateLocationResponse: ValidateLocationResponse? = null
        ) {
            activity?.apply {
                val mIntent = Intent(this, EditDeliveryLocationActivity::class.java)
                val mBundle = Bundle()
                // todo Change this logic to add everything in bundle as this is exceeding 1mb limit of bundle.
                if (isLiquorOrder == true && liquor != null && liquor!!.noLiquorImgUrl != null && !liquor!!.noLiquorImgUrl.isEmpty()) {
                    mBundle.putBoolean(Constant.LIQUOR_ORDER, isLiquorOrder)
                    mBundle.putString(Constant.NO_LIQUOR_IMAGE_URL, liquor!!.noLiquorImgUrl)
                }
                mBundle.putSerializable(BundleKeysConstants.VALIDATE_RESPONSE, validateLocationResponse)
                mBundle.putString(DELIVERY_TYPE, delivery.toString())
                mBundle.putString(NEW_DELIVERY_TYPE, newDelivery?.type)
                mBundle.putString(PLACE_ID, placeId)
                mBundle.putBoolean(IS_FROM_DASH_TAB, isFromDashTab)
                mBundle.putBoolean(IS_COMING_FROM_CHECKOUT, isComingFromCheckout)
                mBundle.putBoolean(IS_COMING_FROM_CNC_SELETION, isComingFromCheckout)
                mBundle.putBoolean(IS_MIXED_BASKET, isMixedBasket)
                mBundle.putBoolean(IS_FBH_ONLY, isFBHOnly)
                mBundle.putBoolean(IS_COMING_FROM_SLOT_SELECTION, isComingFromSlotSelection)
                mBundle.putBoolean(IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN, isFromNewToggleFulfilmentScreen)
                mBundle.putBoolean(LOCATION_UPDATE_REQUEST, isLocationUpdateRequest)
                mBundle.putBoolean(NEED_STORE_SELECTION, needStoreSelection)
                mBundle.putSerializable(SAVED_ADDRESS_RESPONSE, savedAddressResponse)
                mBundle.putSerializable(DEFAULT_ADDRESS, defaultAddress)
                mBundle.putString(KEY_COLLECTING_DETAILS, whoISCollecting)
                mBundle.putSerializable(
                    CheckoutAddressManagementBaseFragment.CART_ITEM_LIST,
                    cartItemList
                )
                mIntent.putExtra(BUNDLE, mBundle)
                startActivityForResult(mIntent, requestCode)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }

        fun setDeliveryAddressView(
            context: Activity?,
            fulfillmentDetails: FulfillmentDetails,
            tvDeliveringTo: TextView,
            tvDeliveryLocation: TextView,
            deliverLocationIcon: ImageView?,
            isComingFromMyPreferences: Boolean = false
        ) {
            with(fulfillmentDetails) {
                when (Delivery?.getType(deliveryType)) {
                    Delivery.CNC -> {
                        tvDeliveringTo?.text =
                            context?.resources?.getString(R.string.collecting_from)
                        tvDeliveryLocation?.text =
                            capitaliseFirstLetter(storeName)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_collection_circle)
                    }

                    Delivery.STANDARD -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        if (isComingFromMyPreferences) {
                            tvDeliveryLocation?.text =
                                capitaliseFirstLetter(address?.address1 ?: "")
                        } else {
                            val fullAddress = capitaliseFirstLetter(address?.address1 ?: "")

                            val formmmatedNickName = getFormattedNickName(
                                address?.nickname,
                                fullAddress, context
                            )

                            formmmatedNickName.append(fullAddress)

                            tvDeliveryLocation?.text = formmmatedNickName
                        }

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_delivery_circle)
                    }

                    Delivery.DASH -> {
                        val timeSlot: String? =
                            WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime
                        if (timeSlot?.isNullOrEmpty() == true || WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryTimeSlots?.isNullOrEmpty() == true) {
                            tvDeliveringTo?.text =
                                context?.resources?.getString(R.string.dash_delivery_bold) + "\t" + context?.resources?.getString(
                                    R.string.no_timeslots_available_title
                                )
                        } else {
                            tvDeliveringTo?.text =
                                context?.resources?.getString(R.string.dash_delivery_bold)
                                    .plus("\t" + timeSlot)
                        }

                        if (isComingFromMyPreferences) {
                            tvDeliveryLocation?.text = capitaliseFirstLetter(
                                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                    ?: address?.address1 ?: ""
                            )
                        } else {
                            val fullAddress = capitaliseFirstLetter(
                                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                    ?: address?.address1 ?: ""
                            )

                            val formmmatedNickName = getFormattedNickName(
                                address?.nickname,
                                fullAddress, context
                            )

                            formmmatedNickName.append(fullAddress)

                            tvDeliveryLocation?.text = formmmatedNickName
                        }

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_dash_delivery_circle)
                    }

                    else -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        tvDeliveryLocation?.text =
                            context?.resources?.getString(R.string.default_location)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_delivery_circle)
                    }
                }
            }
        }

        fun setDeliveryAndLocation(
            context: Activity?,
            fulfillmentDetails: FulfillmentDetails,
            tvDeliveringTo: TextView,
            tvDeliveryLocation: TextView,
        ) {
            with(fulfillmentDetails) {
                when (Delivery?.getType(deliveryType)) {
                    Delivery.CNC -> {
                        tvDeliveringTo?.text =
                            context?.resources?.getString(R.string.click_and_collect)
                        tvDeliveryLocation?.text =
                            capitaliseFirstLetter(storeName)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                    }

                    Delivery.STANDARD -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        val fullAddress = capitaliseFirstLetter(address?.address1 ?: "")
                        val formmmatedNickName = getFormattedNickName(
                            address?.nickname,
                            fullAddress, context
                        )
                        formmmatedNickName.append(fullAddress)
                        tvDeliveryLocation?.text = formmmatedNickName
                        tvDeliveryLocation?.visibility = View.VISIBLE
                    }

                    Delivery.DASH -> {
                        tvDeliveringTo.text = context?.resources?.getString(R.string.dash_delivery)
                        val fullAddress = capitaliseFirstLetter(
                            WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                ?: address?.address1 ?: ""
                        )
                        val formmmatedNickName = getFormattedNickName(
                            address?.nickname,
                            fullAddress, context
                        )
                        formmmatedNickName.append(fullAddress)
                        tvDeliveryLocation?.text = formmmatedNickName
                        tvDeliveryLocation?.visibility = View.VISIBLE
                    }

                    else -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        tvDeliveryLocation?.text =
                            context?.resources?.getString(R.string.default_location)
                        tvDeliveryLocation?.visibility = View.VISIBLE
                    }
                }
            }
        }

        fun setDeliveryAddressViewFoShop(
            context: Activity?,
            fulfillmentDetails: FulfillmentDetails,
            tvDeliveringTo: TextView,
            tvDeliveryLocation: TextView,
            deliverLocationIcon: ImageView?,
        ) {
            with(fulfillmentDetails) {
                when (Delivery?.getType(deliveryType)) {
                    Delivery.CNC -> {
                        tvDeliveringTo?.text = context?.resources?.getString(R.string.click_collect)
                        tvDeliveryLocation?.text =
                            capitaliseFirstLetter(storeName)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_collection_circle)
                    }

                    Delivery.STANDARD -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        val fullAddress = capitaliseFirstLetter(address?.address1 ?: "")

                        val formmmatedNickName = getFormattedNickName(
                            address?.nickname,
                            fullAddress, context
                        )

                        formmmatedNickName.append(fullAddress)

                        tvDeliveryLocation?.text = formmmatedNickName

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_delivery_circle)
                    }

                    Delivery.DASH -> {
                        val timeSlot: String? =
                            WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime

                        tvDeliveringTo?.text =
                            context?.resources?.getString(R.string.dash_delivery_bold)

                        val fullAddress = capitaliseFirstLetter(address?.address1 ?: "")

                        val formmmatedNickName = getFormattedNickName(
                            address?.nickname,
                            fullAddress, context
                        )

                        if (timeSlot?.isNullOrEmpty() == true) {
                            tvDeliveryLocation?.text =
                                context?.getString(R.string.no_timeslots_available_title)
                                    ?.plus("\t\u2022\t")?.plus(
                                        formmmatedNickName.append(
                                            capitaliseFirstLetter(
                                                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                                    ?: address?.address1 ?: ""
                                            )
                                        )
                                    )
                        } else {
                            tvDeliveryLocation?.text =
                                timeSlot.plus("\t\u2022\t").plus(formmmatedNickName).plus(
                                    capitaliseFirstLetter(
                                        WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                            ?: address?.address1 ?: ""
                                    )
                                )
                        }
                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_dash_delivery_circle)
                    }

                    else -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        tvDeliveryLocation?.text =
                            context?.resources?.getString(R.string.default_location)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_delivery_circle)
                    }
                }
            }
        }

        fun setCncStoreValidateResponse(browsingStoreData: Store, listStore: Store) {
            listStore.apply {
                unDeliverableCommerceItems = browsingStoreData.unDeliverableCommerceItems
                distance = browsingStoreData.distance
                deliverable = browsingStoreData.deliverable
                storeId = browsingStoreData.storeId
                deliverySlotsDetails = browsingStoreData.deliverySlotsDetails
                firstAvailableFoodDeliveryDate =
                    browsingStoreData.firstAvailableFoodDeliveryDate
                firstAvailableOtherDeliveryDate =
                    browsingStoreData.firstAvailableOtherDeliveryDate
                storeAddress = browsingStoreData.storeAddress
                quantityLimit = browsingStoreData.quantityLimit
                storeName = browsingStoreData.storeName
                storeDeliveryType = browsingStoreData.storeDeliveryType
                unSellableCommerceItems = browsingStoreData.unSellableCommerceItems
                locationId = browsingStoreData.locationId
                longitude = browsingStoreData.longitude
                latitude = browsingStoreData.latitude
                deliveryDetails = browsingStoreData.deliveryDetails
            }
            setBrowsingCncStore(browsingStoreData)
        }

        fun showChangeDeliveryTypeDialog(
            context: Context,
            requireFragmentManager: FragmentManager,
            deliveryType: Delivery?
        ) {
            var dialogTitle = ""
            var dialogSubTitle: CharSequence = ""
            var dialogBtnText = ""
            var dialogTitleImg: Int = R.drawable.img_delivery_truck
            when (deliveryType) {
                Delivery.STANDARD -> {
                    context.apply {
                        dialogTitle = getString(R.string.change_your_delivery_method_title)
                        dialogSubTitle = getText(R.string.change_your_delivery_method_standard)
                        dialogBtnText = getString(R.string.continue_with_standard_delivery)
                        dialogTitleImg = R.drawable.img_delivery_truck
                    }
                }

                Delivery.CNC -> {
                    context.apply {
                        dialogTitle = getString(R.string.change_your_delivery_method_title)
                        dialogSubTitle = getText(R.string.change_your_delivery_method_cnc)
                        dialogBtnText = getString(R.string.continue_with_cnc_delivery)
                        dialogTitleImg = R.drawable.img_collection_bag
                    }
                }

                Delivery.DASH -> {
                    context.apply {
                        dialogTitle = getString(R.string.change_your_delivery_method_title)
                        dialogSubTitle = getText(R.string.change_your_delivery_method_dash)
                        dialogBtnText = getString(R.string.continue_with_dash_delivery)
                        dialogTitleImg = R.drawable.img_dash_delivery
                    }
                }

                else -> {}
            }
            val customBottomSheetDialogFragment =
                CustomBottomSheetDialogFragment.newInstance(
                    dialogTitle,
                    dialogSubTitle,
                    dialogBtnText,
                    dialogTitleImg,
                    context.resources.getString(R.string.cancel_underline_html)
                )
            customBottomSheetDialogFragment.show(
                requireFragmentManager,
                CustomBottomSheetDialogFragment::class.java.simpleName
            )
        }

        fun getUnsellableList(
            validatePlace: ValidatePlace?,
            deliveryType: Delivery?
        ): MutableList<UnSellableCommerceItem>? {
            return when (deliveryType) {
                Delivery.STANDARD -> {
                    validatePlace?.unSellableCommerceItems
                }

                Delivery.CNC -> {
                    browsingCncStore?.storeId?.let { checkStoreHasUnsellable(validatePlace, it) }
                }

                Delivery.DASH -> {
                    validatePlace?.onDemand?.unSellableCommerceItems
                }

                else -> validatePlace?.unSellableCommerceItems
            }
        }

        private fun checkStoreHasUnsellable(
            validatePlace: ValidatePlace?,
            mStoreId: String
        ): MutableList<UnSellableCommerceItem>? {
            validatePlace?.stores?.forEach {
                if (it.storeId.equals(mStoreId)) {
                    return it.unSellableCommerceItems
                }
            }
            return null
        }

        fun getConfirmLocationRequest(deliveryType: Delivery?): ConfirmLocationRequest {
            return when (deliveryType) {
                Delivery.STANDARD -> {
                    ConfirmLocationRequest(
                        BundleKeysConstants.STANDARD,
                        ConfirmLocationAddress(
                            WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
                                ?: getPreferredPlaceId()
                        ),
                        ""
                    )
                }

                Delivery.CNC -> {
                    ConfirmLocationRequest(
                        BundleKeysConstants.CNC,
                        ConfirmLocationAddress(
                            if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() != null)
                                WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                            else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
                        ),
                        browsingCncStore?.storeId
                    )
                }

                Delivery.DASH -> {
                    ConfirmLocationRequest(
                        BundleKeysConstants.DASH,
                        ConfirmLocationAddress(
                            if (WoolworthsApplication.getDashBrowsingValidatePlaceDetails() != null)
                                WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                            else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
                        ),
                        if (WoolworthsApplication.getDashBrowsingValidatePlaceDetails() != null)
                            WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.onDemand?.storeId
                        else WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.storeId
                    )
                }

                else -> {
                    ConfirmLocationRequest(
                        BundleKeysConstants.STANDARD,
                        ConfirmLocationAddress(WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId),
                        ""
                    )
                }
            }
        }

        fun updateCheckOutLink(jSessionId: String?) {
            val appVersionParam = "appVersion"
            val jSessionIdParam = "JSESSIONID"
            val checkoutLink = AppConfigSingleton.cartCheckoutLink
            val context = WoolworthsApplication.getAppContext()
            val packageManager = context.packageManager

            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)

            val versionName = packageInfo.versionName
            val versionCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toInt() else packageInfo.versionCode
            val appVersion = "$versionName.$versionCode"

            if (checkoutLink != null) {
                val symbolType = if (checkoutLink.contains("?")) "&" else "?"
                WoolworthsApplication.setCartCheckoutLinkWithParams("$checkoutLink$symbolType$appVersionParam=$appVersion&$jSessionIdParam=$jSessionId")
            }
        }

        fun sendEmail(activity: Activity?, emailId: String, subject: String?) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(
                "mailto:" + emailId +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode("")
            )
            val listOfEmail =
                activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
            if (listOfEmail.size > 0) {
                activity?.startActivity(emailIntent)
            } else {
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.INFO,
                    bindString(R.string.contact_us_no_email_error).replace("email_address", emailId)
                        .replace(
                            "subject_line", subject
                                ?: ""
                        )
                )
            }
        }

        fun sendEmail(
            activity: Activity?,
            emailAddress: String,
            subjectLine: String?,
            emailMessage: String,
        ) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(
                "mailto:" + emailAddress +
                        "?subject=" + Uri.encode(subjectLine) +
                        "&body=" + Uri.encode(emailMessage)
            )
            val listOfEmail =
                activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
            if (listOfEmail.size > 0) {
                activity?.startActivity(emailIntent)
            } else {
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.INFO,
                    activity?.resources?.getString(R.string.contact_us_no_email_error)
                        ?.replace("email_address", emailAddress)?.replace(
                            "subject_line", subjectLine
                                ?: ""
                        )
                )
            }
        }

        fun postOneAppEvent(appScreen: String, featureName: String) {
            request(OneAppService().queryServicePostEvent(featureName, appScreen))
        }

        fun parseMoneyValue(
            value: String,
            groupingSeparator: String,
            currencySymbol: String,
        ): String =
            value.replace(groupingSeparator, "").replace(currencySymbol, "")

        fun parseMoneyValueWithLocale(
            locale: Locale,
            value: String,
            groupingSeparator: String,
            currencySymbol: String,
        ): Number {
            val valueWithoutSeparator = parseMoneyValue(value, groupingSeparator, currencySymbol)
            return try {
                NumberFormat.getInstance(locale).parse(valueWithoutSeparator)!!
            } catch (exception: ParseException) {
                0
            }
        }

        fun getLocaleFromTag(localeTag: String): Locale {
            return try {
                Locale.Builder().setLanguageTag(localeTag).build()
            } catch (e: IllformedLocaleException) {
                Locale.getDefault()
            }
        }

        fun highlightText(string: String, keys: MutableList<String>?): SpannableStringBuilder {
            val noteStringBuilder = SpannableStringBuilder(string)
            keys?.forEach { key ->
                val start = string.indexOf(key)
                val end = start.plus(key.length)
                val opensansSemiBoldFont: TypefaceSpan =
                    CustomTypefaceSpan("", getOpenSansSemiBoldFont())
                noteStringBuilder.setSpan(
                    opensansSemiBoldFont,
                    start,
                    end,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                noteStringBuilder.setSpan(
                    ForegroundColorSpan(bindColor(R.color.description_color)),
                    start,
                    end,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }

            return noteStringBuilder
        }

        fun getJSONFileFromRAWResFolder(context: Context?, @RawRes id: Int): JSONObject {
            val awsConfiguration: InputStream? = context?.resources?.openRawResource(id)
            val writer: Writer = StringWriter()
            val buffer = CharArray(1024)
            awsConfiguration?.use { config ->
                val reader: Reader = BufferedReader(InputStreamReader(config, "UTF-8"))
                var n: Int
                while (reader.read(buffer).also {
                        n = it
                    } != -1) {
                    writer.run { write(buffer, 0, n) }
                }
            }
            val awsConfigurationEnvelop = writer.toString()
            return JSONObject(awsConfigurationEnvelop)
        }

        fun firstLetterCapitalization(name: String?): String? {
            val capitaliseFirstLetterInName =
                name?.substring(0, 1)?.toUpperCase(Locale.getDefault())
            val lowercaseOtherLetterInName =
                name?.substring(1, name.length)?.toLowerCase(Locale.getDefault())
            return capitaliseFirstLetterInName?.plus(lowercaseOtherLetterInName)
        }

        fun isOperatingHoursForInAppChat(tradingHours: MutableList<ConfigTradingHours>): Boolean? {
            val (_, opens, closes) = getInAppTradingHoursForToday(tradingHours)

            val now = Calendar.getInstance()
            val hour = now[Calendar.HOUR_OF_DAY] // Get hour in 24 hour format
            val minute = now[Calendar.MINUTE]

            val currentTime = WFormatter.parseDate("$hour:$minute")
            val openingTime = WFormatter.parseDate(opens)
            val closingTime = WFormatter.parseDate(closes)
            return currentTime.after(openingTime) && currentTime.before(closingTime)
        }

        private fun getInAppTradingHoursForToday(tradingHours: MutableList<ConfigTradingHours>?): ConfigTradingHours {
            var tradingHoursForToday: ConfigTradingHours? = null
            tradingHours?.let {
                it.forEach { tradingHours ->
                    if (tradingHours.day.equals(Utils.getCurrentDay(), true)) {
                        tradingHoursForToday = tradingHours
                        return tradingHours
                    }
                }
            }
            return tradingHoursForToday ?: ConfigTradingHours("sunday", "00:00", "00:00")
        }

        fun avoidDoubleClicks(view: View?) {
            view?.apply {
                if (!isClickable) return
                isClickable = false
                GlobalScope.doAfterDelay(AppConstant.DELAY_900_MS) {
                    isClickable = true
                }
            }
        }

        /**
         * Calling the convertToTranslucent method on platforms after Android 5.0
         */
        @SuppressLint("DiscouragedPrivateApi")
        fun convertActivityToTranslucent(activity: Activity) {
            try {
                val getActivityOptions =
                    Activity::class.java.getDeclaredMethod("getActivityOptions")
                getActivityOptions.isAccessible = true
                val options = getActivityOptions.invoke(activity)
                val classes = Activity::class.java.declaredClasses
                var translucentConversionListenerClazz: Class<*>? = null
                for (clazz in classes) {
                    if (clazz.simpleName.contains("TranslucentConversionListener")) {
                        translucentConversionListenerClazz = clazz
                    }
                }
                val convertToTranslucent = Activity::class.java.getDeclaredMethod(
                    "convertToTranslucent",
                    translucentConversionListenerClazz, ActivityOptions::class.java
                )
                convertToTranslucent.isAccessible = true
                convertToTranslucent.invoke(activity, null, options)
            } catch (t: Throwable) {
                FirebaseManager.logException(t)
            }
        }

        fun String.capitaliseFirstLetterInEveryWord(): String =
            split(" ").map { it.lowercase().replaceFirstChar { it -> it.titlecase() } }
                .joinToString(" ")

        fun showGeneralInfoDialog(
            fragmentManager: FragmentManager,
            description: String,
            title: String = "",
            actionText: String = "",
            infoIcon: Int = 0,
            isFromCheckoutScreen: Boolean = false,
            isOutOfStockDialog: Boolean = false
        ) {
            val dialog =
                GeneralInfoDialogFragment.newInstance(
                    description,
                    title,
                    actionText,
                    infoIcon,
                    isFromCheckoutScreen
                )
            dialog.isCancelable = !isFromCheckoutScreen
            if (isOutOfStockDialog) {
                // Firebase event to be triggered when displaying the out of stock dialog
                FirebaseAnalyticsEventHelper.outOfStock()
            }
            fragmentManager.let { fragmentTransaction ->
                dialog.show(
                    fragmentTransaction,
                    GeneralInfoDialogFragment::class.java.simpleName
                )
            }
        }

        fun openUrlInPhoneBrowser(urlString: String?, activity: Activity?) {
            try {
                urlString?.apply {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    activity?.startActivity(intent)
                }
            } catch (exception: ActivityNotFoundException) {
                FirebaseManager.logException("no browser found - $exception")
                activity?.apply {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder
                        .setTitle(R.string.browser_not_found_title)
                        .setMessage(R.string.browser_not_found_msg)
                        .setCancelable(true)
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }

        fun openApplicationSettings(requestCode: Int, activity: Activity?) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            activity?.startActivityForResult(intent, requestCode)
        }

        fun openAccessMyLocationDeviceSettings(requestCode: Int, activity: Activity?) {
            val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity?.startActivityForResult(locIntent, requestCode)
            activity?.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }

        fun getAccount(accountExtras: String?): Pair<ApplyNowState, Account>? {
            return Gson().fromJson<Pair<ApplyNowState, Account>>(
                accountExtras,
                object : TypeToken<Pair<ApplyNowState?, Account?>?>() {}.type
            )
        }

        fun isAppInstalled(activity: Activity?, appURI: String?): Boolean {
            activity?.apply {
                return appURI?.let { this.packageManager.getLaunchIntentForPackage(it) } != null
            }
            return false
        }

        fun isDeliveryOptionClickAndCollect(): Boolean {
            return getPreferredDeliveryType() == Delivery.CNC
        }

        fun isDeliveryOptionDash(): Boolean {
            return getPreferredDeliveryType() == Delivery.DASH
        }

        fun isDeliveryOptionStandard(): Boolean {
            return getPreferredDeliveryType() == Delivery.STANDARD
        }

        @SuppressLint("MissingPermission")
        @JvmStatic
        fun setUserPropertiesToNull() {
            AnalyticsManager.apply {
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_STATE,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.CREDIT_CARD_PRODUCT_STATE,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_STATE,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ATGId,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.C2ID,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserId(FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE)
            }
        }

        fun getUserDefinedDeviceName(activity: Activity?): String {
            var deviceName = ""
            activity?.apply {
                try {
                    if (TextUtils.isEmpty(deviceName)) {
                        deviceName = Settings.Secure.getString(contentResolver, "bluetooth_name")
                    }
                } catch (e: Exception) {
                }
            }
            return deviceName
        }

        fun rotateViewAnimation(): RotateAnimation {
            val animation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            with(animation) {
                duration = AppConstant.DURATION_1000_MS
                repeatCount = 45
                repeatCount = Animation.INFINITE
            }
            return animation
        }

        fun getTimeStamp(): String? {
            return try {
                java.lang.String.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(
                        System.currentTimeMillis()
                    )
                )
            } catch (ex: java.lang.Exception) {
                null
            }

        }

        /**
         * This function should satisfy below conditions
         * - It should not be a store pick up &&
         * - Selected suburb [AppInstanceObject.User.preferredShoppingDeliveryLocation] should match up with suburb id's in mobile config
         * @return Returns boolean value indicating if current suburb delivers liquors
         * [Boolean.true]  if above conditions match else [Boolean.false]
         *
         * @see [za.co.woolworths.financial.services.android.models.dao.AppInstanceObject.User.preferredShoppingDeliveryLocation]
         */
        fun isCurrentSuburbDeliversLiquor(): Boolean {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.liquorDeliverable == true
        }

        /**
         * This function should satisfy below conditions
         * - Retrieve [SessionDao.KEY.LIQUOR_MODAL_SHOWN] flag from SessionDao database
         *
         * @return Returns boolean value indicating if liquor selection modal is shown
         * [Boolean.true]  if above conditions match else [Boolean.false]
         *
         * @see SessionDao
         */
        fun isLiquorModalShown(): Boolean {
            val firstTime = Utils.getSessionDaoValue(KEY.LIQUOR_MODAL_SHOWN)
            return firstTime != null
        }

        fun setLiquorModalShown() {
            try {
                val firstTime = Utils.getSessionDaoValue(KEY.LIQUOR_MODAL_SHOWN)
                if (firstTime == null) {
                    Utils.sessionDaoSave(KEY.LIQUOR_MODAL_SHOWN, "1")
                }
            } catch (ignored: NullPointerException) {
            }
        }

        fun openLinkInInternalWebView(
            activity: Activity?,
            url: String?,
            treatmentPlan: Boolean,
            collectionsExitUrl: String?,
        ) {
            activity?.apply {
                val openInternalWebView = Intent(this, WInternalWebPageActivity::class.java)
                openInternalWebView.putExtra("externalLink", url)
                if (treatmentPlan) {
                    openInternalWebView.putExtra(TREATMENT_PLAN, treatmentPlan)
                    openInternalWebView.putExtra(COLLECTIONS_EXIT_URL, collectionsExitUrl)
                    startActivityForResult(openInternalWebView, RESULT_CODE_CLOSE_VIEW)
                } else {
                    openInternalWebView.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(openInternalWebView)
                }
            }
        }

        fun openTreatmentPlanUrl(activity: Activity?, eligibilityPlan: EligibilityPlan?) {
            var collectionUrlFromConfig: Pair<String?, String?>? = null
            var exitUrl: String? = ""
            val accountOptions = AppConfigSingleton.accountOptions

            when (eligibilityPlan?.productGroupCode) {
                ProductGroupCode.SC -> {
                    collectionUrlFromConfig =
                        accountOptions?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl to accountOptions?.showTreatmentPlanJourney?.storeCard?.collectionsDynamicUrl
                    exitUrl = accountOptions?.showTreatmentPlanJourney?.storeCard?.exitUrl
                }

                ProductGroupCode.PL -> {
                    collectionUrlFromConfig =
                        accountOptions?.collectionsStartNewPlanJourney?.personalLoan?.collectionsUrl to accountOptions?.showTreatmentPlanJourney?.personalLoan?.collectionsDynamicUrl
                    exitUrl = accountOptions?.showTreatmentPlanJourney?.personalLoan?.exitUrl
                }

                ProductGroupCode.CC -> {
                    collectionUrlFromConfig =
                        accountOptions?.collectionsStartNewPlanJourney?.creditCard?.collectionsUrl to accountOptions?.showTreatmentPlanJourney?.creditCard?.collectionsDynamicUrl
                    exitUrl = accountOptions?.collectionsStartNewPlanJourney?.creditCard?.exitUrl
                }

                else -> {}
            }

            /**
             *  Use dynamic collection url when ("collectionsViewExistingPlan")
             *  else use collection url
             */
            val finalCollectionUrlFromConfig =
                when (eligibilityPlan?.actionText == ActionText.VIEW_TREATMENT_PLAN.value
                        || eligibilityPlan?.actionText == ActionText.VIEW_ELITE_PLAN.value) {
                    true -> collectionUrlFromConfig?.second
                    false -> collectionUrlFromConfig?.first
                }

            val url = finalCollectionUrlFromConfig + eligibilityPlan?.appGuid

            openLinkInInternalWebView(
                activity,
                url,
                true,
                exitUrl
            )
        }

        fun linkDeviceIfNecessary(
            activity: Activity?,
            state: ApplyNowState,
            doJob: () -> Unit,
            elseJob: () -> Unit,
        ) {
            if (verifyAppInstanceId() &&
                (Utils.isGooglePlayOrHuaweiMobileServicesAvailable())
            ) {
                doJob()
                activity?.let {
                    val intent = Intent(it, LinkDeviceConfirmationActivity::class.java)
                    intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, state)
                    it.startActivity(intent)
                    it.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
                }
            } else {
                elseJob()
            }
        }

        fun getUpdatedUtils(rating: Float): Float {
            val completeValue: Int = rating.toInt() % 10
            val decimalValue: Int = ((rating % 1) * 10).toInt()

            if (decimalValue >= 0 && decimalValue <= 2) {
                return completeValue.toFloat()
            } else if (decimalValue > 2 && decimalValue <= 7) {
                return (completeValue + .5).toFloat()
            }
            return rating.roundToInt().toFloat()
        }

        fun getPreferredDeliveryType(): Delivery? {
            return Delivery.getType(
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType
                    ?: Delivery.STANDARD.type
            )
        }

        fun getDeliveryType(): FulfillmentDetails? {
            return if (SessionUtilities.getInstance().isUserAuthenticated) {
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails
            } else {
                getAnonymousUserLocationDetails()?.fulfillmentDetails
            }
        }

        fun getDeliveryDetails(isUserBrowsing: Boolean): String? {
            return if (isUserBrowsing) {
                when (browsingDeliveryType) {
                    Delivery.CNC -> {
                        browsingCncStore?.deliveryDetails ?: getPreferredCnCStore()?.deliveryDetails
                        ?: ""
                    }

                    Delivery.DASH -> {
                        WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.onDemand?.deliveryDetails
                            ?: WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryDetails
                            ?: ""
                    }

                    Delivery.STANDARD -> WoolworthsApplication.getValidatePlaceDetails()?.deliveryDetails
                        ?: ""

                    else -> WoolworthsApplication.getValidatePlaceDetails()?.deliveryDetails ?: ""
                }
            } else {
                when (getPreferredDeliveryType()) {
                    Delivery.CNC -> {
                        getPreferredCnCStore()?.deliveryDetails ?: ""
                    }

                    Delivery.DASH -> {
                        WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryDetails
                            ?: ""
                    }

                    Delivery.STANDARD -> WoolworthsApplication.getValidatePlaceDetails()?.deliveryDetails
                        ?: ""

                    else -> WoolworthsApplication.getValidatePlaceDetails()?.deliveryDetails ?: ""
                }
            }
        }

        private fun getPreferredCnCStore(): Store? {
            val deliveryType = getDeliveryType()
            for (store in WoolworthsApplication.getValidatePlaceDetails()?.stores ?: ArrayList()) {
                deliveryType?.let {
                    if (it.storeId == store.storeId) {
                        return store
                    }
                }
            }
            return null
        }

        fun getPreferredPlaceId(): String {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId ?: ""
        }

        private fun getPreferredStoreName(): String {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeName ?: ""
        }

        fun getPreferredDeliveryAddress(): String {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.address1 ?: ""
        }

        fun getPreferredDeliveryAddressOrStoreName(): String {
            return when (getPreferredDeliveryType()) {
                Delivery.CNC, Delivery.STANDARD, Delivery.DASH -> getPreferredStoreName()
                else -> ""
            }
        }

        fun retrieveFulfillmentStoreId(fulFillmentTypeId: String?): String {
            var fulFillmentStoreId: String = ""
            fulFillmentTypeId?.let {
                var typeId = fulFillmentTypeId
                if (typeId.length == 1)
                    typeId = "0$typeId"
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.fulfillmentStores?.let {
                    val details = Gson().fromJson<Map<String, String>>(
                        it,
                        object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                    )
                    fulFillmentStoreId = details?.get(typeId) ?: ""
                }
            }
            return fulFillmentStoreId
        }

        fun retriveFulfillmentStoreIdList(): Map<String, String>? {
            var fulfillmentDetails: Map<String, String>? = null
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.fulfillmentStores?.let {
                fulfillmentDetails = Gson().fromJson<Map<String, String>>(
                    it,
                    object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                )
            }
            return fulfillmentDetails
        }

        fun getStoreDeliveryType(fulfillmentDetails: FulfillmentDetails?): String? {
            val storesJsonElement = fulfillmentDetails?.fulfillmentStores
            if (storesJsonElement != null) {
                try {
                    val storeMap = Gson().fromJson<Map<String, String>>(
                        storesJsonElement,
                        object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                    )
                    val fulfilmentTypes = storeMap.keys
                    if (fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.FOOD_ITEMS.type) && fulfilmentTypes.size == 1) {
                        //Type FOOD
                        return StoreUtils.Companion.StoreDeliveryType.FOOD.type
                    } else if (!fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.FOOD_ITEMS.type) &&
                        (fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS.type) || fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.CRG_ITEMS.type))) {
                        //Type FBH
                        return StoreUtils.Companion.StoreDeliveryType.OTHER.type
                    } else if (fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.FOOD_ITEMS.type)
                        && (fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS.type) || fulfilmentTypes.contains(StoreUtils.Companion.FulfillmentType.CRG_ITEMS.type))) {
                        //Type All items
                        return StoreUtils.Companion.StoreDeliveryType.FOOD_AND_OTHER.type
                    }
                } catch (exception: JsonSyntaxException) {
                    FirebaseManager.logException(exception)
                    return null
                }
            }
            return null
        }

        fun getUniqueDeviceID(result: (String?) -> Unit) {
            val deviceID = Utils.getSessionDaoValue(KEY.DEVICE_ID)
            when (deviceID.isNullOrEmpty()) {
                true -> {
                    FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val resultId = task.result
                            Utils.sessionDaoSave(KEY.DEVICE_ID, resultId)
                            result(resultId)
                        }

                    }
                }

                false -> result(deviceID)
            }
        }


        fun lowercaseEditText(editText: EditText) {
            editText.filters = arrayOf<InputFilter>(
                object : InputFilter.AllCaps() {
                    override fun filter(
                        source: CharSequence,
                        start: Int,
                        end: Int,
                        dest: Spanned?,
                        dstart: Int,
                        dend: Int,
                    ): CharSequence {
                        return source.toString().lowercase()
                    }
                }
            )
        }

        fun hasADayPassed(dateString: String?): Boolean {
            // when dateString = null it means it's the first time to call api
            if (dateString == null) return true
            val from = try {
                LocalDateTime.parse(
                    dateString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                )
            } catch (e: Exception) {
                LocalDateTime.parse(
                    dateString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                )
            }
            val today = LocalDateTime.now()
            var period = ChronoUnit.DAYS.between(from, today)
            return if (period >= 1) {
                Utils.sessionDaoSave(KEY.FICA_LAST_REQUEST_TIME, null)
                true
            } else {
                false
            }
        }

        fun ficaVerifyRedirect(
            activity: Activity?,
            url: String?,
            isWebView: Boolean,
            collectionsExitUrl: String?
        ) {
            activity?.apply {
                val openInternalWebView = Intent(this, WInternalWebPageActivity::class.java)
                openInternalWebView.putExtra("externalLink", url)
                if (isWebView) {
                    openInternalWebView.putExtra(COLLECTIONS_EXIT_URL, collectionsExitUrl)
                    startActivityForResult(openInternalWebView, RESULT_CODE_CLOSE_VIEW)
                } else {
                    openUrlInPhoneBrowser(url, activity)
                    activity.finish()
                }
            }
        }

        fun isFicaEnabled(): Boolean {
            return Utils.isFeatureEnabled(accountOptions?.ficaRefresh?.minimumSupportedAppBuildNumber)
        }

        fun isPetInsuranceEnabled(): Boolean {
            return Utils.isFeatureEnabled(accountOptions?.insuranceProducts?.minimumSupportedAppBuildNumber)
        }

        fun saveAnonymousUserLocationDetails(shoppingDeliveryLocation: ShoppingDeliveryLocation) {
            Utils.sessionDaoSave(
                KEY.ANONYMOUS_USER_LOCATION_DETAILS,
                Utils.objectToJson(shoppingDeliveryLocation)
            )
        }

        fun getAnonymousUserLocationDetails(): ShoppingDeliveryLocation? {
            var location: ShoppingDeliveryLocation? = null
            try {
                SessionDao.getByKey(KEY.ANONYMOUS_USER_LOCATION_DETAILS).value?.let {
                    location = Utils.strToJson(
                        it,
                        ShoppingDeliveryLocation::class.java
                    ) as ShoppingDeliveryLocation?
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
            return location
        }

        fun clearAnonymousUserLocationDetails() {
            Utils.removeFromDb(KEY.ANONYMOUS_USER_LOCATION_DETAILS)
        }

        fun coroutineContextWithExceptionHandler(errorHandler: (AbsaApiFailureHandler) -> Unit): CoroutineContext {
            return (Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                when (throwable) {
                    is SocketException -> errorHandler(AbsaApiFailureHandler.NoInternetApiFailure)
                    is HttpException -> errorHandler(
                        AbsaApiFailureHandler.HttpException(
                            throwable.message(),
                            throwable.code()
                        )
                    )

                    is Exception -> errorHandler(
                        AbsaApiFailureHandler.Exception(
                            throwable.message,
                            throwable.hashCode()
                        )
                    )

                    else -> errorHandler(AbsaApiFailureHandler.NoInternetApiFailure)
                }
            })
        }


        fun cliErrorMessageDialog(appCompatActivity: AppCompatActivity?, data: ErrorMessageDialog) {
            appCompatActivity?.apply {
                val fragmentInstance = CLIErrorMessageButtonDialog.newInstance(data)
                fragmentInstance.show(
                    supportFragmentManager,
                    CLIErrorMessageButtonDialog::class.java.simpleName
                )
            }
        }

        fun vocShoppingHandling(deliveryType: String?): VocTriggerEvent? {
            var event: VocTriggerEvent? = null
            when (Delivery.getType(deliveryType)) {
                Delivery.CNC -> {
                    event = VocTriggerEvent.SHOP_CLICK_COLLECT_CONFIRM
                }

                Delivery.STANDARD -> {
                    event = VocTriggerEvent.CHCKOUT_CNT_TO_PMNT
                }

                else -> {}
            }
            return event
        }

        fun showMinCartValueError(activity: AppCompatActivity, minimumBasketAmount: Double?) {
            activity?.supportFragmentManager?.let {
                showGeneralInfoDialog(
                    it,
                    activity.getString(R.string.minspend_error_msg_desc),
                    String.format(
                        activity.getString(
                            R.string.minspend_error_msg_title,
                            minimumBasketAmount
                        )
                    ),
                    activity.getString(R.string.got_it),
                    R.drawable.ic_cart,
                    true
                )
            }
        }

        @JvmStatic
        fun showQuantityLimitErrror(
            fragmentManager: FragmentManager?,
            title: String,
            desc: String = "Error message",
            context: Context?
        ) {
            if (context == null || fragmentManager == null || getPreferredDeliveryType() != Delivery.DASH) {
                return
            }
            showGeneralInfoDialog(
                fragmentManager = fragmentManager,
                description = desc,
                title = title,
                actionText = context.getString(R.string.got_it),
                infoIcon = R.drawable.icon_dash_delivery_scooter
            )
        }

        fun showPetInsurancePendingDialog(fragmentManager: FragmentManager) {
            val petInsurancePendingFragment =
                PetInsurancePendingFragment.newInstance()
            petInsurancePendingFragment.show(
                fragmentManager,
                PetInsurancePendingFragment::class.java.simpleName
            )
        }

        fun petInsuranceRedirect(
            activity: Activity?,
            url: String?,
            isWebView: Boolean,
            collectionsExitUrl: String?
        ) {
            activity?.apply {
                val openInternalWebView = Intent(this, WInternalWebPageActivity::class.java)
                openInternalWebView.putExtra("externalLink", url)
                openInternalWebView.putExtra(IS_PET_INSURANCE, true)
                if (isWebView) {
                    openInternalWebView.putExtra(COLLECTIONS_EXIT_URL, collectionsExitUrl)
                    startActivityForResult(
                        openInternalWebView,
                        PET_INSURANCE_REQUEST_CODE
                    )
                } else {
                    openUrlInPhoneBrowser(url, activity)
                    activity.finish()
                }
            }
        }

        fun getFormattedNickName(
            nickname: String?,
            address: CharSequence?,
            context: Context?
        ): SpannableStringBuilder {
            val nickNameWithAddress = SpannableStringBuilder()
            var formattedNickName =
                SpannableString(
                    nickname.plus(" ").plus(context?.resources?.getString(R.string.bullet))
                        .plus(" ")
                )

            if (nickname.isNullOrEmpty() == true || nickname?.equals(address) == true) {
                formattedNickName = SpannableString(context?.resources?.getString(R.string.empty))
            }
            nickNameWithAddress.append(formattedNickName)
            return nickNameWithAddress
        }

        /**
         * This function requires to implement setFragmentResultListener with requestkey
         * ADD_TO_SHOPPING_LIST_REQUEST_CODE since it returns a result.
         */
        fun openAddToListPopup(
            activity: Activity?,
            fragmentManager: FragmentManager,
            listOfItems: ArrayList<AddToListRequest>,
            orderId: String? = null,
            eventData: AddToWishListFirebaseEventData? = null
        ) {

            val fragment = AddToListFragment().also {
                it.arguments = Bundle().apply {
                    putString(AddToListViewModel.ARG_ORDER_ID, orderId)
                    putInt(
                        AppConstant.RESULT_CODE, ADD_TO_SHOPPING_LIST_REQUEST_CODE
                    )
                    putParcelableArrayList(AddToListViewModel.ARG_ITEMS_TO_BE_ADDED, listOfItems)
                    putParcelable(BUNDLE_WISHLIST_EVENT_DATA, eventData)
                }
            }
            fragment.show(fragmentManager, AddToListFragment::class.simpleName)
            (activity as? BottomNavigationActivity)?.apply {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOPADDTOLIST,
                    this
                )
            }
        }

        fun setAddToListFragmentResultListener(
            requestCode: Int = ADD_TO_SHOPPING_LIST_REQUEST_CODE,
            activity: FragmentActivity,
            lifecycleOwner: LifecycleOwner,
            toastContainerView: View,
            onToastClick: () -> Unit
        ) {
            activity.supportFragmentManager.setFragmentResultListener(
                requestCode.toString(),
                lifecycleOwner
            ) { _, bundle ->

                when (bundle.getInt(AppConstant.RESULT_CODE, -1)) {
                    ADD_TO_SHOPPING_LIST_REQUEST_CODE -> {
                        val selectedLists: ArrayList<ShoppingList>? =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                bundle.getParcelableArrayList(
                                    AppConstant.Keys.KEY_LIST_DETAILS,
                                    ShoppingList::class.java
                                )
                            } else {
                                bundle.get(AppConstant.Keys.KEY_LIST_DETAILS) as?
                                        ArrayList<ShoppingList>
                            }

                        val listName =
                            if (selectedLists?.size == 1) {
                                selectedLists.getOrNull(0)?.listName
                            } else {
                                activity.getString(R.string.multiple_lists)
                            }
                        val hasGiftProduct = bundle.getBoolean(KEY_HAS_GIFT_PRODUCT)

                        ToastFactory.buildItemsAddedToList(
                            activity = activity,
                            viewLocation = toastContainerView,
                            listName = listName ?: return@setFragmentResultListener,
                            hasGiftProduct = hasGiftProduct,
                            count = bundle.getInt(AppConstant.Keys.KEY_COUNT, 0),
                            onButtonClick = {
                                (activity as? BottomNavigationActivity)?.apply {
                                    navigateToTabIndex(BottomNavigationActivity.INDEX_ACCOUNT, null)
                                }

                                if (selectedLists?.size == 1) {
                                    selectedLists.getOrNull(0)?.let {
                                        ScreenManager.presentShoppingListDetailActivity(
                                            activity,
                                            it.listId,
                                            it.listName,
                                            false
                                        )
                                    }
                                } else {
                                    ScreenManager.presentMyListScreen(activity)
                                }
                                onToastClick()
                            }
                        )
                    }

                    AppConstant.RESULT_FAILED -> {
                        Toast.makeText(
                            activity,
                            R.string.add_to_list_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        fun triggerFireBaseEvents(
            eventName: String,
            arguments: Map<String, String>,
            activity: Activity?
        ) {
            val params = Bundle()
            arguments.forEach { entry ->
                params.putString(entry.key, entry.value)
            }
            logEvent(eventName, params)
            requestInAppReview(eventName, activity)
        }

        fun extractPlistFromDeliveryDetails(): String? {
            val deliveryDetails: String? = Utils.getDeliveryDetails()
            if (deliveryDetails.isNullOrEmpty()) {
                return ""
            } else {
                val deliveryDetailsArray = deliveryDetails?.split("-")
                return deliveryDetailsArray?.getOrNull(1)
            }
        }

        fun getPreferredSuburbId(): String {
            val fulfillmentDetails: FulfillmentDetails? = getDeliveryType()
            fulfillmentDetails ?: return ""
            return when (getPreferredDeliveryType()) {
                Delivery.STANDARD -> fulfillmentDetails.address?.placeId ?: ""
                Delivery.CNC,
                Delivery.DASH -> fulfillmentDetails.storeId ?: ""
                null -> ""
            }
        }
    }
}

fun setBrowsingCncStore(browsingStoreData: Store) {
    KotlinUtils.browsingCncStore = browsingStoreData
}
fun Group.setAlphaForGroupdViews(alpha: Float) = referencedIds.forEach {
    rootView.findViewById<View>(it).alpha = alpha
}

fun Fragment.setDialogPadding(dialog: Dialog?) {
    val inset = 10
    if (dialog != null) {
        val width = (deviceWidth() - resources.getDimension(R.dimen._48sdp)).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(width, height)
        dialog.window?.setBackgroundDrawable(
            InsetDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.transparent
                    )
                ), inset, inset, inset, inset
            )
        )
    }
}

fun RecyclerView.runWhenReady(action: () -> Unit) {
    val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
}

var EditText.value
    get() = this.text.toString()
    set(value) {
        this.setText(value)
    }

fun Fragment.isFragmentAttached(): Boolean {
    if (isAdded && context != null) {
        return true
    }
    return false
}
fun isAValidSouthAfricanNumber(number: String?): Boolean {
    val regex = Regex(AppConstant.SA_MOBILE_NUMBER_PATTERN)
    return regex.matches(number.toString())
}









