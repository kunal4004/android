package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 2018/03/08.
 */

public class ShoppingListAdapter extends RecyclerSwipeAdapter<ShoppingListAdapter.ViewHolder> {

    private ShoppingListNavigator shoppingListNavigator;
    private List<ShoppingList> lists;

    public ShoppingListAdapter(ShoppingListNavigator shoppingListNavigator, List<ShoppingList> lists) {
        this.shoppingListNavigator = shoppingListNavigator;
        this.lists = lists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ShoppingList shoppingList = lists.get(position);
        holder.listName.setText(Utils.toTitleCase(shoppingList.listName));
        holder.listCount.setText(String.valueOf(shoppingList.listCount) + (shoppingList.listCount != 1 ? " Items in List" : " Item in List"));
        holder.tvDelete.setVisibility(View.VISIBLE);
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.listItem.setEnabled(true);
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shoppingListNavigator.onListItemSelected(lists.get(position).listName, lists.get(position).listId);
            }
        });

        holder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.listItem.setEnabled(false);
                holder.tvDelete.setVisibility(View.INVISIBLE);
                holder.progressBar.setVisibility(View.VISIBLE);
                shoppingListNavigator.onClickItemDelete(lists.get(position).listId);
            }
        });
        mItemManger.bindView(holder.itemView, position);
    }


    @Override
    public int getItemCount() {
        return lists.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout listItem;
        public WTextView listName;
        public WTextView listCount;
        public WTextView tvDelete;
        private ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.listName);
            listCount = itemView.findViewById(R.id.listItemCount);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            listItem = itemView.findViewById(R.id.listItem);
            progressBar = itemView.findViewById(R.id.pbDeleteIndicator);
        }
    }

    public void update() {
        notifyDataSetChanged();
        closeAllItems();
    }
}
