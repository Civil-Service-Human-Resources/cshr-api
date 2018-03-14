package uk.gov.cshr.vcm.service;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class RegionFieldBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document document,
            LuceneOptions luceneOptions) {

        if (value != null) {

            //        List<String> keywords = (List<String>) value;
            String[] keywords = value.toString().split(",");

            for (String keyword : keywords) {
                luceneOptions.addFieldToDocument(name, keyword.trim().toLowerCase().replaceAll(" ", "_"), document);
//                luceneOptions.
//document.
            }
//            for (String cat : keywords) {
//                document.add(new StringField("regions", cat.trim(), Field.Store.YES)); // doc is a Document
//            }

        }
    }

//@Override
//    public Object get(String name, Document document) {
//        IndexableField strdate = document.getField(name);
//        return DateTime.parse(strdate.stringValue(), DateTimeFormat.forPattern("yyyyMMdd"));
//    }
//
//    @Override
//    public String objectToString(Object date) {
//        DateTime datetime = (DateTime) date;
//        return datetime.toString(DateTimeFormat.forPattern("yyyyMMdd"));
//    }
}
