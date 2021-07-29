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

package com.grab.grazel.gradle.dependencies

import com.google.common.graph.ImmutableValueGraph
import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import com.grab.grazel.di.qualifiers.RootProject
import com.grab.grazel.gradle.ConfigurationScope
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import javax.inject.Inject

internal class DependenciesGraphsBuilder @Inject constructor(
    @param:RootProject private val rootProject: Project,
    private val dependenciesDataSource: DependenciesDataSource
) {
    fun build(): DependencyGraphs {
        val buildGraph: MutableValueGraph<Project, Configuration> =
            ValueGraphBuilder
                .directed()
                .allowsSelfLoops(false)
                .expectedNodeCount(rootProject.subprojects.size)
                .build()
        val testGraph: MutableValueGraph<Project, Configuration> =
            ValueGraphBuilder
                .directed()
                .allowsSelfLoops(false)
                .expectedNodeCount(rootProject.subprojects.size)
                .build()

        rootProject.subprojects
            .forEach { sourceProject ->
                buildGraph.addNode(sourceProject)
                testGraph.addNode(sourceProject)
                dependenciesDataSource.projectDependencies(sourceProject, ConfigurationScope.BUILD)
                    .forEach { (configuration, projectDependency) ->
                        buildGraph.putEdgeValue(
                            sourceProject,
                            projectDependency.dependencyProject,
                            configuration
                        )
                    }

                dependenciesDataSource.projectDependencies(sourceProject, ConfigurationScope.TEST)
                    .forEach { (configuration, projectDependency) ->
                        testGraph.putEdgeValue(
                            sourceProject,
                            projectDependency.dependencyProject,
                            configuration
                        )
                    }
            }
        return DefaultDependencyGraphs(
            buildGraph = ImmutableValueGraph.copyOf(buildGraph),
            testGraph = ImmutableValueGraph.copyOf(testGraph)
        )
    }
}