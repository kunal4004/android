package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.Utils;

public class TransientActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout mRelRootContainer;
    private Animation mPopEnterAnimation;
    private RelativeLayout mRelPopContainer;
    private boolean viewWasClicked = false;

    private static final int ANIM_DOWN_DURATION = 500;

    public enum VALIDATION_MESSAGE_LIST {
        CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD,
        HIGH_LOAN_AMOUNT, LOW_LOAN_AMOUNT, STORE_LOCATOR_DIRECTION, SIGN_OUT, BARCODE_ERROR,
        SHOPPING_LIST_INFO
    }

    VALIDATION_MESSAGE_LIST current_view;
    String description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this,android.R.color.transparent);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            current_view = (VALIDATION_MESSAGE_LIST) mBundle.getSerializable("key");
            description = mBundle.getString("description");
            if (TextUtils.isEmpty(description)) { //avoid nullpointerexception
                description = "";
            }
            displayView(current_view);
        } else {
            finish();
        }
    }

    private void displayView(VALIDATION_MESSAGE_LIST current_view) {
        switch (current_view) {
            case BARCODE_ERROR:
                setContentView(R.layout.transparent_activity);
                mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
                setAnimation();
                WButton wButton = (WButton) findViewById(R.id.btnBarcodeOk);
                wButton.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case SHOPPING_LIST_INFO:
                setContentView(R.layout.shopping_list_info);
                mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
                if (description.equalsIgnoreCase("viewShoppingList")) {
                    findViewById(R.id.shoppingListDivider).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnViewShoppingList).setVisibility(View.VISIBLE);
                }
                setAnimation();
                WButton wButtonOk = (WButton) findViewById(R.id.btnShopOk);
                WButton wBtnViewShoppingList = (WButton) findViewById(R.id.btnViewShoppingList);
                wButtonOk.setOnClickListener(this);
                wBtnViewShoppingList.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;
        }
    }

    private void startExitAnimation() {
        if (!viewWasClicked) { // prevent more than one click
            viewWasClicked = true;
            TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
            animation.setFillAfter(true);
            animation.setDuration(ANIM_DOWN_DURATION);
            animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    dismissLayout();
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }

    private void dismissLayout() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void setAnimation() {
        mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.popup_enter);
        mRelRootContainer.startAnimation(mPopEnterAnimation);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnBarcodeOk:
            case R.id.relPopContainer:
            case R.id.btnShopOk:
            case R.id.btnOK:
                startExitAnimation();
                break;

            case R.id.btnViewShoppingList:
                Intent shoppingList = new Intent(this, ShoppingListActivity.class);
                startActivity(shoppingList);
                dismissLayout();
                break;
        }
    }
}
