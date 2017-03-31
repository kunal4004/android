package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import za.co.woolworths.financial.services.android.util.DrawImage;

public class ProductViewPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private List<String> mViewPager;

    public ProductViewPagerAdapter(Context mContext, List<String> mViewPager) {
        this.mContext = mContext;
        this.mViewPager = mViewPager;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.product_view,
                collection, false);
        String image = mViewPager.get(position);
        SimpleDraweeView mProductImage = (SimpleDraweeView) v.findViewById(R.id.imProductView);
        DrawImage drawImage = new DrawImage(mContext);
        drawImage.displayImage(mProductImage, image);
        collection.addView(v, 0);
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
