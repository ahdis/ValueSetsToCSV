package ch.ahdis.camel.aggregator;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ";", crlf = "UNIX", generateHeaderColumns = true)
public class CodeElement {
    
    @DataField(pos = 1)
    public String code;
    
    @DataField(pos = 2)
    public String codeSystem;
    
    @DataField(pos = 3)
    public String display;

    @DataField(pos = 4)
    public String de_CH;
    
    @DataField(pos = 5)
    public String fr_CH;

    @DataField(pos = 6)
    public String it_CH;
    
    @DataField(pos = 7)
    public String valueSet;
    
    @DataField(pos = 8)
    public String valueSetTitle;
    
    @DataField(pos = 9)
    public String valueSetOid;
    
    @DataField(pos = 10)
    public String warning;
    
    
    public void addWarning(String txt) {
        if (warning==null) {
            warning = txt;
        } else {
            warning = warning +", "+txt;
        }
    }

}
