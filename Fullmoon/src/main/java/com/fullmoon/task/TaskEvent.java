package com.fullmoon.task;

public class TaskEvent<T> {
	private Task<T> task;
	private T data;

	public TaskEvent() {

	}

	public TaskEvent(Task<T> task, T data) {
		this();
		setTask(task);
		setData(data);
	}

	public Task<T> getTask() {
		return task;
	}

	public void setTask(Task<T> task) {
		this.task = task;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
