# FIMDA: Finding Mutations in the Digital Age

This file intends to document the integration of the software component “SNP Extraction Tool for Human Variations“ (SETH) into the OpenMinTeD platform.

The focus rests on software development and integration.

## Milestones

The following top level milestones do not strictly depend on each other:
1. UIMA XMI data format serialization for SETH output
2. REST endpoint
3. dockerize

## Implementation

- [x] acquire general knowledge about UIMA XMI:
    * Analysis Engines (AEs) produce Analysis Results (ARs): [intro](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae)
    * *Annotators* (e.g. SETH) produce *Annotations*
    * an AR is represented as CAS (Common Analysis Structure): [intro](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual.representing_results_in_cas), [references](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas)
    * a CAS contains the analyzed document, a type system and annotations
- [x] identify relevant UIMA XMI concepts/components e.g. *CAS types*:
    * annotation (describes a region of a document) -> MutationMention 
    * (entity -> Mutation)
- [x] implement relevant CAS types (MVP)
    * use JCas: [reference](https://uima.apache.org/d/uimaj-current/references.html#ugr.ref.jcas)
    * [UIMA annotator tutorial](https://uima.apache.org/doc-uima-annotator.html)
    * [version 3 user guide](https://uima.apache.org/d/uimaj-3.0.0/version_3_users_guide.html)
    * create description xml
    * convert description xml to java class, use [JCasGen](https://uima.apache.org/d/uimaj-current/tools.html#ugr.tools.jcasgen): 
        * requires UIMA SDK [installed](https://uima.apache.org/doc-uima-examples.html) 
        * execute: `PATH/TO/UIMA-SDK/bin/jcasgen.sh PATH/TO/INPUT_DESCRIPTION.xml PATH/TO/OUTPUT/DIRECTORY`
            * example: `/opt/apache-uima/bin/jcasgen.sh /home/arne/devel/Java/SETH/src/main/desc/SethTypeSystem.xml /home/arne/devel/Java/SETH/src/main/java`
        * HANDLED BY MAVEN: [jcasgen-maven-plugin](https://uima.apache.org/d/uimaj-3.0.0/tools.html#ugr.tools.jcasgen.maven_plugin)
    * create an [Analysis Engine Descriptor file](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.xml_intro_ae_descriptor)
    * test
        * using [UIMA Document Analyzer](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.testing_your_annotator)
            * see [how to use UIMA shell scripts](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.using_shell_scripts)
            * build SETH jar package with maven
            * add jar (incl. dependencies) to UIMA classpath, e.g. with: `export UIMA_CLASSPATH="/home/arne/devel/Java/SETH/target/seth-1.3.1-Snapshot-jar-with-dependencies.jar"`
            * start UIMA analyzer: `PATH/TO/UIMA-SDK/bin/documentAnalyzer.sh`
- [x] move to full [UIMA application](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.application)
    * [JSON serialization](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.json)
- [ ] ~~think about logging~~
- [ ] ~~think about multi threading (see [UIMA Multi-threaded Applications](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.applications.multi_threaded))~~
- [x] implement rest service (MVP)
    * use spring-mvc: [guide](https://spring.io/guides/gs/rest-service/)
- [x] implement complete MutationAnnotation CAS type
    * identify relevant features
    * identify feature types
    * define mappings to CAS primitive types and/or integrate required SETH types into SethTypeSystem.xml
- [x] write unit test: produce UIMA json from input text (via [spring](https://spring.io/guides/gs/testing-web/))
- [x] create a release ([maven how-to](http://blog.soebes.de/blog/2016/08/08/maven-how-to-create-a-release/))
    * push to github with [github-release-plugin](https://github.com/jutzig/github-release-plugin)
    * NOTE: private key does not work! credentials has to be stored in maven settings.xml (calling the goal with system parameters during deploy does not work!)
    * NOTE: to delete created (github) tags, execute in git root dir: `git tag -d 0.0.1 && git push --tags -f`
- [x] push image to Docker Hub
    * ~~choose "good" `docker.image.prefix`, currently it is "dfki":~~ has to be the username for docker hub
    * create docker account
    * tag image as `latest`
    * use [maven plugin](https://github.com/spotify/dockerfile-maven): ~~`mvn dockerfile:push -Ddockerfile.username=... -Ddockerfile.password=...`~~ use `mvn release` with maven settings.xml holding credentials (but `-Ddockerfile.username=... -Ddockerfile.password=...` is still possible)
- [x] license compliance
    * create list of included/used packages
- [ ] write how-to-integrate NER service (?)