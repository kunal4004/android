package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils

import android.net.Uri
import android.view.View
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

//TODO:: Move this function once View extension created
fun View.setContentDescription(mainID: String, position: Int? = null, viewName: UniqueIdentifiers) {
    val accessibilityID = mainID.lowercase().replace(" ", "_")
    val indexString = position?.let { "_$it" } ?: ""
    this.contentDescription = accessibilityID + "_" + viewName.value + indexString
}

enum class UniqueIdentifiers(val value: String){
    Title("title"),
    Image("image"),
    ArrowImage("arrow"),
    Description("description")
}