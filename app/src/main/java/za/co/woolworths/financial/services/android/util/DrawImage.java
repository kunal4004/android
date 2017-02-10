package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.awfs.coordination.R;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class DrawImage {

    private Context mContext;

    public DrawImage(Context context) {
        mContext = context;
    }

    public void setupImage(final SimpleDraweeView simpleDraweeView, final String uri) {

        if (TextUtils.isEmpty(uri))
            return;

        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if (imageInfo == null) {
                    return;
                }
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received! " +
                                "Size %d x %d",
                        "Quality level %d, good enough: %s, full quality: %s",
                        imageInfo.getWidth(),
                        imageInfo.getHeight(),
                        qualityInfo.getQuality(),
                        qualityInfo.isOfGoodEnoughQuality(),
                        qualityInfo.isOfFullQuality());
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                FLog.e(getClass(), throwable, "Error loading %s", id);
                //Toast.makeText(mContext,"Error loading, id = "+id,Toast.LENGTH_LONG).show();
            }
        };

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                .setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(new ResizeOptions(450, 800))
                .setProgressiveRenderingEnabled(false)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(controllerListener)
                .setTapToRetryEnabled(true)
                .build();
        simpleDraweeView.setController(controller);

        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
        GenericDraweeHierarchy hierarchy = builder
                .setFailureImage(ContextCompat.getDrawable(mContext, R.drawable.rectangle), ScalingUtils.ScaleType.CENTER)
                .setRetryImage(ContextCompat.getDrawable(mContext, R.drawable.rectangle), ScalingUtils.ScaleType.CENTER)
                //.setProgressBarImage(new ProgressBarDrawable())
                .setPlaceholderImage(ContextCompat.getDrawable(mContext, R.drawable.rectangle))
                .build();
        simpleDraweeView.setHierarchy(hierarchy);
    }

    public void placeholderSetupImage(final SimpleDraweeView simpleDraweeView, final String uri) {

        if (TextUtils.isEmpty(uri))
            return;

        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if (imageInfo == null) {
                    return;
                }
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received! " +
                                "Size %d x %d",
                        "Quality level %d, good enough: %s, full quality: %s",
                        imageInfo.getWidth(),
                        imageInfo.getHeight(),
                        qualityInfo.getQuality(),
                        qualityInfo.isOfGoodEnoughQuality(),
                        qualityInfo.isOfFullQuality());
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                FLog.e(getClass(), throwable, "Error loading %s", id);
                //Toast.makeText(mContext,"Error loading, id = "+id,Toast.LENGTH_LONG).show();
            }
        };

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                .setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(new ResizeOptions(450, 800))
                .setProgressiveRenderingEnabled(false)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setControllerListener(controllerListener)
                .setTapToRetryEnabled(true)
                .build();
        simpleDraweeView.setController(controller);

        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
        GenericDraweeHierarchy hierarchy = builder
                .setFailureImage(ContextCompat.getDrawable(mContext, R.drawable.rectangle), ScalingUtils.ScaleType.CENTER)
                .setRetryImage(ContextCompat.getDrawable(mContext, R.drawable.rectangle), ScalingUtils.ScaleType.CENTER)
                //.setProgressBarImage(new ProgressBarDrawable())
                //.setPlaceholderImage(ContextCompat.getDrawable(mContext, R.drawable.rectangle))
                .build();
        simpleDraweeView.setHierarchy(hierarchy);
    }
}
