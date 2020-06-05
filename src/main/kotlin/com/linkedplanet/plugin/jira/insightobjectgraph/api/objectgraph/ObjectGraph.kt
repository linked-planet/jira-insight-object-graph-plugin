/*
 * #%L
 * insight-object-graph
 * %%
 * Copyright (C) 2018 - 2020 The Plugin Authors
 * %%
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
 * #L%
 */

package com.linkedplanet.plugin.jira.insightobjectgraph.api.objectgraph

data class RootNode(val objects: Map<Int, ObjectNode>)

@Suppress("unused") // used as REST endpoint json response
class ObjectNode(val id: Int,
                 val typeId: Int,
                 val schemaId: Int,
                 val attributes: List<ValueAttributeNode>,
                 val relations: List<DirectRelationNode>,
                 val inverseRelations: List<InverseRelationNode>)

interface AttributeNode<T> {
    val id: Int?
    val typeId: Int
    val name: String
    val values: List<T>
}

interface RelationNode {
    val name: String
    val values: List<Int>
}

class ValueAttributeNode(override val id: Int?,
                         override val typeId: Int,
                         override val name: String,
                         override val values: List<String>) : AttributeNode<String>

class DirectRelationNode(override val id: Int?,
                         override val typeId: Int,
                         override val name: String,
                         override val values: List<Int>) : AttributeNode<Int>, RelationNode

class InverseRelationNode(override val name: String,
                          override val values: List<Int>) : RelationNode
