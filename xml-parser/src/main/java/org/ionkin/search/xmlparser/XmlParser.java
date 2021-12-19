package org.ionkin.search.xmlparser;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.ionkin.search.xmlparser.model.WikiPage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class XmlParser {
    public void parseBz2File(File xmlBz2File, Consumer<WikiPage> action) throws IOException, XMLStreamException, JAXBException {
        try (InputStream in = new BZip2CompressorInputStream(new FileInputStream(xmlBz2File), true)) {
            parse(in, action);
        }
    }

    public void parse(InputStream in, Consumer<WikiPage> action) throws XMLStreamException, JAXBException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader = factory.createXMLStreamReader(in, StandardCharsets.UTF_8.name());

        JAXBContext jc = JAXBContext.newInstance(WikiPage.class);

        boolean running = true;
        int next = xmlReader.next();
        while (running) {
            switch (next) {
                case XMLStreamConstants.START_ELEMENT:
                    if (xmlReader.getLocalName().equals("page")) {
                        WikiPage page = readPage(xmlReader, jc);
                        action.accept(page);
                    }
                    next = xmlReader.next();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    next = xmlReader.next();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    xmlReader.close(); // frees, does not close input stream
                    running = false;
                    break;
                default:
                    next = xmlReader.next();
                    break;
            }
        }
    }

    public WikiPage readPage(XMLStreamReader xmlReader, JAXBContext jc) throws JAXBException {
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (WikiPage) unmarshaller.unmarshal(xmlReader);
    }
}
