package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.util.UpdateNavDrawerTitle;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsFragment extends Fragment{
    public static final int FRAGMENT_CODE_1=1;
    public static final int FRAGMENT_CODE_2=2;
    public static final int FRAGMENT_CODE_3=2;

    UpdateNavDrawerTitle updateTitle;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.wrewards_fragment, container, false);
        /*FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.add(R.id.content_frame,new WRewardsLoggedinAndNotLinkedFragment());
        childFragTrans.commit();*/
        updateTitle = (UpdateNavDrawerTitle) getActivity();

        initialize();
        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FRAGMENT_CODE_1 && resultCode == Activity.RESULT_OK) {

            reloadFragment();
        }
        else if(resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()){
            removeAllChildFragments();

            reloadFragment();
        }
    }
    public void initialize()
    {
        removeAllChildFragments();
        JWTDecodedModel jwtDecodedModel = ((WOneAppBaseActivity)getActivity()).getJWTDecoded();
        if(jwtDecodedModel.AtgSession != null){
            if(jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")){
                FragmentManager childFragMan = getChildFragmentManager();
                FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                WRewardsLoggedinAndLinkedFragment fragmentChild = new WRewardsLoggedinAndLinkedFragment();
                fragmentChild.setTargetFragment(this, FRAGMENT_CODE_1);
                childFragTrans.add(R.id.content_frame,fragmentChild);
                childFragTrans.commit();
                //user is linked and signed in
                updateTitle.onTitleUpdate(getString(R.string.wrewards));
            } else{
                //user is not linked
                //but signed in
                FragmentManager childFragMan = getChildFragmentManager();
                FragmentTransaction childFragTrans = childFragMan.beginTransaction();
                WRewardsLoggedinAndNotLinkedFragment fragmentChild = new WRewardsLoggedinAndNotLinkedFragment();
                fragmentChild.setTargetFragment(this, FRAGMENT_CODE_3);
                childFragTrans.add(R.id.content_frame,fragmentChild);
                childFragTrans.commit();
                updateTitle.onTitleUpdate("");
            }
        }else{
            //user is signed out
            FragmentManager childFragMan = getChildFragmentManager();
            FragmentTransaction childFragTrans = childFragMan.beginTransaction();
            WRewardsLoggedOutFragment fragmentChild = new WRewardsLoggedOutFragment();
            fragmentChild.setTargetFragment(this, FRAGMENT_CODE_2);
            childFragTrans.add(R.id.content_frame,fragmentChild);
            childFragTrans.commit();
            updateTitle.onTitleUpdate("");
        }
    }

    public void removeAllChildFragments()
    {
        FragmentManager fm = getFragmentManager(); // or 'getSupportFragmentManager();'
        int count = fm.getBackStackEntryCount();
        for(int i = 0; i < count; ++i) {
            fm.popBackStack();
        }

    }

    public void reloadFragment()
    {
        WRewardsFragment fragment = (WRewardsFragment)
                getFragmentManager().findFragmentById(R.id.container_body);

        getFragmentManager().beginTransaction()
                .detach(fragment)
                .attach(fragment)
                .commit();
    }

}
