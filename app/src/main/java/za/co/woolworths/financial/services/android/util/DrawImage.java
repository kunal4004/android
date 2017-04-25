package za.co.woolworths.financial.services.android.util;

import android.content.Context;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.Log;

import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import za.co.woolworths.financial.services.android.util.animation.zoomable.ZoomableDraweeView;

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
                .setProgressiveRenderingEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(requestBuilder)
                .build();

        simperDrawerView.setHierarchy(hierarchy);
        simperDrawerView.setController(controller);

    }

    public void widthDisplayImage(final ZoomableDraweeView simperDrawerView, Uri fileUri) {
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());

        GenericDraweeHierarchy hierarchy = builder
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                .setPlaceholderImage(R.drawable.rectangle)
                .setPlaceholderImageScaleType(ScalingUtils.ScaleType.CENTER)
                .build();

        ImageRequest requestBuilder = ImageRequestBuilder.newBuilderWithSource(fileUri)
                .setProgressiveRenderingEnabled(true)
                .setImageDecodeOptions(ImageDecodeOptions.defaults())
                .setLocalThumbnailPreviewsEnabled(true)
                .setRequestPriority(Priority.HIGH)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(new ControllerListener<ImageInfo>() {
                    @Override
                    public void onSubmit(String id, Object callerContext) {

                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        //updateViewSize(imageInfo, simperDrawerView);
                        Log.e("imageInfo", String.valueOf(imageInfo.getWidth()) + " "
                                + String.valueOf(imageInfo.getHeight()));
                    }

                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {

                    }

                    @Override
                    public void onRelease(String id) {

                    }
                })
                .setImageRequest(requestBuilder)
                .build();

        simperDrawerView.setHierarchy(hierarchy);
        simperDrawerView.setController(controller);

    }
}
