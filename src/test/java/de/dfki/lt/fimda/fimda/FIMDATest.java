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

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FIMDATest {

    @Test
    public void testMain() throws ResourceInitializationException, IOException, InvalidXMLException, SAXException {

        FIMDA fimda = new FIMDA();
        Path pathIn = Paths.get(this.getClass().getResource("/input.xml").getFile());
        Path tempPath = Paths.get("./result_temp.xml");
        fimda.annotateXmiToXmi(pathIn, tempPath);

        try {
            // convert to json (xml serialization can differ for equal inputs)
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
}
