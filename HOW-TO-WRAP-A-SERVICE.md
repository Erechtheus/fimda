# How to wrap a Java NLP service for UIMA

This document explains how to implement a [UIMA CAS](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas) complient wrapper for your Java NLP tool. It was mainly destilled from the [UIMA getting started guide](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.getting_started). We are going to use FIMDA as example. 

Essentially, the UIMA interface is build on a CAS representation for the resulting NLP annotations and an annotation engine. Both can be defined via *descriptor files* that are later used to generate respective Java classes for annotations and annotator engines. Objects of these classes are called to produce annotation entities in the required format.

## Create a Type System

The Type System holds CAS Feature Structure representation(s). See the UIMA reference [The Type System](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas.type_system) or the UIMA guide [Defining Types](http://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.defining_types) for further information. The type system specifies what kind of data is available in our annotator.  

[SETH](https://rockt.github.io/SETH/) finds gene mutation mentions in textual input and annotates them with several information: mutation type, mutation residue, wildtype residue, mutation position and [Human Mutation Nomenclature (HGVS)](http://varnomen.hgvs.org/) grounding. We would like to provide this kind of information to our annotator. Therefore, we create the following type system description:

[SethTypeSystem.xml](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/resources/desc/SethTypeSystem.xml)

An *uima.tcas.Annotation* object *de.dfki.lt.fimda.fimda.MutationAnnotation* is defined, that holds the different typed features (e.g., mutation type, mutation position, etc.).

Note: Any *uima.tcas.Annotation* type inherits begin and end features.

## The Analysis Engine Descriptor

The Analysis Engine descriptor defines the capabilities of the NLP service, especially regarding input/output. It should be based on *SethTypeSystem.xml* from the previous step. See the UIMA guide [Introduction to Analysis Engine Descriptor XML Syntax](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.xml_intro_ae_descriptor) or the UIMA reference [Analysis Engine Descriptors](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.xml.component_descriptor.aes) for further information.

Have a look at the [MutationAnnotator.xml](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/resources/desc/MutationAnnotator.xml):

The type system defined in `SethTypeSystem.xml` is imported and all of its features are declared as output capabilities. `input` remains empty because SETH does not rely on any pre-calculated features. Furthermore, the **annotator class** `e.dfki.lt.fimda.fimda.MutationAnnotator` is defined via its fully-qualified class name. The functionality of the annotator class is described below.

## Generate Java Classes

Having the required descriptor files, the UIMA tool **JCasGen** ([UIMA guide](http://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.generating_jcas_sources)) can be used to generate the required classes. In our case, this step will generate the Java class `de.dfki.lt.fimda.fimda.MutationAnnotation`.

JCasGen can be applied automatically in a maven pipeline (see [jcasgen-maven-plugin](https://mvnrepository.com/artifact/org.apache.uima/jcasgen-maven-plugin)) by adding the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>org.apache.uima</groupId>
    <artifactId>jcasgen-maven-plugin</artifactId>
    <version>3.0.0</version>
</dependency>
```

## The Annotator Class

The annotator class guides the construction of an annotation object. It has to implement the interface functions `initialize`, `process` and `destroy`. For simplicity, it should inherit from `org.apache.uima.analysis_component.JCasAnnotator_ImplBase` to get some base functionality out of the box such as default implementations of `initialize` and `destroy`.

Have a look at [MutationAnnotator.java](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java): The class inherets from `JCasAnnotator_ImplBase` and implements [`intialize`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java#L40-L46), where the SETH annotator object is instantiated.

Furthermore, in the [`process`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java#L51-L83) method the text to annotate is retrieved from the CAS object and  the SETH annotator is called. For every found annotation a `MutationAnnotation` object is created and filled with data from the SETH annotation. Finally, the annotation object is added to the CAS object by calling `annotation.addToIndexes()`.

## Calling the Annotator

To annotate some textual input, a [UIMA analysis engine (AE)](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual.aes_annotators_and_analysis_results) has to be instantiated using an annotator description file like `MutationAnnotator.xml` from above. The AE is used to create a CAS object that provides the annotation functionality which can be serialized into UIMA XMI or other output formats like JSON.

For this intent, the [constructor of `FIMDA.java`](http://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L51-L61) implements the following: a `ResourceSpecifier` is created from `MutationAnnotator.xml` that is further used to instantiate an `AnalysisEngine` object by calling `UIMAFramework.produceAnalysisEngine`.

To process content, a CAS object is instantiated in the [`getCas`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L63-L81) method by calling `newCAS` from the AE object. If the input data comes with an own type system, we would like to merge it into our one. Otherwise previous annotations with features not contained in our type system would be dropped and are not available in our output xmi files.

Then, in [`annotateXmiToXmi`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L63-L81) the reference text of this CAS object is set via `setDocumentText` and process executes the functionality defined above with respect to the reference text. Afterwards, the CAS object has to be reset.

Finally, the functions [`readXmi`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L83-L91) and [`writeXmi`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L93-L99) handle (de-)serialization.

As usual, the [`main`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L119-L166) method sticks all together. In addition to the described functionality, it handles the argument parsing [here](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L120-L145) (to get input and output directories) and writes out the (eventually merged) type system [here](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L152-L157).

