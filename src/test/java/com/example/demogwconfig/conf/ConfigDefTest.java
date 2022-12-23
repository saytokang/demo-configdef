
package com.example.demogwconfig.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.demogwconfig.conf.validator.IntRangeValidator;
import com.example.demogwconfig.conf.validator.RegExpValidator;
import com.example.demogwconfig.conf.validator.Validator;

public class ConfigDefTest {

	@Test
	@DisplayName("ConfigDef 생성자 필수값 체크")
	public void test_1() {
		assertThrows(Exception.class, () -> new ConfigDef().define(null, null, null));
	}

	@Test
	@DisplayName("ConfigDef 생성자 필수값 체크")
	public void test_2() {
		assertThrows(Exception.class, () -> new ConfigDef().define("a", Type.INT, null));
	}

	@Test
	@DisplayName("ConfigDef 생성자 필수값 체크")
	public void test_3() {
		assertThrows(Exception.class, () -> new ConfigDef().define("", Type.INT, 9));
	}

	@Test
	@DisplayName("ConfigDef 생성자 필수값 체크")
	public void test_4() {
		ConfigDef def = new ConfigDef().define("aa", Type.INT, 9);
		assertNotNull(def);
	}

	@Test
    @DisplayName("ConfigDef 여러개 설정하고, 설정된 값 확인")
	public void test1() {
		// @formatter:off
		ConfigDef def = new ConfigDef()
                .define("a", Type.INT, 10)
                .define("b", Type.STRING, "string")
				.define("c", Type.LONG, 10L)
                .define("d", Type.BOOLEAN, false)
                .define("e", Type.MAP, new HashMap<>())
				.define("f", Type.LIST, Arrays.asList());
        // @formatter:on

		Properties props = new Properties();
		// props.put("a", "1");
		props.put("a", 1);
		props.put("b", "한글");

		Map<String, Object> map = def.parse(props);
		assertEquals(1, map.get("a"));
		assertEquals("한글", map.get("b"));
	}

	@Test
    @DisplayName("ConfigDef 1개 설정하고, 설정된 값 확인.")
	public void test2() {
		// @formatter:off
		ConfigDef def = new ConfigDef()
            .define("a", Type.INT, 10)
            .define("b", Type.STRING, "string");
        // @formatter:on

		ConfigDef copy = new ConfigDef(def);

		Properties props = new Properties();
		props.put("a", 100);

		Map<String, Object> map = copy.parse(props);
		assertEquals(100, map.get("a"));
		assertEquals(null, map.get("z"));
	}

	@Test
	@DisplayName("Config 설정에서 List 타입의 값 설정하고, 확인")
	public void test3() {
		// @formatter:off
		ConfigDef def = new ConfigDef()
                    .define("list", Type.LIST, Arrays.asList("ab", "12"))
                    .define("b", Type.STRING,	"string");
        // @formatter:on

		Properties props = new Properties();
		props.put("list", "ab,12");

		Map<String, Object> map = def.parse(props);
		assertEquals(Arrays.asList("ab", "12"), map.get("list"));
	}

	@Test
	@DisplayName(", 구분자 list value. space 포함")
	public void test4() {
		// @formatter:off
		ConfigDef def = new ConfigDef()
                .define("list", Type.LIST, Arrays.asList("ab", "12"))
                .define("b", Type.STRING,
				"string");
        // @formatter:on

		Properties props = new Properties();
		props.put("list", "ab, 12 , ");

		Map<String, Object> map = def.parse(props);
		assertEquals(Arrays.asList("ab", "12"), map.get("list"));
	}

	@Test
	@DisplayName("map style data parsing")
	public void test21() {
		ConfigDef def = new ConfigDef().define("map", Type.MAP, Collections.emptyMap());

		Properties props = new Properties();
		props.put("map", "a.k1:10, a.k2:20, b.k3:200");

		Map<String, Object> map = def.parse(props);
		Map<?, ?> actual = (Map<String, Object>) map.get("map");
		assertEquals(3, actual.size());
	}

	@Test
	@DisplayName("validator 기능 체크: define() 함수 - 성공")
	public void test31_ok() {
		String name = "limit.max";
		Validator validator = new IntRangeValidator(1, 100);

		ConfigDef def = new ConfigDef().define(name, Type.INT, 100, validator);

		Properties props = new Properties();
		props.put(name, 20);

		Map<String, Object> map = def.parse(props);
		assertEquals(20, map.get(name));
	}

	@Test
	@DisplayName("validator 기능 체크: define() 함수 - 실패")
	public void test31_fail() {
		String name = "limit.max";
		Validator validator = new IntRangeValidator(1, 100);

		// @formatter:off
		assertThrows(ConfigException.class,
                    () -> new ConfigDef().define(name, Type.INT, 101, validator));
        // @formatter:on

	}

	@Test
	@DisplayName("validator 기능 체크: parse() 함수 - 성공")
	public void test32_ok() {
		String name = "limit.max";
		Validator validator = new IntRangeValidator(1, 100);
		ConfigDef def = new ConfigDef().define(name, Type.INT, 99, validator);

		Properties props = new Properties();
		props.put(name, "1");

		Map<String, Object> map = def.parse(props);
		assertEquals(1, map.get(name));
	}

	@Test
	@DisplayName("validator 기능 체크: parse() 함수 - 실패")
	public void test32_fail() {
		String name = "limit.max";
		Validator validator = new IntRangeValidator(1, 100);
		ConfigDef def = new ConfigDef().define(name, Type.INT, 99, validator);

		Properties props = new Properties();
		props.put(name, "-1");

		assertThrows(ConfigException.class, () -> def.parse(props));
	}

	@Test
	@DisplayName("Validator: 정규식 정상적인 값 확인")
	public void test_regexp_ok() {
		String name = "username";
		Validator validator = new RegExpValidator("^[0-9A-Za-z]{5,10}$");
		ConfigDef def = new ConfigDef().define(name, Type.STRING, "abcde", validator);

		Properties props = new Properties();
		props.put(name, "abc123");

		Map<String, Object> map = def.parse(props);
		assertEquals("abc123", map.get(name));
	}

	@Test
	@DisplayName("Validator : short value 체크에서 실패 확인")
	public void test_regexp_fail() {
		String name = "username";
		Validator validator = new RegExpValidator("^[0-9A-Za-z]{5,10}$");
		ConfigDef def = new ConfigDef().define(name, Type.STRING, "abcde", validator);

		Properties props = new Properties();
		props.put(name, "abc");

		assertThrows(ConfigException.class, () -> def.parse(props));
	}

	@Test
	@DisplayName("Validator: regexp 잘못된 값 체크")
	public void test_regexp_fail2() {
		String name = "username";
		Validator validator = new RegExpValidator("^[0-9A-Za-z]{5,10}$");
		ConfigDef def = new ConfigDef().define(name, Type.STRING, "abcde", validator);

		Properties props = new Properties();
		props.put(name, "abc!@111");

		assertThrows(ConfigException.class, () -> def.parse(props));
	}

}
