package com.fullmoon.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.fullmoon.job.factory.JobFactory;
import com.fullmoon.job.manager.JobManager;
import com.fullmoon.test.jobs.Job1;
import com.fullmoon.worker.impl.DisruptorWorkerPool;
import com.fullmoon.worker.impl.RoutingWorkerPoolImpl;
import com.fullmoon.worker.impl.ScheduledWorkerPoolImpl;

public class SequenceTaskSample {
	// static {
	// Initializer.bootstrap(SequenceTaskSample.class);
	// }

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
		JobFactory factory = new JobFactory();
		DisruptorWorkerPool<Map<String, Object>> workerPool = new DisruptorWorkerPool<Map<String, Object>>(8, 1024);
		factory.setWorkerPool(workerPool);
		factory.setScheduledWorkerPool(new ScheduledWorkerPoolImpl<Map<String, Object>>(service, workerPool));
		factory.setRoutingWorkerPool(new RoutingWorkerPoolImpl<Map<String, Object>>(workerPool));
		factory.getRoutingWorkerPool().register("VTT",
				new DisruptorWorkerPool<Map<String, Object>>(1, 512, "VTT Worker #%d"));

		JobManager manager = new JobManager(1000);
		manager.start();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				factory.getWorkerPool().shutdown();
				factory.getRoutingWorkerPool().shutdown();
				factory.getScheduledWorkerPool().shutdown();
				manager.stop();
			}
		}));

		Job1 job = factory.newJob(Job1.class);
		job.buildTasks();
		job.execute(new HashMap<>());
		job.setTimeout(3000);
		manager.monitor(job);
	}
}
