package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

/**
 * Created by W7099877 on 21/11/2016.
 */

public class MyAccountsCardsAdapter extends PagerAdapter {
    public Activity mContext;
    int[] cards={R.drawable.w_store_card,R.drawable.w_credi_card,R.drawable.w_personal_loan_card};
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
        ImageView myaccountsCard=(ImageView)cView.findViewById(R.id.myaccountsCard);
        myaccountsCard.setBackgroundResource(cards[position]);
        container.addView(cView);
        return cView;
    }

    @Override
    public int getCount() {
        return cards.length;
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
