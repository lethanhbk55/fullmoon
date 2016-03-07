package com.fullmoon.task;

import com.fullmoon.job.Job;
import com.fullmoon.worker.WorkerPool;

public interface Task<T> {

	void setStartedTime(long timeStart);

	String getName();

	void execute(T data);

	void execute(T data, WorkerPool<T> workerPool);

	void rollback();

	void setJob(Job<T> job);

	boolean isSuccessful();

	boolean isTimeout();

	void setTimeout(boolean isTimeout);

	byte[] serialize();

	TaskFailureData getFailureData();

	long getExecuteTime();
}
