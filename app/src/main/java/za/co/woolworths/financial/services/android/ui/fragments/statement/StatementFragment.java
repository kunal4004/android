package za.co.woolworths.financial.services.android.ui.fragments.statement;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.statement.Statement;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.models.rest.GetStatements;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;


public class StatementFragment extends Fragment implements StatementAdapter.StatementListener, View.OnClickListener {

	private WButton mBtnEmailStatement;
	private StatementAdapter mStatementAdapter;
	private RelativeLayout relNextButton;
	private RecyclerView rclEStatement;
	private ConstraintLayout ctNoResultFound;
	private ConstraintLayout ccProgressLayout;
	private ErrorHandlerView mErrorHandlerView;
	private WButton mBtnRetry;

	public StatementFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.statement_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);
		listener();
		setAdapter();
		resetAdapter();
		disableButton();
	}

	private void setAdapter() {
		mStatementAdapter = new StatementAdapter(this);
		Statement datum = new Statement();
		datum.setHeader(true);
		mStatementAdapter.add(datum);
	}

	private void listener() {
		mBtnEmailStatement.setOnClickListener(this);
		mBtnRetry.setOnClickListener(this);
	}

	private void init(View view) {

		ctNoResultFound = (ConstraintLayout) view.findViewById(R.id.ctNoResultFound);
		ccProgressLayout = (ConstraintLayout) view.findViewById(R.id.ccProgressLayout);
		rclEStatement = (RecyclerView) view.findViewById(R.id.rclEStatement);
		relNextButton = (RelativeLayout) view.findViewById(R.id.relNextButton);
		mBtnEmailStatement = (WButton) view.findViewById(R.id.btnEmailStatement);
		RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		mBtnRetry = (WButton) view.findViewById(R.id.btnRetry);

		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, relativeLayout);
		mErrorHandlerView.setMargin(relativeLayout, 0, 0, 0, 0);
		setRecyclerView(rclEStatement);
	}

	private void setRecyclerView(RecyclerView rclEStatement) {
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		rclEStatement.setLayoutManager(layoutManager);
		getStatement();
	}

	@Override
	public void onItemClicked(View v, int position) {
		ArrayList<Statement> arrStatement = mStatementAdapter.getStatementList();
		Statement statement = arrStatement.get(position);
		boolean selectedByUser = statement.selectedByUser();
		/*
		  true - previously selected by user
		  set to false
		 */
		if (selectedByUser) {
			statement.setSelectedByUser(false);
		} else {
			statement.setSelectedByUser(true);
		}

		boolean arrayContainTrue = false;
		for (Statement s : arrStatement) {
			if (s.selectedByUser()) {
				arrayContainTrue = true;
			}
		}
		mStatementAdapter.updateStatementViewState(arrayContainTrue);

		if (arrayContainTrue) {
			enableButton();
		} else {
			disableButton();
		}

		mStatementAdapter.refreshBlockOverlay(position);
	}

	@Override
	public void onViewClicked(View v, int position) {
		Activity activity = getActivity();
		if (activity instanceof StatementActivity) {
			StatementActivity statementActivity = (StatementActivity) activity;
			statementActivity.checkPermission();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnEmailStatement:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_SENT_TO);
				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity()))
					getStatement();
				break;

			default:
				break;
		}
	}

	private void disableButton() {
		mBtnEmailStatement.setAlpha(1.0f);
		relNextButton.setAlpha(0.35f);
		mBtnEmailStatement.setEnabled(false);
	}

	private void enableButton() {
		mBtnEmailStatement.setAlpha(1.0f);
		relNextButton.setAlpha(1.0f);
		mBtnEmailStatement.setEnabled(true);
	}

	private void resetAdapter() {
		rclEStatement.setAdapter(mStatementAdapter);
	}

	public void getStatement() {
		onLoad();
//		Statement statement = new Statement(String.valueOf(WoolworthsApplication.getProductOfferingId()), "6007850115578203", Utils.getDate(6), Utils.getDate(0));
		Statement statement = new Statement(String.valueOf(WoolworthsApplication.getProductOfferingId()), "6007850115578203", "2017-01-01", "2017-11-27");

		GetStatements cliGetStatements = new GetStatements(getActivity(), statement, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				StatementResponse statementResponse = (StatementResponse) object;
				if (statementResponse != null) {
					Response response = statementResponse.response;
					if (statementResponse != null) {
						switch (statementResponse.httpCode) {
							case 200:
								List<Statement> statement = statementResponse.data;
								if (statement.size() == 0) {
									showView(ctNoResultFound);
								} else {
									hideView(ctNoResultFound);
									int index = 0;
									for (Statement d : statement) {
										mStatementAdapter.add(d);
										mStatementAdapter.refreshBlockOverlay(index);
										index++;
									}
								}
								break;

							case 440:
								SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), response.stsParams);
								break;

							default:
								if (response != null) {
									Utils.displayValidationMessage(getActivity(),
											CustomPopUpWindow.MODAL_LAYOUT.ERROR,
											response.desc);
								}
								break;
						}
					}
				}
				onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				onLoadComplete();
				mErrorHandlerView.networkFailureHandler(e);
			}
		});

		cliGetStatements.execute();
	}

	@Override
	public void onResume() {
		super.onResume();
		Activity activity = getActivity();
		if (activity instanceof StatementActivity) {
			StatementActivity statementActivity = (StatementActivity) getActivity();
			statementActivity.setTitle(getString(R.string.statement));
		}
	}

	public void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	public void onLoad() {
		mErrorHandlerView.hideErrorHandler();
		showView(ccProgressLayout);
	}


	public void onLoadComplete() {
		hideView(ccProgressLayout);
	}

	public void getPDFFile() {
		WoolworthsApplication mWoolWorthsApplication = ((WoolworthsApplication) StatementFragment.this.getActivity().getApplication());

		mWoolWorthsApplication.getAsyncApi().getPDFResponse(new Callback<String>() {

			@Override
			public void success(String responseBody, retrofit.client.Response response) {

				try {
					//you can now get your file in the InputStream
					InputStream is = response.getBody().in();

					File folderDir = null;
					folderDir = new File(getActivity().getExternalFilesDir("woolworth") + "/Files");

					File file = new File(folderDir, "statement.pdf");

					if (file.exists()) {
						file.delete();
					}

					if ((folderDir.mkdirs() || folderDir.isDirectory())) {
						BufferedInputStream bufferedInputStream = null;

						bufferedInputStream = new BufferedInputStream(is,
								1024 * 5);

						FileOutputStream fileOutputStream = new FileOutputStream(
								folderDir + "/" + "statement.pdf");
						byte[] buffer = new byte[1024];
						int len1 = 0;
						while ((len1 = is.read(buffer)) != -1) {
							fileOutputStream.write(buffer, 0, len1);
						}
						bufferedInputStream.close();
						fileOutputStream.close();
						is.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				File file = new File(getActivity().getExternalFilesDir("woolworth")+ "/Files/" + "statement.pdf");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),"application/pdf");
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
//				byte[] bytes = ((TypedByteArray) response.getBody()).getBytes();
//
//
//				File dir = Environment.getExternalStorageDirectory();
//
//				File assist = new File("/mnt/sdcard/Sample.pdf");
//				if (assist.exists())
//					assist.mkdir();
//				try {
//					InputStream fis = new FileInputStream(assist);
//
//					long length = assist.length();
//					if (length > Integer.MAX_VALUE) {
//						Log.e("Soileoo.", "cannnottt   readddd");
//					}
//					int offset = 0;
//					int numRead = 0;
//					while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
//						offset += numRead;
//					}
//
//					File data = new File(dir, "mydemo.pdf");
//					OutputStream op = new FileOutputStream(data);
//					op.write(bytes);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}

			}

			@Override
			public void failure(RetrofitError error) {

			}
		});

	}
}
