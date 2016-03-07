package com.fullmoon.job.schedule;

import com.fullmoon.job.Job;

public interface ScheduledJob<T> extends Job<T> {

	void execute(T data, int delay, int period, int times);
}
