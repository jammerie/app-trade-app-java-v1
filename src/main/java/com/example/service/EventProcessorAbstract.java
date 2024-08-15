package com.example.service;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.HashMap;
import java.util.Map;

public abstract class EventProcessorAbstract {
    private static XPathFactory xpathfactory;
    private static XPath xpath;
    private static Map<String, XPathExpression> cachedExpression = new HashMap();
    static {
        xpathfactory = XPathFactory.newInstance();
        xpath = xpathfactory.newXPath();
    }

    private static XPathExpression getXPathExpression(String xpathExpression) {
        return cachedExpression.computeIfAbsent(xpathExpression, key -> {
            try {
                return xpath.compile(xpathExpression);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    String getNodeValue(Document doc, String xpathExpression) throws XPathExpressionException {
        Object result = getXPathExpression(xpathExpression).evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        if (nodes.getLength() > 0) {
            return nodes.item(0).getNodeValue();
        }
        return "";
    }

    abstract void processDoc(Document doc) throws XPathExpressionException;
}
