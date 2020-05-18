package ch.ahdis.camel.aggregator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceDesignationComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemAggregationStrategy implements AggregationStrategy {

    static FhirContext context = FhirContext.forR4();
    public Set<String> codes = new HashSet<String>();
    public String fromSystem;
    
    public CodeSystemAggregationStrategy(String fromSystem) {
        super();
        this.fromSystem = fromSystem;
    }

    public ArrayList<ConceptDefinitionComponent> getValueSetExtract(ValueSet vs) {
        ArrayList<ConceptDefinitionComponent> codeElements = new ArrayList<ConceptDefinitionComponent>();
        if (vs.getCompose() != null && vs.getCompose().getInclude() != null) {
            for (ConceptSetComponent include : vs.getCompose().getInclude()) {
                if (include.getConcept() != null && fromSystem.equals(include.getSystem())) {
                    for (ConceptReferenceComponent concept : include.getConcept()) {
                        if (!codes.contains(concept.getCode())) {
                            ConceptDefinitionComponent codeElement = new ConceptDefinitionComponent();
                            codeElement.setCode(concept.getCode());
                            codeElement.setDisplay(concept.getDisplay());
                            if (concept.getDesignation() != null) {
                                for (ConceptReferenceDesignationComponent designation : concept.getDesignation()) {
                                    if (designation.getLanguage() != null) {
                                        codeElement.addDesignation().setLanguage(designation.getLanguage()).setValue(designation.getValue());
                                    }
                                }
                            }
                            codeElements.add(codeElement);
                            codes.add(concept.getCode());
                        }
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
            ArrayList<ConceptDefinitionComponent> oldBody = oldExchange.getIn().getBody(ArrayList.class);
            ValueSet valueSet = (ValueSet) context.newXmlParser()
                    .parseResource(newExchange.getIn().getBody(String.class));
            oldBody.addAll(getValueSetExtract(valueSet));
            oldExchange.getIn().setBody(oldBody);
        }
        return oldExchange;
    }

}
