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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FIMDAServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void annotateShouldReturnDefaultMessage() throws Exception {

        String text = "p.A123T and Val158Met";
        //List<String> lines = Files.readAllLines(Paths.get(this.getClass().getResource("/result.xml").getFile()), StandardCharsets.UTF_8);
        //String expectedResult = String.join("\n", lines);
        String result = this.restTemplate.getForObject("http://localhost:" + port + "/annotate?text="+text, String.class);
        // create temp result file
        Path tempPath = Paths.get("./result_temp.xml");
        Files.write(tempPath, result.getBytes());

        try {
            // convert to json (xml serialization can differ for equal inputs)
            //FIMDAController controller = new FIMDAController();
            FIMDA fimda = new FIMDA();
            fimda.casFromXmi(Paths.get(this.getClass().getResource("/result.xml").getFile()));
            String expectedJCasStr = fimda.casToJson().toString();
            fimda.resetCas();
            fimda.casFromXmi(tempPath);
            String jCasStr = fimda.casToJson().toString();
            fimda.resetCas();

            assertThat(jCasStr).isEqualToIgnoringWhitespace(expectedJCasStr);
        } finally {
            // delete temp result file
            Files.delete(tempPath);
        }

    }

    @Test
    public void contextLoads() throws Exception {
    }
}
