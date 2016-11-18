package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.adapters.StoreSearchListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.util.Utils.getRecentSearchedHistory;


public class SearchStoresActivity extends AppCompatActivity implements View.OnClickListener {
    public Toolbar toolbar;
    public RecyclerView recyclerView;
    StoreSearchListAdapter searchAdapter;
    public List<StoreDetails> storeDetailsList;
    Handler mHandler;
     SearchView searchView;
    LinearLayout recentSearchLayout;
    LinearLayout recentSearchList;
    WTextView recentSearchListitem;
    SearchHistory search;
    String[] recentSearchData={"Cape Town","PineLand","SeaPoint","Wellington"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_store_activity);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        recyclerView=(RecyclerView)findViewById(R.id.storeList) ;
        recentSearchLayout=(LinearLayout)findViewById(R.id.recentSearchLayout);
        recentSearchList=(LinearLayout)findViewById(R.id.recentSearchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showRecentSearchHistoryView(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mHandler=new Handler();
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });

        recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getApplicationContext(),recyclerView,new RecycleViewClickListner.ClickListener()
        {
            @Override
            public void onClick(View view, int position) {
                Gson gson=new Gson();
                String store=gson.toJson(storeDetailsList.get(position));
                 startActivity(new Intent(getApplicationContext(),StoreDetailsActivity.class).putExtra("store",store));
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {


                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    @Override
    public void onClick(View v) {
        int pos=(Integer) v.getTag();
        showRecentSearchHistoryView(false);
        searchView.setQuery(Utils.getRecentSearchedHistory(SearchStoresActivity.this).get(pos).searchedValue,true);
        //Toast.makeText(getApplicationContext(),recentSearchData[pos],Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.w_search_store_menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchView.setIconified(false);
        searchView.setQueryHint("Search by Store or Location");
      /*  ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.close);*/
        //searchView.clearFocus();
        //searchView.setIconifiedByDefault(false);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //searchViewAndroidActionBar.clearFocus();


                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {

                mHandler.removeCallbacksAndMessages(null);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(newText.isEmpty()) {
                            storeDetailsList.clear();
                          showRecentSearchHistoryView(true);
                        }
                        else {
                            showRecentSearchHistoryView(false);
                            startSearch(newText);
                        }
                    }
                }, 600);

                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void startSearch(final String query)
    {
        new HttpAsyncTask<String,String,LocationResponse>()
        {
            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                search=new SearchHistory();
                search.searchedValue=query;
                Utils.addToRecentSearchedHistory(search,SearchStoresActivity.this);
            }

            @Override
            protected LocationResponse httpDoInBackground(String... params) {
                Location location=Utils.getLastSavedLocation(SearchStoresActivity.this);
                if(location!=null)
                {
                    return ((WoolworthsApplication) getApplication()).getApi().getLocations(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),query,"50000");

                }else {
                    return ((WoolworthsApplication) getApplication()).getApi().getLocations(null,null,query,null);

                }
            }

            @Override
            protected Class<LocationResponse> httpDoInBackgroundReturnType() {
                return LocationResponse.class;
            }

            @Override
            protected LocationResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                LocationResponse locationResponse=new LocationResponse();
                locationResponse.response=new Response();
                return locationResponse;
            }

            @Override
            protected void onPostExecute(LocationResponse locationResponse) {
                super.onPostExecute(locationResponse);
                storeDetailsList=new ArrayList<StoreDetails>() ;
                storeDetailsList=locationResponse.Locations;
                if (storeDetailsList!=null&&storeDetailsList.size()!=0)
                {
                    searchAdapter =new StoreSearchListAdapter(SearchStoresActivity.this,storeDetailsList);
                    recyclerView.setAdapter(searchAdapter);
                }


            }
        }.execute();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
             //   hideSoftKeyboard();
             onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
       // overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);



    }
    public void showRecentSearchHistoryView(boolean status)
    {
        recentSearchList.removeAllViews();
      List<SearchHistory> searchHistories=Utils.getRecentSearchedHistory(SearchStoresActivity.this);
        if(status)
        {

            for(int i=0;i<searchHistories.size();i++)
            {
                View v=getLayoutInflater().inflate(R.layout.recent_search_list_item,null);
                recentSearchListitem=(WTextView)v.findViewById(R.id.recentSerachListItem);
                recentSearchListitem.setText(searchHistories.get(i).searchedValue);
                recentSearchList.addView(v);
                int position=recentSearchList.indexOfChild(v);
                v.setTag(position);
                v.setOnClickListener(this);
            }
            recentSearchLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            recentSearchLayout.setVisibility(View.GONE);
        }

    }
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
