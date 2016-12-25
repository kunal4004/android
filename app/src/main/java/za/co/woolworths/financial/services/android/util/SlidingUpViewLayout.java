package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by dimitrij on 2016/12/23.
 */

public class SlidingUpViewLayout {

    public Context mContext;

    public SlidingUpViewLayout(Context context){
        mContext = context;
    }

    public void openOverlayView() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view =  layoutInflater.inflate(R.layout.open_overlay_view, null);
        WButton mBtnNo = (WButton) view.findViewById(R.id.btnNo);
        WButton mBtnYes = (WButton) view.findViewById(R.id.btnYes);
        final PopupWindow pWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(false);
        mBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
            }
        });
        mBtnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();}
        });
    }

    public void openOverlayGotIT(String description) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View view =  layoutInflater.inflate(R.layout.open_overlay_got_it, null);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        fadeInAnimation.setDuration(300);
        view.setAnimation(fadeInAnimation);
        WButton mBtnGotIT = (WButton) view.findViewById(R.id.btnGotIT);
        WTextView  overlayDescription = (WTextView)view.findViewById(R.id.overlayDescription);
        overlayDescription.setText(description);
        final PopupWindow pWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(false);
        mBtnGotIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
            }
        });
    }
}
