# Insight Object Graph
[![Build Status](https://travis-ci.com/link-time/jira-insight-object-graph-plugin.svg?branch=master)](https://travis-ci.com/link-time/jira-insight-object-graph-plugin)
[![GitHub License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Provides a REST API to simplify Insight object access.

## Motivation
If you need to retrieve field data for lots of Insight objects, you
need many REST calls to accomplish this. This has a big negative impact on the
performance of the client application. Furthermore, the standard Insight REST API
will send responses that contain plenty of information that is not usable to your
application.

By fetching all the needed data at once, we greatly improve the performance for
such applications. The response will describe an object graph (objects are linked
via relations) and every object contains it's attribute values. As an added benefit,
you get the data already properly aggregated and this will most likely simplify the
client code.

## API Documentation
The Open API 3 specification of the REST API can be found in
[doc/rest](doc/rest).