# How to wrap a Java NLP service for UIMA

This document explains how to create a simple REST service that complies to the 
[UIMA CAS (Common Analysis Structure)](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas) 
interface from a given Java NLP service. It uses the FIMDA service implemented within this repository as vivid example.

That intend requires to solve the following main issues:
1) Integration of the given NLP service/tool into the UIMA architecture 
2) Creation of a REST service that receives (textual) input and returns annotations in CAS format

## Integration into UIMA architecture

In its core, the UIMA interface is build on a CAS representation for the resulting NLP annotations and an annotation engine. 
Both can be defined via *descriptor files* that are later used to generate respective Java classes. Then, objects of these classes are called to 
See [Defining CAS-transported custom Java objects](https://uima.apache.org/d/uimaj-3.0.0/version_3_users_guide.html#uv3.custom_java_objects) for further information
or [UIMA Conceptual Overview](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual) for a more general introduction to UIMA.

### Defining Descriptor Files

Descriptor files define UIMA interfaces in xml. At least, two descriptor files are necessary:
1) a `TypeSystem.xml`: holding CAS Feature Structure representation(s). See [SethTypeSystem.xml](src/main/resources/desc/SethTypeSystem.xml) for an example: An `uima.tcas.Annotation` object `de.dfki.lt.fimda.fimda.MutationAnnotation` is defined, that holds different typed features like `mutation type` or `mutation position`. Any `uima.tcas.Annotation` type inherits `begin` and `end` features. 
2) an `Annotator.xml`: defining the capabilities of the NLP service, especially regarding input/output. It should be based on `TypeSystem.xml`. See [MutationAnnotation.xml](src/main/resources/desc/MutationAnnotation.xml) for an example: The type system defined in `SethTypeSystem.xml` is imported and all of its features are declared as output capabilities. `input` remains empty because SETH does not rely on any pre-calculated features. Furthermore, the *annotator class* `de.dfki.lt.fimda.fimda.MutationAnnotator` is defined via its fully-qualified class name. The functionality of the *annotator class* is described below.  

See [Introduction to Analysis Engine Descriptor XML Syntax](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.xml_intro_ae_descriptor) for another example and further information.

Having this, the UIMA tool `JCasGen` can be used to generate the required classes. 
This tool can be applied automatically in the maven pipeline.

### The Annotator Class

TODO

### Calling the Annotator

TODO

## The REST service

TODO