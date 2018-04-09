# How to wrap a Java NLP service for UIMA

This document explains how to create a simple REST service that complies to the 
[UIMA CAS (Common Analysis Structure)](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas) 
interface from a given Java NLP service. It uses the FIMDA service implemented within this repository as vivid example.

This intend requires to solve the following main issues:
1) Integration of the given NLP service/tool into the UIMA architecture 
2) Creation of a REST service that receives (textual) input and returns annotations in CAS format

## Integration into UIMA architecture

In its core, the UIMA interface is build on a CAS representation for the resulting NLP annotations and an annotation engine. 
Both can be defined via *descriptor files* that are later used to generate respective Java classes for annotations 
and annotator engines. Objects of these classes are called to produce annotation entities in the required format.

See [Getting Started](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.getting_started) for the UIMA step-by-step tutorial, [Defining CAS-transported custom Java objects](https://uima.apache.org/d/uimaj-3.0.0/version_3_users_guide.html#uv3.custom_java_objects) for further information regarding CAS
or [UIMA Conceptual Overview](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual) for a more general introduction to UIMA.

### Defining Descriptor Files

Descriptor files define UIMA interfaces in xml. At least, two descriptor files are necessary:
1) a `TypeSystem.xml`: holding CAS Feature Structure representation(s).  
  See [SethTypeSystem.xml](/src/main/resources/desc/SethTypeSystem.xml) for an example: An `uima.tcas.Annotation` object `de.dfki.lt.fimda.fimda.MutationAnnotation` is defined, that holds different typed features like `mutation type` or `mutation position`. Any `uima.tcas.Annotation` type inherits `begin` and `end` features. 
2) an `Annotator.xml`: defining the capabilities of the NLP service, especially regarding input/output. It should be based on `TypeSystem.xml`.  
  See [MutationAnnotator.xml](/src/main/resources/desc/MutationAnnotator.xml) for an example: The type system defined in `SethTypeSystem.xml` is imported and all of its features are declared as output capabilities. `input` remains empty because SETH does not rely on any pre-calculated features. Furthermore, the *annotator class* `de.dfki.lt.fimda.fimda.MutationAnnotator` is defined via its fully-qualified class name. The functionality of the *annotator class* is described below.  

See [Introduction to Analysis Engine Descriptor XML Syntax](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.xml_intro_ae_descriptor) for another example and further information.

Having this, the UIMA tool `JCasGen` can be used to generate the required classes. 
This tool can be applied automatically in a maven pipeline (see [jcasgen-maven-plugin](https://mvnrepository.com/artifact/org.apache.uima/jcasgen-maven-plugin)).

### The Annotator Class

The annotator class guides the construction of an annotation object. It has to implement the interface functions 
`initialize`, `process` and `destroy`. For simplicity, it should inherit from 
`org.apache.uima.analysis_component.JCasAnnotator_ImplBase` to get some base functionality out of the box as default 
implementations of `initialize` and `destroy`.

Have a look at [MutationAnnotator.java](/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java): The class 
implements `intialize`, where the SETH annotator object is instantiated, and `process`. `process` retrieves the text to 
annotate from the CAS object and calls the SETH annotator. For every found annotation a `MutationAnnotation` object is
created and filled with data from the SETH annotation. Finally, the annotation object is added to the CAS object by 
calling `annotation.addToIndexes()`.

### Calling the Annotator

To annotate some textual input, an 
[UIMA analysis engine (AE)](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual.aes_annotators_and_analysis_results) 
has to be instantiated using an annotator description file (see `Annotator.xml` from [above](#defining-descriptor-files)). 
The AE is used to create a (J)CAS object that provides the annotation functionality which can be serialized into 
UIMA XMI or other output formats like JSON. 

For this intend, the constructor of [MutationAnnotator.java](/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java) 
implements the following: a `ResourceSpecifier` is created from `MutationAnnotator.xml` that is further used to
instantiate an `AnalysisEngine` object by calling `UIMAFramework.produceAnalysisEngine`. The function `newJCas` of the
AE object gives the JCAS object.

To process content, the reference text of the JCAS object is set via `setDocumentText` and `process` executes the 
functionality defined above with respect to the reference text. See function `findMutations` of 
[FIMDAController.java](/src/main/java/de/dfki/lt/fimda/fimda/FIMDAController.java). Finally, the functions 
`casToXmi` and `casToJson` implement the serialization of the JCAS object.

## The REST service

The [spring-mvc](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html) framework in 
combination with [spring-boot](https://projects.spring.io/spring-boot/) provides an easy way to create a REST service 
as described in [this guide](https://spring.io/guides/gs/rest-service/).

To create a GET endpoint at URL `/annotate` that returns annotations for a given text, the annotation function that 
produces (and serializes) the JCAS, `findMutations` (see 
[FIMDAController.java](/src/main/java/de/dfki/lt/fimda/fimda/FIMDAController.java)), is annotated  with 
`@RequestMapping("/annotate")` and its input  parameter `text` with `@RequestParam(value="text")`. To return specific 
HTTP headers, status and error messages  (if necessary), the serialized JCAS is wrapped into a `ResponseEntity` as return value. 
Furthermore, the `FIMDAController` (the class that contains `findMutations`) has to be annotated with `@RestController`.
To finally create an execution entry for the app, the package containing the controller class (`FIMDAController`) has 
to implement a class annotated with `@SpringBootApplication` (see [FIMDA.java](/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java))
just calling `SpringApplication.run` in its main method.

Assuming the project is created with maven and having set the required dependencies (at least `uimaj-core`, 
`spring-boot-starter-web` and `org.springframework.boot`) and `jcasgen-maven-plugin` in the `build/plugin` section to 
generate the UIMA classes from description files on the fly (see [pom.xml](/pom.xml) for all maven settings), 
the REST service can be started by executing `mvn spring-boot:run`. It should handle requests to 
`http://localhost:8080/annotate`.