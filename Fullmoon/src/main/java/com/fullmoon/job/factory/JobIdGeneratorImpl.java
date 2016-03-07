package com.fullmoon.job.factory;

public class JobIdGeneratorImpl implements JobIdGenerator {
	private long idSeed = 0;

	@Override
	public long generateId() {
		return idSeed++;
	}

}
