package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.fragments.WFragmentDrawer;
import za.co.woolworths.financial.services.android.ui.fragments.WProductsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WOneAppBaseActivity extends AppCompatActivity implements WFragmentDrawer.FragmentDrawerListener{

    public static Toolbar mToolbar;
    public static AppBarLayout appbar;
    private WFragmentDrawer drawerFragment;
    public StoresNearbyFragment1 frag;
    public WTextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_app_base_activity);
        Utils.updateStatusBarBackground(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // false for hiding the title from actoinBar
        getSupportActionBar().setElevation(0);
        mToolbarTitle=(WTextView)findViewById(R.id.toolbar_title);
        appbar=(AppBarLayout)findViewById(R.id.appbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_drawer_menu);
        drawerFragment = (WFragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);
        displayView(Utils.DEFAULT_SELECTED_NAVIGATION_ITEM);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        WOneAppBaseActivity.appbar.animate().translationY(WOneAppBaseActivity.appbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 1:
                fragment = new WProductsFragment();
                title = getString(R.string.nav_item_products);
                break;
            case 2:
                fragment = new StoresNearbyFragment1();
                frag=(StoresNearbyFragment1) fragment;
                title = getString(R.string.stores_nearby);
                break;
            case 4:
                fragment = new MyAccountsFragment();
                title = getString(R.string.nav_item_accounts);
                break;
            case 0:
                fragment = new WTodayFragment();
                title = getString(R.string.nav_item_today);
                break;

        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            mToolbarTitle.setText(title);

        }
    }
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.w_store_locator_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(WOneAppBaseActivity.this, SearchStoresActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        frag.onActivityResult(requestCode,resultCode,data);
    }
}
