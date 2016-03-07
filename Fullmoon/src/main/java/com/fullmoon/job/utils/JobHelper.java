package com.fullmoon.job.utils;

import java.io.IOException;

import com.fullmoon.job.AbstractJob;
import com.fullmoon.job.Job;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;

public class JobHelper {

	public static Job<?> deserialize(byte[] data)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		PuArray array = PuArrayList.fromObject(data);
		String clazz = array.remove(0).getString();
		Job<?> job = (Job<?>) Class.forName(clazz).newInstance();
		if (job instanceof AbstractJob<?>) {
			((AbstractJob<?>) job).readPuArray(array);
		}
		return job;
	}
}
