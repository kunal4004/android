package za.co.woolworths.financial.services.android.ui.activities.product.shop;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.ADDED_TO_SHOPPING_LIST_RESULT_CODE;

public class ShoppingListDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvEditShoppingListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.shopping_list_detail_activity);
        Bundle bundle = getIntent().getExtras();
        String listId = "", listName = "";
        boolean openFromMyList = false;
        if (bundle != null) {
            openFromMyList = bundle.getBoolean("openFromMyList", false);
            listId = bundle.getString("listId");
            listName = bundle.getString("listName");
        }
        retrieveBundle();
        setUpToolbar(listName);

        tvEditShoppingListItem = findViewById(R.id.tvEditShoppingListItem);
        tvEditShoppingListItem.setOnClickListener(this);

        initFragment(listId, listName, openFromMyList);
    }

    private void retrieveBundle() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpToolbar(String listName) {
        Toolbar shoppingToolbar = findViewById(R.id.mToolbar);
        WTextView tvToolbar = findViewById(R.id.tvToolbar);
        tvToolbar.setText(listName);
        setSupportActionBar(shoppingToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.back24);
        }
    }

    private void initFragment(String listId, String listName, boolean openFromMyList) {
        ShoppingListDetailFragment shoppingListDetailFragment = new ShoppingListDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("listId", listId);
        bundle.putString("listName", listName);
        bundle.putBoolean("openFromMyList", openFromMyList);
        shoppingListDetailFragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flShoppingListDetailFragment, shoppingListDetailFragment,
                        ShoppingListDetailFragment.class.getSimpleName())
                .disallowAddToBackStack()
                .commit();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // response from product detail page
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flShoppingListDetailFragment);
        if ((requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE)
                || (requestCode == PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE)) {
            setResult(ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, data);
            finish();
            overridePendingTransition(0, 0);
            return;
            // response from search product from shopping list
        } else if (requestCode == PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE && resultCode == ADDED_TO_SHOPPING_LIST_RESULT_CODE) {
            fragment.onActivityResult(requestCode, resultCode, data);
            return;
        }

        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvEditShoppingListItem) {
            if (getSupportFragmentManager() == null) return;
            String editButtonText = tvEditShoppingListItem.getText().toString().equalsIgnoreCase(getString(R.string.edit)) ? getString(R.string.done) : getString(R.string.edit);
            tvEditShoppingListItem.setText(editButtonText);
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flShoppingListDetailFragment);
            if (currentFragment instanceof ShoppingListDetailFragment) {
                ((ShoppingListDetailFragment) currentFragment).toogleEditButton(editButtonText);
            }
        }
    }

    public void editButtonVisibility(boolean isVisible) {
        tvEditShoppingListItem.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setToolbarText(String name){
        tvEditShoppingListItem.setText(name);
    }
}
