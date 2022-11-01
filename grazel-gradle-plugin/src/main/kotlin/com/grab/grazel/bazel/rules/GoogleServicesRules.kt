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

package com.grab.grazel.bazel.rules

import com.grab.grazel.bazel.starlark.Assignee
import com.grab.grazel.bazel.starlark.BazelDependency
import com.grab.grazel.bazel.starlark.StatementsBuilder
import com.grab.grazel.bazel.starlark.add
import com.grab.grazel.bazel.starlark.function
import com.grab.grazel.bazel.starlark.load
import com.grab.grazel.bazel.starlark.quote
import com.grab.grazel.bazel.starlark.toStatement


/**
 * Imports and configures
 * [tools_android](https://github.com/bazelbuild/tools_android) repo
 * needed for crashlytics and google services support.
 */
fun StatementsBuilder.toolAndroidRepository(
    repositoryRule: BazelRepositoryRule
) {
    add(repositoryRule)
    load(
        "@${repositoryRule.name}//tools/googleservices:defs.bzl",
        "google_services_workspace_dependencies"
    )
    add("google_services_workspace_dependencies()")
}

internal const val GOOGLE_SERVICES_XML = "GOOGLE_SERVICES_XML"

/**
 * Adds a google services XML target required by crashlytics and other
 * google services
 *
 * @param packageName The package name for the generated target
 * @param googleServicesJson The path to google_services.json relative
 *     to module.
 * @return `StringStatement` instance containing reference to generated
 *     google_services.xml
 */
fun StatementsBuilder.googleServicesXml(
    packageName: String,
    googleServicesJson: String
): Assignee {
    load("@tools_android//tools/googleservices:defs.bzl", "google_services_xml")
    GOOGLE_SERVICES_XML eq Assignee { // Create new statements scope to not add to current scope
        function("google_services_xml") {
            "package_name" eq packageName.quote()
            "google_services_json" eq googleServicesJson.quote()
        }
    }
    return GOOGLE_SERVICES_XML.toStatement()
}

/**
 * Add crashlytics_android_library target from tools_android repo.
 *
 * @param name The name for this target
 * @param packageName The package name of the android binary target
 * @param buildId The build id generated from crashlytics
 * @param resourceFiles The path to resource XML generated by google
 *     services xml target
 * @return The BazelDependency instance of the written target that can
 *     be used in android_binary.deps
 */
fun StatementsBuilder.crashlyticsAndroidLibrary(
    name: String = "crashlytics_lib",
    packageName: String,
    buildId: String,
    resourceFiles: String
): BazelDependency {
    load("@tools_android//tools/crashlytics:defs.bzl", "crashlytics_android_library")
    rule("crashlytics_android_library") {
        "name" eq name.quote()
        "package_name" eq packageName.quote()
        "build_id" eq buildId.quote()
        "resource_files" eq resourceFiles
    }
    return BazelDependency.StringDependency(":$name")
}