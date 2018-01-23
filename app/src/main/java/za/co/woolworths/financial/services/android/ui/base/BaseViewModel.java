package za.co.woolworths.financial.services.android.ui.base;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;

import io.reactivex.disposables.CompositeDisposable;
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
}
