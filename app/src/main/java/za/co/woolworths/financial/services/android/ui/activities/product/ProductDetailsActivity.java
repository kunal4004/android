package za.co.woolworths.financial.services.android.ui.activities.product;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductDetailsActivity extends AppCompatActivity {

	ProductDetailsFragmentNew productDetailsFragmentNew;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.product_details_activity);
		Bundle bundle = getIntent().getExtras();
		productDetailsFragmentNew = new ProductDetailsFragmentNew();
		productDetailsFragmentNew.setArguments(bundle);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, productDetailsFragmentNew).commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (productDetailsFragmentNew != null)
			productDetailsFragmentNew.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (productDetailsFragmentNew != null)
			productDetailsFragmentNew.onRequestPermissionsResult(requestCode, permissions, grantResults);

	}
}
