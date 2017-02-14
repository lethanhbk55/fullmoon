package com.fullmoon.worker.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fullmoon.task.Task;
import com.fullmoon.worker.ScheduledCallback;
import com.fullmoon.worker.ScheduledWorkerPool;
import com.fullmoon.worker.WorkerPool;

public class ScheduledWorkerPoolImpl<T> implements ScheduledWorkerPool<T> {

	private ScheduledExecutorService service;
	private WorkerPool<T> workerPool;
	private final Map<Integer, ScheduledFuture<?>> schuduleIdMapping = new ConcurrentHashMap<>();

	public ScheduledWorkerPoolImpl(ScheduledExecutorService service, WorkerPool<T> workerPool) {
		this.service = service;
		this.workerPool = workerPool;
	}

	@Override
	public void execute(Task<T> task, T data) {
		this.workerPool.execute(task, data);
	}

	@Override
	public void shutdown() {
		if (service != null) {
			this.service.shutdown();
			try {
				if (this.service.awaitTermination(3, TimeUnit.SECONDS)) {
					this.service.shutdownNow();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (this.workerPool != null) {
			this.workerPool.shutdown();
		}
	}

	@Override
	public com.fullmoon.worker.ScheduledFuture schuduleAtFixRate(long delay, long period, int times,
			ScheduledCallback callback) {
		ScheduledFutureImpl scheduledFutureImpl = new ScheduledFutureImpl();
		ScheduledFuture<?> future = this.service.scheduleAtFixedRate(new Runnable() {
			int count = 0;

			@Override
			public void run() {
				if (times > 0) {
					count++;
					if (count > times) {
						schuduleIdMapping.get(scheduledFutureImpl.getId()).cancel(true);
						return;
					}
				}

				callback.call();
			}
		}, delay, period, TimeUnit.MILLISECONDS);
		this.schuduleIdMapping.put(scheduledFutureImpl.getId(), future);
		scheduledFutureImpl.setFuture(future);
		return scheduledFutureImpl;
	}
}
