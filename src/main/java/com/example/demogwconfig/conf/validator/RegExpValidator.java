package com.example.demogwconfig.conf.validator;

import java.util.regex.Pattern;

import com.example.demogwconfig.conf.ConfigException;

public class RegExpValidator implements Validator {

	private String express;

	private final Pattern pattern;

	public RegExpValidator(String express) {
		this.pattern = Pattern.compile(express);
	}

	@Override
	public void verify(String name, Object value) {
		if (value instanceof String) {
			boolean isSuccess = this.pattern.matcher(value.toString()).matches();
			if (!isSuccess) {
				throw new ConfigException(name + " 허용하지 않는 값입니다.");
			}
		}
		else {
			throw new ConfigException(name + " 값은 정규식 " + express + " 를 범위만 허용합니다.");
		}
	}

}
