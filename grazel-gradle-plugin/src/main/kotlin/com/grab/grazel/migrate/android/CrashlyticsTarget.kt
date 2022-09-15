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

package com.grab.grazel.migrate.android

import com.grab.grazel.bazel.rules.crashlyticsAndroidLibrary
import com.grab.grazel.bazel.rules.googleServicesXml
import com.grab.grazel.bazel.starlark.Statement
import com.grab.grazel.bazel.starlark.asString
import com.grab.grazel.migrate.BazelTarget

class CrashlyticsTarget(
    override val name: String = "crashlytics_lib",
    val packageName: String,
    private val buildId: String,
    private val googleServicesJson: String
) : BazelTarget {

    override fun statements(): List<Statement> = com.grab.grazel.bazel.starlark.statements {
        val googleServicesXmlRes = googleServicesXml(
            packageName = packageName,
            googleServicesJson = googleServicesJson
        )
        crashlyticsAndroidLibrary(
            packageName = packageName,
            buildId = buildId,
            resourceFiles = googleServicesXmlRes.asString()
        )
    }
}