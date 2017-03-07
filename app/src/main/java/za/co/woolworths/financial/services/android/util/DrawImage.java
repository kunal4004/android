package za.co.woolworths.financial.services.android.util;

import android.content.Context;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

public class DrawImage {

    private Context mContext;

    public DrawImage(Context context) {
        mContext = context;
    }

    public void displayImage(final ImageView imageView, String url) {
        Glide.with(mContext)
                .load(url)
                .asBitmap()
                .atMost()
                .placeholder(R.drawable.rectangle)
                .override(500, 500)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                        // do something with the bitmap
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }

    public void displayThumbnailImage(final ImageView imageView, String url) {
        Glide.with(mContext)
                .load(url)
                .asBitmap()
                .atMost()
                .thumbnail(1.0f)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                        // do something with the bitmap
                        imageView.setImageBitmap(bitmap);
                        imageView.setAdjustViewBounds(true);
                    }
                });
    }

    public void widthDisplayImage(ImageView imageView, String url) {
        Glide.with(mContext)
                .load(url)
                .dontAnimate()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }


}
