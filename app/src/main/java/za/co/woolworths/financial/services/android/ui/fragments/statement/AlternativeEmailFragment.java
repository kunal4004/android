package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.statement.EmailStatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.StatementUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class AlternativeEmailFragment extends Fragment implements View.OnClickListener, NetworkChangeListener {

	private WLoanEditTextView etAlternativeEmailAddress;
	private WButton btnSendEmail;
	private RelativeLayout relEmailStatement;
	private StatementUtils mStatementUtils;
	private BroadcastReceiver mConnectionBroadcast;
	private LoadState loadState;
	private Call<SendUserStatementResponse>  sendUserStatement;
	private ProgressBar mWoolworthsProgressBar;
	private SendUserStatementRequest mSendUserStatementRequest;
	private String mUserStatement;
	private String mAlternativeEmail;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alternative_email_statement_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mStatementUtils = new StatementUtils(getActivity());
		Bundle argument = getArguments();
		if (argument != null) {
			mUserStatement = argument.getString(StatementActivity.SEND_USER_STATEMENT);
		}
		initView(view);
		disableButton();
		mSendUserStatementRequest = new Gson().fromJson(mUserStatement, SendUserStatementRequest.class);
	}

	private void initView(View view) {
		etAlternativeEmailAddress = (WLoanEditTextView) view.findViewById(R.id.etAlternativeEmailAddress);
		etAlternativeEmailAddress.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		relEmailStatement = (RelativeLayout) view.findViewById(R.id.relEmailStatement);
		btnSendEmail = (WButton) view.findViewById(R.id.btnSendEmail);
		mWoolworthsProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
		showKeyboard();
		onTextChangeListener();
		listener();
		loadState = new LoadState();
		loadSuccess();
		mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
	}


	private void listener() {
		btnSendEmail.setOnClickListener(this);
		WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener =
				new WLoanEditTextView.OnKeyPreImeListener() {
					@Override
					public void onBackPressed() {
						Activity activity = getActivity();
						if (activity instanceof StatementActivity) {
							activity.onBackPressed();
						}
					}
				};

		etAlternativeEmailAddress.setOnKeyPreImeListener(onKeyPreImeListener);
	}

	private void onTextChangeListener() {
		etAlternativeEmailAddress.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String result = s.toString().replaceAll(" ", "");
				if (!s.toString().equals(result)) {
					etAlternativeEmailAddress.setText(result);
					etAlternativeEmailAddress.setSelection(result.length());
				}
				if (s.length() >= 5) {
					enableButton();
				} else {
					disableButton();
				}
			}
		});
	}

	private void enableButton() {
		mStatementUtils.enableView(relEmailStatement);
		mStatementUtils.enableView(btnSendEmail);
	}

	private void disableButton() {
		mStatementUtils.disableView(relEmailStatement);
		mStatementUtils.disableView(btnSendEmail);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		hideKeyboard();
	}

	public void showKeyboard() {
		Activity activity = getActivity();
		if (activity != null) {
			etAlternativeEmailAddress.requestFocus();
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
	}

	public void hideKeyboard() {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.hideSoftInputFromWindow(etAlternativeEmailAddress.getWindowToken(), 0);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSendEmail:
				mAlternativeEmail = etAlternativeEmailAddress.getText().toString().trim();
				if (mStatementUtils.validateEmail(mAlternativeEmail)) {
					Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
					etAlternativeEmailAddress.setCompoundDrawablesWithIntrinsicBounds(null, null, transparentDrawable, null);
					mSendUserStatementRequest.to = mAlternativeEmail;
					sendStatement();
				} else {
					Drawable img = getContext().getResources().getDrawable(R.drawable.validation_error_drawable);
					etAlternativeEmailAddress.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
					ErrorHandlerView errorHandlerView = new ErrorHandlerView(getActivity());
					errorHandlerView.showToast(getString(R.string.email_validation_error_message));
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Activity activity = getActivity();
		Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.STATEMENTS_ALTERNATIVE_EMAIL);
		if (activity instanceof StatementActivity) {
			showKeyboard();
			activity.registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	@Override
	public void onConnectionChanged() {
		retryConnect();
	}


	private void loadSuccess() {
		loadState.setLoadComplete(true);
	}

	private void loadFailure() {
		loadState.setLoadComplete(false);
	}


	private void retryConnect() {
		final Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
						if (!loadState.onLoanCompleted()) {
							btnSendEmail.performClick();
						}
					}
				}
			});
		}
	}


	public void sendStatement() {
		onLoad();

		sendUserStatement = OneAppService.INSTANCE.sendStatementRequest(mSendUserStatementRequest);
		sendUserStatement.enqueue(new CompletionHandler<>(new RequestListener<SendUserStatementResponse>() {
			@Override
			public void onSuccess(SendUserStatementResponse sendUserStatementResponse) {
				if (sendUserStatementResponse != null) {
					switch (sendUserStatementResponse.httpCode) {
						case 200:
							List<EmailStatementResponse> data = sendUserStatementResponse.data;
							EmailStatementResponse emailResponse = data.get(0);
							if (emailResponse.sent) {
								hideKeyboard();
								FragmentUtils fragmentUtils = new FragmentUtils();
								Bundle bundle = new Bundle();
								bundle.putString("alternativeEmail", mAlternativeEmail);
								EmailStatementFragment emailStatementFragment = new EmailStatementFragment();
								emailStatementFragment.setArguments(bundle);
								fragmentUtils.nextFragment((AppCompatActivity) AlternativeEmailFragment.this.getActivity(), getFragmentManager().beginTransaction(), emailStatementFragment, R.id.flEStatement);
							} else {
								hideKeyboard();
								Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.statement_send_email_false_desc));
							}
							break;
						case 440:
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, sendUserStatementResponse.response.stsParams, getActivity());
							break;
						default:
							break;
					}
				}
				loadSuccess();
				onLoadComplete();
			}

			@Override
			public void onFailure(Throwable error) {
				final Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ErrorHandlerView errorHandlerView = new ErrorHandlerView(activity);
							errorHandlerView.showToast();
							loadFailure();
							onLoadComplete();
						}
					});
				}
			}
		},SendUserStatementResponse.class));
	}

	public void onLoad() {
		mWoolworthsProgressBar.setVisibility(View.VISIBLE);
		btnSendEmail.setVisibility(View.GONE);
	}

	public void onLoadComplete() {
		btnSendEmail.setVisibility(View.VISIBLE);
		mWoolworthsProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mStatementUtils != null) {
			mStatementUtils.cancelRequest(sendUserStatement);
		}
	}
}
