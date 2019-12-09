#!/bin/bash

# ---------------------------------------------------------------------------------------------
# Creates the "IT" Insight schema needed for running integration tests.
#
# - Requires: jq
# - Integration tests assume that this schema is created before tests are run.
# - The script will delete the schema first if it is already present. This makes it easier
#   to extend / change the script, as we don't want to have obsolete structures in the schema.
# ---------------------------------------------------------------------------------------------

# parameters used by lib-insight
url="http://localhost:2990"
auth="Basic YWRtaW46YWRtaW4="

# load lib-insight
# shellcheck disable=SC1090
source "$(dirname "$0")/lib-insight.sh"

objectSchemaId=$(recreate_schema "IT")
echo "Object Schema ID - IT: ${objectSchemaId}"

# ---------------------------------------------------------------------------------
# OBJECT TYPES
# ---------------------------------------------------------------------------------
idT1=$(create_object_type "${objectSchemaId}" "T1")
echo "Object Type ID - T1: ${idT1}"

idT1Sub1=$(create_object_type_sub "${objectSchemaId}" "${idT1}" "T1-Sub1")
echo "Object Type ID - T1-Sub1: ${idT1Sub1}"

idT2=$(create_object_type "${objectSchemaId}" "T2")
echo "Object Type ID - T2: ${idT2}"

idT3=$(create_object_type "${objectSchemaId}" "T3")
echo "Object Type ID - T3: ${idT3}"

# ---------------------------------------------------------------------------------
# T1 ATTRIBUTES
# ---------------------------------------------------------------------------------
create_reference_attribute "${idT1}" "T3" "${idT3}" 0 1

# ---------------------------------------------------------------------------------
# T2 ATTRIBUTES
# ---------------------------------------------------------------------------------
create_reference_attribute "${idT2}" "T1-1" "${idT1}" 0 1
create_reference_attribute "${idT2}" "T1-2" "${idT1}" 0 1
create_reference_attribute "${idT2}" "T3" "${idT3}" 0 1

# ---------------------------------------------------------------------------------
# T3 ATTRIBUTES
# ---------------------------------------------------------------------------------
create_reference_attribute "${idT3}" "T2" "${idT2}" 0 1
