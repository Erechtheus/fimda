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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class FIMDAController {

    private AnalysisEngine ae;
    private JCas jcas;

    FIMDAController() throws IOException, InvalidXMLException, ResourceInitializationException {

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

        //create a JCas, given an Analysis Engine (ae)
        jcas = ae.newJCas();
    }

    // public, so it can be used in tests
    public StringWriter casToJson(JCas jc) throws IOException {
        CAS cas = jc.getCas();
        StringWriter sw = new StringWriter();
        JsonCasSerializer jcs = new JsonCasSerializer();
        jcs.setPrettyPrint(true); // do some configuration
        jcs.serialize(cas, sw); // serialize into sw
        return sw;
    }

    // public, so it can be used in tests
    public StringWriter casToXmi(JCas jc) throws SAXException {
        CAS cas = jc.getCas();
        StringWriter sw = new StringWriter();
        XmiCasSerializer ser = new XmiCasSerializer(cas.getTypeSystem());
        XMLSerializer xmlSer = new XMLSerializer(sw, true);
        ser.serialize(cas, xmlSer.getContentHandler());
        return sw;
    }

    // public, so it can be used in tests
    public JCas casFromXmi(String xmi) throws IOException, SAXException, ResourceInitializationException {
        InputStream inputStream = new ByteArrayInputStream(xmi.getBytes(StandardCharsets.UTF_8));
        JCas jc = ae.newJCas();
        CAS cas = jc.getCas();
        XmiCasDeserializer.deserialize(inputStream, cas, true);
        return jc;
    }

    @RequestMapping("/annotate")
    public ResponseEntity<String> findMutations(
            @RequestParam(value="text", defaultValue="p.A123T and Val158Met")
                    String text,
            @RequestHeader(value=HttpHeaders.ACCEPT, defaultValue=MediaType.APPLICATION_XML_VALUE)
                    List<MediaType> acceptHeaders) {

        final HttpHeaders httpHeaders = new HttpHeaders();
        ResponseEntity<String> result;

        try {
            //analyze a document
            jcas.setDocumentText(text);
            ae.process(jcas);
            // serialize XCAS
            // If any of the HTTP accept headers is compatible to `application/xml`, serialize to XML.
            // That is also the default, if no accept header is present (see acceptHeaders parameter annotation).
            if (acceptHeaders.stream().anyMatch(x -> x.includes(MediaType.APPLICATION_XML))){
                httpHeaders.setContentType(MediaType.APPLICATION_XML);
                result = new ResponseEntity<>(casToXmi(jcas).toString(), httpHeaders, HttpStatus.OK);
            // Otherwise, if any of the HTTP accept headers is compatible to `application/json`, serialize to JSON.
            } else if (acceptHeaders.stream().anyMatch(x -> x.includes(MediaType.APPLICATION_JSON))){
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                result = new ResponseEntity<>(casToJson(jcas).toString(), httpHeaders, HttpStatus.OK);
            } else {
                throw new IllegalArgumentException("'Accept' header has to include either " + MediaType.APPLICATION_XML_VALUE + " or " + MediaType.APPLICATION_JSON_VALUE + ", but it is: " + acceptHeaders);
            }


        } catch (AnalysisEngineProcessException | IOException | SAXException | IllegalArgumentException e) {
            //e.printStackTrace();
            result = new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        } finally {
            jcas.reset();
        }

        return result;

    }

}
