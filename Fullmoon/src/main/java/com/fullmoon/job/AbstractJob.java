package com.fullmoon.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fullmoon.task.AbstractTask;
import com.fullmoon.task.Task;
import com.fullmoon.worker.WorkerPool;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.impl.BaseEvent;
import com.nhb.eventdriven.impl.BaseEventHandler;

public abstract class AbstractJob<T> extends BaseLoggable implements Job<T> {
	private long id;
	private List<Task<T>> tasks = new CopyOnWriteArrayList<>();
	private TaskSwitcher<T> taskSwitcher;
	private T data;
	private int currentTaskIndex;
	private JobResult result;
	private Map<String, TaskSwitcher<T>> taskCompletedListeners = new ConcurrentHashMap<>();
	private Map<Task<T>, Long> excutingTimeTaskMapping = new ConcurrentHashMap<>();
	private List<Task<T>> completedTasks = new CopyOnWriteArrayList<>();
	BaseEventHandler taskCompletedListener = new BaseEventHandler(this, "onTaskCompletedHandler");
	private WorkerPool<T> workerPool;
	private Throwable exception;
	private boolean isSuccessful;
	private long startTime;
	private long executeTime;
	private JobHandler handler;
	private boolean isRunning;
	private int timeout;
	private boolean isStarted = false;
	private boolean isFinished = false;

	private void start() {
		startTime = System.currentTimeMillis();
		isRunning = true;
		setStarted(true);
	}

	@Override
	public void execute(T data) {
		try {
			start();
			setData(data);
			if (taskSwitcher == null) {
				if (this.tasks.size() > 0) {
					Task<T> task = this.tasks.get(0);
					currentTaskIndex = 0;
					startingTask(task);
				} else {
					getLogger().warn("This job has no tasks to do, finishing job");
					this.complete();
				}
			} else {
				String nextTask = taskSwitcher.switchTask(data);
				Task<T> task = getTask(nextTask);
				if (task != null) {
					startingTask(task);
				} else {
					getLogger().warn("started task was not found in job, finishing job.");
					this.complete();
				}
			}
		} catch (Exception ex) {
			this.exception = ex;
			getLogger().error("execute task error", ex);
		}
	}

	private void startingTask(Task<T> task) {
		excutingTimeTaskMapping.put(task, System.currentTimeMillis());
		task.setStartedTime(System.currentTimeMillis());
		prepareExecuteTask(task);
		executeTask(task);
	}

	protected void prepareExecuteTask(Task<T> task) {

	}

	private void executeTask(Task<T> task) {
		if (getWorkerPool() != null) {
			task.execute(data, getWorkerPool());
		} else {
			task.execute(data);
		}
	}

	private String getNextTask() {
		if (currentTaskIndex < tasks.size() - 1) {
			currentTaskIndex++;
			return this.tasks.get(currentTaskIndex).getName();
		}
		return null;
	}

