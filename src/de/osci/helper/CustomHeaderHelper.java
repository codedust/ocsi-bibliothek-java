package de.osci.helper;

import de.osci.osci12.soapheader.CustomHeader;
import eu.osci.ws._2014._10.transport.MessageMetaData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;


/**
 * Helfer-Klasse für den Umgang mit {@link CustomHeader}-Elementen, insbesondere mit MessageMetaData-Objekten.
 */
public class CustomHeaderHelper
{

  private static Log log = LogFactory.getLog(CustomHeaderHelper.class);

  private static JAXBContext jaxbContext;

  private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();

  public static final String MESSAGE_META_DATA_ID = "MetaData";

  private static final String OSCI_NAMESPACE =
    " xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:osci=\"http://www.osci.de/2002/04/osci\" "
    + "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xenc=\"http://www.w3"
    + ".org/2001/04/xmlenc#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";

  static
  {
    try
    {
      jaxbContext = JAXBContext.newInstance(eu.osci.ws._2014._10.transport.ObjectFactory.class);

      // XML Factory gegen XXE absichern
      XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      XML_INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }
    catch (final JAXBException e)
    {
      log.error("Cannot create JAXB Context for MessageMetaData, handling MessageMetaData will be impossible",
                e);
    }
  }

  private CustomHeaderHelper()
  {
  }

  /**
   * Füge ein Id-Attribut zum gegebenen XML-String hinzu (so wie es für CustomHeader-Elemente gefordert ist)
   * und gib den veränderten String zurück.
   *
   * @param customHeaderXml XMl-Struktur
   * @param id              gewünschte ID
   * @return
   */
  public static String addIdToCustomHeader(String customHeaderXml, String id)
  {
    customHeaderXml = customHeaderXml.substring(customHeaderXml.indexOf(">") + 1);

    final int start = customHeaderXml.indexOf(">");

    // put ID attribute in element
    return customHeaderXml.substring(0, start)
                          .concat(" Id=\"" + id + "\"")
                          .concat(customHeaderXml.substring(start));
  }

  /**
   * Erzeuge einen OSCI-Signatur-kompatiblen {@link CustomHeader} in kanonisierter Form mit
   * OSCI-Namespace-Deklarationen.
   *
   * @param customHeaderXml
   * @return
   */
  public static String makeOSCICustomHeader(String customHeaderXml)
  {
    final int startAfterTag = customHeaderXml.indexOf(">");

    // put namespace declarations into CustomHeader element
    String osciCustomHeaderXml = customHeaderXml.substring(0, startAfterTag)
                                                .concat(OSCI_NAMESPACE)
                                                .concat(customHeaderXml.substring(startAfterTag));

    // canonize String for OSCI-Message
    Canonizer can = null;
    try
    {
      can = new Canonizer(new ByteArrayInputStream(osciCustomHeaderXml.getBytes(StandardCharsets.UTF_8)),
                          null);
      return new String(Tools.readBytes(can), StandardCharsets.UTF_8);
    }
    catch (IOException | SAXException | ParserConfigurationException | NoSuchAlgorithmException e)
    {
      throw new IllegalArgumentException("Error when canonizing custom header: " + e.getMessage());
    }
  }

  /**
   * Erzeuge einen OSCI-Signatur-kompatiblen {@link CustomHeader} in kanonisierter Form mit
   * OSCI-Namespace-Deklarationen und einer vorgegebenen ID.
   *
   * @param customHeaderXml
   * @param id
   * @return
   */
  public static String makeOSCICustomHeaderWithId(String customHeaderXml, String id)
  {
    return makeOSCICustomHeader(addIdToCustomHeader(customHeaderXml, id));
  }


  /**
   * Erzeuge eine Zeichenkette aus einem {@link MessageMetaData}-Objekt, die in eine OSCI-Nachricht als
   * {@link CustomHeader} eingefügt werden kann.
   *
   * @param mmd
   * @return
   */
  public static String getMessageMetaDataAsCustomHeader(final MessageMetaData mmd)
  {
    String mmdOsciString = "";

    String mmdStringWithId = addIdToCustomHeader(getMessageMetaDataAsString(mmd), MESSAGE_META_DATA_ID);

    mmdOsciString = makeOSCICustomHeader(mmdStringWithId);

    if (log.isDebugEnabled())
    {
      log.debug("get MessageMetaData: " + mmdOsciString);
    }
    return mmdOsciString;
  }


  /**
   * Erzeuge eine Zeichenkette aus einem {@link MessageMetaData}-String, die in eine OSCI-Nachricht als
   * {@link CustomHeader} eingefügt werden kann.
   *
   * @param mmd
   * @return
   */
  public static String getMessageMetaDataStringAsCustomHeader(final String mmd)
  {
    String mmdOsciString = "";

    String mmdStringWithId = addIdToCustomHeader(mmd, MESSAGE_META_DATA_ID);

    mmdOsciString = makeOSCICustomHeader(mmdStringWithId);

    if (log.isDebugEnabled())
    {
      log.debug("get MessageMetaData: " + mmdOsciString);
    }
    return mmdOsciString;
  }

  /**
   * Erzeuge eine Zeichenkette aus einem unveränderten {@link MessageMetaData}-Objekt.
   *
   * @param mmd
   * @return
   */
  public static String getMessageMetaDataAsString(final MessageMetaData mmd)
  {
    String mmdString = "";

    try
    {
      final ByteArrayOutputStream streamMMD = new ByteArrayOutputStream();
      jaxbContext.createMarshaller().marshal(mmd, streamMMD);

      mmdString = new String(streamMMD.toByteArray(), StandardCharsets.UTF_8);
    }
    catch (final Exception ex)
    {
      log.warn("Could not add messageMetaData!", ex);
    }
    return mmdString;
  }


  /**
   * Erzeuge ein {@link MessageMetaData}-Objekt aus einem CustomHeader-String
   *
   * @param customHeaderString
   * @return
   */
  public static MessageMetaData readMessageMetaDataFromOsciString(final String customHeaderString)
  {
    try (final Reader in = new StringReader(customHeaderString))
    {
      return (MessageMetaData)jaxbContext.createUnmarshaller()
                                         .unmarshal(XML_INPUT_FACTORY.createXMLStreamReader(in));
    }
    catch (final Exception ex)
    {
      log.warn("Could not parse messageMetaData", ex);
    }
    return null;
  }
}
