package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class WrapContentDraweeView extends SimpleDraweeView {

	private boolean resizeImage = false;

	// we set a listener and update the view's aspect ratio depending on the loaded image
	private final ControllerListener listener = new BaseControllerListener<ImageInfo>() {
		@Override
		public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
			updateViewSize(imageInfo);
		}

		@Override
		public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
			updateViewSize(imageInfo);
		}
	};

	public WrapContentDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
		super(context, hierarchy);
	}

	public WrapContentDraweeView(Context context) {
		super(context);
	}

	public WrapContentDraweeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void setResizeImage(boolean resizeImage) {
		this.resizeImage = resizeImage;
	}

	@Override
	public void setImageURI(Uri uri, Object callerContext) {


		ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
				.build();

		setController(
				Fresco.newDraweeControllerBuilder()
						.setControllerListener(listener)
						.setUri(uri)
						.setCallerContext(callerContext)
						.setOldController(getController())
						.setImageRequest(request)
						.build());
	}

	void updateViewSize(@Nullable ImageInfo imageInfo) {
		if (imageInfo != null) {
			int imageInfoHeight = imageInfo.getHeight();
			int imageInfoWidth = imageInfo.getWidth();
			if (resizeImage) {
				if (imageInfoHeight > imageInfoWidth) {
					setAspectRatio((float) (imageInfoWidth - (imageInfo.getHeight() - imageInfo.getWidth())) / imageInfoHeight);
				} else {
					setAspectRatio((float) (imageInfoWidth / imageInfoHeight));
				}
			} else {
				setAspectRatio((float) (imageInfoWidth / imageInfoHeight));
			}
		}
	}
}