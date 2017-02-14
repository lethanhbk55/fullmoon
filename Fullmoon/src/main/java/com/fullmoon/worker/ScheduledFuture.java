package com.fullmoon.worker;

public interface ScheduledFuture {
	int getId();

	void cancel();
	
	long getRemainningDelay();
}
