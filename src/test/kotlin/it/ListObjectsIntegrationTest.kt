/*-
 * #%L
 * insight-object-graph
 * %%
 * Copyright (C) 2018 - 2019 The Plugin Authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package it

import com.atlassian.jira.issue.IssueFieldConstants
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.*
import org.junit.Test

class ListObjectsIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun oneObjectOfType_then_jsonResponseContainsObject() {
        val t1Id = createT1("T1")
        given().`when`()
                .get("$OBJECT_LIST_PATH/$INSIGHT_OBJECT_TYPE_NAME_T1")
                .then().statusCode(200)
                .body("find { it.id==$t1Id }.attributes.find { it.name=='Name' }.values", hasItem(objectName("T1")))
    }

    @Test
    fun objectOfChildType_then_jsonResponseContainsObject() {
        val t1Sub1Id = createT1Sub1("T1-Sub1")
        given().`when`()
                .get("$OBJECT_LIST_PATH/$INSIGHT_OBJECT_TYPE_NAME_T1")
                .then().statusCode(200)
                .body("find { it.id==$t1Sub1Id }.attributes.find { it.name=='Name' }.values", hasItem(objectName("T1-Sub1")))
    }

    @Test
    fun nonExistentObjectTypeGiven_then_jsonResponseEmpty() {
        given().`when`()
                .get("$OBJECT_LIST_PATH/NOTFOUND")
                .then().statusCode(200)
                .body("final_list.findAll.size()", equalTo(0))
    }

    @Test
    fun filterAttributes_then_jsonResponseOnlyContainsGivenAttributes() {
        val t1Id = createT1("T1")
        given().`when`()
                .queryParam("attributes", "Name")
                .queryParam("attributes", "Created")
                .get("$OBJECT_LIST_PATH/$INSIGHT_OBJECT_TYPE_NAME_T1")
                .then().statusCode(200)
                .body("find { it.id==$t1Id }.attributes.find { it.name=='Name' }.values", hasItem(objectName("T1")))
                .body("find { it.id==$t1Id }.attributes.find { it.name=='Created' }.values", not(equalTo(null)))
                .body("find { it.id==$t1Id }.attributes.find { it.name=='Key' }", equalTo(null))
    }

    @Test
    fun resolveRelations_then_jsonResponseContainsNamesOfRelatedObjects() {
        val t1Id1 = createT1("T1-1")
        val t1Id2 = createT1("T1-2")
        val t2Id = createT2("T2", t1Id1, t1Id2)
        given().`when`()
                .queryParam("resolve_relations", true)
                .get("$OBJECT_LIST_PATH/$INSIGHT_OBJECT_TYPE_NAME_T2")
                .then().statusCode(200)
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItem(objectName("T1-1")))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-2' }.values", hasItem(objectName("T1-2")))
    }

    @Test
    fun resolveAttributes_then_jsonResponseContainsNamesOfRelatedObjects() {
        val t1Id1 = createT1("T1-1")
        val t1Id2 = createT1("T1-2")
        val t2Id = createT2("T2", t1Id1, t1Id2)
        given().`when`()
                .queryParam("resolve_attributes", "T1-1")
                .queryParam("attributes", "T1-2")
                .get("$OBJECT_LIST_PATH/$INSIGHT_OBJECT_TYPE_NAME_T2")
                .then().statusCode(200)
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItem(objectName("T1-1")))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-2' }.values", hasItem(t1Id2.toString()))
    }

    @Test
    fun issuesOfObjectList_then_jsonResponse_findIssue() {
        val t1Id = createT1("T1")
        val issueId = createIssue("IT", "Task", testId, t1Id)
        given().`when`()
                .get("$ISSUE_LIST_PATH/$JIRA_INSIGHT_OBJECT_FIELD_NAME")
                .then().statusCode(200)
                .body("find { it.id==$issueId }.fields.find { it.name=='Summary' }.values", hasItem(testId))
    }

    @Test
    fun issuesOfUnknownObjectList_then_jsonResponse_NoIssueFound() {
        given().`when`()
                .get("$ISSUE_LIST_PATH/NOTFOUND")
                .then().statusCode(200)
                .body("final_list.findAll.size()", equalTo(0))
    }

    private fun createIssue(projectKey: String,
                            issueType: String,
                            summary: String,
                            objectId: Int?): String? {
        objectId?.let {
            val jsonBody = "" +
                    "{\"fields\": \n {" +
                    "\"project\": {\"key\": \"" + projectKey + "\"},\n" +
                    "\"" + IssueFieldConstants.SUMMARY + "\": \"" + summary + "\",\n" +
                    "\"" + IssueFieldConstants.ISSUE_TYPE + "\": {\"name\": \"" + issueType + "\"},\n" +
                    "\"" + issueFieldIds[JIRA_INSIGHT_OBJECT_FIELD_NAME] + "\": [{ \"key\": \"$INSIGHT_OBJECT_SCHEMA_NAME_IT-$objectId\"}]\n" +
                    "}\n}"
            return given()
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .post("$JIRA_API_BASE_PATH/issue")
                    .then()
                    .statusCode(201)
                    .extract().path("id")
        }
        return null
    }

    private companion object {
        private const val INSIGHT_OBJECT_GRAPH_BASE_PATH = "/insight-object-graph/1.0"
        private const val OBJECT_LIST_PATH = "$INSIGHT_OBJECT_GRAPH_BASE_PATH/objects"
        private const val ISSUE_LIST_PATH = "$INSIGHT_OBJECT_GRAPH_BASE_PATH/issues"
        private const val JIRA_API_BASE_PATH = "/api/2"
    }

}
