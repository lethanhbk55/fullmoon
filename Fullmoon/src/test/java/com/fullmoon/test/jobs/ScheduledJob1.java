package com.fullmoon.test.jobs;

import java.util.Map;

import com.fullmoon.job.schedule.AbstractScheduledJob;
import com.fullmoon.test.tasks.Task1;
import com.fullmoon.test.tasks.Task2;
import com.fullmoon.test.tasks.Task3;
import com.fullmoon.test.tasks.Task4;

public class ScheduledJob1 extends AbstractScheduledJob<Map<String, Object>> {

	@SuppressWarnings("unchecked")
	public void buildTasks() {
		addTasks(new Task1(), new Task2(), new Task3(), new Task4());
	}

	@Override
	protected void onCompleted() {
		this.done();
	}

	@Override
	protected void onIncompleted() {

	}
}
