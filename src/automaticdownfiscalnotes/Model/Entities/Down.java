package automaticdownfiscalnotes.Model.Entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;


public class Down {
    private Calendar date;
    private String document; //nf
    private BigDecimal value;

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value.setScale(2, RoundingMode.HALF_UP);       
    }
    
    
}
