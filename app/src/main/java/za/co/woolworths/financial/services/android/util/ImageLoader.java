package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

public class ImageLoader {

	public static class Builder {

		private Context mContext;
		private int imageWidth;
		private int imageViewHeight;
		private boolean isHeightScale = false;
		private String imageUrl;
		private ImageView imageView;

		public Builder() {
		}

		public Builder setContext(Context context) {
			this.mContext = context;
			return this;
		}

		/**
		 * imageViewWidth takes device width as default width
		 */
		public Builder setImageViewWidth(int imageViewWidth) {
			this.imageWidth = imageViewWidth;
			return this;
		}

		/**
		 * imageViewWidth takes device height as default height
		 */
		public Builder setImageViewHeight(int imageViewHeight) {
			this.imageViewHeight = imageViewHeight;
			return this;
		}

		/***
		 * Set to true will calculate aspect ratio base on width.
		 */
		public Builder isHeightScalable(boolean isHeightScale) {
			this.isHeightScale = isHeightScale;
			return this;
		}

		/***
		 * Represents the device ImageView
		 */
		public Builder into(ImageView imageView) {
			this.imageView = imageView;
			return this;
		}

		/***
		 * The image url
		 */
		public Builder setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}

		/***
		 * Takes device width by default,
		 * setImageWidth will override default value
		 */
		private int getImageWidth() {
			DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
			if (imageWidth == 0) {
				imageWidth = dpToPx(Math.round(displayMetrics.widthPixels / displayMetrics.density));

			}
			return imageWidth;
		}

		private int getImageViewHeight() {
			return getContext().getResources().getDimensionPixelOffset(R.dimen.two_hundred_and_twenty_one_dp);
		}

		private Context getContext() {
			return mContext;
		}

		private String getImageUrl() {
			return imageUrl;
		}

		private ImageView getImageView() {
			return imageView;
		}

		private void loadImage() {

			final ViewTreeObserver observer = getImageView().getViewTreeObserver();
			ViewTreeObserver vto = getImageView().getViewTreeObserver();
			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				public boolean onPreDraw() {
					setImageViewHeight(getImageView().getMeasuredHeight());
					setImageViewWidth(getImageView().getMeasuredWidth());
					return true;
				}
			});


			Transformation transformation = new Transformation() {

				@Override
				public Bitmap transform(Bitmap source) {
					Bitmap bitmap = resizeBitmapFitXY(getImageWidth(), getImageViewHeight(), source);
					return bitmap;

				}

				@Override
				public String key() {
					return "scaleRespectRatio" + getImageWidth() + getImageViewHeight();
				}
			};

			// set the tag to the view
			imageView.setTag(new Target() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

				}

				@Override
				public void onBitmapFailed(Exception e, Drawable errorDrawable) {

				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) {

				}
			});

			Picasso.get()
					.load(getImageUrl())
					.noFade()
					.transform(transformation)
					.into(imageView);
		}

		public ImageLoader load() {
			ImageLoader imageLoader = new ImageLoader();
			loadImage();
			return imageLoader;

		}

		private int dpToPx(int dp) {
			float density = mContext.getResources().getDisplayMetrics().density;
			return Math.round((float) dp * density);
		}

		private Bitmap resizeBitmapFitXY(int width, int height, Bitmap bitmap) {
			Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			float originalWidth = bitmap.getWidth(), originalHeight = bitmap.getHeight();
			Canvas canvas = new Canvas(background);
			float scale, xTranslation = 0.0f, yTranslation = 0.0f;


			if (originalWidth > originalHeight) {
				scale = height / originalHeight;
				xTranslation = (width - originalWidth * scale);
			} else {
				scale = width / originalWidth;
				yTranslation = (height - originalHeight * scale);
			}
			Matrix transformation = new Matrix();
			transformation.postTranslate(xTranslation, yTranslation);
			transformation.preScale(scale, scale);
			Paint paint = new Paint();
			paint.setFilterBitmap(true);
			canvas.drawBitmap(bitmap, transformation, paint);
			bitmap.recycle();
			return background;
		}
	}
}
