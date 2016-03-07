package com.fullmoon.task;

import com.fullmoon.job.Job;
import com.fullmoon.worker.WorkerPool;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

public abstract class AbstractTask<T> extends BaseEventDispatcher implements Task<T> {
	private String name;
	private Job<T> job;
	private boolean isSuccessful;
	private boolean isTimeout;
	private long startedTime;
	private long endedTime;
	private TaskFailureData taskFailureData;

	public AbstractTask() {
		setName(this.getClass().getSimpleName());
	}

	public long getStartedTime() {
		return startedTime;
	}

	public void setStartedTime(long startedTime) {
		this.startedTime = startedTime;
	}

	public AbstractTask(String name) {
		this();
		setName(name);
	}

	@Override
	public void execute(T data, WorkerPool<T> workerPool) {
		workerPool.execute(this, data);
	}

	protected void completed() {
		getLogger().debug("DONE: {}", this.getName());
		setEndedTime(System.currentTimeMillis());
		this.dispatchEvent("taskCompleted", "task", this);
	}

	public abstract void execute(T data);

	@Override
	public byte[] serialize() {
		PuArray array = new PuArrayList();
		writePuArray(array);
		return array.toBytes();
	}

	public void readPuArray(PuArray array) {
		this.name = array.remove(0).getString();
		this.isSuccessful = array.remove(0).getBoolean();
		this.startedTime = array.remove(0).getLong();
		this.endedTime = array.remove(0).getLong();
	}

	private void writePuArray(PuArray array) {
		array.addFrom(this.getClass().getName());
		array.addFrom(this.name);
		array.addFrom(this.isSuccessful);
		array.addFrom(this.startedTime);
		array.addFrom(this.endedTime);
	}

	@Override
	public long getExecuteTime() {
		return this.endedTime - this.startedTime;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Job<T> getJob() {
		return job;
	}

	@Override
	public void setJob(Job<T> job) {
		this.job = job;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	protected void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}

	public boolean isTimeout() {
		return isTimeout;
	}

	public void setTimeout(boolean isTimeout) {
		this.isTimeout = isTimeout;
	}

	public long getEndedTime() {
		return endedTime;
	}

	public void setEndedTime(long endTime) {
		this.endedTime = endTime;
	}

	public TaskFailureData getFailureData() {
		return taskFailureData;
	}

	protected void setFailureData(TaskFailureData taskFailureData) {
		this.taskFailureData = taskFailureData;
	}
}
