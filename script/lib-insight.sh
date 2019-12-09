#!/bin/bash

# usage: handle_response response
handle_response() {
    statusCode=$(echo "${1}" | tail -n 1 | rev | cut -d "}" -f1 | rev)
    if [[ ${statusCode} -gt 399 ]] && [[ ${statusCode} -lt 600 ]]; then
        echo "Error status: ${statusCode}"
    else
        responseWithoutStatus=${1: : -3}
        echo "${responseWithoutStatus}"
    fi
}

# usage: recreate_schema schemaName
recreate_schema() {
    objectSchemaName=$1
    existingSchemaId=$(get_schema_id_by_name "${objectSchemaName}")
    if [[ -n "${existingSchemaId}" ]]; then
        delete_schema "${existingSchemaId}" 2>&1 >/dev/null
    fi
    create_schema "${objectSchemaName}" "${objectSchemaName}"
}

# usage: get_schema_id_by_name schemaName
get_schema_id_by_name() {
    response=$(curl -s -w "%{http_code}" -H "Authorization: $auth" "$url/rest/insight/1.0/objectschema/list")
    handle_response "${response}" | jq ".objectschemas[] | select(.name == \"$1\") | .id"
}

# usage: create_schema key name
create_schema() {
    response=$(curl -s -w "%{http_code}" \
        -H "Authorization: $auth" \
        -H "Content-Type: application/json" \
        -X POST \
        -d "{\
            \"name\":\"$1\",\
            \"objectSchemaKey\":\"$2\",\
            \"description\":\"$1 Schema\"\
            }" \
        "$url/rest/insight/1.0/objectschema/create")
    handle_response "${response}" | jq ".id"
}

# usage: delete_schema id
delete_schema() {
    curl -s -X DELETE -H "Authorization: $auth" "$url/rest/insight/1.0/objectschema/$1"
}

# usage: create_object_type objectSchemaId name
create_object_type() {
    response=$(curl -s -w "%{http_code}" \
        -H "Authorization: $auth" \
        -H "Content-Type: application/json" \
        -X POST \
        -d "{\
            \"name\":\"$2\",\
            \"iconId\":107,\
            \"description\":\"\",\
            \"objectSchemaId\":$1,\
            \"inherited\":\"true\"\
            }" \
        "$url/rest/insight/1.0/objecttype/create")
    handle_response "${response}" | jq ".id"
}

# usage: create_object_type_sub objectSchemaId parentObjectTypeId name
create_object_type_sub() {
    response=$(curl -s -w "%{http_code}" \
        -H "Authorization: $auth" \
        -H "Content-Type: application/json" \
        -X POST \
        -d "{\
            \"name\":\"$3\",\
            \"iconId\":61,\
            \"description\":\"\",\
            \"objectSchemaId\":$1,\
            \"parentObjectTypeId\":$2\
            }" \
        "$url/rest/insight/1.0/objecttype/create")
    handle_response "${response}" | jq ".id"
}

# usage: create_default_attribute objectTypeId name defaultTypeId
create_default_attribute() {
    create_attribute "$1" "$2" 0 "$3" "" "" 0 1 "" ""
}

# usage: create_default_select_attribute objectTypeId name options
create_default_select_attribute() {
    create_attribute "$1" "$2" 0 10 "" "" 0 1 "" "$3"
}

# usage: create_reference_attribute objectTypeId name referencedObjectId minimumCardinality maximumCardinality
create_reference_attribute() {
    create_attribute "$1" "$2" 1 "" "$3" 1 "$4" "$5" "" ""
}

# https://documentation.riada.se/insight/latest/insight-advanced-usage-guide/insight-for-developers/insight-rest-api/version-1-0-documentation/object-type-attributes-rest
# usage: create_attribute objectTypeId name type defaultTypeId typeValue additionalValue minimumCardinality maximumCardinality unique options
create_attribute() {
    response=$(curl -s -w "%{http_code}" \
        -H "Authorization: $auth" \
        -H "Content-Type: application/json" \
        -X POST \
        -d "{\
            \"name\":\"$2\",\
            \"type\":\"$3\",\
            \"defaultTypeId\":\"$4\",\
            \"typeValue\":\"$5\",\
            \"additionalValue\":\"$6\",\
            \"includeChildObjectTypes\":\"true\",\
            \"minimumCardinality\":\"$7\",\
            \"maximumCardinality\":\"$8\",\
            \"uniqueAttribute\":\"$9\",\
            \"options\":\"${10}\"\
            }" \
        $url/rest/insight/1.0/objecttypeattribute/$1)
    handle_response "${response}" | jq ".id"
}
