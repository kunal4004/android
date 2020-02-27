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

import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;

public class ShoppingListSearchResultActivity extends AppCompatActivity {

    private String searchTerm = "";
    private String listID = "";

    public static final int SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE = 2012;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.shopping_list_detail_activity);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            searchTerm = bundle.getString("searchTerm");
            listID = bundle.getString("listID");
        }
        retrieveBundleArgument();
        setUpToolbar(searchTerm);
        initFragment();
    }

    private void retrieveBundleArgument() {
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
        TextView tvToolbar = findViewById(R.id.tvToolbar);
        if (tvToolbar != null)
            tvToolbar.setText(listName);
        setSupportActionBar(shoppingToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.back24);
        }
    }

    private void initFragment() {
        SearchResultFragment searchResultFragment = new SearchResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("searchTerm", searchTerm);
        bundle.putString("listID", listID);
        searchResultFragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flShoppingListDetailFragment, searchResultFragment,
                        SearchResultFragment.class.getSimpleName())
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
        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            setResult(ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, data);
            finish();
            overridePendingTransition(0, 0);
            return;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flShoppingListDetailFragment);
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}
