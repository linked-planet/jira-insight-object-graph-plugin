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

package com.linktime.plugin.jira.insightobjectgraph.impl.insight

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeAttributeBean
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeBean
import javax.inject.Inject
import javax.inject.Named

/**
 * Wraps parts of the Insight API, making it is easier to use.
 */
@Named
class InsightApiFacade
@Inject
constructor(@field:ComponentImport private val objectFacade: ObjectFacade,
            @field:ComponentImport private val objectTypeFacade: ObjectTypeFacade,
            @field:ComponentImport private val objectTypeAttributeFacade: ObjectTypeAttributeFacade,
            @field:ComponentImport private val iqlFacade: IQLFacade) {

    fun loadObjectBean(id: Int): ObjectBean? =
            objectFacade.loadObjectBean(id)

    fun loadObjectType(id: Int): ObjectTypeBean? =
            objectTypeFacade.loadObjectTypeBean(id)

    fun loadObjectTypeAttribute(id: Int): ObjectTypeAttributeBean? =
            objectTypeAttributeFacade.loadObjectTypeAttributeBean(id)

    fun findObjectsByIQL(iql: String): List<ObjectBean> =
            iqlFacade.findObjects(iql)

    fun findObjectsByIQLAndSchema(schemaId: Int, iql: String): List<ObjectBean> =
            iqlFacade.findObjects(schemaId, iql)

}
