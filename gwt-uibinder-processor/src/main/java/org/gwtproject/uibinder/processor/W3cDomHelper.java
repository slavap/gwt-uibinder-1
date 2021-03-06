package org.gwtproject.uibinder.processor;

import java.io.IOException;
import java.io.StringReader;
import javax.annotation.processing.ProcessingEnvironment;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 */
public class W3cDomHelper {

  private static final String LOAD_EXTERNAL_DTD =
      "http://apache.org/xml/features/nonvalidating/load-external-dtd";

  private final SAXParserFactory factory;
  private final MortalLogger logger;
  private final ProcessingEnvironment processingEnvironment;
  //private final Pro

  public W3cDomHelper(MortalLogger logger, ProcessingEnvironment processingEnvironment) {
    this.logger = logger;
    this.processingEnvironment = processingEnvironment;
    this.factory = SAXParserFactory.newInstance();
    try {
      factory.setFeature(LOAD_EXTERNAL_DTD, true);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      // ignore since parser doesn't know about this feature
    }
    factory.setNamespaceAware(true);
  }


  public Document documentFor(String string, String resourcePath) throws SAXParseException {
    try {
      if (resourcePath != null) {
        int pos = resourcePath.lastIndexOf('/');
        resourcePath = (pos < 0) ? "" : resourcePath.substring(0, pos + 1);
      }
      W3cDocumentBuilder handler = new W3cDocumentBuilder(logger, resourcePath,
          processingEnvironment);
      SAXParser parser = factory.newSAXParser();
      InputSource input = new InputSource(new StringReader(string));
      input.setSystemId(resourcePath);
      parser.parse(input, handler);
      return handler.getDocument();
    } catch (SAXParseException e) {
      // Let SAXParseExceptions through.
      throw e;
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
