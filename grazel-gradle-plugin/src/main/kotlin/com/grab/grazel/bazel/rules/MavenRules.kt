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

import com.grab.grazel.bazel.starlark.AssigneeBuilder
import com.grab.grazel.bazel.starlark.StarlarkType
import com.grab.grazel.bazel.starlark.StatementsBuilder
import com.grab.grazel.bazel.starlark.StringStatement
import com.grab.grazel.bazel.starlark.array
import com.grab.grazel.bazel.starlark.asString
import com.grab.grazel.bazel.starlark.assigneeBuilder
import com.grab.grazel.bazel.starlark.load
import com.grab.grazel.bazel.starlark.obj
import com.grab.grazel.bazel.starlark.quote

sealed class MavenRepository : AssigneeBuilder {
    data class DefaultMavenRepository(
        val url: String,
        val username: String? = null,
        val password: String? = null
    ) : MavenRepository() {
        override fun build() = when {
            username == null || password == null -> StringStatement(url.quote)
            else -> StringStatement(
                url.split("://").joinToString(separator = "://$username:$password@").quote
            )
        }
    }
}

/**
 * External variables usually followed by the given array like `repository = EXTERNAL + [ ... ]`
 *
 * This method helps build an `Assignee` that composes this case.
 *
 * @param externalVariables The external variables that needs to be inject before given `arrayValues`
 * @param arrayValues The actual array values that needs to be combined with external variables.
 * @param appendExternal Indicates whether to append or prepend the External variables
 */
private fun combineExternalVariablesAndArray(
    externalVariables: Set<String>,
    arrayValues: List<String>,
    appendExternal: Boolean = false,
) = assigneeBuilder {
    val externalRepositoryConversion = externalVariables.joinToString(separator = " + ")
    val extraPlus = if (externalVariables.isEmpty()) "" else "+"
    val repositoryArray = array(arrayValues).asString()

    if (appendExternal) {
        StringStatement("$repositoryArray $extraPlus $externalRepositoryConversion")
    } else {
        StringStatement("$externalRepositoryConversion $extraPlus $repositoryArray")
    }
}

fun StatementsBuilder.mavenInstall(
    name: String? = null,
    rulesJvmExternalName: String,
    artifacts: Set<MavenInstallArtifact> = emptySet(),
    mavenRepositories: Set<MavenRepository> = emptySet(),
    externalArtifacts: Set<String> = emptySet(),
    externalRepositories: Set<String> = emptySet(),
    jetify: Boolean = false,
    mavenInstallJson: String? = null,
    mavenInstallJsonEnabled: Boolean = false,
    jetifyIncludeList: List<String> = emptyList(),
    failOnMissingChecksum: Boolean = true,
    resolveTimeout: Int = 600,
    excludeArtifacts: Set<String> = emptySet(),
    overrideTargets: Map<String, String> = emptyMap(),
    versionConflictPolicy: String? = null,
    artifactPinning: Boolean = false,
) {
    load("@$rulesJvmExternalName//:defs.bzl", "maven_install")
    load("@$rulesJvmExternalName//:specs.bzl", "maven")

    rule("maven_install") {
        name?.let { "name" `=` it.quote }

        "artifacts" `=` combineExternalVariablesAndArray(
            externalArtifacts,
            artifacts.map { it.asString() }
        )

        "repositories" `=` combineExternalVariablesAndArray(
            externalRepositories,
            mavenRepositories.map { it.build().asString() }.sorted(),
            true,
        )

        if (jetify) {
            "jetify" `=` "True"
        }

        jetifyIncludeList.notEmpty {
            "jetify_include_list" `=` array(jetifyIncludeList.quote)
        }

        if (!failOnMissingChecksum) {
            "fail_on_missing_checksum" `=` "False"
        }

        "resolve_timeout" `=` resolveTimeout
        excludeArtifacts.notEmpty {
            "excluded_artifacts" `=` excludeArtifacts.quote
        }

        if (overrideTargets.isNotEmpty()) {
            "override_targets" `=` obj {
                overrideTargets.forEach { (mavenArtifact, bazelLabel) ->
                    mavenArtifact.quote `=` bazelLabel.quote
                }
            }
        }

        val prefix = if (!mavenInstallJsonEnabled) "#" else ""
        mavenInstallJson?.let {
            "${prefix}maven_install_json" `=` "//:$mavenInstallJson".quote
        }

        versionConflictPolicy?.let {
            "version_conflict_policy" `=` it.quote
        }

        if (artifactPinning) {
            "fail_if_repin_required" `=` "False"
        }
    }

    if (artifactPinning) {
        load("@$name//:defs.bzl".quote) {
            "${name}_pinned_maven_install" `=` "pinned_maven_install".quote
        }
        add("${name}_pinned_maven_install()")
    }
}

/**
 * Type representing artifacts in `rules_jvm_external`'s `maven_install` rule.
 *
 * This assumes `load("@rules_jvm_external//:specs.bzl", "maven")` is already loaded
 */
sealed class MavenInstallArtifact : StarlarkType {
    // Full maven coordinates
    abstract val id: String

    data class SimpleArtifact(
        val coordinates: String,
    ) : MavenInstallArtifact() {
        override val id: String get() = coordinates

        override fun StatementsBuilder.statements() {
            add(coordinates.quote)
        }
    }

    /**
     * Type representing artifacts in `maven_install`'s `maven.artifact`
     */
    sealed class Exclusion : StarlarkType {
        data class SimpleExclusion(
            val coordinates: String
        ) : Exclusion() {
            override fun StatementsBuilder.statements() {
                add(coordinates.quote)
            }
        }

        data class DetailedExclusion(
            val group: String,
            val artifact: String
        ) : Exclusion() {
            override fun StatementsBuilder.statements() {
                rule("maven.exclusion") {
                    "group" `=` group.quote
                    "artifact" `=` artifact.quote
                }
            }
        }
    }

    data class DetailedArtifact(
        val group: String,
        val artifact: String,
        val version: String,
        val exclusions: List<Exclusion>,
    ) : MavenInstallArtifact() {
        override val id: String = "$group:$artifact:$version"
        override fun StatementsBuilder.statements() {
            rule("maven.artifact") {
                "group" `=` group.quote
                "artifact" `=` artifact.quote
                "version" `=` version.quote
                "exclusions" `=` exclusions.map { it.asString() }
            }
        }
    }
}