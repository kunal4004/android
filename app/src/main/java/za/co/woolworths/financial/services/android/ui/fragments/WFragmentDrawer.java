package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.NavListItem;
import za.co.woolworths.financial.services.android.ui.adapters.NavigationDrawerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class WFragmentDrawer extends Fragment {


    private static NavListItem navItem;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerAdapter adapter;
    private View containerView;
    public ListView listView;
    private FragmentDrawerListener drawerListener;
    private static String[] titles = null;
    private static TypedArray images = null;
    private List<NavListItem> mMenuList;

    public WFragmentDrawer() {
        // Required empty public constructor
    }

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.w_navigation_drawer_fragment, container, false);
        listView = (ListView) layout.findViewById(R.id.drawerList);

        mMenuList = getData();
        adapter = new NavigationDrawerAdapter(getActivity(), mMenuList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                adapter.setSelectedPosition(position);

                adapter.notifyDataSetChanged();

                drawerListener.onDrawerItemSelected(view, position);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(containerView);
                    }
                });

            }
        });
        return layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titles = getActivity().getResources().getStringArray(R.array.nav_drawer_labels);
        images = getActivity().getResources().obtainTypedArray(R.array.nav_drawer_images);

    }

    public static List<NavListItem> getData() {
        List<NavListItem> data = new ArrayList<>();
        // preparing navigation drawer items
        for (int i = 0; i < titles.length; i++) {
            navItem = new NavListItem();
            navItem.setName(titles[i]);
            navItem.setImage(images.getResourceId(i, -1));
            data.add(navItem);
        }
        return data;
    }
    public void notifyNavigationDrawer(int voucherCount)
    {
        mMenuList.get(3).setCount(voucherCount);
        adapter.notifyDataSetChanged();
    }
    public void setUp(int fragmentId, final DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                containerView.setClickable(true);
                getActivity().invalidateOptionsMenu();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                containerView.setClickable(false);
                getActivity().invalidateOptionsMenu();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerToggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_drawer_menu, getActivity().getTheme());
        mDrawerToggle.setHomeAsUpIndicator(drawable);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

    }


    public interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position);
    }
}
