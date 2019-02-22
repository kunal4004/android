package za.co.woolworths.financial.services.android.ui.views

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.awfs.coordination.R
import com.google.gson.JsonParser
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToShoppingListFragment
import android.util.DisplayMetrics
import com.google.gson.JsonArray
import com.google.gson.JsonObject


class ToastFactory {

    companion object {
        private const val POPUP_DELAY_MILLIS = 3000

        fun buildShoppingListToast(viewLocation: View, buttonIsVisible: Boolean, data: Intent?, toastInterface: IToastInterface): PopupWindow? {
            val context = WoolworthsApplication.getAppContext()

            val shoppingList = data?.getStringExtra(AddToShoppingListFragment.POST_ADD_TO_SHOPPING_LIST)
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
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val layout = inflater?.inflate(R.layout.add_to_cart_success, null)
            // set the custom display
            val tvButtonClick = layout?.findViewById<WTextView>(R.id.tvView)
            val tvBoldTitle = layout?.findViewById<WTextView>(R.id.tvCart)
            val tvAddedTo = layout?.findViewById<WTextView>(R.id.tvAddToCart)
            // initialize your popupWindow and use your custom layout as the view
            val popupWindow = PopupWindow(layout,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true)

            tvButtonClick?.visibility = if (buttonIsVisible) View.VISIBLE else View.GONE
            tvBoldTitle?.visibility = View.VISIBLE
            tvAddedTo?.setAllCaps(false)

            shoppingListArray?.let { tvBoldTitle?.setText(context.getString(R.string.shopping_list).plus(if (it.size() > 1) "s" else "")) }
            shoppingListObject?.let { tvBoldTitle?.setText(context.getString(R.string.shopping_list).plus(if (it.size() > 1) "s" else "")) }

            // handle popupWindow click event
            tvButtonClick?.setOnClickListener {
                toastInterface.onToastButtonClicked(shoppingListObject ?: shoppingListArray)
                popupWindow.dismiss() // dismiss the window
            }

            // dismiss the popup window after 3sec
            Handler().postDelayed({ popupWindow.dismiss() }, POPUP_DELAY_MILLIS.toLong())
            popupWindow.showAtLocation(viewLocation, Gravity.BOTTOM, 0, convertDpToPixel(75f, context))
            popupWindow.isOutsideTouchable = true
            return popupWindow
        }

        private fun convertDpToPixel(dp: Float, context: Context): Int {
            return (dp * (context.resources?.displayMetrics?.densityDpi?.toFloat()?.div(DisplayMetrics.DENSITY_DEFAULT)!!)).toInt()
        }
    }
}