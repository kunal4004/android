package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.io.Serializable;
import java.util.ArrayList;

import za.co.woolworths.financial.services.android.util.photo.PhotoDraweeView;


public class MultipleImageActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList mAuxiliaryImages;
    private PhotoDraweeView mProductImage;
    private int mCurrentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.product_multiple_images);
        getBundle();
        initView();
        setImage();
    }


    private void getBundle() {
        Intent extras = getIntent();
        if (extras != null) {
            mCurrentPosition = extras.getExtras().getInt("position");
            Serializable mSerialiseAuxiliaryImages =
                    getIntent().getSerializableExtra("auxiliaryImages");
            if (mSerialiseAuxiliaryImages != null)
                mAuxiliaryImages = ((ArrayList) mSerialiseAuxiliaryImages);
        }
    }


    private void setImage() {
        mProductImage.setPhotoUri(Uri.parse(mAuxiliaryImages.get(mCurrentPosition).toString()), mProductImage);
    }

    private void initView() {
        ImageView mCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
        mProductImage = (PhotoDraweeView) findViewById(R.id.imProductView);
        mCloseProduct.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        closeView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imCloseProduct:
                closeView();
                break;
        }
    }

    private void closeView() {
        finish();
        overridePendingTransition(0, 0);
    }
}
