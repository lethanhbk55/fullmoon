package com.fullmoon.job.factory;

import com.fullmoon.job.AbstractJob;
import com.fullmoon.job.Job;
import com.fullmoon.job.routing.AbstractRoutingJob;
import com.fullmoon.job.schedule.AbstractScheduledJob;
import com.fullmoon.worker.RoutingWorkerPool;
import com.fullmoon.worker.ScheduledWorkerPool;
import com.fullmoon.worker.WorkerPool;

@SuppressWarnings("rawtypes")
public final class JobFactory {
	private WorkerPool workerPool;
	private ScheduledWorkerPool scheduledWorkerPool;
	private RoutingWorkerPool routingWorkerPool;

	private JobIdGenerator idGenerator = new JobIdGeneratorImpl();

	@SuppressWarnings("unchecked")
	public <T extends Job> T newJob(Class<T> clazz) {
		try {
			T job = clazz.newInstance();
			if (job instanceof AbstractJob) {
				AbstractJob abstractJob = (AbstractJob) job;
				abstractJob.setId(idGenerator.generateId());
				if (this.workerPool == null) {
					throw new NullPointerException("Default Worker Pool hasn't been set");
				}
				abstractJob.setWorkerPool(workerPool);
			}

			if (job instanceof AbstractScheduledJob) {
				AbstractScheduledJob scheduledJob = (AbstractScheduledJob) job;
				if (scheduledWorkerPool == null) {
					throw new NullPointerException("Scheduled Worker Pool hasn't been set");
				}
				scheduledJob.setWorkerPool(scheduledWorkerPool);
			}

			if (job instanceof AbstractRoutingJob) {
				AbstractRoutingJob routingJob = (AbstractRoutingJob) job;
				if (routingWorkerPool == null) {
					throw new NullPointerException("Routing Worker Pool hasn't been set");
				}
				routingJob.setWorkerPool(routingWorkerPool);
			}
			return job;
		} catch (Exception e) {
			throw new RuntimeException("create job instance error", e);
		}

	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(WorkerPool workerPool) {
		this.workerPool = workerPool;
	}

	public JobIdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(JobIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public ScheduledWorkerPool getScheduledWorkerPool() {
		return scheduledWorkerPool;
	}

	public void setScheduledWorkerPool(ScheduledWorkerPool scheduledWorkerPool) {
		this.scheduledWorkerPool = scheduledWorkerPool;
	}

	public RoutingWorkerPool getRoutingWorkerPool() {
		return routingWorkerPool;
	}

	public void setRoutingWorkerPool(RoutingWorkerPool routingWorkerPool) {
		this.routingWorkerPool = routingWorkerPool;
	}
}
