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

package com.linktime.plugin.jira.insightobjectgraph.impl.objectgraph

import com.linktime.plugin.jira.insightobjectgraph.api.objectgraph.*
import com.linktime.plugin.jira.insightobjectgraph.impl.insight.InsightApiFacade
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named


@Named
class ObjectGraphServiceImpl
@Inject
constructor(private val insightApiFacade: InsightApiFacade) : ObjectGraphService {

    override fun getObjectGraph(blacklistRelations: Set<String>, whitelistRelations: Set<String>, rootObjectId: Int): RootNode? =
            // if we cannot find the root object, we return null instead of an empty RootNode
            insightApiFacade.loadObjectBean(rootObjectId)
                    ?.let { addObject(blacklistRelations, whitelistRelations, RootNode(emptyMap()), rootObjectId) }

    private fun addObject(blacklistRelations: Set<String>,
                          whitelistRelations: Set<String>,
                          rootNode: RootNode,
                          objectId: Int): RootNode {
        log.debug("Attempting to add object $objectId to graph ...")
        if (rootNode.objects.contains(objectId)) {
            log.debug("Not adding $objectId: Graph already contains object")
            return rootNode
        }

        val objectBean = insightApiFacade.loadObjectBean(objectId) ?: run {
            log.debug("Not adding $objectId: Insight Object not found")
            return rootNode
        }

        val schemaId = insightApiFacade.loadObjectType(objectBean.objectTypeId)?.objectSchemaId ?: run {
            log.debug("Not adding $objectId: Object type ${objectBean.objectTypeId} not found")
            return rootNode
        }
        val objectNode = toObjectNode(blacklistRelations, whitelistRelations, objectBean, schemaId)
        val newRootNode = RootNode(rootNode.objects + (objectBean.id to objectNode))
        return (objectNode.relations + objectNode.inverseRelations)
                .flatMap { it.values }
                .fold(newRootNode) { acc, it ->
                    val subTree = addObject(blacklistRelations, whitelistRelations, acc, it)
                    // the sub tree node contains all the object IDs that we found in that tree
                    // very important to pass that as a base into the next sub tree, so we don't collect the same
                    // objects over and over again
                    acc.copy(objects = acc.objects + subTree.objects)
                }
    }

    private fun toObjectNode(blacklistRelations: Set<String>, whitelistRelations: Set<String>, objectBean: ObjectBean, schemaId: Int): ObjectNode {
        fun <T : RelationNode> filterBlacklisted(relations: List<T>): List<T> =
                relations.filterNot {
                    val result = blacklistRelations.contains(it.name)
                    if (result) log.debug("Filter relation due to blacklist: ${it.name} ($blacklistRelations)")
                    result
                }

        fun <T : RelationNode> filterWhitelisted(relations: List<T>): List<T> =
                if (whitelistRelations.isEmpty()) relations else relations.filter {
                    val result = whitelistRelations.contains(it.name)
                    if (!result) log.debug("Filter relation due to whitelist: ${it.name} ($whitelistRelations)")
                    result
                }

        fun <T : RelationNode> filterRelations(relations: List<T>): List<T> =
                filterWhitelisted(filterBlacklisted(relations))

        val attributes = mutableListOf<ValueAttributeNode>()
        val relations = mutableListOf<DirectRelationNode>()
        for (objectAttributeBean in objectBean.objectAttributeBeans) {
            val logContext = "[objectTypeId: ${objectAttributeBean.objectTypeAttributeId}, objectAttributeId: ${objectAttributeBean.id}]"
            log.debug("Loading object type for object attribute ... $logContext")
            val objectTypeAttribute = insightApiFacade.loadObjectTypeAttribute(objectAttributeBean.objectTypeAttributeId)
            if (objectTypeAttribute == null) {
                log.debug("Could not load object type for object attribute, discarding attribute $logContext")
                continue
            } else if (objectTypeAttribute.isObjectReference) {
                log.debug("Object attribute is object reference -> add to both relations and attributes $logContext")
                val objects = objectAttributeBean.objectAttributeValueBeans.map { it.referencedObjectBeanId }
                relations += DirectRelationNode(objectAttributeBean.id.toInt(), objectTypeAttribute.id, objectTypeAttribute.name, objects)
                attributes += ValueAttributeNode(objectAttributeBean.id.toInt(), objectTypeAttribute.id, objectTypeAttribute.name, objects.map { it.toString() })
            } else {
                log.debug("Object attribute is NO object reference -> only add to attributes $logContext")
                val values = objectAttributeBean.objectAttributeValueBeans.map { it.value.toString() }
                attributes += ValueAttributeNode(objectAttributeBean.id.toInt(), objectTypeAttribute.id, objectTypeAttribute.name, values)
            }
        }

        // collect inverse relations as well
        val iql = "object having outboundReferences(objectId = ${objectBean.id})"
        log.debug("Collecting inverse relations for ${objectBean.id} with IQL \"$iql\" ...")
        val inverseRelations = insightApiFacade.findObjectsByIQL(iql)
                // relation name is the Object Type Name if available, and the Object Type ID otherwise
                .groupBy { insightApiFacade.loadObjectType(it.objectTypeId)?.name ?: it.objectTypeId.toString() }
                .map { (name, objects) -> InverseRelationNode(name, objects.map { it.id }) }

        return ObjectNode(
                objectBean.id, objectBean.objectTypeId, schemaId,
                attributes, filterRelations(relations), filterRelations(inverseRelations))
    }

    companion object {
        private val log = LoggerFactory.getLogger(ObjectGraphServiceImpl::class.java)
    }

}
