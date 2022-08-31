package com.grab.grazel.gradle.dependencies

import com.google.common.graph.Graphs
import com.google.common.graph.ImmutableValueGraph
import com.grab.grazel.bazel.starlark.BazelDependency
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration


internal interface DependencyGraphs {
    fun nodes(vararg buildGraphType: BuildGraphType): Set<Project>
    fun dependenciesSubGraph(
        project: Project,
        vararg buildGraphTypes: BuildGraphType
    ): Set<Project>

    fun directDependencies(
        project: Project,
        buildGraphTypes: BuildGraphType?
    ): Set<Project>
}

internal class DefaultDependencyGraphs(
    private val buildGraphs: Map<BuildGraphType, ImmutableValueGraph<Project, Configuration>>
) : DependencyGraphs {
    override fun nodes(vararg buildGraphType: BuildGraphType): Set<Project> {
        return when {
            buildGraphType.isEmpty() -> buildGraphs.values.flatMap { it.nodes() }.toSet()
            else -> {
                buildGraphType.flatMap {
                    buildGraphs[it]?.nodes() ?: emptyList()
                }.toSet()
            }
        }
    }

    override fun dependenciesSubGraph(
        project: Project,
        vararg buildGraphTypes: BuildGraphType
    ): Set<Project> =
        if (buildGraphTypes.isEmpty()) {
            buildGraphs.values.flatMap {
                Graphs.reachableNodes(it.asGraph(), project)
            }
        } else {
            buildGraphTypes.flatMap { buildGraphType ->
                Graphs.reachableNodes(buildGraphs[buildGraphType]!!.asGraph(), project)
            }
        }.toSet()

    override fun directDependencies(
        project: Project,
        buildGraphType: BuildGraphType?
    ): Set<Project> =
        if (buildGraphType == null) {
            buildGraphs.values.flatMap {
                Graphs.reachableNodes(it.asGraph(), project)
            }
        } else {
            buildGraphs[buildGraphType]!!.successors(project)
        }.toSet()
}