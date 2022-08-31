package com.grab.grazel.fake

import com.grab.grazel.gradle.dependencies.BuildGraphType
import com.grab.grazel.gradle.dependencies.DependencyGraphs
import org.gradle.api.Project

internal class FakeDependencyGraphs(
    private val directDeps: Set<Project> = emptySet(),
    private val dependenciesSubGraph: Set<Project> = emptySet(),
    private val nodes: Set<Project> = emptySet()
) : DependencyGraphs {
    override fun nodes(vararg buildGraphType: BuildGraphType): Set<Project> = nodes

    override fun dependenciesSubGraph(
        project: Project,
        vararg buildGraphTypes: BuildGraphType
    ): Set<Project> =
        dependenciesSubGraph

    override fun directDependencies(
        project: Project,
        buildGraphTypes: BuildGraphType?
    ): Set<Project> = directDeps
}