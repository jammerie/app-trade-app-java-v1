# Sample Trade Application

## Running the Application
### Launching
Prerequisite:
- JDK 17
- Unix/Linux Shell (though possible to work with windows, these steps was tested with MaxOSS)

To start the application run the following
```shell
./gradlew bootRun
```

### Loading data
```shell
cp event-sample/* event/incoming
```

Once copied the console output should show the following output

```text
2024-08-15T15:44:59.022+10:00  INFO 70769 --- [trade] [       Thread-1] com.example.service.EventFolderService   : Complete processing of event0.xml
```


### Querying the data
```shell
curl http://localhost:8080/api/query
```

### Running the H2 SQL shell

Load the following in a browser
```text
http://localhost:8080/h2-console

JDBC URL : jdbc:h2:mem:testdb;IGNORECASE=TRUE
user Name : sa
Password : password
```

## Issue discovered on the Event File
The sample event file provide did not include the XSD, hence while testing it was discovered that the XPATH expression was not able to evaluate the path propertly.

Hence in the event-sample the XSD namespace was dropping
[event0.xml](event-sample%2Fevent0.xml)

e.g.
```xml
<requestConfirmation>
    .....
</requestConfirmation>
```

Amendment: later on while looking closer, realized that the XSD is an open standard and could be easily obtained.  If needed can look into this further.


## Some design considerations

### XPath compilation [EventProcessorAbstract.java](src%2Fmain%2Fjava%2Fcom%2Fexample%2Fservice%2FEventProcessorAbstract.java)

XPath expression compilation can be expensive, hence the XPath once compiled is store in the cache 
```java
    private static XPathExpression getXPathExpression(String xpathExpression) {
        return cachedExpression.computeIfAbsent(xpathExpression, key -> {
            try {
                return xpath.compile(xpathExpression);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
        });
    }
```

### XML parsing [EventFolderService.java](src%2Fmain%2Fjava%2Fcom%2Fexample%2Fservice%2FEventFolderService.java)

Building XML is also expensive, hence just need to build the DocumentBuilderFactory once and it could be re-used 
```java
    private static DocumentBuilderFactory factory;
static {
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
}
```

### Updating the record parsing

Modify the [entity.yaml](src%2Fmain%2Fresources%2Fentity.yaml) file which will describe each columns and the xpath expression for the column.

And then run the generator

```shell
./gradlew GenerateEntity
```

Which will generate the following classes
- [Record.java](src%2Fmain%2Fjava%2Fcom%2Fexample%2Fentity%2FRecord.java)
- [RecordRepository.java](src%2Fmain%2Fjava%2Fcom%2Fexample%2Frepository%2FRecordRepository.java)
- [RecordService.java](src%2Fmain%2Fjava%2Fcom%2Fexample%2Fservice%2FRecordService.java)

It's also possible to generate multiple Record type class

### Retrieving the data
The following service 
[RecordQueryService.java](src%2Fmain%2Fjava%2Fcom%2Fexample%2Fservice%2FRecordQueryService.java)
expose an endpoint which will retrieve the data