	private Task<T> getTask(String name) {
		for (int i = 0; i < this.tasks.size(); i++) {
			Task<T> task = this.tasks.get(i);
			if (task.getName().equalsIgnoreCase(name)) {
				currentTaskIndex = i;
				return task;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public final void onTaskCompletedHandler(Event rawEvent) {
		BaseEvent event = (BaseEvent) rawEvent;
		Task<T> completedTask = (Task<T>) event.get("task");
		if (completedTask.isTimeout()) {
			return;
		}

		if (!completedTask.isSuccessful()) {
			getLogger().info("this job do task {} unsuccessful, stop job.", completedTask.getName());
			this.incomplete();
		} else {
			completedTasks.add(completedTask);
			excutingTimeTaskMapping.remove(completedTask);
			String taskName = completedTask.getName();

			String nextTask = null;

			if (taskCompletedListeners.containsKey(taskName)) {
				TaskSwitcher<T> switcher = taskCompletedListeners.remove(taskName);
				setTaskSwitcher(switcher);
			}

			if (this.taskSwitcher != null) {
				nextTask = this.taskSwitcher.switchTask(data);
				// remove switcher
				this.taskSwitcher = null;
			}

			if (nextTask == null) {
				nextTask = getNextTask();
			}

			if (nextTask != null) {
				Task<T> task = getTask(nextTask);
				if (task != null) {
					startingTask(task);
				} else {
					getLogger().info("next task cannot be found, finishing job");
					this.complete();
				}
			} else {
				getLogger().warn("next task cannot be found, finishing job");
				this.complete();
			}
		}
	}

	@Override
	public void resume() {
		if (currentTaskIndex >= 0 && currentTaskIndex < this.tasks.size()) {
			Task<T> task = this.tasks.get(currentTaskIndex);
			task.execute(data);
		}
	}

	@Override
	public void rollback() {
		for (int i = completedTasks.size() - 1; i >= 0; i--) {
			Task<T> task = completedTasks.get(i);
			if (task.isSuccessful()) {
				task.rollback();
			}
		}
	}

	@Override
	public byte[] serialize() {
		PuArray array = new PuArrayList();
		try {
			writePuArray(array);
		} catch (IOException e) {
			throw new RuntimeException("error while serialize data, make sure your data can serialize", e);
		}

		return array.toBytes();
	}

	@SuppressWarnings("unchecked")
	public void readPuArray(PuArray array)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.id = array.remove(0).getLong();
		this.isRunning = array.remove(0).getBoolean();
		this.isFinished = array.remove(0).getBoolean();
		this.isSuccessful = array.remove(0).getBoolean();

		byte[] resultBytes = array.remove(0).getRaw();

		try (ByteArrayInputStream resultContext = new ByteArrayInputStream(resultBytes);
				ObjectInputStream objectInputStream = new ObjectInputStream(resultContext)) {
			this.result = (JobResult) objectInputStream.readObject();
		} catch (IOException e) {
			throw new RuntimeException("error while deserialize job result", e);
		}

		byte[] dataBytes = array.remove(0).getRaw();
		try (ByteArrayInputStream dataContext = new ByteArrayInputStream(dataBytes);
				ObjectInputStream objectInputStream = new ObjectInputStream(dataContext);) {
			this.data = (T) objectInputStream.readObject();
		} catch (IOException e) {
			throw new RuntimeException("error while deserialize job data", e);
		}

		this.tasks = new ArrayList<>();
		while (array.size() > 0) {
			byte[] taskContext = array.remove(0).getRaw();
			PuArray taskArray = PuArrayList.fromObject(taskContext);
			String className = taskArray.remove(0).getString();
			Task<T> task = (Task<T>) Class.forName(className).newInstance();
			if (task instanceof AbstractTask<?>) {
				((AbstractTask<?>) task).readPuArray(taskArray);
				addTask(task);
			}
		}
	}

	private void writePuArray(PuArray array) throws IOException {
		array.addFrom(this.getClass().getName());
		array.addFrom(this.id);
		array.addFrom(this.isRunning);
		array.addFrom(this.isFinished);
		array.addFrom(this.isSuccessful);

		try (ByteArrayOutputStream resultContext = new ByteArrayOutputStream();
				ObjectOutputStream resultOutputStream = new ObjectOutputStream(resultContext)) {
			resultOutputStream.writeObject(this.result);
			array.addFrom(resultContext.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("error while serialize result object", e);
		}

		try (ByteArrayOutputStream dataContext = new ByteArrayOutputStream();
				ObjectOutputStream dataOutputStream = new ObjectOutputStream(dataContext)) {
			dataOutputStream.writeObject(getData());
			array.addFrom(dataContext.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("error while serialize data ojbect", e);
		}
		if (this.tasks != null) {
			for (Task<?> task : this.tasks) {
				array.addFrom(task.serialize());
			}
		}
	}

	private void complete() {
		this.executeTime = System.currentTimeMillis() - this.startTime;
		setSuccessful(true);
		setFinished(true);
		onCompleted();
		isRunning = false;
		if (handler != null) {
			handler.onJobFinished(this);
		}
	}

	private void incomplete() {
		this.executeTime = System.currentTimeMillis() - this.startTime;
		setFinished(true);
		this.onIncompleted();
		isRunning = false;
		if (handler != null) {
			handler.onJobFinished(this);
		}
	}

	protected abstract void onCompleted();

	protected abstract void onIncompleted();

	protected void onJobTimedOut() {

	}

	public void addTask(Task<T> task) {
		tasks.add(task);
		task.setJob(this);
		if (task instanceof AbstractTask<?>) {
			((AbstractTask<T>) task).addEventListener("taskCompleted", taskCompletedListener);
		}
	}

	@SuppressWarnings("unchecked")
	public void addTasks(Task<T>... tasks) {
		for (Task<T> task : tasks) {
			addTask(task);
		}
	}

	public void addTasks(List<Task<T>> tasks) {
		for (Task<T> task : tasks) {
			addTask(task);
		}
	}

	public void addTaskCompletedListener(String taskName, TaskSwitcher<T> switcher) {
		if (taskName != null) {
			taskCompletedListeners.put(taskName, switcher);
		}
	}

	public void removeTaskCompletedListener(String taskName) {
		this.taskCompletedListeners.remove(taskName);
	}

	@Override
	public String getReport() {
		StringBuilder builder = new StringBuilder();
		builder.append("____________JOB REPORT_____________\n");
		builder.append("***********************************\n");

		builder.append(String.format("%-30s%-60s\n", "Id", this.id));
		builder.append(String.format("%-30s%-60s\n", "Execute Time", this.executeTime + " ms"));
		builder.append(String.format("%-30s%-60s\n", "Is Successful", this.isSuccessful));

		if (this.completedTasks != null) {
			for (Task<T> task : completedTasks) {
				builder.append(String.format("%-30s%-60s\n", task.getName(), task.getExecuteTime() + " ms"));
			}
		}

		builder.append("************************************");
		return builder.toString();
	}

	public boolean isTimeout() {
		long executingTime = System.currentTimeMillis() - this.startTime;
		return executingTime > getTimeout();
	}

	@Override
	public long getExecuteTime() {
		return this.executeTime;
	}

	@Override
	public List<Task<T>> getTasks() {
		return tasks;
	}

	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public TaskSwitcher<T> getTaskSwitcher() {
		return taskSwitcher;
	}

	@Override
	public void setTaskSwitcher(TaskSwitcher<T> taskSwitcher) {
		this.taskSwitcher = taskSwitcher;
	}

	@Override
	public JobResult getResult() {
		return result;
	}

	protected void setResult(JobResult result) {
		this.result = result;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}

	public JobHandler getHandler() {
		return handler;
	}

	public void setHandler(JobHandler handler) {
		this.handler = handler;
	}

	public Task<T> getLastDidTask() {
		if (this.currentTaskIndex >= 0 && this.currentTaskIndex < this.tasks.size()) {
			return this.tasks.get(this.currentTaskIndex);
		}
		return null;
	}

	public WorkerPool<T> getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(WorkerPool<T> workerPool) {
		this.workerPool = workerPool;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public final void onJobTimeout() {
		getLogger().warn("jobId {} was timeout, execute time: {}, try resume or rollback", getId(),
				System.currentTimeMillis() - this.startTime);
		this.onJobTimedOut();
		if (this.getHandler() != null) {
			this.getHandler().onJobTimedOut(this);
		}
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
}
