package com.fullmoon.job.schedule;

import com.fullmoon.job.AbstractJob;
import com.fullmoon.worker.ScheduledCallback;
import com.fullmoon.worker.ScheduledFuture;
import com.fullmoon.worker.ScheduledWorkerPool;

public abstract class AbstractScheduledJob<T> extends AbstractJob<T> implements ScheduledJob<T> {
	private ScheduledFuture future;

	@Override
	public final void execute(T data, int delay, int period, int times) {
		ScheduledWorkerPool<T> workerPool = (ScheduledWorkerPool<T>) getWorkerPool();
		this.future = workerPool.schuduleAtFixRate(delay, period, times, new ScheduledCallback() {

			@Override
			public void call() {
				AbstractScheduledJob.this.execute(data);
			}
		});
	}

	protected void done() {
		if (future != null) {
			future.cancel();
		}
	}
}
