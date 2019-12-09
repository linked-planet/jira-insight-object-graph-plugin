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

package it

import io.restassured.RestAssured
import it.util.InsightRestUtil
import org.junit.Before
import org.junit.BeforeClass
import java.util.*

abstract class AbstractIntegrationTest {

    protected companion object {
        const val INSIGHT_OBJECT_SCHEMA_NAME_IT = "IT"
        const val INSIGHT_OBJECT_TYPE_NAME_T1 = "T1"
        const val INSIGHT_OBJECT_TYPE_NAME_T1_SUB1 = "T1-Sub1"
        const val INSIGHT_OBJECT_TYPE_NAME_T2 = "T2"
        const val INSIGHT_OBJECT_TYPE_NAME_T3 = "T3"

        const val JIRA_INSIGHT_OBJECT_FIELD_NAME = "Insight Objects"

        var objectTypeIds: Map<String, Int> = emptyMap()
        var t1AttributeIds: Map<String, Int> = emptyMap()
        var t2AttributeIds: Map<String, Int> = emptyMap()
        var t3AttributeIds: Map<String, Int> = emptyMap()

        var issueFieldIds: Map<String, String> = emptyMap()

        @BeforeClass
        @JvmStatic
        fun configureRestAssured() {
            RestAssured.port = 2990
            RestAssured.basePath = "/rest"
            RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin")
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        }

        @BeforeClass
        @JvmStatic
        fun initInsightIds() {
            objectTypeIds = InsightRestUtil.getObjectTypeIdMap(INSIGHT_OBJECT_SCHEMA_NAME_IT)
            t1AttributeIds = InsightRestUtil.getObjectTypeAttributeIdMap(objectTypeIds.getValue(INSIGHT_OBJECT_TYPE_NAME_T1_SUB1))
            t2AttributeIds = InsightRestUtil.getObjectTypeAttributeIdMap(objectTypeIds.getValue(INSIGHT_OBJECT_TYPE_NAME_T2))
            t3AttributeIds = InsightRestUtil.getObjectTypeAttributeIdMap(objectTypeIds.getValue(INSIGHT_OBJECT_TYPE_NAME_T3))
        }

        @BeforeClass
        @JvmStatic
        fun initFieldIds() {
            issueFieldIds = InsightRestUtil.getIssueFieldIdByName()
        }
    }


    protected var testId: String = ""

    @Before
    fun setUp() {
        testId = UUID.randomUUID().toString()
    }

    protected fun createT1(name: String): Int {
        val attributes = HashMap<Int, Collection<String>>()
        attributes[t1AttributeIds.getValue("Name")] = listOf(objectName(name))
        return InsightRestUtil.createInsightObject(objectTypeIds[INSIGHT_OBJECT_TYPE_NAME_T1], attributes) ?: 0
    }

    protected fun createT1Sub1(name: String): Int {
        val attributes = HashMap<Int, Collection<String>>()
        attributes[t1AttributeIds.getValue("Name")] = listOf(objectName(name))
        return InsightRestUtil.createInsightObject(objectTypeIds[INSIGHT_OBJECT_TYPE_NAME_T1_SUB1], attributes) ?: 0
    }

    protected fun createT2(name: String, t1Id1: Int? = null, t1Id2: Int? = null): Int {
        val attributes = HashMap<Int, Collection<String>>()
        attributes[t2AttributeIds.getValue("Name")] = listOf(objectName(name))
        if (t1Id1 != null) attributes[t2AttributeIds.getValue("T1-1")] = listOf(t1Id1.toString())
        if (t1Id2 != null) attributes[t2AttributeIds.getValue("T1-2")] = listOf(t1Id2.toString())
        return InsightRestUtil.createInsightObject(objectTypeIds[INSIGHT_OBJECT_TYPE_NAME_T2], attributes) ?: 0
    }

    protected fun createT3(name: String, t2Id: Int): Int {
        val attributes = HashMap<Int, Collection<String>>()
        attributes[t3AttributeIds.getValue("Name")] = listOf(objectName(name))
        attributes[t3AttributeIds.getValue("T2")] = listOf(t2Id.toString())
        return InsightRestUtil.createInsightObject(objectTypeIds[INSIGHT_OBJECT_TYPE_NAME_T3], attributes) ?: 0
    }

    protected fun objectName(name: String): String = "$name-$testId"

}
