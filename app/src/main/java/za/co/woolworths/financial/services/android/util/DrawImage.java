package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;

import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.IOException;

import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;

public class DrawImage {

	private Context mContext;

	public DrawImage(Context context) {
		mContext = context;
	}

	public void displayImage(final WrapContentDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			image.setLegacyVisibilityHandlingEnabled(true);
			try {

				imgUrl = android.net.Uri.encode(imgUrl, "@#&=*+-_.,:!?()/~'%");

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

				imgUrl = android.net.Uri.encode(imgUrl, "@#&=*+-_.,:!?()/~'%");

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


	public void handleGIFImage(SimpleDraweeView simpleDraweeView) throws IOException {
		ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.raw.calc).build();
		DraweeController controller = Fresco.newDraweeControllerBuilder()
				.setUri(imageRequest.getSourceUri())
				.setAutoPlayAnimations(true)
				.build();
		simpleDraweeView.setController(controller);
	}
}
