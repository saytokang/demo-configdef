package com.example.demogwconfig.conf.validator;

@FunctionalInterface
public interface Validator {

	void verify(String name, Object value);

}
