# How to wrap a Java NLP service for UIMA

This document explains how to implement a [UIMA CAS](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas) complient wrapper for your Java NLP tool. It was mainly destilled from the [UIMA getting started guide](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.getting_started). We are going to use FIMDA as example. 

Essentially, the UIMA interface is build on a CAS representation for the resulting NLP annotations and an annotation engine. Both can be defined via **descriptor files** that are later used to generate respective Java classes for annotations and annotator engines. Objects of these classes are called to produce annotation entities in the required format.

## Create a Type System

The Type System holds CAS Feature Structure representation(s). See the UIMA reference [The Type System](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas.type_system) or the UIMA guide [Defining Types](http://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.defining_types) for further information. The type system specifies what kind of data is available in our annotator.  

[SETH](https://rockt.github.io/SETH/) finds gene mutation mentions in textual input and annotates them with several information: mutation type, mutation residue, wildtype residue, mutation position and [Human Mutation Nomenclature (HGVS)](http://varnomen.hgvs.org/) grounding. We would like to provide this kind of information to our annotator. Therefore, we create the following type system description ([SethTypeSystem.xml](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/resources/desc/SethTypeSystem.xml)):

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!-- APACHE 2.0 LICENSE HEADER -->
<typeSystemDescription xmlns="http://uima.apache.org/resourceSpecifier">
    <name>SethTypeSystem</name>
    <description>Type System Definition for SETH</description>
    <vendor>Apache Software Foundation</vendor>
    <types>
        <typeDescription>
            <name>de.dfki.lt.fimda.fimda.MutationAnnotation</name>
            <description>Mutation annotation created with SETH</description>
            <supertypeName>uima.tcas.Annotation</supertypeName>
            <features>
                <featureDescription>
                    <name>mtType</name>
                    <description>mutation type</description>
                    <rangeTypeName>uima.cas.String</rangeTypeName>
                </featureDescription>
                <featureDescription>
                    <name>wtResidue</name>
                    <description>wtResidue</description>
                    <rangeTypeName>uima.cas.String</rangeTypeName>
                </featureDescription>
                <featureDescription>
                    <name>mtResidue</name>
                    <description>mtResidue</description>
                    <rangeTypeName>uima.cas.String</rangeTypeName>
                </featureDescription>
                <featureDescription>
                    <name>mtPosition</name>
                    <description>mutation position</description>
                    <rangeTypeName>uima.cas.String</rangeTypeName>
                </featureDescription>
                <featureDescription>
                    <name>anTool</name>
                    <description>annotation tool</description>
                    <rangeTypeName>uima.cas.String</rangeTypeName>
                </featureDescription>
                <featureDescription>
                    <name>hgvs</name>
                    <description>hgvs</description>
                    <rangeTypeName>uima.cas.String</rangeTypeName>
                </featureDescription>
            </features>
        </typeDescription>
    </types>
</typeSystemDescription>
```

An `uima.tcas.Annotation` object `de.dfki.lt.fimda.fimda.MutationAnnotation` is defined, that holds the different typed features (e.g., mutation type, mutation position, etc.).

Note: Any `uima.tcas.Annotation` type inherits begin and end features.

## The Analysis Engine Descriptor

The Analysis Engine descriptor defines the capabilities of the NLP service, especially regarding input/output. It should be based on *SethTypeSystem.xml* from the previous step. See the UIMA guide [Introduction to Analysis Engine Descriptor XML Syntax](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae.xml_intro_ae_descriptor) or the UIMA reference [Analysis Engine Descriptors](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.xml.component_descriptor.aes) for further information.

Have a look at the [MutationAnnotator.xml](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/resources/desc/MutationAnnotator.xml):

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!-- APACHE 2.0 LICENSE HEADER -->
<!--  Descriptor for the MutationAnnotator. -->
<analysisEngineDescription xmlns="http://uima.apache.org/resourceSpecifier">
    <frameworkImplementation>org.apache.uima.java</frameworkImplementation>
    <primitive>true</primitive>
    <annotatorImplementationName>
        de.dfki.lt.fimda.fimda.MutationAnnotator
    </annotatorImplementationName>
    <analysisEngineMetaData>
        <name>SETH Mutation Annotator</name>
        <description>A mutation annotator using SETH (SNP Extraction Tool for Human Variations).</description>
        <version>1.0</version>
        <vendor>Deutsches Forschungszentrum für Künstliche Intelligenz (DFKI)</vendor>
        <typeSystemDescription>
            <imports>
                <import location="SethTypeSystem.xml"/>
            </imports>
        </typeSystemDescription>
        <capabilities>
            <capability>
                <inputs />
                <outputs>
                    <type allAnnotatorFeatures ="true">de.dfki.lt.fimda.fimda.MutationAnnotation</type>
                </outputs>
            </capability>
        </capabilities>
    </analysisEngineMetaData>
</analysisEngineDescription>
```

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

Have a look at [MutationAnnotator.java](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java): The class inherets from `JCasAnnotator_ImplBase` and implements [`intialize`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java#L40-L46), where the SETH annotator object is instantiated:

```java
...

public class MutationAnnotator extends JCasAnnotator_ImplBase {

    private SETH seth;

    /**
     * @see org.apache.uima.analysis_component.AnalysisComponent#initialize(UimaContext)
     */
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

        String mtRegex_filename = "src/main/resources/SETH/mutations.txt";
        seth = new SETH(mtRegex_filename, true, true);
    }

...

}
```

Furthermore, in the [`process`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/MutationAnnotator.java#L51-L83) method the text to annotate is retrieved from the CAS object and  the SETH annotator is called. For every found annotation a `MutationAnnotation` object is created and filled with data from the SETH annotation. Finally, the annotation object is added to the CAS object by calling `annotation.addToIndexes()`.

```java
    /**
     * @see org.apache.uima.analysis_component.AnalysisComponent#process(AbstractCas)
     */
    public void process(JCas aJCas) {

        // The JCas object is the data object inside UIMA where all the
        // information is stored. It contains all annotations created by
        // previous annotators, and the document text to be analyzed.

        // get document text from JCas
        String docText = aJCas.getDocumentText();

        //SETH seth = new SETH();
        List<MutationMention> mutations = seth.findMutations(docText);
        try {
            for (MutationMention mutation : mutations) {
                //System.out.println(mutation.toNormalized());

                // match found - create the match as annotation in
                // the JCas with some additional meta information
                MutationAnnotation annotation = new MutationAnnotation(aJCas);
                annotation.setBegin(mutation.getStart());
                annotation.setEnd(mutation.getEnd());
                annotation.setMtPosition(mutation.getPosition());
                annotation.setMtResidue(mutation.getMutResidue());
                annotation.setWtResidue(mutation.getWtResidue());
                annotation.setMtType(mutation.getType().toString());
                annotation.setHgvs(mutation.toHGVS());
                annotation.setAnTool("SETH");
                annotation.addToIndexes();

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
```


## Calling the Annotator

To annotate some textual input, a [UIMA analysis engine (AE)](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual.aes_annotators_and_analysis_results) has to be instantiated using an annotator description file like `MutationAnnotator.xml` from above. The AE is used to create a CAS object that provides the annotation functionality which can be serialized into UIMA XMI or other output formats like JSON.

For this intent, the [constructor of `FIMDA.java`](http://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L51-L61) implements the following: a `ResourceSpecifier` is created from `MutationAnnotator.xml` that is further used to instantiate an `AnalysisEngine` object by calling `UIMAFramework.produceAnalysisEngine`:
```java
    FIMDA() throws IOException, InvalidXMLException, ResourceInitializationException {
        //get Resource Specifier from XML file
        XMLInputSource in;
        URL url = getClass().getResource("/resources/desc/MutationAnnotator.xml");
        // important: use URL to create the XMLInputSource, otherwise it wont work from the jar package
        in = new XMLInputSource(url);
        ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

        //create AE here
        ae = UIMAFramework.produceAnalysisEngine(specifier);
    }
```

To process content, a CAS object is instantiated in the [`getCas`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L63-L81) method by calling `newCAS` from the AE object. If the input data comes with an own type system, we would like to merge it into our one. Otherwise previous annotations with features not contained in our type system would be dropped and are not available in our output xmi files:
```java
    CAS getCas(Path externalTypeSystem) throws IOException, ResourceInitializationException {
        CAS aCAS = ae.newCAS();
        // merge external type system, if that file is available
        if (nonNull(externalTypeSystem) && Files.exists(externalTypeSystem)) {
            File tFile = externalTypeSystem.toFile();
            try {
                List<TypeSystemDescription> tsds = new ArrayList<>();
                tsds.add(TypeSystemUtil.typeSystem2TypeSystemDescription(aCAS.getTypeSystem()));
                tsds.add(UIMAFramework.getXMLParser()
                        .parseTypeSystemDescription(new XMLInputSource(tFile)));
                TypeSystemDescription merged = CasCreationUtils.mergeTypeSystems(tsds);
                aCAS = CasCreationUtils.createCas(merged, null, null, null);
            }
            catch (InvalidXMLException e) {
                throw new IOException(e);
            }
        }
        return aCAS;
    }
```

Then, in [`annotateXmiToXmi`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L63-L81) the reference text of this CAS object is set via `setDocumentText` and process executes the functionality defined above with respect to the reference text. Afterwards, the CAS object has to be reset.
```java
    void annotateXmiToXmi(CAS aCAS, Path pathIn, Path pathOut) {
        try {
            readXmi(aCAS, pathIn);
            ae.process(aCAS);
            writeXmi(aCAS, pathOut);
        } catch (SAXException | IOException | AnalysisEngineProcessException e){
            System.err.println(pathIn.toString() + ": error while processing file ("+e.getMessage()+")");
        } finally {
            aCAS.reset();
        }
    }
```

Finally, the functions [`readXmi`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L83-L91) and [`writeXmi`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L93-L99) handle (de-)serialization:
```java
    void readXmi(CAS aCAS, Path xmi) throws IOException {
        // Read XMI file
        try (InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(xmi))) {
            XmiCasDeserializer.deserialize(inputStream, aCAS, true);
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
    }

    void writeXmi(CAS aCAS, Path pathOut) throws SAXException, IOException {
        StringWriter sw = new StringWriter();
        XmiCasSerializer ser = new XmiCasSerializer(aCAS.getTypeSystem());
        XMLSerializer xmlSer = new XMLSerializer(sw, true);
        ser.serialize(aCAS, xmlSer.getContentHandler());
        Files.write(pathOut, sw.toString().getBytes());
    }
```

As usual, the [`main`](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L119-L166) method sticks all together:
```java
 public static void main(String[] args) throws ResourceInitializationException, IOException, InvalidXMLException {
 
...
 
        FIMDA fimda = new FIMDA();
        Files.createDirectories(pathOutDir);
        Path externalTypeSystemFile = pathInDir.resolve("typesystem.xml");
        CAS aCAS = fimda.getCas(externalTypeSystemFile);

...

        try (Stream<Path> paths = Files.walk(pathInDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xmi"))
                    .map(Path::getFileName)
                    .forEach(s -> fimda.annotateXmiToXmi(aCAS, pathInDir.resolve(s), pathOutDir.resolve(s)));
        }

...
     
    }
```

In addition to the described functionality, it handles the [argument parsing](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L120-L145) (to get input and output directories):
```java
        Options options = new Options();

        Option input = new Option("i", "input", true, "input directory containing CAS XMI files (only files with extension '.xmi' are processed)");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output directory");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("FIMDA", options);

            System.exit(1);
            return;
        }

        Path pathInDir = Paths.get(cmd.getOptionValue("input"));
        Path pathOutDir = Paths.get(cmd.getOptionValue("output"));
```

and [writes out the (eventually merged) type system](https://github.com/Erechtheus/fimda/blob/9bbd103d057b8733854b359e64e4227aa531f8d7/src/main/java/de/dfki/lt/fimda/fimda/FIMDA.java#L152-L157).
```java
        // write out (merged/new) typesystem.xml
        try (OutputStream typeOS = new FileOutputStream(pathOutDir.resolve("typesystem.xml").toFile())) {
            TypeSystemUtil.typeSystem2TypeSystemDescription(aCAS.getTypeSystem()).toXML(typeOS);
        } catch (IOException | SAXException e) {
            throw new IOException(e);
        }
```

