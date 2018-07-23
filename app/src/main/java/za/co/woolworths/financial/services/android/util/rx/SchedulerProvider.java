package za.co.woolworths.financial.services.android.util.rx;

import io.reactivex.Scheduler;

public interface SchedulerProvider {

	Scheduler ui();

	Scheduler computation();

	Scheduler io();

}
