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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.StreamSupport

internal object JsonUtil {

    fun groupJsonObjectArrayByNameAndId(jsonArray: JsonArray): Map<String, Int> {
        return groupJsonObjectArrayBy(
                jsonArray,
                "name", Function { it.asString },
                "id", Function { it.asInt }
        )
    }

    fun <K, V> groupJsonObjectArrayBy(jsonArray: JsonArray,
                                      keyAttribute: String,
                                      keyTransform: Function<JsonElement, K>,
                                      valueAttribute: String,
                                      valueTransform: Function<JsonElement, V>): Map<K, V> {
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map<JsonObject> { it.asJsonObject }
                // if an attribute's value is unset, then it does not have an id
                .filter { o -> o.has("id") }
                .collect(Collectors.toMap<JsonObject, K, V>(
                        { x -> keyTransform.apply(x.get(keyAttribute)) },
                        { x -> valueTransform.apply(x.get(valueAttribute)) }))
    }

}
