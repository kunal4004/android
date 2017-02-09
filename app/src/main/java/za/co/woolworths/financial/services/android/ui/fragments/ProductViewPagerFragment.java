package za.co.woolworths.financial.services.android.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class ProductViewPagerFragment extends Fragment {

    public static String KEY_IMAGE_URL = "image_url";
    private String mImageUrl;

    public ProductViewPagerFragment() {
    }

    public static ProductViewPagerFragment newInstance(String text) {
        ProductViewPagerFragment f = new ProductViewPagerFragment();
        Bundle b = new Bundle();
        b.putString(KEY_IMAGE_URL, text);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mImageUrl = getArguments().getString(KEY_IMAGE_URL);
        return inflater.inflate(R.layout.product_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SimpleDraweeView mProductImage = (SimpleDraweeView) view.findViewById(R.id.imProductView);
        Log.e("productImagexx", mImageUrl);
        // setupImage(mProductImage,mImageUrl);
    }

    private void setupImage(final SimpleDraweeView simpleDraweeView, final String uri) {
        simpleDraweeView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                simpleDraweeView.getViewTreeObserver().removeOnPreDrawListener(this);
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        //.setResizeOptions(new ResizeOptions(simpleDraweeView.getWidth(), simpleDraweeView.getHeight()))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(simpleDraweeView.getController())
                        .setImageRequest(request)
                        .build();

                simpleDraweeView.setController(controller);
                simpleDraweeView.setImageURI(uri);
                return true;
            }
        });
    }


}
