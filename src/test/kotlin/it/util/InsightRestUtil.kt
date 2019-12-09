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

package it.util

import com.google.gson.JsonParser
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import it.util.JsonUtil.groupJsonObjectArrayByNameAndId
import java.util.stream.Collectors

/**
 * Generic Insight REST utilities, not making any assumptions about project-specific object types etc.
 */
internal object InsightRestUtil {

    private const val INSIGHT_API_BASE_PATH = "/insight/1.0"
    private val JIRA_API_BASE_PATH = "/api/2"

    fun getIssueFieldIdByName(): Map<String, String> {
        val path = JIRA_API_BASE_PATH + "/field"
        val responseJsonString = get(path).then().extract().asString()
        return JsonParser().parse(responseJsonString).asJsonArray
                .map { it.asJsonObject }
                .map { it.get("name").asString to it.get("id").asString }
                .toMap()
    }

    /**
     * @return schemaName -> objectSchemaId
     */
    private val objectSchemaIdMap: Map<String, Int>
        get() {
            val path = "$INSIGHT_API_BASE_PATH/objectschema/list"
            val jsonString = get(path).then().extract().asString()
            val jsonArray = JsonParser().parse(jsonString).asJsonObject.getAsJsonArray("objectschemas")
            return groupJsonObjectArrayByNameAndId(jsonArray)
        }


    // -------------------------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------------------------

    /**
     * @return schemaName -> objectSchemaId
     */
    fun getObjectTypeIdMap(insightObjectSchemaName: String): Map<String, Int> {
        val objectSchemaId = objectSchemaIdMap[insightObjectSchemaName]
        val path = "$INSIGHT_API_BASE_PATH/objectschema/$objectSchemaId/objecttypes/flat"
        val jsonString = get(path).then().extract().asString()
        val jsonArray = JsonParser().parse(jsonString).asJsonArray
        return groupJsonObjectArrayByNameAndId(jsonArray)
    }

    /**
     * @return attributeName -> objectTypeAttributeId
     */
    fun getObjectTypeAttributeIdMap(objectTypeId: Int?): Map<String, Int> {
        objectTypeId?.let {
            val path = "$INSIGHT_API_BASE_PATH/objecttype/$objectTypeId/attributes"
            val jsonString = get(path).then().extract().asString()
            val jsonArray = JsonParser().parse(jsonString).asJsonArray
            return groupJsonObjectArrayByNameAndId(jsonArray)
        }
        return emptyMap()
    }


    // -------------------------------------------------------------------------------------------
    // POST
    // -------------------------------------------------------------------------------------------

    fun createInsightObject(objectTypeId: Int?, attributes: Map<Int, Collection<String>>): Int? {
        objectTypeId?.let {
            val path = "$INSIGHT_API_BASE_PATH/object/create"
            val attributesJson = if (attributes.isEmpty())
                ""
            else
                attributes.entries.stream()
                        .map { entry ->
                            "" +
                                    "{\n" +
                                    "    \"objectTypeAttributeId\": " + entry.key + ",\n" +
                                    "    \"objectAttributeValues\": [\n" +
                                    entry.value.stream().map { v -> "{\"value\": \"$v\"}" }.collect(Collectors.joining(",\n")) +
                                    "      ]\n" +
                                    "}"
                        }
                        .collect(Collectors.joining(",\n", "", "\n"))
            val jsonBody = "" +
                    "{\n" +
                    "    \"objectTypeId\": " + objectTypeId + ",\n" +
                    "    \"attributes\": [\n" + attributesJson + "]\n" +
                    "}"
            return given()
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .post(path)
                    .then()
                    .statusCode(201)
                    .extract().path("id")
        }
        return null
    }

    fun createInsightObjectAttribute(objectId: Int, objectTypeAttributeId: Int, values: Collection<String>) {
        val path = "$INSIGHT_API_BASE_PATH/objectattribute/create"
        val valuesJson = values.stream()
                .map { v -> "{ \"value\": \"$v\" }" }
                .collect(Collectors.joining(",\n", "", "\n"))
        val jsonBody = "" +
                "{\n" +
                "    \"objectId\": $objectId," +
                "    \"objectTypeAttributeId\": $objectTypeAttributeId," +
                "    \"objectAttributeValues\": [\n" +
                valuesJson +
                "    ]\n" +
                "}"
        given()
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .post(path)
                .then()
                .statusCode(201)
    }

}
