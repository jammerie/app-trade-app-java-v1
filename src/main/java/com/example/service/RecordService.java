package com.example.service;

import com.example.entity.Record;
import com.example.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;

@Service
public class RecordService extends EventProcessorAbstract {
    @Autowired
    private RecordRepository repository;

    @Override
    void processDoc(Document doc) throws XPathExpressionException {
        Record record = new Record();

        
        record.setBuyerParty(
                getNodeValue(doc, "//buyerPartyReference/@href")        
        );
        
        record.setSellerParty(
                getNodeValue(doc, "//sellerPartyReference/@href")        
        );
        
        record.setAmount(
                getNodeValue(doc, "//paymentAmount/amount/text()")        
        );
        
        record.setCurrency(
                getNodeValue(doc, "//paymentAmount/currency/text()")        
        );
        
        
        repository.save(record);
    }
}