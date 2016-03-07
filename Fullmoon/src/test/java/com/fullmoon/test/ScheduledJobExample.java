package com.fullmoon.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.fullmoon.job.factory.JobFactory;
import com.fullmoon.job.manager.JobManager;
import com.fullmoon.test.jobs.ScheduledJob1;
import com.fullmoon.worker.impl.DisruptorWorkerPool;
import com.fullmoon.worker.impl.RoutingWorkerPoolImpl;
import com.fullmoon.worker.impl.ScheduledWorkerPoolImpl;
import com.nhb.common.utils.Initializer;

public class ScheduledJobExample {
	static {
		Initializer.bootstrap(ScheduledJobExample.class);
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
		JobFactory factory = new JobFactory();
		factory.setWorkerPool(new DisruptorWorkerPool<Map<String, Object>>(8, 1024));
		factory.setScheduledWorkerPool(
				new ScheduledWorkerPoolImpl<Map<String, Object>>(service, factory.getWorkerPool()));
		factory.setRoutingWorkerPool(new RoutingWorkerPoolImpl<Map<String, Object>>(factory.getWorkerPool()));
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

		ScheduledJob1 job = factory.newJob(ScheduledJob1.class);
		job.buildTasks();
		job.execute(new HashMap<>(), 3000, 10000, 3);
		job.setTimeout(2000);
		manager.monitor(job);
	}
}
