package com.fullmoon.worker;

public interface RoutingWorkerPool<T> extends WorkerPool<T> {

	void register(String domain, WorkerPool<T> workerPool);

	void deregister(String domain);
}
