package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 24/11/2016.
 */

public class TestViewpagerAdapter extends PagerAdapter
{
    public Activity mContext;
    public TestViewpagerAdapter(Activity context)
    {
        this.mContext=context;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }



    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View cView=mContext.getLayoutInflater().inflate(R.layout.test_storecard_layout,container,false);
        WTextView storeName=(WTextView)cView.findViewById(R.id.storeName);
        WTextView storeOfferings=(WTextView)cView.findViewById(R.id.offerings);
        WTextView storeDistance=(WTextView)cView.findViewById(R.id.distance);
        WTextView storeAddress=(WTextView)cView.findViewById(R.id.storeAddress);
        WTextView storeTimeing=(WTextView)cView.findViewById(R.id.timeing);
        storeName.setText("NEWLANDS");
        storeOfferings.setText("FOODS • CLOTHING • BEAUTY");
        storeDistance.setText("5");
        storeAddress.setText("Dean Saint Arcade Centre, Dean Street, Newlands, Cape Town, 7700");
        storeTimeing.setText("Open until 19h00");
        container.addView(cView);
        return cView;
    }



    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public float getPageWidth(int position) {
        return 0.92f;
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}