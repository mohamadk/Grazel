/*
 * Copyright 2021 Grabtaxi Holdings PTE LTD (GRAB)
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

package com.grab.grazel.extension

import com.grab.grazel.bazel.rules.BazelRepositoryRule
import com.grab.grazel.bazel.rules.HttpArchiveRule
import groovy.lang.Closure
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.listProperty

internal const val RULES_JVM_EXTERNAL_NAME = "rules_jvm_external"
internal const val RULES_JVM_EXTERNAL_SHA256 = "f36441aa876c4f6427bfb2d1f2d723b48e9d930b62662bf723ddfb8fc80f0140"
internal const val RULES_JVM_EXTERNAl_TAG = "4.1"

internal val MAVEN_INSTALL_REPOSITORY = HttpArchiveRule(
    name = RULES_JVM_EXTERNAL_NAME,
    sha256 = RULES_JVM_EXTERNAL_SHA256,
    stripPrefix = "rules_jvm_external-%s".format(RULES_JVM_EXTERNAl_TAG),
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip".format(RULES_JVM_EXTERNAl_TAG)
)

/**
 * Configuration for [rules_jvm_external](github.com/bazelbuild/rules_jvm_external)'s maven_install rule.
 * @param repository `WORKSPACE` repository details for `rules_jvm_external`
 * @param resolveTimeout Maps to `maven_install.resolve_timeout`
 * @param excludeArtifactsDenyList By default, per
 * [artifact exclude rules](https://github.com/bazelbuild/rules_jvm_external#detailed-dependency-information-specifications)
 * are automatically generated from Gradle, `excludeArtifactsDenyList` can be used to prevent an artifact from getting
 * automatically excluded. Specify in maven `groupId:artifact` format.
 * @param excludeArtifacts Global exclude artifacts, maps to `maven_install.excluded_artifacts`. Specify in maven `groupId:artifact` format
 */
data class MavenInstallExtension(
    private val objects: ObjectFactory,
    var repository: BazelRepositoryRule = MAVEN_INSTALL_REPOSITORY,
    var resolveTimeout: Int = 600,
    var excludeArtifactsDenyList: ListProperty<String> = objects.listProperty(),
    var excludeArtifacts: ListProperty<String> = objects.listProperty(),
    var jetifyIncludeList: ListProperty<String> = objects.listProperty(),
    var jetifyExcludeList: ListProperty<String> = objects.listProperty()
) {
    // TODO GitRepositoryRule
    /**
     * Configure an HTTP Archive for `rules_jvm_external`.
     *
     * @param closure closure called with default value set to [MAVEN_INSTALL_REPOSITORY]
     */
    fun httpArchiveRepository(closure: Closure<*>) {
        repository = MAVEN_INSTALL_REPOSITORY
        closure.delegate = repository
        closure.call()
    }

    /**
     * Configure an HTTP Archive for `rules_jvm_external`.
     *
     * @param builder Builder called with default value of [MAVEN_INSTALL_REPOSITORY]
     */
    fun httpArchiveRepository(builder: HttpArchiveRule.() -> Unit) {
        repository = MAVEN_INSTALL_REPOSITORY.apply(builder)
    }
}