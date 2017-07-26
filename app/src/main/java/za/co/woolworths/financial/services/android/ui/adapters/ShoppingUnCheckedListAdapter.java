package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WOnItemClickListener;

public class ShoppingUnCheckedListAdapter extends RecyclerSwipeAdapter<ShoppingUnCheckedListAdapter.SimpleViewHolder> {
    private List<ShoppingList> mShoppingList;

    private WOnItemClickListener wOnItemClickListener;

    public ShoppingUnCheckedListAdapter(List<ShoppingList> mShoppingList, WOnItemClickListener onItemClick) {
        this.mShoppingList = mShoppingList;
        this.wOnItemClickListener = onItemClick;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        ImageView mCheckedItem;
        SwipeLayout swipeLayout;
        WTextView mProductName;
        ImageView imgdelete;
        LinearLayout cardlayout;

        SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            mProductName = (WTextView) itemView.findViewById(R.id.productName);
            imgdelete = (ImageView) itemView.findViewById(R.id.msgDelete);
            mCheckedItem = (ImageView) itemView.findViewById(R.id.checkedItem);
            cardlayout = (LinearLayout) itemView.findViewById(R.id.cardLayout);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {

        holder.mProductName.setText(mShoppingList.get(position).getProduct_name());

        mItemManger.bindView(holder.itemView, position);
        holder.imgdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wOnItemClickListener.onDelete(mShoppingList.get(position).getProduct_id());
                mShoppingList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mShoppingList.size());
                notifyDataSetChanged();
                mItemManger.closeAllItems();
            }
        });

        holder.mProductName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wOnItemClickListener.onItemClick(mShoppingList.get(position).getProduct_id(), 0);
                mShoppingList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mShoppingList.size());
                notifyDataSetChanged();
                mItemManger.closeAllItems();
            }
        });

        holder.mCheckedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wOnItemClickListener.onItemClick(mShoppingList.get(position).getProduct_id(), 0);
                mShoppingList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mShoppingList.size());
                notifyDataSetChanged();
                mItemManger.closeAllItems();
            }
        });

        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                Log.d("swiping", "swiping");
                wOnItemClickListener.onSwipeListener(0, layout);
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_row, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mShoppingList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
}