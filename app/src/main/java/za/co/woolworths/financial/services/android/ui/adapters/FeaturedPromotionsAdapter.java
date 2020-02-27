package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.os.Parcelable;

import androidx.viewpager.widget.PagerAdapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.Promotion;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment;
import za.co.woolworths.financial.services.android.util.ImageManager;

public class FeaturedPromotionsAdapter extends PagerAdapter {
    public Activity mContext;
    private List<Promotion> promotions;
    private BottomNavigator mBottomNavigator;

    public FeaturedPromotionsAdapter(Activity mContext, List<Promotion> promotions) {
        this.mContext = mContext;
        this.promotions = promotions;
        this.mBottomNavigator = (BottomNavigator) mContext;
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
    public Object instantiateItem(ViewGroup container, final int position) {
        View cView = mContext.getLayoutInflater().inflate(R.layout.featured_promotion_list_item, container, false);
        final ImageView promotionImage = cView.findViewById(R.id.promotionImage);
        container.addView(cView);

        if (TextUtils.isEmpty(promotions.get(position).image)) return cView;
        String featurePromotionImageUrl = android.net.Uri.encode(promotions.get(position).image, "@#&=*+-_.,:!?()/~'%");
        ImageManager.Companion.loadImage(promotionImage, featurePromotionImageUrl);

        cView.setOnClickListener(v -> {
            mBottomNavigator.setSelectedIconPosition(BottomNavigationActivity.INDEX_ACCOUNT);
            mBottomNavigator.pushFragment(ProductListingFragment.Companion.newInstance(ProductsRequestParams.SearchType.NAVIGATE, promotionImage.getContext().getResources().getString(R.string.featured_promotions), promotions.get(position).path));
        });
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
