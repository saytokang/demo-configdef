package com.example.demogwconfig.conf.validator;

import com.example.demogwconfig.conf.ConfigDefException;

public class IntRangeValidator implements Validator {

	private final int min;

	private final int max;

	public IntRangeValidator(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public void verify(String name, Object value) {
		if (value instanceof Integer) {
			int val = (Integer) value;
			checkBoundary(name, val);
			return;
		}

		if (value instanceof String) {
			int val = 0;
			try {
				val = Integer.parseInt(value.toString());
			}
			catch (Exception e) {
				throw new ConfigDefException(value + " 값은 int 값만 허용합니다.");
			}
			checkBoundary(name, val);
		}
		else {
			String msg = String.format("%s 값은 INT 이고, between %d and %d 입니다.", name, min, max);
			throw new ConfigDefException(msg);
		}
	}

	private void checkBoundary(String name, int val) {
		if (val < min || val > max) {
			String msg = String.format("%s 값은 between %d and %d 입니다.", name, min, max);
			throw new ConfigDefException(msg);
		}
	}

}
