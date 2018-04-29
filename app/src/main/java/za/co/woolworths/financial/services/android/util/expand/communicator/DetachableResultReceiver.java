package za.co.woolworths.financial.services.android.util.expand.communicator;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DetachableResultReceiver extends ResultReceiver {

	private Receiver mReceiver;

	public DetachableResultReceiver(Handler handler) {
		super(handler);
	}

	public void clearReceiver() {
		mReceiver = null;
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public interface Receiver {
		void onReceiveResult(int resultCode, Bundle resultData);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			mReceiver.onReceiveResult(resultCode, resultData);
		}
	}
}