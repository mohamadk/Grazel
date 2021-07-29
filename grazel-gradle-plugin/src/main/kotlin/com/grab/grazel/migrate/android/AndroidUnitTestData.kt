package com.grab.grazel.migrate.android

import com.grab.grazel.bazel.rules.TestSize
import com.grab.grazel.bazel.starlark.BazelDependency
import com.grab.grazel.extension.TestExtension

data class AndroidUnitTestData(
    val name: String,
    val srcs: List<String>,
    val deps: List<BazelDependency>,
    val customPackage: String
)

internal fun AndroidUnitTestData.toUnitTestTarget(config: TestExtension): AndroidUnitTestTarget {
    return AndroidUnitTestTarget(
        name = name,
        srcs = srcs,
        deps = deps,
        size = TestSize.valueOf(config.defaultTestSize.toUpperCase()),
        customPackage = customPackage
    )
}
