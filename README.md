# FIMDA
[UIMA](https://uima.apache.org/) wrapper for [SETH](http://rockt.github.io/SETH/) with docker container and webservice

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/Erechtheus/fimda.svg?branch=master)](https://travis-ci.org/Erechtheus/fimda)

[How to wrap a service](/HOW-TO-WRAP-A-SERVICE.md) explains the necessary steps to create an 
[UIMA CAS]((https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas) ) compliant REST service given a Java 
NLP tool taking FIMDA as example.

# Usage

This wrapper exposes a REST service that can be queried by sending plain text to the endpoint (see [Query the Rest Service](#query-the-rest-service)).

## Start the Rest Service

### Via Docker image from Docker Hub

Requires [docker](https://docs.docker.com/). Execute:

`docker run -p 8080:8080 erechtheus/fimda`

### Via Spring Boot

Requires [git](https://git-scm.com/) and [maven](https://maven.apache.org/index.html). Clone this repo and switch into the directory:

`git clone https://github.com/Erechtheus/fimda.git && cd fimda`

Compile and start the service:

`mvn spring-boot:run`

### Via local docker Image

Requires [git](https://git-scm.com/), [maven](https://maven.apache.org/index.html) and [docker](https://docs.docker.com/). Clone this repo and switch into the directory:

`git clone https://github.com/Erechtheus/fimda.git && cd fimda`

Compile and create a local docker image (`erechtheus/fimda:latest`):

`mvn package`

Start the local docker image:

`docker run -p 8080:8080 erechtheus/fimda`

## Query the Rest Service

Now, a rest service should be available at `http://localhost:8080/annotate`. It **requires** the parameter `text` holding the input.

Call it like: [http://localhost:8080/annotate?text=p.A123T%20and%20Val158Met](http://localhost:8080/annotate?text=p.A123T%20and%20Val158Met)

Note: The input text (in the example above: `p.A123T and Val158Met`) should be **url encoded**.

The default **output format** is [UIMA XMI CAS](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.xmi). 
If the endpoint receives an http `Accept` header that is compatible to `application/json`, 
[JSON CAS Serialization](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.json) is used.

Note: XMI serialization has higher precedence, so ensure that your request does not additionally contain an `Accept` header 
that is compatible to `application/xml` (i.e. `*/*`), if you want to get json back.


# License

Copyright 2018 Deutsches Forschungszentrum für Künstliche Intelligenz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This project uses 3rd party tools. You can find the list of 3rd party tools including their authors and licenses [here](THIRD-PARTY.txt).



