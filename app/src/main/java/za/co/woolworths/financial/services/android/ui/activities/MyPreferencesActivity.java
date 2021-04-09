package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

import com.awfs.coordination.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.LinkDeviceOTPFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.WStoreLocatorActivity.mToolbar;

public class MyPreferencesActivity extends AppCompatActivity implements MyPreferencesInterface {

    private NavController navigationHost = null;
    private Toolbar mPrefsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.my_preferences_activity);

        NavHostFragment myPreferencesNavHostFrag = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.myPreferencesNavHostFrag);
        if (myPreferencesNavHostFrag != null) {
            navigationHost = myPreferencesNavHostFrag.getNavController();
        }
        mPrefsToolbar = findViewById(R.id.mPrefsToolbar);
        setActionBar();
        setNavHostStartDestination();
    }

    private void setActionBar() {
        setSupportActionBar(mPrefsToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.back24);
        }
    }

    private void setNavHostStartDestination() {
        if (navigationHost == null) {
            return;
        }
        NavGraph graph = navigationHost.getGraph();
        graph.setStartDestination(R.id.myPreferencesFragment);

        navigationHost.setGraph(graph, getIntent() != null ? getIntent().getExtras() : null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().getFragments().get(0);
        if (navHostFragment != null) {
            List<Fragment> childFragments = navHostFragment.getChildFragmentManager().getFragments();
            for (Fragment fragment : childFragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
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

    @Override
    public void onBackPressed() {
        if (navigationHost.getCurrentDestination() == null) {
            return;
        }

        switch (navigationHost.getCurrentDestination().getId()) {
            case R.id.myPreferencesFragment:
                finishActivity();
                break;
            default:
                navigationHost.popBackStack();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // permission was granted, yay! Do the
        // contacts-related task you need to do.
        getCurrentFragment().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Fragment getCurrentFragment() {
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.myPreferencesNavHostFrag);
        return navHostFragment == null ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void setToolbarTitle(@NotNull String titleTxt) {
        if (mPrefsToolbar == null) {
            return;
        }
        WTextView title = mPrefsToolbar.findViewById(R.id.toolbarText);
        title.setText(titleTxt);
    }
}
