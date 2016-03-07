package com.fullmoon.test.tasks;

import java.util.Map;

import com.fullmoon.job.routing.AbstractRoutingTask;

public class Task2 extends AbstractRoutingTask<Map<String, Object>> {

	@Override
	public void execute(Map<String, Object> data) {
		setSuccessful(true);
		completed();
	}

	@Override
	public void rollback() {
		System.out.println("task2 roolback");
	}

	@Override
	public void setDomainOnExecute(Map<String, Object> data) {
		setDomain("VTT");
	}

}
