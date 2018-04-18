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

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.json.JsonCasSerializer;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FIMDA {

    private AnalysisEngine ae;
    private JCas jcas;

    FIMDA() throws IOException, InvalidXMLException, ResourceInitializationException {
        //get Resource Specifier from XML file
        XMLInputSource in;
        try {
            String fn = getClass().getResource("/resources/desc/MutationAnnotator.xml").getFile();
            System.out.println(fn);
            in = new XMLInputSource(fn);
        } catch (FileNotFoundException e) {
            InputStream ins = getClass().getResourceAsStream("/resources/desc/MutationAnnotator.xml");
            //BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            //System.out.println(reader.lines().collect(Collectors.joining()));

            // TODO: seems to work?
            // in the compiled jar, the resource files are moved into the classes folder via maven
            //in = new XMLInputSource("classpath:resources/desc/MutationAnnotator.xml");
            in = new XMLInputSource(ins, null);
        }

        ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

        //create AE here
        ae = UIMAFramework.produceAnalysisEngine(specifier);

    }

    // public, so it can be used in tests
    public StringWriter casToJson() throws IOException {
        CAS cas = jcas.getCas();
        StringWriter sw = new StringWriter();
        JsonCasSerializer jcs = new JsonCasSerializer();
        jcs.setPrettyPrint(true); // do some configuration
        jcs.serialize(cas, sw); // serialize into sw
        //jcas.reset(); // empty the cas
        return sw;
    }

    // public, so it can be used in tests
    public StringWriter casToXmi() throws SAXException {
        CAS cas = jcas.getCas();
        StringWriter sw = new StringWriter();
        XmiCasSerializer ser = new XmiCasSerializer(cas.getTypeSystem());
        XMLSerializer xmlSer = new XMLSerializer(sw, true);
        ser.serialize(cas, xmlSer.getContentHandler());
        //jcas.reset(); // empty the cas
        return sw;
    }

    // public, so it can be used in tests
    public void casFromXmi(Path path) throws IOException, SAXException, ResourceInitializationException {
        InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(path));
        jcas = ae.newJCas();
        CAS cas = jcas.getCas();
        XmiCasDeserializer.deserialize(inputStream, cas, true);
    }

    public void casFromText(String text) throws ResourceInitializationException {
        jcas = ae.newJCas();
        jcas.setDocumentText(text);
    }

    public void process() throws AnalysisEngineProcessException {
        ae.process(jcas);
    }

    public void resetCas() {
        jcas.reset();
    }

    public void annotateXmiToXmi(Path pathIn, Path pathOut) throws ResourceInitializationException, SAXException, IOException {

        System.out.println("annotate input from CAS XMI file '"+ pathIn);
        casFromXmi(pathIn);
        try {
            process();
            StringWriter resultXmi = casToXmi();
            System.out.println("write result as CAS XMI to '" + pathOut + "'");
            Files.write(pathOut, resultXmi.toString().getBytes());
        } catch (AnalysisEngineProcessException | SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            resetCas();
        }
    }

    public static void main(String[] args) throws ResourceInitializationException, IOException, InvalidXMLException, SAXException {
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
        Path pathIn = Paths.get(cmd.getOptionValue("input"));
        Path pathOut = Paths.get(cmd.getOptionValue("output"));
        fimda.annotateXmiToXmi(pathIn, pathOut);
    }
}
