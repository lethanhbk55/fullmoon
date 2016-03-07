package com.fullmoon.worker;

public interface ScheduledFuture {
	long getId();

	void cancel();
}
