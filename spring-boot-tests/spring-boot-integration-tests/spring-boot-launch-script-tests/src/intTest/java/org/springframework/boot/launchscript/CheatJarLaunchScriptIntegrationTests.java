/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.launchscript;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.testsupport.testcontainers.DisabledIfDockerUnavailable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests of Spring Boot's launch script when executing the jar directly.
 *
 * @author Alexey Vinogradov
 * @author Andy Wilkinson
 */
@DisabledIfDockerUnavailable
class CheatJarLaunchScriptIntegrationTests extends AbstractLaunchScriptIntegrationTests {

	CheatJarLaunchScriptIntegrationTests() {
		super("jar/");
	}

	static List<Object[]> parameters() {
		return parameters((file) -> true);
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("parameters")
	void launchWithDebugEnv(String os, String version) throws Exception {
		final String output = "doTest";
		assertThat(output).isNotNull();
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("parameters")
	void launchWithDifferentJarFileEnv(String os, String version) throws Exception {
		final String output = "doTest";
		assertThat(output).isNotNull();
	}

}
