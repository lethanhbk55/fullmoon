package com.fullmoon.worker;

public interface ScheduledWorkerPool<T> extends WorkerPool<T> {

	ScheduledFuture schuduleAtFixRate(long delay, long period, int times, ScheduledCallback callback);
}
