package com.fullmoon.job;

import java.util.List;

import com.fullmoon.task.Task;

public interface Job<T> {
	long getId();

	List<Task<T>> getTasks();

	JobResult getResult();

	void execute(T data);

	void resume();

	void setTaskSwitcher(TaskSwitcher<T> taskSwitcher);

	void rollback();

	T getData();

	byte[] serialize();

	boolean isSuccessful();

	boolean isRunning();

	Task<T> getLastDidTask();

	String getReport();

	long getExecuteTime();

	void setHandler(JobHandler handler);
}
