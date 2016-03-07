package com.fullmoon.test;

import java.io.IOException;
import java.util.HashMap;

import com.fullmoon.job.utils.JobHelper;
import com.fullmoon.test.jobs.Job1;
import com.nhb.common.utils.Initializer;

public class SerializeJobSample {

	static {
		Initializer.bootstrap(SerializeJobSample.class);
	}

	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		Job1 job = new Job1();
		job.buildTasks();
		HashMap<String, Object> data = new HashMap<>();
		data.put("name", "thanh");
		job.execute(data);
		System.out.println(job.getReport());

		byte[] bytes = job.serialize();
		Job1 newJob = (Job1) JobHelper.deserialize(bytes);
		newJob.execute(newJob.getData());
		System.out.println(newJob.getReport());
	}
}
