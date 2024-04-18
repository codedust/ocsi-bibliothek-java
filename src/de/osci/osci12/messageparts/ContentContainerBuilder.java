package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.Canonizer;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.extinterfaces.OSCIDataSourceExt123;
import de.osci.osci12.messagetypes.OSCIMessage;


/**
 * ContentContainer-Parser.
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
 * @author R. Lindemann / N. B\u00FCngener
 * @version 2.4.1
 */
class ContentContainerBuilder extends MessagePartParser
{

  private static Log log = LogFactory.getLog(ContentContainerBuilder.class);

  private boolean readContent = false;

  private boolean underlyingCoco = false;

  private ContentContainerBuilder cocoBuilder = null;

  private de.osci.osci12.extinterfaces.OSCIDataSource swapBuffer = null;

  private Attachment att = null;

  private String contentID = null;

  private ContentContainer coco;

  private de.osci.osci12.encryption.EncryptedDataBuilder encDataBuilder = null;

  private OSCISignatureBuilder sigBuilder;

  private OutputStreamWriter osw;

  // Canonizer, aus dem der Parser liest
  Canonizer can;

  String cocoNS;

  private String soapNSPrefix;

  private String osciNSPrefix;

  private String dsNSPrefix;

  private String xencNSPrefix;

  private String xsiNSPrefix;

