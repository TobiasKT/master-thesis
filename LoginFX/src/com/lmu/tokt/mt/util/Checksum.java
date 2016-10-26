package com.lmu.tokt.mt.util;

import java.util.Random;

public class Checksum {

	public static Checksum instance;

	private int mChecksum;

	public static Checksum getInstance() {
		if (instance == null) {
			return instance = new Checksum();
		} else {
			return instance;
		}

	}

	public Checksum() {
	}

	public int calculateChecksum() {

		// max=9999
		// min=1000
		Random rand = new Random();
		int checksum = rand.nextInt(9999 - 1000 + 1) + 1000;
		mChecksum = checksum;
		return checksum;
	}

	public int getChecksum() {
		return mChecksum;
	}
	
	public void resetChecksum(){
		mChecksum = 0;
	}

}
