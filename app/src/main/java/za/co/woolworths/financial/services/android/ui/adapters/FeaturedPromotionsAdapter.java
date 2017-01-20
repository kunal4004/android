package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Promotion;

/**
 * Created by W7099877 on 18/01/2017.
 */

public class FeaturedPromotionsAdapter extends PagerAdapter {
    public Activity mContext;
    public List<Promotion> promotions;
    public FeaturedPromotionsAdapter(Activity mContext, List<Promotion> promotions)
    {
        this.mContext=mContext;
        this.promotions=promotions;
    }
    @Override
    public int getCount() {
        return promotions.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

         return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View cView=mContext.getLayoutInflater().inflate(R.layout.featured_prmotion_list_item,container,false);
        SimpleDraweeView image=(SimpleDraweeView)cView.findViewById(R.id.promotionImage);
        image.setImageURI(promotions.get(position).image);
        container.addView(cView);
        return cView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
    @Override
    public float getPageWidth(int position) {
        return 0.96f;
    }
}
