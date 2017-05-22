package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Counter;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AbstractFragmentListener;
import za.co.woolworths.financial.services.android.util.UpdateNavDrawerTitle;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsFragment extends AbstractFragmentListener {
    public static final int FRAGMENT_CODE_1 = 1;
    public static final int FRAGMENT_CODE_2 = 2;
    public static final int FRAGMENT_CODE_3 = 2;

    UpdateNavDrawerTitle updateTitle;
    ImageView mBurgerButtonPressed;
    WTextView wRewarsToolbarTitle;
    private Counter mCounter;

    public interface HideActionBarComponent {
        void onWRewardsDrawerPressed();
    }

    private HideActionBarComponent hideActionBarComponent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_fragment, container, false);
        mBurgerButtonPressed = (ImageView) view.findViewById(R.id.imBurgerButtonPressed);
        wRewarsToolbarTitle = (WTextView) view.findViewById(R.id.toolbarText);
        /*FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.add(R.id.content_frame,new WRewardsLoggedinAndNotLinkedFragment());
        childFragTrans.commit();*/
        mCounter = ((WoolworthsApplication) getActivity().getApplication()).getCounter();

        updateTitle = (UpdateNavDrawerTitle) getActivity();
        mBurgerButtonPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideActionBarComponent.onWRewardsDrawerPressed();
            }
        });
        initialize();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            hideActionBarComponent = (HideActionBarComponent) getActivity();
        } catch (ClassCastException ex) {
            Log.e("Interface", ex.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FRAGMENT_CODE_1 && resultCode == Activity.RESULT_OK) {
            try {
                reloadFragment();
            } catch (Exception ex) {
            }
        } else if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            getAvailableActivity(new IActivityEnabledListener() {
                @Override
                public void onActivityEnabled(AppCompatActivity activity) {
                    // Do manipulations with your activity
                    removeAllChildFragments(activity);
                    reloadFragment();
                }
            });
        }
    }

    public void initialize() {
        getAvailableActivity(new IActivityEnabledListener() {
            @Override
            public void onActivityEnabled(AppCompatActivity activity) {
                // Do manipulations with your activity
                removeAllChildFragments(activity);
            }
        });
        JWTDecodedModel jwtDecodedModel = ((WOneAppBaseActivity) getActivity()).getJWTDecoded();
        if (jwtDecodedModel.AtgSession != null) {
            if (jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")) {
                FragmentManager childFragMan = getChildFragmentManager();
                FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                WRewardsLoggedinAndLinkedFragment fragmentChild = new WRewardsLoggedinAndLinkedFragment();
                fragmentChild.setTargetFragment(this, FRAGMENT_CODE_1);
                childFragTrans.add(R.id.content_frame, fragmentChild);
                childFragTrans.commit();
                //user is linked and signed in
                wRewarsToolbarTitle.setText(getString(R.string.wrewards));
                mCounter.setAccountIsActive(true);
                mCounter.setActiveVoucher(1);
            } else {
                //user is not linked
                //but signed in
                mCounter.setAccountIsActive(true);
                mCounter.setActiveVoucher(1);
                FragmentManager childFragMan = getChildFragmentManager();
                FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                WRewardsLoggedinAndNotLinkedFragment fragmentChild = new WRewardsLoggedinAndNotLinkedFragment();
                fragmentChild.setTargetFragment(this, FRAGMENT_CODE_3);
                childFragTrans.add(R.id.content_frame, fragmentChild);
                childFragTrans.commit();
                wRewarsToolbarTitle.setText("");
            }
        } else {

            mCounter.setAccountIsActive(false);
            mCounter.setActiveVoucher(0);
            //user is signed out
            FragmentManager childFragMan = getChildFragmentManager();
            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
            WRewardsLoggedOutFragment fragmentChild = new WRewardsLoggedOutFragment();
            fragmentChild.setTargetFragment(this, FRAGMENT_CODE_2);
            childFragTrans.add(R.id.content_frame, fragmentChild);
            childFragTrans.commit();
            wRewarsToolbarTitle.setText("");
        }
    }

    public void removeAllChildFragments(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager(); // or 'getSupportFragmentManager();'
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            for (int i = 0; i < count; ++i) {
                fm.popBackStack();
            }
        } else {
            Log.e("fragmentNull", "fragmentNull");
        }
    }

    public void reloadFragment() {
        WRewardsFragment fragment = (WRewardsFragment)
                getFragmentManager().findFragmentById(R.id.container_body);

        getFragmentManager().beginTransaction()
                .detach(fragment)
                .attach(fragment)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}
