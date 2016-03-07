package com.fullmoon.job;

@SuppressWarnings("rawtypes")
public interface JobHandler {
	void onJobFinished(Job job);

	void onJobTimedOut(Job job);
}
