package com.grab.grazel.gradle.dependencies

import com.google.common.graph.ImmutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import com.grab.grazel.fake.FakeConfiguration
import com.grab.grazel.fake.FakeProject
import com.grab.grazel.gradle.ConfigurationScope
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.junit.Test
import kotlin.test.assertEquals


class DefaultDependencyGraphsTest {
    private val projectA = FakeProject("A")
    private val projectB = FakeProject("B")
    private val projectC = FakeProject("C")
    private val projectD = FakeProject("D")
    private val projectE = FakeProject("E")

    private val dependenciesGraphs = DefaultDependencyGraphs(
        buildGraphs = mapOf(
            BuildGraphType(ConfigurationScope.BUILD) to buildBuildGraphs(),
            BuildGraphType(ConfigurationScope.TEST) to buildTestGraphs()
        )
    )

    @Test
    fun nodesShouldReturnAllNodesIfNoScopePassed() {
        val buildNodes = setOf(projectA, projectB, projectC)
        val testNodes = setOf(projectA, projectB, projectC, projectD, projectE)
        assertEquals(
            testNodes + buildNodes, dependenciesGraphs.nodes()
        )
    }

    @Test
    fun nodesShouldReturnTheCorrectItemsBaseOnScopes() {
        val buildNodes = setOf(projectA, projectB, projectC)
        val testNodes = setOf(projectA, projectB, projectC, projectD, projectE)
        assertEquals(buildNodes, dependenciesGraphs.nodes(BuildGraphType(ConfigurationScope.BUILD)))
        assertEquals(testNodes, dependenciesGraphs.nodes(BuildGraphType(ConfigurationScope.TEST)))
        assertEquals(
            testNodes + buildNodes,
            dependenciesGraphs.nodes(
                BuildGraphType(ConfigurationScope.TEST),
                BuildGraphType(ConfigurationScope.BUILD)
            )
        )
    }

    @Test
    fun directDependenciesShouldReturnDirectDepsFromBuildScope() {
        val directDepsFromAWithBuildScope = setOf(projectB, projectC)
        assertEquals(
            directDepsFromAWithBuildScope,
            dependenciesGraphs.directDependencies(projectA, BuildGraphType(ConfigurationScope.BUILD))
        )
    }

    @Test
    fun dependenciesSubGraphShouldReturnDepsFromAllScopeIfNoScopePassed() {
        val expectDeps = setOf(projectB, projectC, projectD, projectE)
        assertEquals(
            expectDeps,
            dependenciesGraphs.dependenciesSubGraph(projectB)
        )
    }

    @Test
    fun dependenciesSubGraphShouldReturnDepsFromBuildScope() {
        val expectDeps = setOf(projectB, projectC)
        assertEquals(
            expectDeps,
            dependenciesGraphs.dependenciesSubGraph(
                projectB,
                BuildGraphType(ConfigurationScope.BUILD)
            )
        )
    }

    @Test
    fun dependenciesSubGraphShouldReturnDepsFromBuildAndTestScope() {
        val expectDeps = setOf(projectB, projectC, projectD, projectE)
        assertEquals(
            expectDeps,
            dependenciesGraphs.dependenciesSubGraph(
                projectB,
                BuildGraphType(ConfigurationScope.BUILD),
                BuildGraphType(ConfigurationScope.TEST)
            )
        )
    }

    private fun buildBuildGraphs(): ImmutableValueGraph<Project, Configuration> =
        ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(6)
            .build<Project, Configuration>().apply {
                putEdgeValue(projectA, projectB, FakeConfiguration())
                putEdgeValue(projectA, projectC, FakeConfiguration())
                putEdgeValue(projectB, projectC, FakeConfiguration())
            }.run { ImmutableValueGraph.copyOf(this) }

    private fun buildTestGraphs(): ImmutableValueGraph<Project, Configuration> =
        ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .expectedNodeCount(6)
            .build<Project, Configuration>().apply {
                putEdgeValue(projectA, projectB, FakeConfiguration())
                putEdgeValue(projectA, projectC, FakeConfiguration())
                putEdgeValue(projectB, projectC, FakeConfiguration())
                putEdgeValue(projectC, projectD, FakeConfiguration())
                putEdgeValue(projectB, projectE, FakeConfiguration())
                putEdgeValue(projectA, projectE, FakeConfiguration())
            }.run { ImmutableValueGraph.copyOf(this) }
}