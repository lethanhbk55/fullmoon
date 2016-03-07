package com.fullmoon.job.routing;

import com.fullmoon.task.AbstractTask;

public abstract class AbstractRoutingTask<T> extends AbstractTask<T> implements RoutingTask<T> {
	private String domain;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
