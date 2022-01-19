/*
 * Copyright 2022 Grabtaxi Holdings PTE LTD (GRAB)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grab.grazel.migrate.dependencies

import com.grab.grazel.buildProject
import com.grab.grazel.fake.FakeLogger
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import java.io.PrintStream
import kotlin.test.assertTrue

class MavenPinningOutputStreamTest {

    private lateinit var rootProject: Project
    private lateinit var mavenPinningOutputStream: MavenPinningOutputStream
    private lateinit var logger: FakeLogger

    @Before
    fun setup() {
        rootProject = buildProject("root")
        logger = FakeLogger()
        mavenPinningOutputStream = MavenPinningOutputStream(logger = logger)
    }

    @Test
    fun `assert when no known errors are preset for empty input`() {
        PrintStream(mavenPinningOutputStream, false).apply {
            println("empty")
            flush()
        }
        assertTrue("No parsed errors") { mavenPinningOutputStream.errors.isEmpty() }
        assertTrue("Logs are reported") { logger.logs.any { it.message?.contains("empty") == true } }
    }

    @Test
    fun `assert when invalid signature error is preset for rules_jvm_external output`() {
        PrintStream(mavenPinningOutputStream, false).apply {
            println("Error in fail: maven_install.json contains an invalid signature and may be corrupted.")
            println("Empty")
            flush()
        }
        assertTrue("Stream contains Invalid Signature error") {
            mavenPinningOutputStream.errors.any { it is MavenPinningError.InvalidSignature }
        }
        assertTrue("Logs are not reported after error") { logger.logs.none { it.message == "Empty" } }
    }
}