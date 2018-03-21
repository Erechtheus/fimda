# FIMDA
UIMA wrapper for SETH with docker container and webservice

# Usage

Requires maven and git.

Clone this repo and switch into the directory:

`git clone https://github.com/Erechtheus/fimda.git && cd fimda`

To start the service, execute:

`mvn spring-boot:run`

or, to start via docker (has to be installed), execute:

```bash
mvn package
docker run -p 8080:8080 dfki/fimda:0.0.1-Snapshot
```

Note: `mvn package` creates the local docker image `dfki/fimda:0.0.1-Snapshot`

