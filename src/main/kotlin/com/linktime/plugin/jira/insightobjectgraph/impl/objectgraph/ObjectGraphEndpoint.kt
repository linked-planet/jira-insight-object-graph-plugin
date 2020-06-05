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

import com.linktime.plugin.jira.insightobjectgraph.api.objectgraph.ObjectGraphService
import com.linktime.plugin.jira.insightobjectgraph.impl.util.toJson
import org.apache.commons.httpclient.HttpStatus
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/graph")
class ObjectGraphEndpoint
@Inject
constructor(private val objectGraphService: ObjectGraphService) {

    @GET
    @Path("{insight_object_id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getObjectGraph(@PathParam("insight_object_id") insightObjectId: Int,
                       @QueryParam("blacklist_relations") blacklistRelations: Set<String>,
                       @QueryParam("whitelist_relations") whitelistRelations: Set<String>): Response {
        val objectGraph = objectGraphService.getObjectGraph(blacklistRelations, whitelistRelations, insightObjectId)
        return if (objectGraph == null)
            Response.status(HttpStatus.SC_NOT_FOUND).build()
        else
            Response.ok(toJson(objectGraph.objects.values)).build()
    }

}
