/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.env;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.boot.origin.TextResourceOrigin;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link OriginTrackedYamlLoader}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
class OriginTrackedYamlLoaderTests {

	private OriginTrackedYamlLoader loader;

	private List<Map<String, Object>> result;

	@BeforeEach
	void setUp() {
		Resource resource = new ClassPathResource("test-yaml.yml", getClass());
		this.loader = new OriginTrackedYamlLoader(resource);
	}

	@Test
	void processSimpleKey() {
		OriginTrackedValue value = getValue("name");
		assertThat(value.toString()).isEqualTo("Martin D'vloper");
		assertThat(getLocation(value)).isEqualTo("3:7");
	}

	@Test
	void processMap() {
		OriginTrackedValue perl = getValue("languages.perl");
		OriginTrackedValue python = getValue("languages.python");
		OriginTrackedValue pascal = getValue("languages.pascal");
		assertThat(perl.toString()).isEqualTo("Elite");
		assertThat(getLocation(perl)).isEqualTo("13:11");
		assertThat(python.toString()).isEqualTo("Elite");
		assertThat(getLocation(python)).isEqualTo("14:13");
		assertThat(pascal.toString()).isEqualTo("Lame");
		assertThat(getLocation(pascal)).isEqualTo("15:13");
	}

	@Test
	void processCollection() {
		OriginTrackedValue apple = getValue("foods[0]");
		OriginTrackedValue orange = getValue("foods[1]");
		OriginTrackedValue strawberry = getValue("foods[2]");
		OriginTrackedValue mango = getValue("foods[3]");
		assertThat(apple.toString()).isEqualTo("Apple");
		assertThat(getLocation(apple)).isEqualTo("8:7");
		assertThat(orange.toString()).isEqualTo("Orange");
		assertThat(getLocation(orange)).isEqualTo("9:7");
		assertThat(strawberry.toString()).isEqualTo("Strawberry");
		assertThat(getLocation(strawberry)).isEqualTo("10:7");
		assertThat(mango.toString()).isEqualTo("Mango");
		assertThat(getLocation(mango)).isEqualTo("11:7");
	}

	@Test
	void processMultiline() {
		OriginTrackedValue education = getValue("education");
		assertThat(education.toString()).isEqualTo("4 GCSEs\n3 A-Levels\nBSc in the Internet of Things\n");
		assertThat(getLocation(education)).isEqualTo("16:12");
	}

	@Test
	void processListOfMaps() {
		OriginTrackedValue name = getValue("example.foo[0].name");
		OriginTrackedValue url = getValue("example.foo[0].url");
		OriginTrackedValue bar1 = getValue("example.foo[0].bar[0].bar1");
		OriginTrackedValue bar2 = getValue("example.foo[0].bar[1].bar2");
		assertThat(name.toString()).isEqualTo("springboot");
		assertThat(getLocation(name)).isEqualTo("22:15");
		assertThat(url.toString()).isEqualTo("https://springboot.example.com/");
		assertThat(getLocation(url)).isEqualTo("23:14");
		assertThat(bar1.toString()).isEqualTo("baz");
		assertThat(getLocation(bar1)).isEqualTo("25:19");
		assertThat(bar2.toString()).isEqualTo("bling");
		assertThat(getLocation(bar2)).isEqualTo("26:19");
	}

	@Test
	void processEmptyAndNullValues() {
		OriginTrackedValue empty = getValue("empty");
		OriginTrackedValue nullValue = getValue("null-value");
		assertThat(empty.getValue()).isEqualTo("");
		assertThat(getLocation(empty)).isEqualTo("27:8");
		assertThat(nullValue.getValue()).isEqualTo("");
		assertThat(getLocation(nullValue)).isEqualTo("28:13");
	}

	@Test
	void processEmptyListAndMap() {
		OriginTrackedValue emptymap = getValue("emptymap");
		OriginTrackedValue emptylist = getValue("emptylist");
		assertThat(emptymap.getValue()).isEqualTo(Collections.emptyMap());
		assertThat(emptylist.getValue()).isEqualTo(Collections.emptyList());
	}

	@Test
	void unsupportedType() {
		String yaml = "value: !!java.net.URL [!!java.lang.String [!!java.lang.StringBuilder [\"http://localhost:9000/\"]]]";
		Resource resource = new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8));
		this.loader = new OriginTrackedYamlLoader(resource);
		assertThatExceptionOfType(ConstructorException.class).isThrownBy(this.loader::load);
	}

	@Test
	void emptyDocuments() {
		this.loader = new OriginTrackedYamlLoader(new ClassPathResource("test-empty-yaml.yml", getClass()));
		List<Map<String, Object>> loaded = this.loader.load();
		assertThat(loaded).isEmpty();
	}

	@Test
	void loadWhenLargeNumberOfNodesLoadsYaml() {
		StringBuilder yaml = new StringBuilder();
		int size = 500;
		yaml.append("defs:\n");
		for (int i = 0; i < size; i++) {
			yaml.append(" - def" + i + ": &def" + i + "\n");
			yaml.append("    - value: " + i + "\n");
		}
		yaml.append("refs:\n");
		for (int i = 0; i < size; i++) {
			yaml.append("  ref" + i + ":\n");
			yaml.append("   - value: *def" + i + "\n");
		}
		Resource resource = new ByteArrayResource(yaml.toString().getBytes(StandardCharsets.UTF_8));
		this.loader = new OriginTrackedYamlLoader(resource);
		Map<String, Object> loaded = this.loader.load().get(0);
		assertThat(loaded).hasSize(size * 2);
	}

	@Test
	void loadWhenRecursiveLoadsYaml() {
		Resource resource = new ClassPathResource("recursive.yml", getClass());
		this.loader = new OriginTrackedYamlLoader(resource);
		Map<String, Object> loaded = this.loader.load().get(0);
		assertThat(loaded.get("test.a.spring")).hasToString("a");
		assertThat(loaded.get("test.b.boot")).hasToString("b");
	}

	@Test
	void loadWhenUsingAnchors() {
		Resource resource = new ClassPathResource("anchors.yml", getClass());
		this.loader = new OriginTrackedYamlLoader(resource);
		Map<String, Object> loaded = this.loader.load().get(0);
		assertThat(loaded.get("some.path.config.key")).hasToString("value");
		assertThat(loaded.get("some.anotherpath.config.key")).hasToString("value");
	}

	private OriginTrackedValue getValue(String name) {
		if (this.result == null) {
			this.result = this.loader.load();
		}
		return (OriginTrackedValue) this.result.get(0).get(name);
	}

	private String getLocation(OriginTrackedValue value) {
		return ((TextResourceOrigin) value.getOrigin()).getLocation().toString();
	}

}
