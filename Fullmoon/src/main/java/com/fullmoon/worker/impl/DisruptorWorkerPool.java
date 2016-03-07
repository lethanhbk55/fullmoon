package com.fullmoon.worker.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.fullmoon.task.Task;
import com.fullmoon.task.TaskEvent;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;

public class DisruptorWorkerPool<T> implements ExceptionHandler<TaskEvent<T>>, com.fullmoon.worker.WorkerPool<T> {
	private WorkerPool<TaskEvent<T>> workerPool;
	private RingBuffer<TaskEvent<T>> ringBuffer;

	@SuppressWarnings("hiding")
	private class MessageHandler<T> implements WorkHandler<TaskEvent<T>> {

		@Override
		public void onEvent(TaskEvent<T> data) throws Exception {
			Task<T> task = data.getTask();
			T puo = data.getData();
			if (task != null) {
				task.execute(puo);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public DisruptorWorkerPool(int poolSize, int ringBufferSize, String threadNamePattern) {
		RingBuffer<TaskEvent<T>> _ringBuffer = RingBuffer.createMultiProducer(new EventFactory<TaskEvent<T>>() {

			@Override
			public TaskEvent<T> newInstance() {
				return new TaskEvent<T>();
			}
		}, ringBufferSize);

		MessageHandler<T>[] workers = new MessageHandler[poolSize];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new MessageHandler<T>();
		}

		workerPool = new WorkerPool<TaskEvent<T>>(_ringBuffer, _ringBuffer.newBarrier(), this, workers);
		_ringBuffer.addGatingSequences(this.workerPool.getWorkerSequences());
		ringBuffer = this.workerPool.start(new ThreadPoolExecutor(workers.length, workers.length, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private AtomicLong threadNumber = new AtomicLong(1);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format(threadNamePattern, threadNumber.getAndIncrement()));
					}
				}));
	}

	public DisruptorWorkerPool(int poolSize, int ringBufferSize) {
		this(poolSize, ringBufferSize, "Task Worker #%d");
	}

	@Override
	public void execute(Task<T> task, T data) {
		long sequence = this.ringBuffer.next();
		try {
			TaskEvent<T> event = this.ringBuffer.get(sequence);
			event.setTask(task);
			event.setData(data);
		} finally {
			this.ringBuffer.publish(sequence);
		}
	}

	@Override
	public void handleOnShutdownException(Throwable arg0) {

	}

	@Override
	public void handleOnStartException(Throwable arg0) {

	}

	@Override
	public void handleEventException(Throwable arg0, long arg1, TaskEvent<T> arg2) {

	}

	@Override
	public void shutdown() {
		System.out.println("shutting down worker pool...");

		if (this.workerPool != null) {
			this.workerPool.drainAndHalt();
		}
	}
}
