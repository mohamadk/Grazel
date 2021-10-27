package com.grab.grazel.migrate.kotlin

import com.grab.grazel.bazel.starlark.BazelDependency

data class UnitTestData(
    val name: String,
    val srcs: List<String>,
    val deps: List<BazelDependency>,
    val associates: List<BazelDependency>
)

internal fun UnitTestData.toUnitTestTarget(): UnitTestTarget {
    return UnitTestTarget(
        name = name,
        srcs = srcs,
        deps = deps,
        associates = associates
    )
}
