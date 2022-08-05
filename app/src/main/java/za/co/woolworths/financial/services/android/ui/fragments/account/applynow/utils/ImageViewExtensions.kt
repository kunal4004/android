package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou


fun ImageView.loadSvg(url: String?) {
    GlideToVectorYou
        .init()
        .with(context)
        .load(Uri.parse(url), this);

}
fun ImageView.load(url: String?) {
    Glide.with(context).load(url).into(this)
}
