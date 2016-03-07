package com.fullmoon.job.routing;

import com.fullmoon.job.AbstractJob;
import com.fullmoon.task.Task;

public abstract class AbstractRoutingJob<T> extends AbstractJob<T> implements RoutingJob<T> {

	@Override
	protected void prepareExecuteTask(Task<T> task) {
		if (task instanceof RoutingTask) {
			RoutingTask<T> routingTask = (RoutingTask<T>) task;
			routingTask.setDomainOnExecute(getData());
		}
	}
}
