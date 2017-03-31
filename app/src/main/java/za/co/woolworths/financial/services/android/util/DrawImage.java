package za.co.woolworths.financial.services.android.util;

import android.content.Context;

import android.graphics.drawable.Animatable;
import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
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
                // .setResizeOptions(new ResizeOptions(600, 120))
                .setProgressiveRenderingEnabled(true)
                .build();

        ControllerListener<ImageInfo> contollerListener = new BaseControllerListener() {

            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo != null) {
                    updateViewSize(imageInfo, simperDrawerView);
                }
            }
        };

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(contollerListener)
                .setImageRequest(requestBuilder)
                .build();

        simperDrawerView.setHierarchy(hierarchy);
        simperDrawerView.setController(controller);

    }

    private void updateViewSize(ImageInfo imageinfo, SimpleDraweeView simpleDraweeView) {
        //this is my own implementation of changing simple-drawee-view height
        // you canhave yours using imageinfo.getHeight() or imageinfo.getWidth();
        simpleDraweeView.getLayoutParams().height = imageinfo.getHeight();
        // don't forget to call this method. thanks to @plamenko for reminding me.
        simpleDraweeView.requestLayout();
    }
}
