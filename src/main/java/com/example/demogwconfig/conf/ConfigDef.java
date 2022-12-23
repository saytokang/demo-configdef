package com.example.demogwconfig.conf;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.example.demogwconfig.conf.validator.Validator;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigDef {

	private final Map<String, ConfigKey> configKeys;

	private EnumMap<Type, Converter> operators = new EnumMap<>(Type.class);

	public ConfigDef() {
		configKeys = new HashMap<>();
		setup();
	}

	public ConfigDef(ConfigDef def) {
		configKeys = new HashMap<>(def.configKeys);
		setup();
	}

	private void setup() {
		operators.put(Type.INT, TypeUtils::toInt);
		operators.put(Type.BOOLEAN, TypeUtils::toBoolean);
		operators.put(Type.STRING, TypeUtils::toText);
		operators.put(Type.LONG, TypeUtils::toLong);
		operators.put(Type.MAP, TypeUtils::toMap);
		operators.put(Type.LIST, TypeUtils::toList);
	}

	public ConfigDef define(String name, Type type, Object value) {
		define(name, type, value, null, null);

		return this;
	}

	public ConfigDef define(String name, Type type, Object value, Validator validator) {
		define(name, type, value, validator, null);

		return this;
	}

	public ConfigDef define(String name, Type type, Object value, Validator validator, String description) {
		Assert.notNull(name, "name is required.");
		Assert.isTrue(StringUtils.hasText(name), "name should not blank.");
		Assert.notNull(type, "type is required.");
		Assert.notNull(value, "value is required.");

		if (configKeys.containsKey(name)) {
			throw new ConfigException(name + " 는(은) 이미 등록되어 있습니다.");
		}

		ConfigKey newConfigKey = ConfigKey.builder().name(name).type(type).value(value).validator(validator)
				.description(description).build();
		if (validator != null) {
			validator.verify(name, value);
		}
		configKeys.put(name, newConfigKey);

		return this;
	}

	public Map<String, Object> parse(Properties props) {
		Map<String, Object> values = new HashMap<>();
		props.forEach((k, v) -> {
			ConfigKey configKey = findByName(k);
			if (configKey == null) {
				throw new ConfigException(k + " key not found");
			}

			Object value = null;
			try {
				value = operators.get(configKey.type).convert(v);
			}
			catch (Exception e) {
				log.warn("parsing error. set default value");
				value = configKey.value;
			}

			if (configKey.validator != null) {
				configKey.validator.verify(k.toString(), v);
			}
			values.put(k.toString(), value);
		});

		return values;
	}

	ConfigKey findByName(Object key) {
		// @formatter:off
		return configKeys.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(key))
                .findFirst()
				.map(Entry::getValue)
                .orElse(null);
        // @formatter:on
	}

	private static class ConfigKey {

		private final String name;

		private final Type type;

		private final Object value;

		private final Validator validator;

		private final String decription;

		@Builder
		public ConfigKey(String name, Type type, Object value, Validator validator, String description) {
			this.name = name;
			this.type = type;
			this.value = value;
			this.validator = validator;
			this.decription = description;
		}

	}

	@FunctionalInterface
	private interface Converter {

		Object convert(Object type);

	}

	private static class TypeUtils {

		static boolean isStringType(Object obj) {
			return obj instanceof String;
		}

		static Object toInt(Object value) {
			if (value instanceof Integer) {
				return value;
			}

			if (!isStringType(value)) {
				throw new ConfigException(value + " 는 int 타입이어야 합니다.");
			}

			try {
				return Integer.valueOf(value.toString());
			}
			catch (Exception e) {
				throw new ConfigException(value + " 는 Integer 타입이어야 합니다.");
			}
		}

		static Object toBoolean(Object value) {
			if (value instanceof Boolean) {
				return value;
			}

			if (!isStringType(value)) {
				throw new ConfigException(value + " 는 boolean 타입이어야 합니다.");
			}

			try {
				return Boolean.valueOf(value.toString());
			}
			catch (Exception e) {
				throw new ConfigException(value + " 는 boolean 타입이어야 합니다.");
			}
		}

		static Object toText(Object value) {
			if (value instanceof String) {
				return value;
			}

			throw new ConfigException(value + " 는 string 타입이어야 합니다.");
		}

		static Object toLong(Object value) {
			if (value instanceof Long) {
				return value;
			}

			if (!isStringType(value)) {
				throw new ConfigException(value + " 는 long 타입이어야 합니다.");
			}

			try {
				return Long.valueOf(value.toString());
			}
			catch (Exception e) {
				throw new ConfigException(value + " 는 long 타입이어야 합니다.");
			}
		}

		static Object toMap(Object value) {
			if (value instanceof Map) {
				return value;
			}

			if (!isStringType(value)) {
				throw new ConfigException(value + "는 <key>:<value> 형식이어야 합니다.");
			}

			Map<String, String> map = new HashMap<>();

			// @formatter:off
			Arrays
                .asList(value.toString().split("\\s*,\\s*", -1))
                .forEach(word -> {
                    String[] entry = word.split("\\s*:\\s*", -1);
                    if (entry.length != 2) {
                        throw new ConfigException("Map entry should be <key>:<value>.");
                    }
                    map.put(entry[0], entry[1]);
                });
            // @formatter:on

			return map;
		}

		static Object toList(Object value) {
			if (value instanceof List) {
				return value;
			}

			if (!isStringType(value)) {
				throw new ConfigException(value + " 는 , 구분자를 포함하고 있어야 합니다.");
			}

			// @formatter:off
			return Arrays.asList(value.toString().split("\\s*,\\s*", -1))
                    .stream()
                    .map(String::trim)
					.filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            // @formatter:on
		}

	}

}
