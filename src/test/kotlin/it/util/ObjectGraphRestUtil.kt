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

package it.util

import io.restassured.RestAssured.given
import io.restassured.response.Response

object ObjectGraphRestUtil {

    private const val INSIGHT_OBJECT_GRAPH_BASE_PATH = "/insight-object-graph/1.0"
    private const val PATH_GRAPH = "$INSIGHT_OBJECT_GRAPH_BASE_PATH/graph"

    fun getObjectGraph(insightObjectId: Int,
                       blacklistRelations: Set<String>? = null,
                       whitelistRelations: Set<String>? = null): Response {
        val baseRequest = given().`when`()
        if (blacklistRelations != null) {
            baseRequest.queryParam("blacklist_relations", blacklistRelations)
        }
        if (whitelistRelations != null) {
            baseRequest.queryParam("whitelist_relations", whitelistRelations)
        }
        return baseRequest.get("$PATH_GRAPH/$insightObjectId")
    }

}
