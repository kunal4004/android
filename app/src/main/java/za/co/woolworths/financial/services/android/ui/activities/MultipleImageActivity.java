package za.co.woolworths.financial.services.android.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.photo.PhotoDraweeView;


public class MultipleImageActivity extends AppCompatActivity implements View.OnClickListener {

	private String mAuxiliaryImages;
	private PhotoDraweeView mProductImage;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.product_multiple_images);
		getBundle();
		initView();
		setImage();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_DETAIL_IMAGE_ZOOM);
	}

	private void getBundle() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mAuxiliaryImages = extras.getString("auxiliaryImages");
		}
	}


	private void setImage() {
		mProductImage.setPhotoUri(Uri.parse(mAuxiliaryImages), mProductImage);
	}

	private void initView() {
		ImageView mCloseProduct = findViewById(R.id.imClose);
		mProductImage = findViewById(R.id.imProductView);
		mCloseProduct.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		closeView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imClose:
				closeView();
				break;
		}
	}

	private void closeView() {
		finish();
		overridePendingTransition(0, 0);
	}
}
