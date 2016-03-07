package com.fullmoon.job.manager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fullmoon.job.AbstractJob;
import com.fullmoon.job.Job;

@SuppressWarnings("rawtypes")
public final class JobManager {
	private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
	private List<Job> jobs = new CopyOnWriteArrayList<>();
	private int period;

	public JobManager(int period) {
		this.period = period;
	}

	public final void start() {
		this.service.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				for (Job job : jobs) {
					if (job instanceof AbstractJob) {
						AbstractJob abstractJob = (AbstractJob) job;
						if (abstractJob.isStarted()) {
							if (abstractJob.isTimeout()) {
								abstractJob.onJobTimeout();
								release(job);
							} else if (abstractJob.isFinished()) {
								release(job);
							}
						}
					}
				}
			}
		}, period, period, TimeUnit.MILLISECONDS);
	}

	public final void stop() {
		this.service.shutdown();
		try {
			if (this.service.awaitTermination(3, TimeUnit.SECONDS)) {
				this.service.shutdownNow();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void monitor(Job job) {
		jobs.add(job);
	}

	public void release(Job job) {
		jobs.remove(job);
	}
}
