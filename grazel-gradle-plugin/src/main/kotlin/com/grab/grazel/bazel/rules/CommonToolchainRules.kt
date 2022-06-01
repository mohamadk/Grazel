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

import com.grab.grazel.bazel.starlark.StatementsBuilder
import com.grab.grazel.bazel.starlark.function
import com.grab.grazel.bazel.starlark.load
import com.grab.grazel.bazel.starlark.quote
import com.grab.grazel.extension.CommonToolchainExtension

private const val BUILDIFIER_RELEASE = "BUILDIFIER_RELEASE"
private const val BUILDIFIER_CONFIG = "BUILDIFIER_CONFIG"

fun StatementsBuilder.registerCommonToolchains(
    bazelCommonRepoName: String,
    toolchains: CommonToolchainExtension,
) {
    val buildifier = toolchains.buildifier
    if (buildifier.releaseVersion == null && buildifier.supportedOs == null && buildifier.supportedArch == null ) {
        load("@$bazelCommonRepoName//toolchains:toolchains.bzl", "register_common_toolchains")
        function("register_common_toolchains")
    } else {
        load(
            "@$bazelCommonRepoName//toolchains:toolchains.bzl",
            "register_common_toolchains",
            "buildifier_version",
        )

        BUILDIFIER_RELEASE eq """buildifier_version(
            ${buildifier.releaseVersion.notNullOrBlank(default = {""}) {
                "version = ${it.quote()},"
            }}
            ${buildifier.supportedOs.notNullOrEmpty(default = {""}) {
                "supported_os = ${it.quote},"
            }}
            ${buildifier.supportedArch.notNullOrEmpty(default = {""}) {
                "supported_arch = ${it.quote},"
            }}
        )""".trimIndent()

        function("register_common_toolchains") {
            "buildifier" eq BUILDIFIER_RELEASE
        }
    }
}

fun StatementsBuilder.configureCommonToolchains(
    bazelCommonRepoName: String,
    toolchains: CommonToolchainExtension,
) {
    val buildifier = toolchains.buildifier

    load(
        "@$bazelCommonRepoName//toolchains:toolchains.bzl",
        "configure_common_toolchains",
    )

    buildifier.targetName?.let {
        BUILDIFIER_CONFIG eq """{
            "name": ${it.quote()}
        }""".trimIndent()
    }

    function("configure_common_toolchains") {
        buildifier.targetName?.let {
            "buildifier" eq BUILDIFIER_CONFIG
        }
    }
}
