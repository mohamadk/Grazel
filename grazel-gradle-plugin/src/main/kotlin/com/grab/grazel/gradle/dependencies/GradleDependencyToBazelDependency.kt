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

package com.grab.grazel.gradle.dependencies

import com.android.build.gradle.api.BaseVariant
import com.grab.grazel.bazel.starlark.BazelDependency
import com.grab.grazel.gradle.AndroidVariantsExtractor
import com.grab.grazel.gradle.isAndroid
import org.gradle.api.Project
import java.lang.IllegalStateException
import javax.inject.Inject

internal class GradleDependencyToBazelDependency @Inject constructor(
    private val androidVariantsExtractor: AndroidVariantsExtractor
) {

    /**
     * [variant] can only be null if and only if the [project] is a Java/Kotlin project
     */
    fun map(
        project: Project,
        dependency: Project,
        variant: BaseVariant?
    ): BazelDependency.ProjectDependency {

        return if (project.isAndroid) {
            if (variant == null) {
                throw IllegalStateException(
                    "please provide the variant for the android project " +
                        "${project.name}"
                )
            }
            if (dependency.isAndroid) {// project is an android project, dependent is also
                val dependentVariants = androidVariantsExtractor.getVariants(dependency)

                if (dependentVariants.firstOrNull { dependentVariant ->
                        dependentVariant.name == variant.name
                    } != null
                ) {
                    BazelDependency.ProjectDependency(dependency, variant.name.variantNameSuffix())
                } else {
                    if (isThereAnyCompatibleDependentBuildType(dependentVariants, variant)) {
                        BazelDependency.ProjectDependency(
                            dependency,
                            variant.buildType.name.variantNameSuffix()
                        )
                    } else {
                        throw IllegalStateException(
                            "${project.name} with variant=${variant.name}" +
                                " depends on ${dependency.name} which doesn't have that variant " +
                                "${dependentVariants.map { it.name }}"
                        )
                    }
                }
            } else {// project is an android project, dependent is NOT
                BazelDependency.ProjectDependency(dependency)
            }
        } else {// Kotlin/Java Library
            if (dependency.isAndroid) {
                throw IllegalStateException(
                    "${project.name} is not android project but it " +
                        "depends on ${dependency.name} which is an android project this is not a " +
                        "valid dependency"
                )
            } else {
                BazelDependency.ProjectDependency(dependency)
            }
        }
    }


    private fun isThereAnyCompatibleDependentBuildType(
        dependentVariants: Set<BaseVariant>,
        variant: BaseVariant
    ) = dependentVariants.firstOrNull { dependentVariant ->
        dependentVariant.name == variant.buildType.name
    } != null
}