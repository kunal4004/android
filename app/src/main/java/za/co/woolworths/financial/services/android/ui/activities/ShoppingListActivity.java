package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListCheckedAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingUnCheckedListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WObservableScrollView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WOnItemClickListener;

public class ShoppingListActivity extends AppCompatActivity implements WOnItemClickListener, ObservableScrollViewCallbacks {

    private RecyclerView mUncheckedItem;
    private RecyclerView mCheckItem;
    private Toolbar mToolbar;
    private ShoppingListCheckedAdapter checkedShoppingListAdapter;
    private ShoppingUnCheckedListAdapter unCheckedShoppingListAdapter;
    private ArrayList<ShoppingList> uncheckedItemList;
    private ArrayList<ShoppingList> checkedItemList;
    public WTextView mCheckListTitle;
    private List<ShoppingList> mGetShoppingList;
    private RelativeLayout mNoItemInList;
    private WObservableScrollView mNestedScroll;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.shopping_list_activity);
        initUI();
        actionBar();
        bindDataWithView(this);
    }

    private void initUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mUncheckedItem = (RecyclerView) findViewById(R.id.uncheckedItem);
        mCheckItem = (RecyclerView) findViewById(R.id.checkedItem);
        mCheckListTitle = (WTextView) findViewById(R.id.checkListTitle);
        mNoItemInList = (RelativeLayout) findViewById(R.id.noItemInList);
        mNestedScroll = (WObservableScrollView) findViewById(R.id.nestedScroll);
        mNestedScroll.setScrollViewCallbacks(this);
    }

    private void actionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.close_24);
        }
    }

    private void bindDataWithView(WOnItemClickListener context) {
        LinearLayoutManager checkedLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager unCheckedLayoutManager = new LinearLayoutManager(this);

        checkedLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unCheckedLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mUncheckedItem.setHasFixedSize(false);
        mUncheckedItem.setLayoutManager(unCheckedLayoutManager);
        mCheckItem.setHasFixedSize(false);
        mCheckItem.setLayoutManager(checkedLayoutManager);

        adapterList();


        checkedShoppingListAdapter = new ShoppingListCheckedAdapter(checkedItemList, context);
        unCheckedShoppingListAdapter = new ShoppingUnCheckedListAdapter(uncheckedItemList, context);
        checkedShoppingListAdapter.setMode(Attributes.Mode.Single);
        unCheckedShoppingListAdapter.setMode(Attributes.Mode.Single);

        mCheckItem.setAdapter(checkedShoppingListAdapter);
        mUncheckedItem.setAdapter(unCheckedShoppingListAdapter);
    }

    private void adapterList() {
        if (mGetShoppingList != null)
            mGetShoppingList.clear();

        mGetShoppingList = Utils.getShoppingList(this);

        uncheckedItemList = new ArrayList<>();
        checkedItemList = new ArrayList<>();

        if (uncheckedItemList != null) {
            uncheckedItemList.clear();
        }

        if (checkedItemList != null) {
            checkedItemList.clear();
        }

        for (ShoppingList s : mGetShoppingList) {
            if (s.isProductIsChecked()) {
                checkedItemList.add(s);
            } else {
                uncheckedItemList.add(s);
            }
        }

        shoppingListEmptyView();
    }

    @Override
    public void onItemClick(String productId, int section) {
        switch (section) {
            case 0:
                for (int x = 0; x < mGetShoppingList.size(); x++) {
                    if (productId.equalsIgnoreCase(mGetShoppingList.get(x).getProduct_id())) {
                        ShoppingList shoppingList = mGetShoppingList.get(x);
                        ShoppingList updatedShopList = new ShoppingList(shoppingList.getProduct_id(),
                                shoppingList.getProduct_name(), true);
                        mGetShoppingList.set(x, updatedShopList);
                        Collections.swap(mGetShoppingList, 0, x);
                        checkedItemList.add(0, updatedShopList);
                    }
                }

                checkedShoppingListAdapter.notifyItemInserted(0);
                checkedShoppingListAdapter.notifyItemRangeChanged(0, checkedItemList.size());
                checkedShoppingListAdapter.notifyDataSetChanged();
                checkedShoppingListAdapter.mItemManger.closeAllItems();


                Utils.sessionDaoSave(this, SessionDao.KEY.STORE_SHOPPING_LIST,
                        new Gson().toJson(mGetShoppingList));
                break;

            case 1:
                for (int x = 0; x < mGetShoppingList.size(); x++) {
                    if (productId.equalsIgnoreCase(mGetShoppingList.get(x).getProduct_id())) {
                        ShoppingList shoppingList = mGetShoppingList.get(x);
                        ShoppingList updatedShopList = new ShoppingList(shoppingList.getProduct_id(),
                                shoppingList.getProduct_name(), false);
                        mGetShoppingList.set(x, updatedShopList);
                        Collections.swap(mGetShoppingList, 0, x);
                        uncheckedItemList.add(uncheckedItemList.size(), updatedShopList);
                    }
                }

                unCheckedShoppingListAdapter.notifyItemInserted(uncheckedItemList.size());
                unCheckedShoppingListAdapter.notifyItemRangeChanged(0, uncheckedItemList.size());
                unCheckedShoppingListAdapter.notifyDataSetChanged();
                unCheckedShoppingListAdapter.mItemManger.closeAllItems();

                Utils.sessionDaoSave(this, SessionDao.KEY.STORE_SHOPPING_LIST,
                        new Gson().toJson(mGetShoppingList));
                break;
        }
    }

    @Override
    public void onSwipeListener(int index, SwipeLayout layout) {
        switch (index) {
            case 0:
                checkedShoppingListAdapter.closeAllItems();
                checkedShoppingListAdapter.notifyDataSetChanged();

                break;

            case 1:
                unCheckedShoppingListAdapter.closeAllItems();
                unCheckedShoppingListAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onDelete(String productId) {
        ArrayList toRemove = new ArrayList();
        for (ShoppingList str : mGetShoppingList) {
            if (productId.equalsIgnoreCase(str.getProduct_id())) {
                toRemove.add(str);
            }
        }
        mGetShoppingList.removeAll(toRemove);
        Utils.sessionDaoSave(this, SessionDao.KEY.STORE_SHOPPING_LIST,
                new Gson().toJson(mGetShoppingList));

        shoppingListEmptyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    private void shoppingListEmptyView() {
        if (mGetShoppingList.size() == 0) {
            mNoItemInList.setVisibility(View.VISIBLE);
            mNestedScroll.setVisibility(View.GONE);
        } else {
            mNoItemInList.setVisibility(View.GONE);
            mNestedScroll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        try {
            switch (scrollState) {
                case UP:
                    hideViews();
                    break;
                case DOWN:
                    showViews();
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
        }
    }


    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getBottom())
                .setInterpolator(new AccelerateInterpolator()).start();
    }

    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
    }

}
