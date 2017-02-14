package com.fullmoon.worker.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fullmoon.worker.ScheduledFuture;

class ScheduledFutureImpl implements ScheduledFuture {
	private java.util.concurrent.ScheduledFuture<?> future;
	private int id;
	private static final AtomicInteger idSeed = new AtomicInteger();

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

	public int getId() {
		return id;
	}

	private void generateId() {
		this.id = idSeed.incrementAndGet();
	}

	@Override
	public long getRemainningDelay() {
		return this.future.getDelay(TimeUnit.MILLISECONDS);
	}
}
