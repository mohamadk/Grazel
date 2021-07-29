package com.grab.grazel.migrate.kotlin

import com.grab.grazel.bazel.rules.TestSize
import com.grab.grazel.bazel.starlark.BazelDependency
import com.grab.grazel.extension.TestExtension

data class UnitTestData(
    val name: String,
    val srcs: List<String>,
    val deps: List<BazelDependency>
)

internal fun UnitTestData.toUnitTestTarget(config: TestExtension): UnitTestTarget {
    return UnitTestTarget(
        name = name,
        srcs = srcs,
        deps = deps,
        size = TestSize.valueOf(config.defaultTestSize.toUpperCase())
    )
}
