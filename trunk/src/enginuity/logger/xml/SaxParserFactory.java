package enginuity.logger.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public final class SaxParserFactory {

    private SaxParserFactory() {
    }

    public static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(true);
        parserFactory.setXIncludeAware(false);
        return parserFactory.newSAXParser();
    }

    public static void main(String args[]) {
        try {
            SAXParser parser = SaxParserFactory.getSaxParser();
            BufferedInputStream b = new BufferedInputStream(new FileInputStream(new File("/ecu_defs.xml")));
            System.out.println(b.available());
            parser.parse(b, new DefaultHandler());
            System.out.println(parser.isValidating());

        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
