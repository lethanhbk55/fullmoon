package com.fullmoon.test.jobs;

import java.util.Map;

import com.fullmoon.job.AbstractJob;
import com.fullmoon.test.tasks.Task1;
import com.fullmoon.test.tasks.Task2;
import com.fullmoon.test.tasks.Task3;
import com.fullmoon.test.tasks.Task4;

public class Job1 extends AbstractJob<Map<String, Object>> {

	@SuppressWarnings("unchecked")
	public void buildTasks() {
		addTasks(new Task1(), new Task2(), new Task3(), new Task4());
	}

	@Override
	protected void onCompleted() {
		getLogger().debug("do job completed, success: {}, exception: {}", this.isSuccessful(), this.getException());
	}

	@Override
	protected void onIncompleted() {
		// TODO Auto-generated method stub

	}
}
