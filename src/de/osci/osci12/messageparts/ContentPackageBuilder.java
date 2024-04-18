package de.osci.osci12.messageparts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.osci.helper.Canonizer;
import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.EncryptedDataBuilder;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


/**
 * ContentPackage-Parser.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann / N. Büngener
 * @version 2.4.1
 */
class ContentPackageBuilder extends MessagePartParser
{
  private static Log log = LogFactory.getLog(ContentPackageBuilder.class);
  private Object lastObject = null;
  private de.osci.osci12.encryption.EncryptedDataBuilder encDataBuilder = null;
  private ContentContainerBuilder cocoBuilder = null;

  // Canonizer, aus dem der Parser liest
  private Canonizer can;

  //  protected String soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix;
  /**
   * Creates a new ContentPackageBuilder object.
   *
   * @param reader undocumented
   * @param msg undocumented
   * @param can undocumented
   */
  public ContentPackageBuilder(XMLReader reader, OSCIMessage msg, Canonizer can)
  {
    super(reader, null);
    this.msg = msg;
    this.can = can;
  }

  /**
   * Creates a new ContentPackageBuilder object.
   *
   * @param parentBuilder undocumented
   */
  public ContentPackageBuilder(OSCIMessageBuilder parentBuilder)
  {
    super(parentBuilder);
    can = parentBuilder.getCanStream();
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   *
   * @throws SAXException undocumented
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start-Element: " + localName);

    if (ParserHelper.isElement(CommonTags.ContentPackage, localName, uri))
      ; //nothing to do
    else if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
    {
      try
      {
        encDataBuilder = new EncryptedDataBuilder(this.xmlReader, this, attributes);
        this.xmlReader.setContentHandler(encDataBuilder);
      }
      catch (Exception ex)
      {
        throw new SAXException(ex);
      }
    }
    else if (localName.equals("ContentContainer") && uri.equals(OSCI_XMLNS))
    {
      cocoBuilder = new ContentContainerBuilder(this.xmlReader, this, msg, attributes, can);
      this.xmlReader.setContentHandler(cocoBuilder);
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   *
   * @throws SAXException undocumented
   */
  public void endElement(String uri, String localName, String qName)
                  throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    try
    {
      if (ParserHelper.isElement(CommonTags.ContentPackage, localName, uri))
      {
        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("ContentContainer") && uri.equals(OSCI_XMLNS))
      {
        lastObject = cocoBuilder.getContentContainer();

        // nicht aus EncryptedDataOSCI.decrypt() aufgerufen
        if (parentHandler != null)
          msg.contentContainer.put(((ContentContainer) lastObject).getRefID(), (ContentContainer) lastObject);
      }
      else if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
      {
        if (log.isDebugEnabled())
          log.debug("Encrypted-Data wird hinzugefügt.");

        lastObject = new EncryptedDataOSCI(encDataBuilder.getEncryptedData(), msg);
        ((EncryptedDataOSCI) lastObject).setRefID(encDataBuilder.getEncryptedData().getId());

        // nicht aus EncryptedDataOSCI.decrypt() aufgerufen
        if (parentHandler != null)
          msg.encryptedData.put(((EncryptedDataOSCI) lastObject).getRefID(), (EncryptedDataOSCI) lastObject);

        ((EncryptedDataOSCI) lastObject).setNSPrefixes(msg);
        ((EncryptedDataOSCI) lastObject).setNS(msg.ns);
      }
      else
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      log.error("Fehler beim End Element. ", ex);
      //      ex.printStackTrace();
      throw new SAXException(ex);
    }
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Object getLastCreatedObject()
  {
    return lastObject;
  }
}
