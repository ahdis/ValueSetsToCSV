package ch.ahdis.camel.aggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemAggregator {
    
    static FhirContext context = FhirContext.forR4();

    public static Processor conceptsToCodeSytem(String oidCodeSystem) {
        return exchange -> {
            ArrayList<ConceptDefinitionComponent> concepts = exchange.getIn().getBody(ArrayList.class);
            Collections.sort(concepts, new Comparator<ConceptDefinitionComponent>() {
                @Override
                public int compare(ConceptDefinitionComponent o1, ConceptDefinitionComponent o2) {
                    return o1.getCode().compareTo(o2.getCode());
                }} );
            CodeSystem codeSystem = new CodeSystem();
            codeSystem.setConcept(concepts);
            codeSystem.setUrl(oidCodeSystem);
            String serialised = context.newXmlParser().encodeResourceToString(codeSystem);
            exchange.getIn().setBody(serialised);
        };
    }

    public static void main(String args[]) throws Exception {
        CamelContext context = new DefaultCamelContext();
        String codeSystem = "urn:oid:2.16.756.5.30.1.143.5.1";
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("file:/Users/oliveregger/Documents/github/artdecor2ig/cdachresp-/resources/valueset?noop=true").aggregate(header("CamelFileParent"), new CodeSystemAggregationStrategy(codeSystem))
                .completionFromBatchConsumer().process(conceptsToCodeSytem(codeSystem)).to("file:./output/");
            }
        });
        context.start();
        Thread.sleep(10000);
        context.stop();
    }

}


// CamelFileParent