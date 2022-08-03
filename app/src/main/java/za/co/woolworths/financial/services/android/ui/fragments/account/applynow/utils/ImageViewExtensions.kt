package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils

import android.widget.ImageView
import com.bumptech.glide.Glide


fun ImageView.loadSvg(url: String?) {
    Glide.with(context).load(url).dontTransform().into(this)
}
fun ImageView.load(url: String?) {
    Glide.with(context).load(url).into(this)
}
