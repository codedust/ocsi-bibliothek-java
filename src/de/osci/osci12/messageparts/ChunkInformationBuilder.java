package de.osci.osci12.messageparts;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


/**
 * Parst die Struktur des ChunkInformation XML
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 * Nachrichtenspezifische Werte fuer die ChunkInformation-Werte:
 * (ChunkSize,ChunkNumber,TotalMessageSize,ReceivedChunks,TotalChunkNumber) PartialStoreDelivery (1,1,1,0,1)
 * ResponseToPartailStoreDelivery(0,1,0,1,1) FetchDelivery(1,1,0,1,0) ResponseToFetchDelivery(0,1,1,0,1)
 *
 * @author R. Lindemann
 * @version 2.4.1
 * @since 1.8.0
 */
public class ChunkInformationBuilder extends MessagePartParser
{

  private static Log log = LogFactory.getLog(ChunkInformationBuilder.class);

  private ChunkInformation chunkInformation = null;

  /**
   * Creates a new ChunkInformationBuilder object.
   *
   * @param xmlReader xmlReader Objekt
   * @param parentHandler Der parent Parser
   * @param checkInstance Die Prüfinformationen für die ChunkInformation
   */
  public ChunkInformationBuilder(XMLReader xmlReader,
                                 DefaultHandler parentHandler,
                                 ChunkInformation.CheckInstance checkInstance)
  {
    super(xmlReader, parentHandler);
    chunkInformation = new ChunkInformation(checkInstance);
    OSCIMessage msg = ((OSCIMessageBuilder)parentHandler).getOSCIMessage();
    chunkInformation.setNSPrefixes(msg);
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
      log.debug("Start-Element: " + localName);

    try
    {
      if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
      {
        if (attributes.getValue("ChunkNumber") != null)
        {
          chunkInformation.setChunkNumber(Integer.parseInt(attributes.getValue("ChunkNumber")));
        }
        if (attributes.getValue("TotalChunkNumbers") != null)
        {
          chunkInformation.setTotalChunkNumbers(Integer.parseInt(attributes.getValue("TotalChunkNumbers")));
        }
        if (attributes.getValue("TotalMessageSize") != null)
        {
          chunkInformation.setTotalMessageSize(Long.parseLong(attributes.getValue("TotalMessageSize")));
        }
        if (attributes.getValue("ChunkSize") != null)
        {
          chunkInformation.setChunkSize(Long.parseLong(attributes.getValue("ChunkSize")));
        }
        if (attributes.getValue("ReceivedChunks") != null)
        {
          chunkInformation.setReceivedChunks(parseReceivedChunks(attributes.getValue("ReceivedChunks")));
        }
      }
      else
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    catch (SAXException e)
    {
      throw e;
    }
    catch (Exception ex)
    {
      log.error("Fehler im Start-Element", ex);
      throw new SAXException(ex);
    }
  }

  /**
   * @param uri Namespace URI
   * @param localName Elementname
   * @param qName Prefix und Elementname
   * @throws SAXException Im Fehlerfall wird eine SaxException geworfen
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    try
    {
      if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
      {
        parentHandler.endElement(uri, localName, qName);
        xmlReader.setContentHandler(parentHandler);
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
      log.error("Fehler im End-Element!", ex);
      throw new SAXException(ex);
    }

    currentElement = null;
  }

  /**
   * @param ch Übergebener Character
   * @param start Startposition
   * @param length Länge des Characters
   * @throws SAXException Im Fehlerfall wird eine SaxException geworfen
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    if (currentElement == null)
    {
      for ( int i = 0 ; i < length ; i++ )
      {
        if (ch[start + i] > ' ')
          throw new SAXException(DialogHandler.text.getString("unexpected_char"));
      }
    }
    else
      currentElement.append(ch, start, length);
  }

  private ArrayList<Integer> parseReceivedChunks(String _receivedChunks)
  {
    if (_receivedChunks == null)
    {
      return null;
    }
    ArrayList<Integer> arrayChunks = new ArrayList<>();
    StringTokenizer strToken = new StringTokenizer(_receivedChunks, " ");
    while (strToken.hasMoreTokens())
    {
      arrayChunks.add(Integer.parseInt(strToken.nextToken()));
    }
    return arrayChunks;
  }

  /**
   * @return Liefert das konstruierte ChunkInformation Objekt.
   */
  public ChunkInformation getChunkInformationObject()
  {
    return chunkInformation;
  }
}
