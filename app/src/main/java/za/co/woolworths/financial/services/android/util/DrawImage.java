package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class DrawImage {

	private Context mContext;

	public DrawImage(Context context) {
		mContext = context;
	}

	public void displayImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl))
						.build();
				DraweeController controller = Fresco.newDraweeControllerBuilder()
						.setOldController(image.getController())
						.setImageRequest(request)
						.build();
				image.setController(controller);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	public void displaySmallImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
				builder.setActualImageScaleType(ScalingUtils.ScaleType.FIT_END);
				GenericDraweeHierarchy hierarchy = builder
						.build();

				if (!TextUtils.isEmpty(imgUrl)) {
					int lastIndex = imgUrl.lastIndexOf('/') + 1;
					String path = imgUrl.substring(0, lastIndex);
					String params = imgUrl.substring(lastIndex, imgUrl.length());
					String encodedParams = android.net.Uri.encode(params, "UTF-8");
					imgUrl = path + encodedParams;
				}

				ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl))
						.build();
				DraweeController controller = Fresco.newDraweeControllerBuilder()
						.setOldController(image.getController())
						.setImageRequest(request)
						.build();
				image.setHierarchy(hierarchy);
				image.setController(controller);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	public void widthDisplayImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl))
						.build();
				DraweeController controller = Fresco.newDraweeControllerBuilder()
						.setOldController(image.getController())
						.setImageRequest(request)
						.build();

				image.setController(controller);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	public void widthDisplayImage(final SimpleDraweeView simperDrawerView, Uri fileUri) {
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
		GenericDraweeHierarchy hierarchy = builder
				.build();
		ImageRequest requestBuilder = ImageRequestBuilder.newBuilderWithSource(fileUri)
				.setProgressiveRenderingEnabled(false)
				.build();

		DraweeController controller = Fresco.newDraweeControllerBuilder()
				.setImageRequest(requestBuilder)
				.build();

		simperDrawerView.setHierarchy(hierarchy);
		simperDrawerView.setController(controller);

	}
}
