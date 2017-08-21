package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import za.co.woolworths.financial.services.android.util.DrawImage;

public class ProductViewPagerAdapter extends PagerAdapter {

    public interface MultipleImageInterface {
        void SelectedImage(int position,View view);
    }
    private MultipleImageInterface multipleImageInterface;

    private final Context mContext;
    private List<String> mViewPager;

    public ProductViewPagerAdapter(Context mContext, List<String> mViewPager,
                                   MultipleImageInterface multipleImageInterface) {
        this.mContext = mContext;
        this.multipleImageInterface = multipleImageInterface;
        this.mViewPager = mViewPager;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, final int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.product_view,
                collection, false);
        String image = mViewPager.get(position);
        Log.e("imageSize",image);
        SimpleDraweeView mProductImage = (SimpleDraweeView) v.findViewById(R.id.imProductView);
        DrawImage drawImage = new DrawImage(mContext);
        drawImage.displayImage(mProductImage, image);
        collection.addView(v, 0);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("imageSelector","rear");
                multipleImageInterface.SelectedImage(position,v);
            }
        });
        return v;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mViewPager.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
