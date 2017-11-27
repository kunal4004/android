package za.co.woolworths.financial.services.android.models.service;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxBus {

	public RxBus() {
	}

	private PublishSubject<Object> bus = PublishSubject.create();

	public void send(Object o) {
		bus.onNext(o);
	}

	public Observable<Object> toObservable() {
		return bus;
	}
}
