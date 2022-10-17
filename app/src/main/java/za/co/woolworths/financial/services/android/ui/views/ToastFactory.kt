package za.co.woolworths.financial.services.android.ui.views

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToShoppingListFragment
import android.util.DisplayMetrics
import android.view.View.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.gson.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_TOAST
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ReceiverManager
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class ToastFactory {

    companion object {
        private const val POPUP_DELAY_MILLIS = 3000
        private const val POPUP_3000_DELAY_MILLIS: Long = 3000

        fun buildShoppingListToast(
            activity: Activity,
            viewLocation: View,
            buttonIsVisible: Boolean,
            data: Intent?,
            toastInterface: IToastInterface
        ): PopupWindow? {
            val context = WoolworthsApplication.getAppContext()

            val shoppingList =
                data?.getStringExtra(AddToShoppingListFragment.POST_ADD_TO_SHOPPING_LIST)
            var shoppingListObject: JsonObject? = null
            var shoppingListArray: JsonArray? = null

            shoppingList?.let {
                val element = JsonParser().parse(it)
                element?.let { item ->
                    when (element) {
                        is JsonObject -> shoppingListObject = item as? JsonObject
                        is JsonArray -> shoppingListArray = item as? JsonArray
                    }
                }
            }

            // inflate your xml layout
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(
                layout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            tvButtonClick?.visibility = if (buttonIsVisible) VISIBLE else GONE
            tvBoldTitle?.visibility = VISIBLE
            tvAddedTo?.isAllCaps = true

            shoppingListObject?.let {
                when (it.size()) {
                    1 -> {
                        var shoppingList: JsonElement? = null
                        it.entrySet()?.forEach { item -> shoppingList = item.value }
                        tvBoldTitle?.setText(shoppingList?.asJsonObject?.get("name")?.asString)
                    }
                    else -> tvBoldTitle?.setText(
                        context.getString(R.string.shopping_list).plus("s")
                    )
                }
            }

            // handle popupWindow click event
            tvButtonClick?.setOnClickListener {
                toastInterface.onToastButtonClicked(shoppingListObject ?: shoppingListArray)
                popupWindow.dismiss() // dismiss the window
            }

            // dismiss the popup window after 3sec
            popupWindow.isFocusable = false
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            popupWindow.showAtLocation(
                viewLocation,
                Gravity.BOTTOM,
                0,
                convertDpToPixel(getDeviceHeight(activity), context)
            )
            return popupWindow
        }

        fun buildPushNotificationAlertToast(
            activity: Activity,
            viewLocation: View,
            toastInterface: IToastInterface,
        ): PopupWindow? {
            // inflate your xml layout
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            val tvFullText = layout?.findViewById<WTextView>(R.id.tvFullText)
            tvAddedTo?.visibility = GONE
            tvBoldTitle?.visibility = GONE
            tvFullText?.visibility = VISIBLE
            tvButtonClick?.text = activity.getString(R.string.push_notification_turn_on_text)

            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(
                layout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            // handle popupWindow click event
            tvButtonClick?.setOnClickListener {
                toastInterface.onToastButtonClicked(null)
                popupWindow.dismiss() // dismiss the window
            }

            // dismiss the popup window after 3sec
            popupWindow.isFocusable = false
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            viewLocation.post {
                popupWindow.showAtLocation(
                    viewLocation,
                    Gravity.BOTTOM,
                    0,
                    convertDpToPixel(getDeviceHeight(activity), activity)
                )
            }
            return popupWindow
        }

        private fun convertDpToPixel(dp: Float, context: Context): Int {
            return (dp * (context.resources?.displayMetrics?.densityDpi?.toFloat()
                ?.div(DisplayMetrics.DENSITY_DEFAULT)!!)).toInt()
        }

        fun buildAddToCartSuccessToast(
            viewLocation: View,
            buttonIsVisible: Boolean,
            activity: Activity
        ): PopupWindow? {
            val context = WoolworthsApplication.getAppContext()
            // inflate your xml layout
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(
                layout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            tvButtonClick?.visibility = if (buttonIsVisible) VISIBLE else GONE
            tvBoldTitle?.visibility = GONE
            tvAddedTo?.text = context.getString(R.string.toast_added_to_cart)
            tvAddedTo?.setAllCaps(true)

            // handle popupWindow click event
            tvButtonClick?.setOnClickListener {

                ScreenManager.presentShoppingCart(activity)
                popupWindow.dismiss() // dismiss the window
            }
            popupWindow.isFocusable = false

            // dismiss the popup window after 3sec
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            popupWindow.showAtLocation(
                viewLocation,
                Gravity.BOTTOM,
                0,
                convertDpToPixel(getDeviceHeight(activity), context)
            )
            return popupWindow
        }

        fun showToast(activity: Activity, viewLocation: View, message: String): PopupWindow? {
            val context = WoolworthsApplication.getAppContext()
            // inflate your xml layout
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(
                layout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            tvButtonClick?.visibility = GONE
            tvBoldTitle?.visibility = VISIBLE
            tvAddedTo?.visibility = GONE
            tvAddedTo?.setAllCaps(true)

            popupWindow.isFocusable = false

            // dismiss the popup window after 3sec
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            popupWindow.showAtLocation(
                viewLocation,
                Gravity.BOTTOM,
                0,
                convertDpToPixel(getDeviceHeight(activity), context)
            )
            return popupWindow
        }

        fun buildShoppingListFromSearchResultToast(
            activity: Activity,
            viewLocation: View,
            listName: String,
            count: Int
        ): PopupWindow? {
            val context = WoolworthsApplication.getAppContext()

            // inflate your xml layout
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(
                layout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            tvButtonClick?.visibility = GONE
            tvBoldTitle?.visibility = VISIBLE
            tvAddedTo?.isAllCaps = true
            tvAddedTo?.setText("$count ITEM".plus(if (count > 1) "S" else "").plus(" ADDED TO"))
            tvBoldTitle?.setText(listName)

            popupWindow.isFocusable = false

            // dismiss the popup window after 3sec
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            popupWindow.showAtLocation(
                viewLocation,
                Gravity.BOTTOM,
                0,
                convertDpToPixel(getDeviceHeight(activity), context)
            )
            return popupWindow
        }

        private fun getDeviceHeight(activity: Activity): Float {
            val display = activity.windowManager?.defaultDisplay
            val size = Point()
            display?.getSize(size)
            return (size.y * (3 / 4)).toFloat() + 100f
        }

        fun buildAddToCartSuccessToast(
            viewLocation: View,
            buttonIsVisible: Boolean,
            activity: Activity,
            toastInterface: IToastInterface?
        ): PopupWindow? {
            val context = WoolworthsApplication.getAppContext()
            // inflate your xml layout
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(
                layout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            tvButtonClick?.visibility = if (buttonIsVisible) VISIBLE else GONE
            tvBoldTitle?.visibility = GONE
            tvAddedTo?.text = context.getString(R.string.toast_added_to_cart)
            tvAddedTo?.isAllCaps = true

            tvButtonClick?.setOnClickListener {
                toastInterface?.onToastButtonClicked(JsonObject())
                popupWindow.dismiss() // dismiss the window
            }
            popupWindow.isFocusable = false

            // dismiss the popup window after 3sec
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            popupWindow.showAtLocation(
                viewLocation,
                Gravity.BOTTOM,
                0,
                convertDpToPixel(getDeviceHeight(activity), context)
            )
            return popupWindow
        }

        fun showItemsLimitToastOnAddToCart(
            viewLocation: View,
            productCountMap: ProductCountMap,
            activity: Activity,
            count: Int = 0,
            viewButtonVisible: Boolean = true
        ): PopupWindow {
            val context = WoolworthsApplication.getAppContext()
            // inflate your xml layout
            val inflater = LayoutInflater.from(context)
            val view = inflater?.inflate(R.layout.items_limit_custom_toast, null)
            val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            val tvTotalProductCount = view?.findViewById<TextView>(R.id.totalProductCount)
            val tvNoOfItemsAddedToCart = view?.findViewById<TextView>(R.id.noOfItemsAddedToCart)
            val tvFoodLayoutMessage = view?.findViewById<TextView>(R.id.foodLayoutMessage)
            val toastView = view?.findViewById<ConstraintLayout>(R.id.toastView)

            productCountMap.let {
                tvTotalProductCount?.apply {
                    text = it.totalProductCount.toString()
                    setTextColor(ContextCompat.getColor(context, R.color.black90))

                    // Removing Toast colors for CNC / Dash toast
//                    it.quantityLimit?.foodLayoutColour?.let { color -> setTextColor(Color.parseColor(color)) }
                }
//                (toastView?.background as GradientDrawable).setColor(Color.parseColor(it.quantityLimit?.foodLayoutColour))
                // Removing Toast colors for CNC / Dash toast
                (toastView?.background as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.black90))
                tvFoodLayoutMessage?.text =
                if(KotlinUtils.isDeliveryOptionDash())
                    context.getString(R.string.dash_item_limit_message, it.quantityLimit?.foodMaximumQuantity ?: 0)
                else
                    it.quantityLimit?.foodLayoutMessage ?: ""

                tvNoOfItemsAddedToCart?.text = context.resources.getQuantityString(R.plurals.toast_item_added_to_cart_message, count, count)
            }

            // View button on toast
            val viewCart = view?.findViewById<TextView>(R.id.viewCart)
            viewCart?.visibility = if (viewButtonVisible) VISIBLE else INVISIBLE
            viewCart?.setOnClickListener {
                ScreenManager.presentShoppingCart(activity)
                popupWindow.dismiss() // dismiss the window
            }

            popupWindow.isFocusable = false
            popupWindow.showAtLocation(
                viewLocation,
                Gravity.BOTTOM,
                0,
                convertDpToPixel(getDeviceHeight(activity), context)
            )
            Handler(Looper.getMainLooper()).postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())

            return popupWindow
        }

        fun liveChatHeadUpNotificationWindow(
            viewLocation: View?,
            activity: Activity?,
            sendMessageResponse: SendMessageResponse?
        ): PopupWindow {
            val context = WoolworthsApplication.getAppContext()
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val view = inflater?.inflate(R.layout.items_live_chat_head_up_notification, null)
            val notificationTitleTextView =
                view?.findViewById<TextView>(R.id.notificationTitleTextView)
            val notificationDescTextView =
                view?.findViewById<TextView>(R.id.notificationDescTextView)
            val toastContainerConstraintLayout =
                view?.findViewById<ConstraintLayout>(R.id.toastView)
            when (sendMessageResponse?.sessionState) {
                SessionStateType.DISCONNECT -> {
                    notificationTitleTextView?.text =
                        bindString(R.string.chat_notification_ended_by_agent)
                    notificationDescTextView?.text = sendMessageResponse?.content ?: ""
                    notificationDescTextView?.visibility = GONE
                }
                else -> {
                    notificationTitleTextView?.text = bindString(R.string.chat_notification_title)
                    notificationDescTextView?.text = sendMessageResponse?.content
                    notificationDescTextView?.visibility = VISIBLE
                }
            }
            val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true
            )

            val receiverManager = activity?.let { ReceiverManager.init(it) }

            val mToastNotifier = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    popupWindow.dismiss() // dismiss the window
                }
            }

            toastContainerConstraintLayout?.setOnClickListener {
                activity ?: return@setOnClickListener
                LiveChatDBRepository().resetUnReadMessageCount()
                activity.sendBroadcast(Intent(LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE))
                activity.startActivity(Intent(activity, WChatActivity::class.java))
                popupWindow.dismiss() // dismiss the window
            }
            popupWindow.isFocusable = false
            GlobalScope.doAfterDelay(POPUP_3000_DELAY_MILLIS) {
                try{
                    popupWindow.dismiss() // dismiss the window
                }
                catch (e: Exception){
                    FirebaseManager.logException("popupWindow already dismissed : $e")
                }
            }

            popupWindow.showAtLocation(
                viewLocation,
                Gravity.TOP,
                0,
                16
            )

            if (receiverManager?.isReceiverRegistered(mToastNotifier) == false) {
                receiverManager.registerReceiver(
                    mToastNotifier,
                    IntentFilter(LIVE_CHAT_TOAST)
                )
            }

            popupWindow.setOnDismissListener {
                if (receiverManager?.isReceiverRegistered(mToastNotifier) == true)
                    receiverManager.unregisterReceiver(mToastNotifier)
            }

            return popupWindow
        }
    }
}