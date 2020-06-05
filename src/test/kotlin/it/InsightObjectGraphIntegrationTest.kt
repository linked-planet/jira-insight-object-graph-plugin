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

package it

import it.util.InsightRestUtil.createInsightObjectAttribute
import it.util.ObjectGraphRestUtil
import org.hamcrest.Matchers.*
import org.junit.Test

class InsightObjectGraphIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun basicObjectsWithRelations_then_jsonResponse_basicObjectsAndRelations() {
        // T2 -> T1
        // T3-1 -> T2
        // T3-2 -> T2
        val t1Id = createT1("T1")
        val t2Id = createT2("T2", t1Id)
        val t3Id1 = createT3("T3-1", t2Id)
        val t3Id2 = createT3("T3-2", t2Id)

        ObjectGraphRestUtil.getObjectGraph(t1Id)
                .then().statusCode(200)
                .body("find { it.id==$t1Id }.attributes.find { it.name=='Name' }.values", hasItems(objectName("T1")))
                .body("find { it.id==$t1Id }.inverseRelations.find { it.name=='T2' }.values", hasItems(t2Id))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-1' }.values", hasItems(t1Id))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItems(t1Id.toString()))
                .body("find { it.id==$t2Id }.inverseRelations.find { it.name=='T3' }.values", hasItems(t3Id1, t3Id2))
                .body("find { it.id==$t3Id1 }.relations.find { it.name=='T2' }.values", hasItems(t2Id))
                .body("find { it.id==$t3Id1 }.attributes.find { it.name=='T2' }.values", hasItems(t2Id.toString()))
                .body("find { it.id==$t3Id2 }.relations.find { it.name=='T2' }.values", hasItems(t2Id))
                .body("find { it.id==$t3Id2 }.attributes.find { it.name=='T2' }.values", hasItems(t2Id.toString()))
    }

    @Test
    fun doubleSidedRelation_then_jsonResponse_containsBothRelations() {
        // T2 -> T3
        // T3 -> T2
        val t2Id = createT2("T2")
        val t3Id = createT3("T3", t2Id)
        createInsightObjectAttribute(t2Id, t2AttributeIds.getValue("T3"), listOf(t3Id.toString()))

        ObjectGraphRestUtil.getObjectGraph(t2Id)
                .then().statusCode(200)
                .body("find { it.id==$t2Id }.relations.find { it.name=='T3' }.values", hasItems(t3Id))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T3' }.values", hasItems(t3Id.toString()))
                .body("find { it.id==$t2Id }.inverseRelations.find { it.name=='T3' }.values", hasItems(t3Id))
                .body("find { it.id==$t3Id }.relations.find { it.name=='T2' }.values", hasItems(t2Id))
                .body("find { it.id==$t3Id }.attributes.find { it.name=='T2' }.values", hasItems(t2Id.toString()))
                .body("find { it.id==$t3Id }.inverseRelations.find { it.name=='T2' }.values", hasItems(t2Id))
    }

    @Test
    fun relationViaTwoAttributes_then_jsonResponse_containsInverseRelationOnlyOnce() {
        // T2 -> T1 via different attributes
        val t1Id = createT1("T1")
        val t2Id = createT2("T2", t1Id, t1Id)

        ObjectGraphRestUtil.getObjectGraph(t1Id)
                .then().statusCode(200)
                .body("find { it.id==$t1Id }.inverseRelations.find { it.name=='T2' }.values", equalTo(listOf(t2Id)))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-1' }.values", hasItems(t1Id))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItems(t1Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-2' }.values", hasItems(t1Id))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-2' }.values", hasItems(t1Id.toString()))
    }

    @Test
    fun blacklistedRelation_then_jsonResponse_doesNotContainRelationOnAnyObject() {
        val t1Id = createT1("T1")
        val t2Id = createT2("T2", t1Id)
        val t3Id = createT3("T3", t2Id)
        createInsightObjectAttribute(t1Id, t1AttributeIds.getValue("T3"), listOf(t3Id.toString()))
        createInsightObjectAttribute(t2Id, t2AttributeIds.getValue("T3"), listOf(t3Id.toString()))

        ObjectGraphRestUtil.getObjectGraph(t1Id, blacklistRelations = setOf("T3"))
                .then().statusCode(200)
                .body("find { it.id==$t1Id }.relations.find { it.name=='T3' }", nullValue())
                .body("find { it.id==$t1Id }.attributes.find { it.name=='T3' }.values", hasItems(t3Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T3' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T3' }.values", hasItems(t3Id.toString()))
                .body("find { it.id==$t2Id }.inverseRelations.find { it.name=='T3' }", nullValue())
    }

    @Test
    fun whitelistedRelation_then_jsonResponse_onlyContainsThatRelationOnAnyObject() {
        val t1Id = createT1("T1")
        val t2Id = createT2("T2", t1Id)
        val t3Id = createT3("T3", t2Id)
        createInsightObjectAttribute(t1Id, t1AttributeIds.getValue("T3"), listOf(t3Id.toString()))
        createInsightObjectAttribute(t2Id, t2AttributeIds.getValue("T3"), listOf(t3Id.toString()))

        ObjectGraphRestUtil.getObjectGraph(t1Id, whitelistRelations = setOf("T2"))
                .then().statusCode(200)
                .body("find { it.id==$t1Id }.inverseRelations.find { it.name=='T2' }.values", hasItems(t2Id))
                .body("find { it.id==$t1Id }.relations.find { it.name=='T3' }", nullValue())
                .body("find { it.id==$t1Id }.attributes.find { it.name=='T3' }.values", hasItems(t3Id.toString()))
                .body("find { it.id==$t1Id }.inverseRelations.find { it.name=='T3' }", nullValue())
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-1' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItems(t1Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-2' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-2' }.values", equalTo(emptyList<String>()))
                .body("find { it.id==$t2Id }.inverseRelations.find { it.name=='T3' }", nullValue())

        ObjectGraphRestUtil.getObjectGraph(t2Id, whitelistRelations = setOf("T3"))
                .then().statusCode(200)
                .body("find { it.id==$t2Id }.relations.find { it.name=='T3' }.values", hasItems(t3Id))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T3' }.values", hasItems(t3Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-1' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItems(t1Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-2' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-2' }.values", equalTo(emptyList<String>()))
                .body("find { it.id==$t2Id }.inverseRelations.find { it.name=='T3' }.values", hasItems(t3Id))
                .body("find { it.id==$t3Id }.relations.find { it.name=='T2' }", nullValue())
                .body("find { it.id==$t3Id }.attributes.find { it.name=='T2' }.values", hasItems(t2Id.toString()))
    }

    @Test
    fun whitelistAndBlacklistRelations_then_jsonResponse_onlyContainsWhitelistedRelationsExcludingBlacklisted() {
        val t1Id = createT1("T1")
        val t2Id = createT2("T2", t1Id)
        val t3Id = createT3("T3", t2Id)
        createInsightObjectAttribute(t1Id, t1AttributeIds.getValue("T3"), listOf(t3Id.toString()))
        createInsightObjectAttribute(t2Id, t2AttributeIds.getValue("T3"), listOf(t3Id.toString()))

        ObjectGraphRestUtil.getObjectGraph(t2Id, blacklistRelations = setOf("T2"), whitelistRelations = setOf("T2", "T3"))
                .then().statusCode(200)
                .body("find { it.id==$t2Id }.relations.find { it.name=='T3' }.values", hasItems(t3Id))
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T3' }.values", hasItems(t3Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-1' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-1' }.values", hasItems(t1Id.toString()))
                .body("find { it.id==$t2Id }.relations.find { it.name=='T1-2' }", nullValue())
                .body("find { it.id==$t2Id }.attributes.find { it.name=='T1-2' }.values", equalTo(emptyList<String>()))
                .body("find { it.id==$t2Id }.inverseRelations.find { it.name=='T3' }.values", hasItems(t3Id))
                .body("find { it.id==$t3Id }.relations.find { it.name=='T2' }", nullValue())
                .body("find { it.id==$t3Id }.attributes.find { it.name=='T2' }.values", hasItems(t2Id.toString()))
    }

}
