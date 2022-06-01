package com.grab.grazel.migrate

import com.google.common.truth.Truth
import com.grab.grazel.GrazelExtension
import com.grab.grazel.bazel.starlark.asString
import com.grab.grazel.buildProject
import com.grab.grazel.di.DaggerGrazelComponent
import com.grab.grazel.gradle.ANDROID_LIBRARY_PLUGIN
import com.grab.grazel.gradle.KOTLIN_ANDROID_PLUGIN
import com.grab.grazel.migrate.internal.WorkspaceBuilder
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.repositories
import org.junit.Before
import org.junit.Test

class DaggerWorkspaceRuleTest {
    private lateinit var rootProject: Project
    private lateinit var subProject: Project

    private lateinit var workspaceFactory: WorkspaceBuilder.Factory

    @Before
    fun setup() {
        rootProject = buildProject("root")
        rootProject.extensions.add(GrazelExtension.GRAZEL_EXTENSION, GrazelExtension(rootProject))
        val grazelComponent = DaggerGrazelComponent.factory().create(rootProject)
        workspaceFactory = grazelComponent.workspaceBuilderFactory().get()

        subProject = buildProject("subproject", rootProject)
        subProject.run {
            plugins.apply {
                apply(ANDROID_LIBRARY_PLUGIN)
                apply(KOTLIN_ANDROID_PLUGIN)
            }
            repositories {
                mavenCentral()
                google()
            }
        }
    }

    @Test
    fun `should declare dagger dependencies on WORKSPACE when project depend on dagger and dagger rules declared`() {
        // Given
        val daggerTag = "2.37"
        val daggerSha = "0f001ed38ed4ebc6f5c501c20bd35a68daf01c8dbd7541b33b7591a84fcc7b1c"
        subProject.dependencies {
            add("implementation", "com.google.dagger:dagger:$daggerTag")
        }
        rootProject.configure<GrazelExtension> {
            rules {
                dagger {
                    tag = daggerTag
                    sha = daggerSha
                }
            }
        }

        // When
        val workspaceStatements = workspaceFactory
            .create(listOf(rootProject, subProject))
            .build()
            .asString()

        // Then
        Truth.assertThat(workspaceStatements.removeSpaces()).apply {
            contains(
                """load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")""".removeSpaces()
            )
            contains(
                """DAGGER_TAG = "$daggerTag"""".removeSpaces()
            )
            contains(
                """DAGGER_SHA = "$daggerSha"""".removeSpaces()
            )
            contains(
                """http_archive(
                        name = "dagger",
                        strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
                        sha256 = DAGGER_SHA,
                        url = "https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG
                    )""".trimIndent().removeSpaces()
            )
        }
    }

    private fun String.removeSpaces(): String {
        return replace(" ", "")
    }
}