package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

/**
 * Created by W7099877 on 21/11/2016.
 */

public class MyAccountsCardsAdapter extends PagerAdapter {
    public Activity mContext;
    public MyAccountsCardsAdapter(Activity context)
    {
        this.mContext=context;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View cView=mContext.getLayoutInflater().inflate(R.layout.my_accounts_cards_pager_item,container,false);

        container.addView(cView);
        return cView;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public float getPageWidth(int position) {
        return 1f;
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
