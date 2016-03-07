package com.fullmoon.worker;

import com.fullmoon.task.Task;

public interface WorkerPool<T> {
	void execute(Task<T> task, T data);

	void shutdown();
}
