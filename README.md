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
docker run -p 8080:8080 dfki/fimda:latest
```

Note: `mvn package` creates the local docker image `dfki/fimda:latest`

Now, a rest service should be available at `http://localhost:8080/annotate`. It requires the parameter `text` holding the input.

Call it like: [http://localhost:8080/annotate?text=p.A123T%20and%20Val158Met](http://localhost:8080/annotate?text=p.A123T%20and%20Val158Met)

Note: the input text (`p.A123T and Val158Met`) is url encoded.

## License

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



