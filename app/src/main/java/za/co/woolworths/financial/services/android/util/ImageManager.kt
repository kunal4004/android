package za.co.woolworths.financial.services.android.util

import android.view.View
import android.widget.ImageView
import com.awfs.coordination.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.Target

class ImageManager {

    companion object {
        fun setPicture(productImage: ImageView?, img_location: String) = productImage?.let { image ->
            productImage.context?.apply {
                Glide.with(this)
                        .load(img_location)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .placeholder(R.drawable.woolworth_logo_icon)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .fitCenter()
                        .dontAnimate()
                        .into(image)
            }
        }

        fun setPictureWithoutPlaceHolder(productImage: ImageView?, img_location: String) = productImage?.let { image ->
            productImage.visibility = if (img_location.isEmpty()) View.GONE else View.VISIBLE
            productImage.context?.apply {
                Glide.with(this)
                        .load(img_location)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .fitCenter()
                        .dontAnimate()
                        .into(image)
            }
        }

        fun setPictureCenterInside(productImage: ImageView?, img_location: String) = productImage?.let { image ->
            productImage.context?.apply {
                Glide.with(this)
                        .load(img_location)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .centerInside()
                        .dontAnimate()
                        .into(image)
            }
        }
    }
}