package de.dfki.lt.fimda.fimda;

/*-
 * #%L
 * FIMDA: Finding Mutations in the Digital Age
 * %%
 * Copyright (C) 2018 Deutsche Forschungszentrum für Künstliche Intelligenz (DFKI)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.cli.*;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.json.JsonCasSerializer;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class FIMDA {

    private AnalysisEngine ae;

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

    CAS getCas(Path externalTypeSystem) throws IOException, ResourceInitializationException {
        CAS aCAS = ae.newCAS();
        if (nonNull(externalTypeSystem) && Files.exists(externalTypeSystem)) {
            File tFile = externalTypeSystem.toFile();
            try {
                List<TypeSystemDescription> tsds = new ArrayList<>();
                //if (mergeTypeSystem) {
                tsds.add(TypeSystemUtil.typeSystem2TypeSystemDescription(aCAS.getTypeSystem()));
                //}
                tsds.add(UIMAFramework.getXMLParser()
                        .parseTypeSystemDescription(new XMLInputSource(tFile)));
                TypeSystemDescription merged = CasCreationUtils.mergeTypeSystems(tsds);

                // Create a temporary CAS with the merged TS
                //CAS mergedCas = CasCreationUtils.createCas(merged, null, null, null);
                aCAS = CasCreationUtils.createCas(merged, null, null, null);

                // Create a holder for the CAS metadata
                //CASMgrSerializer casMgrSerializer = Serialization
                //        .serializeCASMgr((CASImpl) mergedCas);

                // Reinitialize CAS with merged type system
                //((CASImpl) aCAS).setupCasFromCasMgrSerializer(casMgrSerializer);
            }
            catch (InvalidXMLException e) {
                throw new IOException(e);
            }
        }
        return aCAS;
    }

    void readXmi(CAS aCAS, Path xmi) throws IOException {
        // Read XMI file
        try (InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(xmi))) {
            XmiCasDeserializer.deserialize(inputStream, aCAS, true);
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
    }

    StringWriter casToJson(CAS aCAS) throws IOException {
        StringWriter sw = new StringWriter();
        JsonCasSerializer jcs = new JsonCasSerializer();
        jcs.setPrettyPrint(true); // do some configuration
        jcs.serialize(aCAS, sw); // serialize into sw
        return sw;
    }

    StringWriter casToXmi(CAS aCAS) throws SAXException {
        StringWriter sw = new StringWriter();
        XmiCasSerializer ser = new XmiCasSerializer(aCAS.getTypeSystem());
        XMLSerializer xmlSer = new XMLSerializer(sw, true);
        ser.serialize(aCAS, xmlSer.getContentHandler());
        return sw;
    }

    @Deprecated
    CAS casFromXmi(Path path) throws IOException, SAXException, ResourceInitializationException {
        InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(path));
        CAS aCAS = ae.newCAS();
        XmiCasDeserializer.deserialize(inputStream, aCAS, true);
        return aCAS;
    }

    CAS casFromText(String text) throws ResourceInitializationException {
        CAS aCAS = ae.newCAS();
        aCAS.setDocumentText(text);
        return aCAS;
    }

    void annotateXmiToXmi(CAS aCAS, Path pathIn, Path pathOut, Path externalTypeSystem) throws ResourceInitializationException, IOException {

        //System.out.println("annotate input from CAS XMI file '"+ pathIn);
        //CAS aCAS = readXmi(pathIn, externalTypeSystem);
        //CAS aCAS = getCas(externalTypeSystem);
        readXmi(aCAS, pathIn);

        try {
            ae.process(aCAS);
            StringWriter resultXmi = casToXmi(aCAS);
            //System.out.println("write result as CAS XMI to '" + pathOut + "'");
            Files.write(pathOut, resultXmi.toString().getBytes());
        } catch (AnalysisEngineProcessException | SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            aCAS.reset();
        }
    }

    public static void main(String[] args) throws ResourceInitializationException, IOException, InvalidXMLException {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input CAS XMI file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output CAS XMI file");
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

        FIMDA fimda = new FIMDA();
        Path pathInDir = Paths.get(cmd.getOptionValue("input"));
        Path pathOutDir = Paths.get(cmd.getOptionValue("output"));

        Files.createDirectories(pathOutDir);
        Path externalTypeSystemFile = pathInDir.resolve("typesystem.xml");
        //if (Files.exists(externalTypeSystemFile))
        CAS aCAS = fimda.getCas(externalTypeSystemFile);
        // TODO: write out (merged/new) typesystem.xml (from aCAS.getTypeSystem())

        try (Stream<Path> paths = Files.walk(pathInDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xmi"))
                    .map(Path::getFileName)
                    .forEach(s -> {
                        try {
                            fimda.annotateXmiToXmi(aCAS, pathInDir.resolve(s), pathOutDir.resolve(s), externalTypeSystemFile);
                        } catch (ResourceInitializationException | IOException e) {
                            System.err.println(s.toString() + ": error while processing file ("+e.getMessage()+")");
                        }
                    });
        }
    }
}
