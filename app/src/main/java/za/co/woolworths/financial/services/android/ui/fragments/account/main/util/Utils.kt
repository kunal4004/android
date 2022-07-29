package za.co.woolworths.financial.services.android.ui.fragments.account.main.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ActivityCompat.startActivityForResult
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import java.io.Serializable


fun Intent.putExtras(vararg params: Pair<String, Any?>): Intent {
    if (params.isEmpty()) return this
    params.forEach { (key, value) ->
        when (value) {
            is Int -> putExtra(key, value)
            is Byte -> putExtra(key, value)
            is Char -> putExtra(key, value)
            is Long -> putExtra(key, value)
            is Float -> putExtra(key, value)
            is Short -> putExtra(key, value)
            is Double -> putExtra(key, value)
            is Boolean -> putExtra(key, value)
            is Bundle -> putExtra(key, value)
            is String -> putExtra(key, value)
            is IntArray -> putExtra(key, value)
            is ByteArray -> putExtra(key, value)
            is CharArray -> putExtra(key, value)
            is LongArray -> putExtra(key, value)
            is FloatArray -> putExtra(key, value)
            is Parcelable -> putExtra(key, value)
            is ShortArray -> putExtra(key, value)
            is DoubleArray -> putExtra(key, value)
            is BooleanArray -> putExtra(key, value)
            is CharSequence -> putExtra(key, value)
            is Array<*> -> {
                when {
                    value.isArrayOf<String>() ->
                        putExtra(key, value as Array<String?>)
                    value.isArrayOf<Parcelable>() ->
                        putExtra(key, value as Array<Parcelable?>)
                    value.isArrayOf<CharSequence>() ->
                        putExtra(key, value as Array<CharSequence?>)
                    else -> putExtra(key, value)
                }
            }
            is Serializable -> putExtra(key, value)
        }
    }
    return this
}
inline fun <reified T : Activity> Context.openActivity(vararg params: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java)
    intent.putExtras(*params)
    this.startActivity(intent)
}

inline fun <reified T : Activity> Context.openActivityForResult(vararg params: Pair<String, Any?>,requestCode:Int = 0) {
    val intent = Intent(this, T::class.java)
    intent.putExtras(*params)
    startActivityForResult((this as Activity),intent,requestCode,null)
}

/**
 * targetedShimmerLayout is the layout which we want to add the shimmer over it
 * shimmerContainer is the layout that contains the shimmerFrame as child
 * */
fun ShimmerFrameLayout.loadingState(state: Boolean,targetedShimmerLayout:View? = null,shimmerContainer:View? = null){
        when (state) {
            true -> {
                shimmerContainer?.visibility = VISIBLE
                this.visibility = VISIBLE
                targetedShimmerLayout?.visibility = GONE
                val shimmer = Shimmer.AlphaHighlightBuilder().build()
                this.setShimmer(shimmer)
                this.startShimmer()
            }
            false -> {
                shimmerContainer?.visibility = GONE
                this.visibility = GONE
                targetedShimmerLayout?.visibility = VISIBLE
                this.stopShimmer()
                this.setShimmer(null)
            }
    }
}

fun ShimmerFrameLayout.loadingState(state: Boolean){
    when (state) {
        true -> {
            val shimmer = Shimmer.AlphaHighlightBuilder().build()
            this.setShimmer(shimmer)
            this.startShimmer()
        }
        false -> {
            this.stopShimmer()
            this.setShimmer(null)
        }
    }
}