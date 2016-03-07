package com.fullmoon.worker.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fullmoon.job.routing.RoutingTask;
import com.fullmoon.task.Task;
import com.fullmoon.worker.RoutingWorkerPool;
import com.fullmoon.worker.WorkerPool;

public class RoutingWorkerPoolImpl<T> implements RoutingWorkerPool<T> {
	private WorkerPool<T> defaultWorkerPool;
	private Map<String, WorkerPool<T>> domainWorkerPools = new ConcurrentHashMap<>();

	public RoutingWorkerPoolImpl(WorkerPool<T> defaultWorkerPool) {
		this.defaultWorkerPool = defaultWorkerPool;
	}

	@Override
	public void execute(Task<T> task, T data) {
		if (task instanceof RoutingTask) {
			RoutingTask<T> routingTask = (RoutingTask<T>) task;
			if (domainWorkerPools.containsKey(routingTask.getDomain())) {
				domainWorkerPools.get(routingTask.getDomain()).execute(routingTask, data);
			} else {
				defaultWorkerPool.execute(routingTask, data);
			}
		} else {
			defaultWorkerPool.execute(task, data);
		}
	}

	@Override
	public void shutdown() {
		if (this.defaultWorkerPool != null) {
			this.defaultWorkerPool.shutdown();
		}

		for (WorkerPool<T> workerPool : domainWorkerPools.values()) {
			workerPool.shutdown();
		}
	}

	@Override
	public void register(String domain, WorkerPool<T> workerPool) {
		this.domainWorkerPools.put(domain, workerPool);
	}

	@Override
	public void deregister(String domain) {
		this.domainWorkerPools.remove(domain);
	}

}
