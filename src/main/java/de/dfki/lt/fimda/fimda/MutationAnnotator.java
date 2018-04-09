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

import de.hu.berlin.wbi.objects.MutationMention;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import seth.SETH;

import java.util.List;

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
}
