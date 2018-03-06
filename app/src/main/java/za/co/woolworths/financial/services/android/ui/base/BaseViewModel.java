package za.co.woolworths.financial.services.android.ui.base;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.text.TextUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public abstract class BaseViewModel<N> extends ViewModel {

	private N mNavigator;
	private SchedulerProvider mSchedulerProvider = null;
	private final ObservableBoolean mIsLoading = new ObservableBoolean(false);

	private CompositeDisposable mCompositeDisposable;

	public BaseViewModel() {
		this.mCompositeDisposable = new CompositeDisposable();
	}

	public BaseViewModel(
			SchedulerProvider schedulerProvider) {
		this.mSchedulerProvider = schedulerProvider;
		this.mCompositeDisposable = new CompositeDisposable();
	}

	public void setNavigator(N navigator) {
		this.mNavigator = navigator;
	}

	public N getNavigator() {
		return mNavigator;
	}

	public SchedulerProvider getSchedulerProvider() {
		return mSchedulerProvider;
	}

	public CompositeDisposable getCompositeDisposable() {
		return mCompositeDisposable;
	}

	public ObservableBoolean getIsLoading() {
		return mIsLoading;
	}

	public void setIsLoading(boolean isLoading) {
		mIsLoading.set(isLoading);
	}

	public String isEmpty(String value) {
		if (TextUtils.isEmpty(value)) {
			return "";
		} else {
			return value;
		}
	}

	@Override
	protected void onCleared() {
		mCompositeDisposable.dispose();
		super.onCleared();
	}

	public void cancelRequest(HttpAsyncTask httpAsyncTask) {
		setIsLoading(false);
		if (httpAsyncTask != null) {
			if (httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	public void consumeObservable(Consumer consumer) {
		mCompositeDisposable.add(WoolworthsApplication.getInstance()
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(consumer));
	}
}
