package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import za.co.wigroup.menudrawerlib.DrawerOnClickListener;
import za.co.wigroup.menudrawerlib.LibBaseDrawerActivity;
import za.co.wigroup.menudrawerlib.items.DrawerItem;
import za.co.wigroup.menudrawerlib.items.LogoDrawerItem;
import za.co.wigroup.menudrawerlib.items.SelectedLogoDrawerItem;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;


public abstract class BaseDrawerActivity extends LibBaseDrawerActivity {

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int upId = Resources.getSystem().getIdentifier("up", "id", "android");
        if (upId > 0) {
            ImageView up = (ImageView) findViewById(upId);
            up.setImageResource(getClosedDrawerIndicator());
        }
    }

    @Override
    protected int getClosedDrawerIndicator() {
        return R.drawable.ic_navigation_drawer;
    }

    @Override
    protected int getOpenDrawerIndicator() {
        return R.drawable.ic_navigation_drawer_open;
    }

    @Override
    protected int getOpenString() {
        return 0;
    }

    @Override
    protected int getCloseString() {
        return 0;
    }

    @Override
    protected List<DrawerItem> getDrawerItemList() {
        ArrayList<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
        WSelectedLogoDrawerItem counterSelectedLogoDrawerItem = new WSelectedLogoDrawerItem(getString(R.string.drawer_rewards), R.drawable.ic_dr_rewards, new DrawerOnClickListener() {
            @Override
            public void onClick(boolean b) {
                if (!b) {
                    startActivity(new Intent(BaseDrawerActivity.this, WRewardsActivity.class));
                    finish();
                } else {
                    closeDrawer();
                }
            }
        });
        counterSelectedLogoDrawerItem.setDrawerItemView(R.layout.w_drawer_item_counter_view);
       // TextView tv = (TextView)findViewById(R.id.drawer_item_counter);
      //  tv.setText("5");
        drawerItems.add(counterSelectedLogoDrawerItem);

        SelectedLogoDrawerItem selectedLogoDrawerItem = new SelectedLogoDrawerItem(getString(R.string.drawer_accounts), R.drawable.ic_dr_account, new DrawerOnClickListener() {
            @Override
            public void onClick(boolean b) {
                if (!b) {
                    startActivity(new Intent(BaseDrawerActivity.this, AccountsActivity.class));
                    finish();
                } else {
                    closeDrawer();
                }
            }
        });

        selectedLogoDrawerItem.setDrawerItemView(R.layout.w_drawer_item_view);
        drawerItems.add(selectedLogoDrawerItem);


        ////thtis
        SelectedLogoDrawerItem applyNowLogoDrawerItem = new SelectedLogoDrawerItem(getString(R.string.drawer_apply_now), R.drawable.applynowicon, new DrawerOnClickListener() {
            @Override
            public void onClick(boolean b) {
                if (!b) {
                    Intent i =new Intent(BaseDrawerActivity.this, WebViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("title","Apply Now");
                    bundle.putString("link", WoolworthsApplication.getApplyNowLink());
                    i.putExtra("Bundle", bundle);
                    startActivity(i);
                    ///finish();
                } else {
                    closeDrawer();
                }
            }
        });
        applyNowLogoDrawerItem.setDrawerItemView(R.layout.w_drawer_item_view);
        drawerItems.add(applyNowLogoDrawerItem);






        SelectedLogoDrawerItem object = new SelectedLogoDrawerItem(getString(R.string.drawer_faq), R.drawable.ic_dr_faq, new DrawerOnClickListener() {
            @Override
            public void onClick(boolean b) {
                if (!b) {
                    Intent i =new Intent(BaseDrawerActivity.this, WebViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("title","FAQ");
                    bundle.putString("link", WoolworthsApplication.getFaqLink());
                    i.putExtra("Bundle",bundle);
                    startActivity(i);
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.settings_faq_url)));
                    //startActivity(browserIntent);
                } else {
                    closeDrawer();
                }

            }
        });
        object.setDrawerItemView(R.layout.w_drawer_item_view);
        drawerItems.add(object);
        SelectedLogoDrawerItem object1 = new SelectedLogoDrawerItem(getString(R.string.drawer_contact), R.drawable.ic_dr_contact, new DrawerOnClickListener() {
            @Override
            public void onClick(boolean b) {
                if (!b) {
                    startActivity(new Intent(BaseDrawerActivity.this, ContactUsActivity.class));
                    finish();
                } else {
                    closeDrawer();
                }
            }
        });
        object1.setDrawerItemView(R.layout.w_drawer_item_view);
        drawerItems.add(object1);
        return drawerItems;
    }

    @Override
    protected int getCurrentItemIndex() {
        if (this instanceof WRewardsActivity) {
            return 0;
        } else if (this instanceof AccountsActivity) {
            return 1;
        } else if (this instanceof ContactUsActivity) {
            return 4;
        }
        return 0;
    }

    @Override
    protected void setDrawerTitleOpen() {
        getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.app_name), 3, this));
    }

    protected void setWoolworthsTitle(int string) {
        String title = getString(string);
        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(new CalligraphyTypefaceSpan(Typeface.createFromAsset(getAssets(), "fonts/WFutura-Medium.ttf")), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getActionBar().setTitle(spannableString);
    }

    @Override
    public void onBackPressed() {
        if (getCurrentItemIndex() != 0) {
            startActivity(new Intent(this, WRewardsActivity.class));
        }
        super.onBackPressed();
    }

    private class WSelectedLogoDrawerItem extends WLogoDrawerItem {
        public WSelectedLogoDrawerItem(String mTitle, int mLogo, DrawerOnClickListener mDrawerOnClickListener) {
            super(mTitle, mLogo, mDrawerOnClickListener);
            this.setDrawerItemView(R.layout.w_drawer_item_counter_view);
        }

        public View getView(int position, View view, ViewGroup viewGroup, Context c) {
            View view1 = super.getView(position, view, viewGroup, c);
            int se = getCurrentItemIndex();
            if(se == 0){
                view1.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_selected).setVisibility(View.VISIBLE);
            }else{
                view1.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_selected).setVisibility(View.INVISIBLE);
            }
            int num = WoolworthsApplication.getNumVouchers();
            if(num==0){
                (view1.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_counter)).setVisibility(View.INVISIBLE);
            }else {
                (view1.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_counter)).setVisibility(View.VISIBLE);
                ((TextView)view1.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_counter)).setText(String.valueOf(WoolworthsApplication.getNumVouchers()));
            }
            return view1;
        }
    }

    private class WLogoDrawerItem extends DrawerItem {
        private int mLogo;


        public WLogoDrawerItem(String mTitle, int mLogo, DrawerOnClickListener mDrawerOnClickListener) {
            super(mTitle, mDrawerOnClickListener);
            this.mLogo = mLogo;
            this.setDrawerItemView(za.co.wigroup.menudrawerlib.R.layout.drawer_item_logo_view);
        }

        public View getView(int position, View view, ViewGroup viewGroup, Context c) {
            View logoView = super.getView(position, view, viewGroup, c);
            ((ImageView)logoView.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_logo)).setImageResource(this.mLogo);
            ((TextView)logoView.findViewById(za.co.wigroup.menudrawerlib.R.id.drawer_item_counter)).setText("");
            return logoView;
        }
    }



}
