package ch.ahdis.camel.aggregator;

import java.util.ArrayList;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceDesignationComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetAggregationStrategy implements AggregationStrategy {

    static FhirContext context = FhirContext.forR4();

    public ArrayList<CodeElement> getValueSetExtract(ValueSet vs) {
        ArrayList<CodeElement> codeElements = new ArrayList<CodeElement>();
        if (vs.getCompose() != null && vs.getCompose().getInclude() != null) {
            for (ConceptSetComponent include : vs.getCompose().getInclude()) {
                if (include.getConcept() != null) {
                    for (ConceptReferenceComponent concept : include.getConcept()) {
                        CodeElement codeElement = new CodeElement();
                        codeElement.codeSystem = include.getSystem();
                        codeElement.valueSet = vs.getId();
                        codeElement.valueSetTitle = vs.getTitle();
                        codeElement.valueSetOid = vs.getIdentifier().get(0).getValue();
                        codeElement.code = concept.getCode();
                        codeElement.display = concept.getDisplay();
                        if (concept.getDesignation() != null) {
                            for (ConceptReferenceDesignationComponent designation : concept.getDesignation()) {
                                if (designation.getLanguage() != null) {
                                    switch (designation.getLanguage()) {
                                    case "de-CH":
                                        if (codeElement.de_CH != null) {
                                            codeElement.addWarning("de-CH, multiple entries: "+codeElement.de_CH);
                                        }
                                        codeElement.de_CH = designation.getValue();
                                        break;
                                    case "fr-CH":
                                        if (codeElement.fr_CH != null) {
                                            codeElement.addWarning("fr-CH, multiple entries: "+codeElement.fr_CH);
                                        }
                                        codeElement.fr_CH = designation.getValue();
                                        break;
                                    case "it-CH":
                                        if (codeElement.it_CH != null) {
                                            codeElement.addWarning("it-CH, multiple entries: "+codeElement.it_CH);
                                        }
                                        codeElement.it_CH = designation.getValue();
                                        break;
                                    }
                                }
                            }
                        }
                        codeElements.add(codeElement);
                    }
                }
            }
        }
        return codeElements;
    }

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        if (oldExchange == null && newExchange != null) {
            ValueSet valueSet = (ValueSet) context.newXmlParser()
                    .parseResource(newExchange.getIn().getBody(String.class));
            newExchange.getIn().setBody(getValueSetExtract(valueSet));
            return newExchange;
        }
        if (newExchange != null) {
            @SuppressWarnings("unchecked")
            ArrayList<CodeElement> oldBody = oldExchange.getIn().getBody(ArrayList.class);
            ValueSet valueSet = (ValueSet) context.newXmlParser()
                    .parseResource(newExchange.getIn().getBody(String.class));
            oldBody.addAll(getValueSetExtract(valueSet));
            oldExchange.getIn().setBody(oldBody);
        }
        return oldExchange;
    }

}
