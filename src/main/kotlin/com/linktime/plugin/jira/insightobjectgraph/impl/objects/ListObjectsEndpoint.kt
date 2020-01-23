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

import com.linktime.plugin.jira.insightobjectgraph.api.objects.ListObjectsService
import com.linktime.plugin.jira.insightobjectgraph.impl.util.toJson
import org.apache.commons.httpclient.HttpStatus
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/")
class ListObjectsEndpoint
@Inject
constructor(private val listService: ListObjectsService) {

    @GET
    @Path("objects/{object-type-name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSiteObjects(@PathParam("object-type-name") objectTypeName: String,
                       @QueryParam("schemaId") schemaId: Int?,
                       @QueryParam("attributes") attributes: Set<String>,
                       @QueryParam("resolve_attributes") resolveAttributes: Set<String>,
                       @QueryParam("resolve_relations") resolveRelations: Boolean,
                       @QueryParam("iql_string") iqlString: String?): Response {

        val siteObjects = listService.getObjects(objectTypeName, schemaId, attributes, resolveAttributes, resolveRelations, iqlString)
        return if (siteObjects == null)
            Response.status(HttpStatus.SC_NOT_FOUND).build()
        else
            Response.ok(toJson(siteObjects.objects)).build()
    }

    @GET
    @Path("issues/{related-object-type}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSiteIssues(@PathParam("related-object-type") type: String): Response {
        val siteIssues = listService.getIssues(type)
        return if (siteIssues == null)
            Response.status(HttpStatus.SC_NOT_FOUND).build()
        else
            Response.ok(toJson(siteIssues.issues)).build()
    }

}
