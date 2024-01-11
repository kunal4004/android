package za.co.woolworths.financial.services.android.ui.activities.product;

import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.ADDED_TO_SHOPPING_LIST_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.MY_LIST_LIST_ID;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.MY_LIST_LIST_NAME;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.PRODUCT_DETAILS_FROM_MY_LIST_SEARCH;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.util.AppConstant.Keys.EXTRA_SEND_DELIVERY_DETAILS_PARAMS;
import static za.co.woolworths.financial.services.android.util.Utils.DY_CHANNEL;
import static za.co.woolworths.financial.services.android.util.Utils.IPAddress;
import static za.co.woolworths.financial.services.android.util.Utils.KEYWORD_SEARCH_EVENT_NAME;
import static za.co.woolworths.financial.services.android.util.Utils.KEYWORD_SEARCH_V1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl;
import za.co.woolworths.financial.services.android.models.network.NetworkConfig;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.shop.ChanelMessageDialogFragment;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event;
@AndroidEntryPoint
public class ProductSearchActivity extends AppCompatActivity
        implements View.OnClickListener, ChanelMessageDialogFragment.IChanelMessageDialogDismissListener {
    public LinearLayoutManager mLayoutManager;
    public Toolbar toolbar;
    private EditText mEditSearchProduct;
    private LinearLayout recentSearchLayout;
    private LinearLayout recentSearchList;
    private String mSearchTextHint = "";
    private String mListID;
    private boolean isUserBrowsingDash;
    public static final String EXTRA_SEARCH_TEXT_HINT = "SEARCH_TEXT_HINT";
    public static final String EXTRA_LIST_ID = "listId";
    public static final int PRODUCT_SEARCH_ACTIVITY_RESULT_CODE = 1244;
    private DyChangeAttributeViewModel dyKeywordSearchViewModel;
    private String dyServerId = null;
    private String dySessionId = null;
    private NetworkConfig config = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_search_activity);
        Utils.updateStatusBarBackground(this);
        setActionBar();
        initUI();
        showRecentSearchHistoryView(true);
        mEditSearchProduct.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if(TextUtils.isEmpty(mEditSearchProduct.getText().toString())) {
                    return false;
                }
                searchProduct(mEditSearchProduct.getText().toString());
                return true;
            }
            return false;
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isUserBrowsingDash = bundle.getBoolean(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, false);
            if (!TextUtils.isEmpty(bundle.getString(EXTRA_SEARCH_TEXT_HINT))) {
                mListID = bundle.getString(MY_LIST_LIST_ID);
                mSearchTextHint = getString(R.string.shopping_search_hint);
                mEditSearchProduct.setHint(mSearchTextHint);
            }
        }
    }

    private void prepareDyKeywordSearchRequestEvent(String searchProductBrand) {
        config = new NetworkConfig(new AppContextProviderImpl());
        if (Utils.getDyServerId() != null)
            dyServerId = Utils.getDyServerId();
        if (Utils.getDySessionId() != null)
            dySessionId = Utils.getDySessionId();
        User user = new User(dyServerId, dyServerId);
        Session session = new Session(dySessionId);
        Device device = new Device(IPAddress,config.getDeviceModel());
        Context context = new Context(device,null,DY_CHANNEL,null);
        Properties properties = new Properties(null,null,KEYWORD_SEARCH_V1,searchProductBrand,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        Event events = new Event(null,null,null,null,null,null,null,null,null,null,null,null,KEYWORD_SEARCH_EVENT_NAME,properties);
        ArrayList<Event> event = new ArrayList<>();
        event.add(events);
        PrepareChangeAttributeRequestEvent dyKeywordSearchRequestEvent = new PrepareChangeAttributeRequestEvent(
                context,
                event,
                session,
                user);
        dyKeywordSearchViewModel.createDyChangeAttributeRequest(dyKeywordSearchRequestEvent);
    }

    private void initUI() {
        mLayoutManager = new LinearLayoutManager(ProductSearchActivity.this);
        mEditSearchProduct = findViewById(R.id.toolbarText);
        recentSearchLayout = findViewById(R.id.recentSearchLayout);
        recentSearchList = findViewById(R.id.recentSearchList);
    }

    private void setActionBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(null);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_search);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        canGoBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                searchProduct(mEditSearchProduct.getText().toString());
                return true;
            case R.id.action_search:
                canGoBack();
                break;
        }
        return false;
    }

	private void searchProduct(String searchProductBrand) {
		if (searchProductBrand.length() > 2) {
			   SearchHistory search = new SearchHistory();
				search.searchedValue = searchProductBrand;
				saveRecentSearch(search);
				if (TextUtils.isEmpty(mSearchTextHint)) {
					LoadState loadState = new LoadState();
					loadState.setSearchProduct(searchProductBrand);
					loadState.setSendDeliveryDetails(isUserBrowsingDash);
					Utils.sendBus(loadState);
					mEditSearchProduct.setText("");
					finish();
					overridePendingTransition(0, 0);
				} else {
					Intent intent = new Intent();
                    intent.putExtra(MY_LIST_LIST_ID, mListID);
                    intent.putExtra(MY_LIST_LIST_NAME, search.searchedValue);
                    intent.putExtra(EXTRA_SEND_DELIVERY_DETAILS_PARAMS, isUserBrowsingDash);
                    setActivityResult(intent, PRODUCT_SEARCH_ACTIVITY_RESULT_CODE);
				}
            if (Boolean.TRUE.equals(AppConfigSingleton.getDynamicYieldConfig().isDynamicYieldEnabled())) {
                dyKeywordSearchViewModel = new ViewModelProvider(this).get(DyChangeAttributeViewModel.class);
                prepareDyKeywordSearchRequestEvent(searchProductBrand);
            }
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void saveRecentSearch(SearchHistory searchHistory) {
        List<SearchHistory> histories;
        histories = getRecentSearch();
        SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_PRODUCT_SEARCH);
        sessionDao.key = SessionDao.KEY.STORES_PRODUCT_SEARCH;
        Gson gson = new Gson();
        boolean isExist = false;
        if (histories == null) {
            histories = new ArrayList<>();
            histories.add(0, searchHistory);
            sessionDao.value = gson.toJson(histories);
            try {
                sessionDao.save();
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
        } else {
            for (SearchHistory s : histories) {
                if (s.searchedValue.equalsIgnoreCase(searchHistory.searchedValue)) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                histories.add(0, searchHistory);
                if (histories.size() > 5)
                    histories.remove(5);
                sessionDao.value = gson.toJson(histories);
                try {
                    sessionDao.save();
                } catch (Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            }
        }
    }

    public List<SearchHistory> getRecentSearch() {
        List<SearchHistory> historyList = null;
        try {
            SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_PRODUCT_SEARCH);
            if (sessionDao.value == null) {
                historyList = new ArrayList<>();
            } else {
                Gson gson = new Gson();
                Type type = new TypeToken<List<SearchHistory>>() {
                }.getType();
                historyList = gson.fromJson(sessionDao.value, type);
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
        return historyList;
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @SuppressLint("InflateParams")
    public void showRecentSearchHistoryView(boolean status) {
        recentSearchList.removeAllViews();
        View storeItem = getLayoutInflater().inflate(R.layout.stores_recent_search_header_row, null);
        recentSearchList.addView(storeItem);
        List<SearchHistory> searchHistories = getRecentSearch();
        if (status && searchHistories != null) {
            for (int i = 0; i < searchHistories.size(); i++) {
                View v = getLayoutInflater().inflate(R.layout.recent_search_list_item, null);
                TextView recentSearchListitem = v.findViewById(R.id.recentSerachListItem);
                recentSearchListitem.setText(searchHistories.get(i).searchedValue);
                recentSearchList.addView(v);
                int position = recentSearchList.indexOfChild(v) - 1;
                v.setTag(position);
                v.setOnClickListener(this);
            }
            recentSearchLayout.setVisibility(View.VISIBLE);
        } else {
            recentSearchLayout.setVisibility(View.GONE);
        }
    }

    private void canGoBack() {
        this.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        showRecentSearchHistoryView(false);
        searchProduct(getRecentSearch().get(pos).searchedValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRecentSearchHistoryView(true);
        if (mEditSearchProduct != null)
            mEditSearchProduct.requestFocus();
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SEARCH, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE && resultCode == ADDED_TO_SHOPPING_LIST_RESULT_CODE) {
            setActivityResult(data, ADDED_TO_SHOPPING_LIST_RESULT_CODE);
        }
        if (resultCode == PRODUCT_DETAILS_FROM_MY_LIST_SEARCH) {
            setActivityResult(data, PRODUCT_DETAILS_FROM_MY_LIST_SEARCH);
        }
    }

    private void setActivityResult(Intent data, int addToShoppingListResultCode) {
        setResult(addToShoppingListResultCode, data);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideSoftKeyboard();
    }

    @Override
    public void onDialogDismiss() {
        mEditSearchProduct.setText("");
    }
}