package com.fullmoon.job.routing;

import com.fullmoon.task.Task;

public interface RoutingTask<T> extends Task<T> {
	String getDomain();

	void setDomainOnExecute(T data);
}
