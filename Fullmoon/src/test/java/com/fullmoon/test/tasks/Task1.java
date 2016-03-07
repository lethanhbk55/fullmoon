package com.fullmoon.test.tasks;

import java.util.Map;

import com.fullmoon.task.AbstractTask;

public class Task1 extends AbstractTask<Map<String, Object>> {

	@Override
	public void execute(Map<String, Object> data) {
		System.out.println("task1 execute " + data);
		setSuccessful(true);
		completed();
	}

	@Override
	public void rollback() {

	}
}
