package za.co.woolworths.financial.services.android.models.service.event;

public class LoadState {

	private boolean loadComplete;

	public boolean onLoanCompleted() {
		return loadComplete;
	}

	public void setLoadComplete(boolean loadComplete) {
		this.loadComplete = loadComplete;
	}
}
