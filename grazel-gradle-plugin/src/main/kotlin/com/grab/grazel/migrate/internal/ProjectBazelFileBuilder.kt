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

package com.grab.grazel.migrate.internal

import com.grab.grazel.bazel.starlark.statements
import com.grab.grazel.migrate.BazelFileBuilder
import com.grab.grazel.migrate.TargetBuilder
import org.gradle.api.Project
import javax.inject.Inject
import javax.inject.Singleton

class ProjectBazelFileBuilder(
    private val project: Project,
    private val targetBuilders: Set<TargetBuilder>
) : BazelFileBuilder {

    @Singleton
    class Factory
    @Inject
    constructor(
        private val targetBuilders: Set<@JvmSuppressWildcards TargetBuilder>
    ) {
        fun create(project: Project) = ProjectBazelFileBuilder(project, targetBuilders)
    }

    override fun build() = statements {
        targetBuilders
            .sortedBy(TargetBuilder::sortOrder)
            .filter { it.canHandle(project) }
            .flatMap { it.build(project) }
            .forEach { it.statements(this) }
    }
}