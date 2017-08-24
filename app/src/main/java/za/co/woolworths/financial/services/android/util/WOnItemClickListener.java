package za.co.woolworths.financial.services.android.util;


import com.daimajia.swipe.SwipeLayout;

public interface WOnItemClickListener {
    void onItemClick(String productId,int section);
    void onSwipeListener(int index, SwipeLayout layout);
    void onDelete(String productId);
}
