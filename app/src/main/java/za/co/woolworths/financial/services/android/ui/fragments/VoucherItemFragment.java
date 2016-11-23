package za.co.woolworths.financial.services.android.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.text.ParseException;

import za.co.wigroup.androidutils.Util;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.ui.activities.VoucherTermsAndConditionsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WebViewActivity;
import za.co.woolworths.financial.services.android.util.WFormatter;

@SuppressLint("ValidFragment")
public class VoucherItemFragment extends Fragment
{
	private final Voucher mVoucher;

	AsyncTask<Bundle, Void, Bitmap> task;
	public VoucherItemFragment(Voucher voucher)
	{
		mVoucher = voucher;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		final Activity activity = this.getActivity();
		View view = inflater.inflate(R.layout.w_reward_detail_fragment, null);
		((TextView) view.findViewById(R.id.w_rewards_fragment_description)).setText(mVoucher.description.toUpperCase());

		if ("PERCENTAGE".equals(mVoucher.type))
		{
			((TextView) view.findViewById(R.id.w_rewards_fragment_value)).setText(String.valueOf(WFormatter.formatPercent(mVoucher.amount)));
		}
		else
		{
			((TextView) view.findViewById(R.id.w_rewards_fragment_value)).setText(String.valueOf(WFormatter.formatAmount(mVoucher.amount)));
		}
		((TextView) view.findViewById(R.id.w_rewards_fragment_minimum_spend)).setText(String.valueOf(WFormatter.formatAmount(mVoucher.minimumSpend)));
		TextView fromDate = (TextView) view.findViewById(R.id.w_rewards_fragment_valid_from);
		try
		{
			fromDate.setText(String.valueOf(WFormatter.formatDate(mVoucher.validFromDate)));
		}
		catch (ParseException e)
		{
			fromDate.setText(String.valueOf(mVoucher.validFromDate));
		}
		TextView toDate = (TextView) view.findViewById(R.id.w_rewards_fragment_valid_to);
		try
		{
			toDate.setText(String.valueOf(WFormatter.formatDate(mVoucher.validToDate)));
		}
		catch (ParseException e)
		{

			toDate.setText(String.valueOf(mVoucher.validToDate));
		}
		view.findViewById(R.id.w_reward_detail_terms).setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mVoucher.termsAndConditions == null || mVoucher.termsAndConditions.isEmpty())
				{

					Intent i =new Intent(activity, WebViewActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("title","WREWARDS T&Cs");
					bundle.putString("link", WoolworthsApplication.getWrewardsTCLink());
					i.putExtra("Bundle",bundle);
					startActivity(i);

					//Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.rewards_terms_and_conditions_url)));
					//startActivity(browserIntent);
				}
				else
				{
					Intent intent = new Intent(getActivity(), VoucherTermsAndConditionsActivity.class);
					intent.putExtra(VoucherTermsAndConditionsActivity.TNC, mVoucher.termsAndConditions);
					startActivity(intent);
				}
			}
		});

		// Barcode
		((TextView) view.findViewById(R.id.w_reward_detail_barcode_text)).setText(WFormatter.formatVoucher(mVoucher.voucherNumber));

		// http://stackoverflow.com/questions/7733813/how-can-you-tell-when-a-layout-has-been-drawn
		// Barcode generation must start after the screen is laid out, so that we know how much white space is availiable when determining the size of the barcode.
		// By doing this we can attempt to fill the white space.
		ViewTreeObserver observer = view.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
				{
					getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				else
				{
					getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}

				generateBarcode();
			}
		});

		return view;
	}

	private void generateBarcode()
	{
		// Calculate desired dimensions of barcode
		int availableHeight = getView().findViewById(R.id.w_reward_detail_whitespace0).getHeight() + getView().findViewById(R.id.w_reward_detail_whitespace1).getHeight();

		int minHeight = Util.dpToPx(52);
		int maxHeight = Util.dpToPx(128);

		int height = Math.min(Math.max(availableHeight, minHeight), maxHeight); // Height of the barcode will be the remaining white space on the screen, clamped between 52-128 dp
		int width  = getView().getWidth() - Util.dpToPx(16); // -16dp to add a margin to the left and right.

		// Generate Barcode
		Bundle args = new Bundle();
		args.putString("voucher", mVoucher.voucherNumber);
		args.putInt("width", width);
		args.putInt("height", height);

		 task = new AsyncTask<Bundle, Void, Bitmap>()
		{
			@Override
			protected Bitmap doInBackground(Bundle... params)
			{
				if (params != null && params.length > 0)
				{
					try
					{
						Bundle args = params[0];

						String data = args.getString("voucher").replaceAll("\\s+", ""); // Remove whitespace
						int width = args.getInt("width");
						int height = args.getInt("height");

						Code128Writer writer = new Code128Writer();
						BitMatrix bm = writer.encode(data, BarcodeFormat.CODE_128, width, height);
						Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

						int foreground = getResources().getColor(android.R.color.black);
						int background = getResources().getColor(android.R.color.transparent);

						for (int i = 0; i < width; i++)
						{
							for (int j = 0; j < height; j++)
							{
								bitmap.setPixel(i, j, bm.get(i, j) ? foreground : background);
							}
						}

						return bitmap;
					}
					catch (WriterException e)
					{
						e.printStackTrace();
						WiGroupLogger.e(getActivity().getApplicationContext(), "VoucherItemFragment", "Error generating barcode image", e);
					}
				}
				else
				{
					// Should not be possible since the bundle params are set by the method calling this async task.
					WiGroupLogger.wtf(getActivity().getApplicationContext(), "VoucherItemFragment", "Bundle parameters are empty, unable to generate a barcode.");
				}

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap bitmap)
			{
				super.onPostExecute(bitmap);
				((ImageView) getView().findViewById(R.id.w_reward_detail_barcode_img)).setImageBitmap(bitmap);

			}
		};

		task.execute(args);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		task.cancel(true);
	}
}