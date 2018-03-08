package uk.gov.cshr.vcm.service;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

public class DefaultMaxSalaryBridge implements FieldBridge, TwoWayFieldBridge {

    @Override
    public void set(String field, Object value, Document dcmnt, LuceneOptions lo) {

        if (value == null) {

            lo.addFieldToDocument(
                    field,
                    dcmnt.getField("salaryMin").stringValue(),
                    dcmnt);
        }
        else {
            lo.addFieldToDocument(
                    field,
                    value.toString(),
                    dcmnt);
        }
    }

    @Override
    public Object get(String string, Document dcmnt) {
        return string;
    }

    @Override
    public String objectToString(Object o) {
        return o.toString();
    }

}
