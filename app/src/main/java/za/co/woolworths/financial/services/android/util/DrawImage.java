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
//    Log.e("displayImage",url);
//        String imageRep = url.replace(" ","%20");
//        Glide.with(mContext)
//                .load(imageRep)
//                .dontAnimate()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .listener(new RequestListener<String, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        Log.e("IMAGE_EXCEPTION", "Exception " + e.toString());
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        return false;
//                    }
//                })
//                .into(imageView);

        Glide.with(mContext)
                .load(url)
                .asBitmap()
                .atMost()
                .placeholder(R.drawable.rectangle)
                .override(500, 500)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                        // do something with the bitmap
                        imageView.setImageBitmap(bitmap);
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
