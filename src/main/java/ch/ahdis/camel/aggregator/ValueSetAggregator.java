package ch.ahdis.camel.aggregator;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.BindyType;

public class ValueSetAggregator {


    public static void main(String args[]) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("file:/Users/oliveregger/Documents/github/artdecor2ig/cdachresp-/resources/valueset?noop=true").aggregate(header("CamelFileParent"), new ValueSetAggregationStrategy())
                .completionFromBatchConsumer().marshal().bindy(BindyType.Csv, CodeElement.class).to("file:./output/");
            }
        });
        context.start();
        Thread.sleep(10000);
        context.stop();
    }

}


// CamelFileParent