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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

@RestController
public class FIMDAController {

    private FIMDA fimda;

    FIMDAController() throws IOException, InvalidXMLException, ResourceInitializationException {
        fimda = new FIMDA();
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
            fimda.casFromText(text);
            fimda.process();

            // serialize XCAS
            // If any of the HTTP accept headers is compatible to `application/xml`, serialize to XML.
            // That is also the default, if no accept header is present (see acceptHeaders parameter annotation).
            if (acceptHeaders.stream().anyMatch(x -> x.includes(MediaType.APPLICATION_XML))){
                httpHeaders.setContentType(MediaType.APPLICATION_XML);
                result = new ResponseEntity<>(fimda.casToXmi().toString(), httpHeaders, HttpStatus.OK);
            // Otherwise, if any of the HTTP accept headers is compatible to `application/json`, serialize to JSON.
            } else if (acceptHeaders.stream().anyMatch(x -> x.includes(MediaType.APPLICATION_JSON))){
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                result = new ResponseEntity<>(fimda.casToJson().toString(), httpHeaders, HttpStatus.OK);
            } else {
                throw new IllegalArgumentException("'Accept' header has to include either " + MediaType.APPLICATION_XML_VALUE + " or " + MediaType.APPLICATION_JSON_VALUE + ", but it is: " + acceptHeaders);
            }

        } catch (AnalysisEngineProcessException | ResourceInitializationException | IOException | SAXException | IllegalArgumentException e) {
            result = new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        } finally {
            fimda.resetCas();
        }

        return result;

    }

}
