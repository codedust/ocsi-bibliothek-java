package de.osci.osci12.messagetypes;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.Base64;
import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.ChunkInformationBuilder;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesHBuilder;


/**
 * ResponseToFetchDelivery-Parser.
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
 */
class ResponseToPartialFetchDeliveryBuilder extends OSCIMessageBuilder
{

  private static Log log = LogFactory.getLog(ResponseToFetchDeliveryBuilder.class);

  private Boolean insideResponseToFetchDelivery = null;

  private Boolean insideFetchDelivery = null;

  ChunkInformationBuilder chunkInformationBuilder = null;

  ResponseToPartialFetchDelivery rspToPartialFetchDel = null;

  /**
   * Konstruktor des ResponseToPartialFetchDeliveryBuilder-Objekts
   *
   * @param parentHandler Parent Parser
   */
  public ResponseToPartialFetchDeliveryBuilder(OSCIEnvelopeBuilder parentHandler)
  {
    super(parentHandler);
    rspToPartialFetchDel = new ResponseToPartialFetchDelivery(parentHandler.dhHandler);
    msg = rspToPartialFetchDel;
  }

  /**
   * @param uri Namespace URI
   * @param localName Elementname
   * @param qName Prefix und Elementname
   * @param attributes List von Attributen
   * @throws SAXException Im Fehlerfall wird eine SaxException geworfen
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start Element RspFwdDel: " + localName);

    if (insideBody)
    {
      if (ParserHelper.isElement(CommonTags.ContentPackage, localName, uri)) {
        setContentPackageHandler(localName);
      }
    }
    else if (insideHeader)
    {
      // ### Auswerten des ControlBlock Headers###
      if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
        parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes,
                                                                           new int[]{1, 1, 1, 1}));
      // ### Auswerten des ClientSignature Headers###
      else if (ParserHelper.isElement(HeaderTags.SupplierSignature, localName, uri))
        parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                 this,
                                                                                                 attributes));
      else if (ParserHelper.isElement(HeaderTags.responseToPartialFetchDelivery, localName, uri))
      {
        addFoundMsgPartIds(attributes.getValue("Id"),
                           HeaderTags.responseToPartialFetchDelivery.getNamespace().getUri() + ":"
                                                      + HeaderTags.responseToPartialFetchDelivery.getElementName());
        if (insideResponseToFetchDelivery != null)
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        insideResponseToFetchDelivery = true;
      }
      else if (insideResponseToFetchDelivery != null && insideResponseToFetchDelivery)
      {
        if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
        {
          chunkInformationBuilder = new ChunkInformationBuilder(parentBuilder.xmlReader, this,
                                                                ChunkInformation.CheckInstance.ResponsePartialFetchDelivery);
          parentBuilder.xmlReader.setContentHandler(chunkInformationBuilder);
          chunkInformationBuilder.startElement(uri, localName, qName, attributes);
        }
        else if (ParserHelper.isElement(CommonTags.Feedback, localName, uri))
        {
          parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
        }
        else if (ParserHelper.isElement(HeaderTags.fetchDelivery, localName, uri))
        {
          if (insideFetchDelivery != null)
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
          insideFetchDelivery = true;
        }
        else if (ParserHelper.isElement(CommonTags.MessageId, localName, uri) && !insideFetchDelivery)
        {
          currentElement = new StringBuffer();
        }
        else if (insideFetchDelivery != null && insideFetchDelivery)
        {
          if (ParserHelper.isElement(CommonTags.MessageId, localName, uri))
          {
            rspToPartialFetchDel.setSelectionMode(OSCIMessage.SELECT_BY_MESSAGE_ID);
            currentElement = new StringBuffer();
          }
          else if (localName.equals("ReceptionOfDelivery") && uri.equals(OSCI_XMLNS))
          {
            rspToPartialFetchDel.setSelectionMode(OSCIMessage.SELECT_BY_DATE_OF_RECEPTION);
            currentElement = new StringBuffer();
          }
          else if (localName.equals("RecentModification") && uri.equals(OSCI_XMLNS))
          {
            rspToPartialFetchDel.setSelectionMode(OSCIMessage.SELECT_BY_RECENT_MODIFICATION);
            currentElement = new StringBuffer();
          }

        }
      }
      else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates, localName, uri))
      {
        int[] check = {0, -1};
        parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes,
                                                                                       check));
      }
      else if (ParserHelper.isElement(HeaderTags.NonIntermediaryCertificates, localName, uri))
      {
        int[] check = {-1, -1, -1, -1, -1, -1, 0};
        parentBuilder.xmlReader.setContentHandler(new NonIntermediaryCertificatesHBuilder(this, attributes,
                                                                                          check));
      }
      else if (ParserHelper.isElement(HeaderTags.FeatureDescription, localName, uri))
      {
        FeatureDescriptionHBuilder featureBuilder = new FeatureDescriptionHBuilder(this, attributes);
        parentBuilder.xmlReader.setContentHandler(featureBuilder);
        featureBuilder.startElement(uri, localName, qName, attributes);
      }
      else if (!(localName.equals("SelectionRule") && uri.equals(OSCI_XMLNS)))
        startCustomSoapHeader(uri, localName, qName, attributes);
    }
    else
      super.startElement(uri, localName, qName, attributes);
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
      log.debug("End-Element: " + localName);
    if (ParserHelper.isElement(CommonTags.MessageId, localName, uri) && insideResponseToFetchDelivery
        && !insideFetchDelivery)
    {
      try
      {
        rspToPartialFetchDel.messageId = new String(Base64.decode(currentElement.toString()),
                                                    Constants.CHAR_ENCODING);
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }
    }
    if (ParserHelper.isElement(CommonTags.MessageId, localName, uri) && insideFetchDelivery)
    {
      try
      {
        rspToPartialFetchDel.setSelectionRule(new String(Base64.decode(currentElement.toString()),
                                                         Constants.CHAR_ENCODING));
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }
    }
    else if ((localName.equals("ReceptionOfDelivery") && uri.equals(OSCI_XMLNS))
             || (localName.equals("RecentModification") && uri.equals(OSCI_XMLNS)) && insideFetchDelivery)
    {
      rspToPartialFetchDel.setSelectionRule(currentElement.toString());
    }
    else if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
    {
      rspToPartialFetchDel.setChunkInformation(chunkInformationBuilder.getChunkInformationObject());
    }
    else if (localName.equals("Header") && uri.equals(SOAP_XMLNS))
    {
      insideHeader = false;
    }
    else if (ParserHelper.isElement(HeaderTags.responseToPartialFetchDelivery, localName, uri))
      insideResponseToFetchDelivery = false;
    else if (ParserHelper.isElement(HeaderTags.fetchDelivery, localName, uri))
      insideFetchDelivery = false;
    else
      super.endElement(uri, localName, qName);

    currentElement = null;
  }
}
