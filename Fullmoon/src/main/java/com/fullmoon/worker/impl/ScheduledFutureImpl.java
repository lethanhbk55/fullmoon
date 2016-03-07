package com.fullmoon.worker.impl;

import com.fullmoon.worker.ScheduledFuture;

class ScheduledFutureImpl implements ScheduledFuture {
	private java.util.concurrent.ScheduledFuture<?> future;
	private long id;
	private static long idSeed = 0;

	public ScheduledFutureImpl() {
		generateId();
	}

	@Override
	public void cancel() {
		this.future.cancel(true);
	}

	void setFuture(java.util.concurrent.ScheduledFuture<?> future) {
		this.future = future;
	}

	public long getId() {
		return id;
	}

	private void generateId() {
		this.id = idSeed++;
	}
}
