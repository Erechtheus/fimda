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

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FIMDA {

    private AnalysisEngine ae;
    private JCas jcas;

    FIMDA() throws IOException, InvalidXMLException, ResourceInitializationException {
        //get Resource Specifier from XML file
        XMLInputSource in;
        try {
            String fn = getClass().getResource("/resources/desc/MutationAnnotator.xml").getFile();
            in = new XMLInputSource(fn);
        } catch (FileNotFoundException e){
            // in the compiled jar, the resource files are moved into the classes folder via maven
            in = new XMLInputSource("classpath:resources/desc/MutationAnnotator.xml");
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
    private void casFromXmi(String xmi) throws IOException, SAXException, ResourceInitializationException {
        InputStream inputStream = new ByteArrayInputStream(xmi.getBytes(StandardCharsets.UTF_8));
        jcas = ae.newJCas();
        CAS cas = jcas.getCas();
        XmiCasDeserializer.deserialize(inputStream, cas, true);
    }

    private void casFromText(String text) throws ResourceInitializationException {
        jcas = ae.newJCas();
        jcas.setDocumentText(text);
    }

    public void annotateTextPlain(String text) throws AnalysisEngineProcessException, ResourceInitializationException {
        casFromText(text);
        //analyze a document
        ae.process(jcas);
    }

    public void annotateXmi(String xmi) throws AnalysisEngineProcessException, ResourceInitializationException, SAXException, IOException {
        casFromXmi(xmi);
        //analyze a document
        ae.process(jcas);
    }

    public void resetCas() {
        jcas.reset();
    }

}
