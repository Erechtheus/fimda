# FIMDA
UIMA wrapper for [SETH](http://rockt.github.io/SETH/) with docker container and webservice
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/Erechtheus/fimda.svg?branch=master)](https://travis-ci.org/Erechtheus/fimda)

# Usage

Requires [maven](https://maven.apache.org/index.html) and [git](https://git-scm.com/).

Clone this repo and switch into the directory:

`git clone https://github.com/Erechtheus/fimda.git && cd fimda`

To start the service, execute:

`mvn spring-boot:run`

or, to start via [docker](https://docs.docker.com/) (has to be installed), execute:

```bash
mvn package
docker run -p 8080:8080 dfki/fimda:0.0.1-Snapshot
```

Note: `mvn package` creates the local docker image `dfki/fimda:0.0.1-Snapshot`

Now, a rest service should be available at `http://localhost:8080/annotate`. It requires the parameter `text` holding the input.

Call it like: [http://localhost:8080/annotate?text=p.A123T%20and%20Val158Met](http://localhost:8080/annotate?text=p.A123T%20and%20Val158Met)

Note: the input text (`p.A123T and Val158Met`) is url encoded.


