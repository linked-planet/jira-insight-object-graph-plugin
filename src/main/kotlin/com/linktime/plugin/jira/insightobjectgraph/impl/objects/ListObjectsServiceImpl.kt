/*
 * #%L
 * insight-object-graph
 * %%
 * Copyright (C) 2018 - 2019 The Plugin Authors
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

package com.linktime.plugin.jira.insightobjectgraph.impl.objects

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.config.properties.ApplicationProperties
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.security.JiraAuthenticationContext
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport
import com.riadalabs.jira.plugins.insight.services.model.ObjectAttributeBean
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeAttributeBean
import com.linktime.plugin.jira.insightobjectgraph.api.objects.ListObjectsService
import com.linktime.plugin.jira.insightobjectgraph.impl.insight.InsightApiFacade
import javax.inject.Inject
import javax.inject.Named

@Named
class ListObjectsServiceImpl
@Inject
constructor(private val insightApiFacade: InsightApiFacade,
            @ComponentImport private val searchService: SearchService,
            @ComponentImport private val authenticationContext: JiraAuthenticationContext,
            @ComponentImport private val fieldManager: CustomFieldManager,
            @ComponentImport private val applicationProperties: ApplicationProperties) : ListObjectsService {

    override fun getIssues(type: String): ListObjectsService.IssueListRootNode? {
        val searchResult = searchService.search(
                authenticationContext.loggedInUser,
                searchService.parseQuery(authenticationContext.loggedInUser, "\"$type\" is not empty").query,
                PagerFilter.getUnlimitedFilter())
        val issueNodes = searchResult.results
                .map { ListObjectsService.IssueNode(it.id, getIssueFields(it, type)) }
        return ListObjectsService.IssueListRootNode(issueNodes)
    }

    private fun getIssueFields(issue: Issue, type: String): List<ListObjectsService.FieldNode> =
            listOf(
                    ListObjectsService.FieldNode("Key", listOf(issue.key)),
                    ListObjectsService.FieldNode("Summary", listOf(issue.summary ?: "")),
                    ListObjectsService.FieldNode("Assignee", listOf(issue.assignee?.username ?: "")),
                    ListObjectsService.FieldNode("Reporter", listOf(issue.reporter?.username ?: "")),
                    ListObjectsService.FieldNode("Created", listOf(issue.created?.toString() ?: "")),
                    ListObjectsService.FieldNode("Updated", listOf(issue.updated?.toString() ?: "")),
                    ListObjectsService.FieldNode("Status", listOf(issue.status?.name ?: "")),
                    ListObjectsService.FieldNode("Description", listOf(issue.description ?: "")),
                    ListObjectsService.FieldNode("Priority", listOf(issue.priority?.name ?: "")),
                    ListObjectsService.FieldNode("Type", listOf(issue.issueType?.name ?: "")),
                    ListObjectsService.FieldNode("Resolution", listOf(issue.resolution?.name ?: "")),
                    ListObjectsService.FieldNode("Duedate", listOf(issue.dueDate?.toString() ?: "")),
                    ListObjectsService.FieldNode("Project", listOf(issue.projectObject?.originalKey ?: "")),
                    ListObjectsService.FieldNode("URL", listOf(issueURL(issue) ?: "")),
                    ListObjectsService.FieldNode(type, customFieldContent(issue, type))
            )

    private fun issueURL(issue: Issue): String? =
            applicationProperties.getString(APKeys.JIRA_BASEURL)
                    ?.plus("/browse/")
                    ?.plus(issue.key)


    private fun customFieldContent(issue: Issue, name: String): List<String> {
        val customFieldValue = issue.getCustomFieldValue(fieldManager.getCustomFieldObjectsByName(name).first())
        return if (customFieldValue is List<*>)
            customFieldValue.filterIsInstance<ObjectBean>().map { it.objectKey }
        else
            emptyList()
    }

    override fun getObjects(objectTypeName: String,
                            schemaId: Int?,
                            attributes: Set<String>,
                            resolveAttributes: Set<String>,
                            resolveRelations: Boolean): ListObjectsService.InsightObjectListRootNode? {
        val iql = "objectType in objectTypeAndChildren(\"$objectTypeName\")"
        val iqlResult = when (schemaId) {
            null -> insightApiFacade.findObjectsByIQL(iql)
            else -> insightApiFacade.findObjectsByIQLAndSchema(schemaId, iql)
        }
        return iqlResult
                .map { objectBean ->
                    val objectAttributes = getObjectAttributes(objectBean, resolveAttributes, resolveRelations) {
                        (attributes.isEmpty() && resolveAttributes.isEmpty()) ||
                                attributes.contains(it.name) || resolveAttributes.contains(it.name)
                    }
                    ListObjectsService.InsightObjectNode(objectBean.id, objectAttributes)
                }
                .let { ListObjectsService.InsightObjectListRootNode(it) }
    }

    private fun getObjectAttributes(
            objectBean: ObjectBean,
            resolveAttributes: Set<String>,
            resolveRelations: Boolean,
            filter: (ListObjectsService.InsightObjectAttributeNode) -> Boolean)
            : List<ListObjectsService.InsightObjectAttributeNode> {
        val attributes = objectBean.objectAttributeBeans.mapNotNull { convertAttribute(it, resolveAttributes, resolveRelations) }
        val syntheticAttributes = listOf(
                ListObjectsService.InsightObjectAttributeNode("URL", listOf(objectURL(objectBean) ?: "")))
        return (attributes + syntheticAttributes)
                .filter(filter)
    }

    private fun convertAttribute(attribute: ObjectAttributeBean,
                                 resolveAttributes: Set<String>,
                                 resolveRelations: Boolean): ListObjectsService.InsightObjectAttributeNode? =
            insightApiFacade.loadObjectTypeAttribute(attribute.objectTypeAttributeId)?.let { objectTypeAttributeBean ->
                val isObjectReference = objectTypeAttributeBean.type == ObjectTypeAttributeBean.Type.REFERENCED_OBJECT
                val attributeValues = attribute.objectAttributeValueBeans.map { it.value.toString() }
                val values =
                        if (isObjectReference && (resolveRelations || resolveAttributes.contains(objectTypeAttributeBean.name))) {
                            attributeValues
                                    .mapNotNull { it.toIntOrNull() }
                                    .mapNotNull { insightApiFacade.loadObjectBean(it) }
                                    .map { it.label }
                        } else {
                            attribute.objectAttributeValueBeans.map { it.value.toString() }
                        }
                ListObjectsService.InsightObjectAttributeNode(objectTypeAttributeBean.name, values)
            }

    private fun objectURL(objectBean: ObjectBean): String? =
            applicationProperties.getString(APKeys.JIRA_BASEURL)
                    ?.plus("/secure/ShowObject.jspa?id=")
                    ?.plus(objectBean.id)

}