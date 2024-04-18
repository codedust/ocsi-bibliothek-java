package de.osci.osci12.soapheader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.Namespaces;
import de.osci.osci12.common.Constants.OSCIFeatures;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.MessagePartParser;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


/**
 * <p>
 * Parser für die FeatureDescription
 * </p>
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann
 * @version 2.4.1
 * @since 1.8.0
 */
public class FeatureDescriptionHBuilder extends MessagePartParser
{

  private static Log log = LogFactory.getLog(FeatureDescriptionHBuilder.class);

  private FeatureDescriptionH featureDesc = null;

  private boolean insideSupportedFeatures = false;

  /**
   * Konstruktor für das FeatureDescriptionH-Objekt
   *
   * @param parent Der Parent Parser
   * @param atts Attribute des FeatureDescrption Objektes
   * @throws SAXException Im Fehlerfall wird eine SaxException geworfen
   */
  public FeatureDescriptionHBuilder(OSCIMessageBuilder parent, Attributes atts) throws SAXException
  {
    super(parent);
    parent.addFoundMsgPartIds(atts.getValue("Id"),
                              HeaderTags.FeatureDescription.getNamespace().getUri() + ":"
                                                   + HeaderTags.FeatureDescription.getElementName());
    featureDesc = new FeatureDescriptionH();
    featureDesc.setNSPrefixes(msg);
    if (atts.getValue("Id") != null)
    {
      featureDesc.setRefID(atts.getValue("Id"));
    }
  }

  /**
   * @return Liefert die konstruierte FeatreDescription
   */
  public FeatureDescriptionH getFeatureDescriptionH()
  {
    return featureDesc;
  }

  /**
   * @param uri Namespace URI
   * @param localName Elementname
   * @param qName Prefix und Elementname
   * @param attributes List von Attributen
   * @throws SAXException Im Fehlerfall wird eine SaxException geworfen
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("FeatureDescription Element: " + localName);

    if (ParserHelper.isElement(HeaderTags.FeatureDescription, localName, uri))
    {
      if (attributes.getValue("MaxChunkSize") != null)
      {
        featureDesc.setMaxChunkSize(Long.valueOf(attributes.getValue("MaxChunkSize")));
      }
      if (attributes.getValue("MaxMessageSize") != null)
      {
        featureDesc.setMaxMessageSize(Long.valueOf(attributes.getValue("MaxMessageSize")));
      }
      if (attributes.getValue("MinChunkSize") != null)
      {
        featureDesc.setMinChunkSize(Long.valueOf(attributes.getValue("MinChunkSize")));
      }
      if (attributes.getValue("ChunkMessageTimeout") != null)
      {
        featureDesc.setChunkMessageTimeout(Long.valueOf(attributes.getValue("ChunkMessageTimeout")));
      }
    }
    else if ("SupportedFeatures".equals(localName) && uri.equals(Namespaces.OSCI2017.getUri()))
    {
      insideSupportedFeatures = true;
    }
    else if (insideSupportedFeatures && "Feature".equals(localName)
             && uri.equals(Namespaces.OSCI2017.getUri()))
    {
      if (attributes.getValue("Key") != null)
      {
        try
        {

          OSCIFeatures feature = OSCIFeatures.valueOf(attributes.getValue("Key"));
          featureDesc.getSupportedFeatures().add(feature);
        }
        catch (IllegalArgumentException | NullPointerException ex)
        {
          log.info("Could not parse key from feature description! Unknown feature (maybe new?): "
                    + attributes.getValue("Key"));
        }
      }

    }
    else
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }

  }

  /**
   * @param uri Namespace URI
   * @param localName Elementname
   * @param qName Prefix und Elementname
   * @throws SAXException Im Fehlerfall wird eine SaxException geworfen
   */
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element Von FeatureDescription: " + localName);


    if ("SupportedFeatures".equals(localName) && uri.equals(Namespaces.OSCI2017.getUri()))
    {
      insideSupportedFeatures = false;
    }
    else if ("Feature".equals(localName) && uri.equals(Namespaces.OSCI2017.getUri()))
    {
      // nothing to do
    }
    else if (ParserHelper.isElement(HeaderTags.FeatureDescription, localName, uri))
    {
      xmlReader.setContentHandler(this.parentHandler);
      msg.setFeatureDescription(featureDesc);
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);


  }
}