  /**
   * Creates a new ContentContainerBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param parentMessage undocumented
   * @param atts undocumented
   * @param can undocumented
   */
  public ContentContainerBuilder(XMLReader xmlReader,
                                 DefaultHandler parentHandler,
                                 OSCIMessage parentMessage,
                                 Attributes atts,
                                 Canonizer can)
  {
    super(xmlReader, parentHandler);
    msg = parentMessage;
    coco = new ContentContainer();
    cocoNS = can.getContainerNS().remove(0);

    try
    {
      coco.ns = cocoNS.getBytes(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {}

    StringTokenizer ns = new StringTokenizer(cocoNS.substring(1), " =");
    String prefix;
    String uri;

    while (ns.hasMoreElements())
    {
      prefix = ns.nextToken();
      prefix = prefix.substring(prefix.indexOf(":") + 1);
      uri = ns.nextToken();
      uri = uri.substring(1, uri.length() - 1);

      if (uri.equals(SOAP_XMLNS))
        soapNSPrefix = prefix;
      else if (uri.equals(OSCI_XMLNS))
        osciNSPrefix = prefix;
      else if (uri.equals(DS_XMLNS))
        dsNSPrefix = prefix;
      else if (uri.equals(XENC_XMLNS))
        xencNSPrefix = prefix;
      else if (uri.equals(XSI_XMLNS))
        xsiNSPrefix = prefix;
    }

    coco.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
    coco.stateOfObject = ContentContainer.STATE_OF_OBJECT_PARSING;

    if (atts.getValue("Id") != null)
      coco.setRefID(atts.getValue("Id"));

    this.can = can;
  }

  /**
   * Gets the contentContainer attribute of the ContentContainerBuilder object
   *
   * @return The contentContainer value
   */
  public ContentContainer getContentContainer()
  {
    return coco;
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   * @throws SAXException undocumented
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("start-Element: " + localName);

    if (localName.equals("Signature") && uri.equals(DS_XMLNS))
    {
      sigBuilder = new OSCISignatureBuilder(this.xmlReader, this, attributes, true);
      sigBuilder.sig.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);

      try
      {
        sigBuilder.sig.ns = cocoNS.getBytes(Constants.CHAR_ENCODING);
      }
      catch (UnsupportedEncodingException ex)
      {}

      this.xmlReader.setContentHandler(sigBuilder);
    }
    else if (localName.equals("Content") && uri.equals(OSCI_XMLNS))
    {
      contentID = attributes.getValue("Id");

      if (attributes.getValue("href") != null)
      {
        String href = attributes.getValue("href").substring(4);

        if (msg.attachments.containsKey(href))
        {
          att = msg.attachments.get(href);

          if (log.isDebugEnabled())
            log.debug("########## Attachment gefunden. ");
        }
        else
        {
          if (log.isDebugEnabled())
            log.debug("Attchment wird der Nachricht hinzugef\u00FCgt.");

          try
          {
            if (msg.isSigned())
              att = new Attachment(null, href, 0, msg.signatureHeader.getDigestMethods().get("cid:" + href));
            else
              att = new Attachment(null, href, 0, null);
          }
          catch (Exception ex1)
          {
            throw new SAXException(ex1);
          }

          msg.attachments.put(att.getRefID(), att);
          att.stateOfAttachment = Attachment.STATE_OF_ATTACHMENT_PARSING;
        }
      }

      // else
      // readContent = true;
    }
    else if (localName.equals("Base64Content") && uri.equals(OSCI_XMLNS))
    {
      contentID = attributes.getValue("Id");
      readContent = true;

      try
      {
        swapBuffer = de.osci.osci12.common.DialogHandler.getNewDataBuffer();

        if (swapBuffer instanceof OSCIDataSourceExt123)
          ((OSCIDataSourceExt123)swapBuffer).setConfidential(true);

        osw = new OutputStreamWriter(swapBuffer.getOutputStream(), Constants.CHAR_ENCODING);
      }
      catch (Exception ex)
      {
        throw new SAXException(ex);
      }
    }
    else if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
    {
      if (encDataBuilder != null)
      {
        if (log.isDebugEnabled())
          log.debug("Encrypted-Data wird hinzugef\u00FCgt.");

        EncryptedDataOSCI enc = new EncryptedDataOSCI(encDataBuilder.getEncryptedData(), msg);
        enc.setNS(cocoNS);
        enc.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
        coco.addEncryptedData(enc);
      }

      encDataBuilder = new de.osci.osci12.encryption.EncryptedDataBuilder(this.xmlReader, this, attributes);
      this.xmlReader.setContentHandler(encDataBuilder);
    }
    else if (localName.equals("ContentContainer") && uri.equals(OSCI_XMLNS))
    {
      readContent = false;
      underlyingCoco = true;
      cocoBuilder = new ContentContainerBuilder(this.xmlReader, this, msg, attributes, can);
      this.xmlReader.setContentHandler(cocoBuilder);
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   * undocumented
   *
   * @param ch undocumented
   * @param start undocumented
   * @param length undocumented
   * @throws SAXException undocumented
   */
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("readContent: " + readContent + " " + new String(ch, start, length));

    if (readContent)
    {
      try
      {
        osw.write(ch, start, length);
      }
      catch (Exception ex)
      {
        throw new SAXException(ex);
      }
    }
    else
    {
      for ( int i = 0 ; i < length ; i++ )
      {
        if (ch[start + i] > ' ')
          throw new SAXException(DialogHandler.text.getString("unexpected_char"));
      }
    }
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @throws SAXException undocumented
   */
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    try
    {
      // Ein Content Element wird beendet und dem coco zugef\u00FCgt
      if (localName.equals("Content") && uri.equals(OSCI_XMLNS))
      {
        if (att != null)
        {
          if (log.isDebugEnabled())
            log.debug("######### attachment:");

          Content co = new Content(att);

          // für spätere Serialisierung
          co.transformers.set(0,
                              "<" + dsNSPrefix + ":Transform Algorithm=\""
                                 + Constants.TRANSFORM_CANONICALIZATION + "\"></" + dsNSPrefix
                                 + ":Transform>");
          co.coNS = cocoNS;
          co.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
          co.setRefID(contentID);

          if (log.isDebugEnabled())
            log.debug("RefID: " + contentID);

          coco.addContentInternal(co, false);
          att = null;
        }

        // else if (readContent)
        // {
        // }
      }
      else if (localName.equals("Base64Content") && uri.equals(OSCI_XMLNS))
      {
        if (log.isDebugEnabled())
          log.debug("#### read Content");

        osw.close();

        Content co = new Content(swapBuffer);
        co.coNS = cocoNS;
        co.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
        co.setRefID(contentID);
        coco.addContent(co);
        readContent = false;
        swapBuffer = null;
      }
      else if (localName.equals("ContentContainer") && uri.equals(OSCI_XMLNS))
      {
        if (underlyingCoco)
        {
          ContentContainer newCoco = cocoBuilder.getContentContainer();
          Content tmpCnt = new Content(newCoco);
          tmpCnt.transformers.set(0,
                                  "<" + cocoBuilder.dsNSPrefix + ":Transform Algorithm=\""
                                     + Constants.TRANSFORM_CANONICALIZATION + "\"></" + cocoBuilder.dsNSPrefix
                                     + ":Transform>");
          tmpCnt.coNS = cocoBuilder.cocoNS;
          tmpCnt.setRefID(contentID);
          tmpCnt.setNSPrefixes(cocoBuilder.soapNSPrefix,
                               cocoBuilder.osciNSPrefix,
                               cocoBuilder.dsNSPrefix,
                               cocoBuilder.xencNSPrefix,
                               cocoBuilder.xsiNSPrefix);
          this.coco.addContent(tmpCnt);
          underlyingCoco = false;
        }
        else
        {
          // gibt es noch weitere encryptedData Elemente die dem coco noch nicht hinzugef\u00FCgt wurden ?
          if (encDataBuilder != null)
          {
            if (log.isDebugEnabled())
              log.debug("Encrypted-Data wird hinzugef\u00FCgt.Parent Message: ");

            EncryptedDataOSCI enc = new EncryptedDataOSCI(encDataBuilder.getEncryptedData(), msg);
            enc.setNS(cocoNS);
            enc.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
            coco.addEncryptedData(enc);
            encDataBuilder = null;
          }

          if (coco.signerList.size() > 0)
          {
            Map<String, OSCISignatureReference> refs = coco.signerList.get(0).getReferences();
            Content[] cnt = coco.getContents();
            String refKey;

            for ( int i = 0 ; i < cnt.length ; i++ )
            {
                refKey = "#" + cnt[i].getRefID();
              
                if(refs.containsKey(refKey))
                {
                  cnt[i].transformers = refs.get(refKey).transformerAlgorithms;
                }
                else
                {
                  // keine Signatur für diesen Content im signierten ContentContainer gefunden!                
                  throw new SAXException(DialogHandler.text.getString("signature_violation") + ": " + refKey);
                }
            }
          }
          // zur\u00FCck zum Parent
          this.xmlReader.setContentHandler(this.parentHandler);

          if (log.isDebugEnabled())
            log.debug("parentHandler: " + parentHandler);

          parentHandler.endElement(uri, localName, qName);
        }
      }
      else if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
      {
        if (encDataBuilder != null)
        {
          EncryptedDataOSCI enc = new EncryptedDataOSCI(encDataBuilder.getEncryptedData(), msg);
          enc.setNS(cocoNS);
          enc.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
          coco.addEncryptedData(enc);
        }

        encDataBuilder = null;
      }
      else
      {
        if (log.isDebugEnabled())
          log.debug("Name des Elementes: " + localName);

        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
    }
    catch (IOException ex)
    {
      log.error("Fehler beim End-Element.", ex);
      throw new SAXException(ex);
    }
  }
}
