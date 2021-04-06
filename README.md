# Insight Object Graph for Jira
![Build Status](https://github.com/linked-planet/jira-insight-object-graph-plugin/workflows/Maven/badge.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Provides a REST API to simplify [Mindville Insight][insight] object access.

## Motivation
If you need to retrieve field data for lots of Insight objects, you
need many REST calls to accomplish this. This has a negative impact on the
performance of the client application. Furthermore, the standard Insight REST API
will send responses that contain plenty of information that is not usable to your
application.

## Overview
This plugin provides REST endpoints as explained below.

See [doc/rest/api-1-0.yml](doc/rest/api-1-0.yml) for the Open API specification.

### /objects
Retrieve all objects of a given object type, set IQL filter,
select attributes to retrieve and to resolve names of linked objects.

### /object-graph
Describes an object graph (objects are linked via relations) and every object
contains its attribute values. Data is properly aggregated.

### /issues
Retrieve all Jira issues having related objects in a given Insight custom field.


[insight]: https://www.mindville.com/insight-asset-management-CMDB-software-for-jira
