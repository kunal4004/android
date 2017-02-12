package za.co.woolworths.financial.services.android.util;

import android.content.Context;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class DrawImage {

    private Context mContext;

    public DrawImage(Context context) {
        mContext = context;
    }

    public void displayImage(ImageView imageView, String url) {
        Glide.with(mContext)
                .load(url)
                .dontAnimate()
                .centerCrop()
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }
}
