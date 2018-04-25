package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class PicassoUtils {

	private Activity activity;

	public PicassoUtils(Activity activity) {
		this.activity = activity;
	}

	public Transformation picassoTransFormation(final ImageView image) {
		return new Transformation() {

			@Override
			public Bitmap transform(Bitmap source) {
				int targetWidth = image.getWidth();

				double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
				int targetHeight = (int) (targetWidth * aspectRatio);
				Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
				if (result != source) {
					// Same bitmap is returned if sizes are the same
					source.recycle();
				}
				return result;
			}

			@Override
			public String key() {
				return "transformation" + " desiredWidth";
			}
		};
	}

	public void loadImage(ImageView imageView, String url) {
		Picasso.with(activity)
				.load(url)
				.error(android.R.drawable.stat_notify_error)
				.transform(picassoTransFormation(imageView))
				.into(imageView, new Callback() {
					@Override
					public void onSuccess() {
						Log.e("Picasso", "onSuccess");
					}

					@Override
					public void onError() {
						Log.e("Picasso", "error");
					}
				});
	}
}
