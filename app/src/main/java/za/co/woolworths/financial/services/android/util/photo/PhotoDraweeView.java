package za.co.woolworths.financial.services.android.util.photo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

public class PhotoDraweeView extends SimpleDraweeView implements IAttacher, OnScaleInterface {

    private Attacher mAttacher;
    private boolean mEnableDraweeMatrix = true;
    private float permScaleFactor;

    public PhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public PhotoDraweeView(Context context) {
        super(context);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        if (mAttacher == null || mAttacher.getDraweeView() == null) {
            mAttacher = new Attacher(this, this);
        }
    }

    public Attacher getAttacher() {
        return mAttacher;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        if (mEnableDraweeMatrix) {
            canvas.concat(mAttacher.getDrawMatrix());
        }
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Override
    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Override
    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setOrientation(@Attacher.OrientationMode int orientation) {
        mAttacher.setOrientation(orientation);
    }

    @Override
    public void setZoomTransitionDuration(long duration) {
        mAttacher.setZoomTransitionDuration(duration);
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener listener) {
        mAttacher.setOnDoubleTapListener(listener);
    }

    @Override
    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mAttacher.setOnScaleChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        mAttacher.setOnLongClickListener(listener);
    }

    @Override
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mAttacher.getOnPhotoTapListener();
    }

    @Override
    public OnViewTapListener getOnViewTapListener() {
        return mAttacher.getOnViewTapListener();
    }

    @Override
    public void update(int imageInfoWidth, int imageInfoHeight) {
        mAttacher.update(imageInfoWidth, imageInfoHeight);
    }

    public boolean isEnableDraweeMatrix() {
        return mEnableDraweeMatrix;
    }

    public void setEnableDraweeMatrix(boolean enableDraweeMatrix) {
        mEnableDraweeMatrix = enableDraweeMatrix;
    }

    public void setPhotoUri(Uri uri, PhotoDraweeView mProductImage) {
        setPhotoUri(mProductImage, uri, null);
    }

    public void setPhotoUri(final PhotoDraweeView mProductImage, final Uri uri, @Nullable final Context context) {
        mEnableDraweeMatrix = false;

        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder
                (mProductImage.getContext().getResources());
        final GenericDraweeHierarchy hierarchy = builder
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER)
                .setFadeDuration(0)
                .build();

        Fresco.getImagePipeline().evictFromMemoryCache(uri);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setCallerContext(context)
                .setAutoPlayAnimations(true)
                .setUri(uri)
                .setOldController(getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        super.onFailure(id, throwable);
                        mEnableDraweeMatrix = false;
                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo,
                                                Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        mEnableDraweeMatrix = true;
                        init();
                        if (imageInfo != null) {
                            mAttacher.resetMatrix();
                            int width = imageInfo.getWidth();
                            int height = imageInfo.getHeight();
                            update(width, height);
                            if (width >= height) {
                                switch (getResources().getDisplayMetrics().densityDpi) {

                                    case DisplayMetrics.DENSITY_MEDIUM:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.27);
                                        mProductImage.setMinimumScale((float) 1.27);
                                        break;
                                    case DisplayMetrics.DENSITY_HIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.27);
                                        mProductImage.setMinimumScale((float) 1.27);
                                        break;
                                    case DisplayMetrics.DENSITY_XHIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.27);
                                        mProductImage.setMinimumScale((float) 1.27);
                                        break;
                                    case DisplayMetrics.DENSITY_XXHIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.27);
                                        mProductImage.setMinimumScale((float) 1.27);
                                        break;
                                    case DisplayMetrics.DENSITY_XXXHIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.27);
                                        mProductImage.setMinimumScale((float) 1.27);
                                        break;
                                    default:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.27);
                                        mProductImage.setMinimumScale((float) 1.27);
                                        break;
                                }
                            } else {
                                switch (getResources().getDisplayMetrics().densityDpi) {
                                    case DisplayMetrics.DENSITY_MEDIUM:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.3855);
                                        mProductImage.setMinimumScale((float) 1.3855);
                                        break;
                                    case DisplayMetrics.DENSITY_HIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.315);
                                        mProductImage.setMinimumScale((float) 1.315);
                                        break;
                                    case DisplayMetrics.DENSITY_XHIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.205);
                                        mProductImage.setMinimumScale((float) 1.205);
                                        break;
                                    case DisplayMetrics.DENSITY_XXHIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.335);
                                        mProductImage.setMinimumScale((float) 1.335);
                                        break;

                                    case DisplayMetrics.DENSITY_XXXHIGH:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.4);
                                        mProductImage.setMinimumScale((float) 1.4);
                                        break;

                                    default:
                                        mProductImage.setZoomTransitionDuration(800);
                                        mProductImage.setScale((float) 1.305);
                                        mProductImage.setMinimumScale((float) 1.305);
                                        break;
                                }
                            }
                            mProductImage.setMaximumScale((float) 6.0);
                        }
                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {
                        super.onIntermediateImageFailed(id, throwable);
                        mEnableDraweeMatrix = false;
                    }

                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        super.onIntermediateImageSet(id, imageInfo);
                        mEnableDraweeMatrix = true;
                        if (imageInfo != null) {
                            update(imageInfo.getWidth(), imageInfo.getHeight());
                        }
                    }
                })
                .build();
        setHierarchy(hierarchy);
        setController(controller);
    }


    @Override
    public float onScale(float scaleFactor) {
        setPermScaleFactor(scaleFactor);
        return scaleFactor;
    }

    public float getPermScaleFactor() {
        return permScaleFactor;
    }

    public void setPermScaleFactor(float permScaleFactor) {
        this.permScaleFactor = permScaleFactor;
    }
}
