package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.ArrayList;

/**
 * Created by W7099877 on 21/11/2016.
 */

public class MyAccountsCardsAdapter extends PagerAdapter {
    public Activity mContext;
    ArrayList<Integer> cards;

    public MyAccountsCardsAdapter(Activity context, ArrayList<Integer> cards) {
        this.mContext = context;
        this.cards = cards;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View cView = mContext.getLayoutInflater().inflate(R.layout.my_accounts_cards_pager_item, container, false);
        ImageView myaccountsCard = (ImageView) cView.findViewById(R.id.myaccountsCard);
        myaccountsCard.setImageResource(cards.get(position));
        container.addView(cView);
        return cView;
    }

    @Override
    public int getCount() {
        return cards.size();
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
