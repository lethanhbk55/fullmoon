package com.fullmoon.test.tasks;

import java.util.Map;

import com.fullmoon.task.AbstractTask;

public class Task3 extends AbstractTask<Map<String, Object>> {

	@Override
	public void rollback() {

	}

	@Override
	public void execute(Map<String, Object> data) {
		setSuccessful(true);
		completed();
	}

}
