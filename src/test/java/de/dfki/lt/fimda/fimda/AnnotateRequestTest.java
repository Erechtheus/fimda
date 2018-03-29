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

import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnnotateRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void annotateShouldReturnDefaultMessage() throws Exception {

        String text = "p.A123T and Val158Met";
        List<String> lines = Files.readAllLines(Paths.get(this.getClass().getResource("/result.xml").getFile()), StandardCharsets.UTF_8);
        String expectedResult = String.join("\n", lines);
        String result = this.restTemplate.getForObject("http://localhost:" + port + "/annotate?text="+text, String.class);

        // convert to json (xml serialization can differ for equal inputs)
        FIMDAController controller = new FIMDAController();
        JCas expectedJCas = controller.casFromXmi(expectedResult);
        String expectedJCasStr = controller.casToJson(expectedJCas).toString();
        JCas jCas = controller.casFromXmi(result);
        String jCasStr = controller.casToJson(jCas).toString();

        assertThat(jCasStr).isEqualToIgnoringWhitespace(expectedJCasStr);
    }
}
