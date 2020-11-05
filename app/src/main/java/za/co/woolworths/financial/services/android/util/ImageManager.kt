package za.co.woolworths.financial.services.android.util

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.awfs.coordination.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ImageManager {

    companion object {
        fun setPicture(productImage: ImageView?, img_location: String) = productImage?.let { image ->
            productImage.scaleType = ImageView.ScaleType.CENTER
            productImage.context?.apply {
                Glide.with(this)
                        .load(img_location)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>?, p3: Boolean): Boolean {

                                return false
                            }
                            override fun onResourceReady(drawable: Drawable?, p1: Any?, p2: Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean {
                                productImage.adjustViewBounds = true
                                return false
                            }
                        })
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

        fun setPictureOverrideWidthHeight(productImage: ImageView?, img_location: String,width:Int, height: Int) = productImage?.let { image ->
            productImage.visibility = if (img_location.isEmpty()) View.GONE else View.VISIBLE
            productImage.context?.apply {
                Glide.with(this)
                    .load(img_location)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>?, p3: Boolean): Boolean {
                            return false
                        }
                        override fun onResourceReady(drawable: Drawable?, p1: Any?, p2: Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean {
                            productImage.adjustViewBounds = true
                            return false
                        }
                    })
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(width, height)
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

        fun loadImage(productImage: ImageView?, img_location: String) = productImage?.let { image ->
            productImage.visibility = if (img_location.isEmpty()) View.GONE else View.VISIBLE
            productImage.context?.apply {
                Glide.with(this)
                        .load(img_location)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .dontAnimate()
                        .into(image)
            }
        }
    }
}