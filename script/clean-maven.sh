#!/bin/bash

# ---------------------------------------------------------------------------------------------
# Sometimes the plugin won't start because third-party code is not properly
# bundled into the plugin (e.g. cannot find com.google.gson).
#
# This happened with Java plugins already, but seems to be even more often the
# case when using Kotlin.
#
# The problem can be solved by deleting the target/dependency-maven-plugin-markers
# folder and then re-packaging.
#
# To automate the process, create 2 Intellij run configurations:
#   1) bash script, to run this script
#   2) maven package, specifying the bash script configuration to be executed "Before launch"
# ---------------------------------------------------------------------------------------------

rm -rf target/dependency-maven-plugin-markers