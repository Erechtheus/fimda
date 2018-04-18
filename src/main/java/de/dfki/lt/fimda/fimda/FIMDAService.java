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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FIMDAService {

    public static void main(String[] args) {
        SpringApplication.run(FIMDAService.class, args);
    }
}
