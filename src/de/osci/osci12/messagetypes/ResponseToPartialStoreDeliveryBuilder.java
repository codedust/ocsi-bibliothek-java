package de.osci.osci12.messagetypes;


import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.messageparts.ChunkInformation.CheckInstance;
import de.osci.osci12.messageparts.ChunkInformationBuilder;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundleBuilder;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;


/**
 * ResponseToPartialStoreDelivery-Parser.
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
class ResponseToPartialStoreDeliveryBuilder extends OSCIMessageBuilder
{

  private static Log log = LogFactory.getLog(ResponseToPartialStoreDeliveryBuilder.class);

  /**
   * Objekt ProcessCardBundle für ProcessCard Information
   */
  private boolean insideRspToPartialStoreDelivery = false;

  ResponseToPartialStoreDelivery rspToPartialStoreDelivery = null;

  ChunkInformationBuilder chunkInformationBuilder = null;

  FeedbackBuilder feedbackBuilder = null;

  private ProcessCardBundleBuilder processCardBuilder = null;

  /**
   * Konstruktor für das ResponseToPartialStoreDeliveryBuilder-Objekt
   *
   * @param parentHandler Parent Parser Objekt
   */
  public ResponseToPartialStoreDeliveryBuilder(OSCIEnvelopeBuilder parentHandler)
    throws NoSuchAlgorithmException
  {
    super(parentHandler);
    rspToPartialStoreDelivery = new ResponseToPartialStoreDelivery(parentHandler.dhHandler, true);
    msg = rspToPartialStoreDelivery;
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
      log.debug("Start Element RspStrDel: " + localName);

    if (insideHeader)
    {
      // ### Auswerten des ControlBlock Headers###
      if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
      {
        parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes,
                                                                           new int[]{1, -1, 1, 1}));
      }

      // ### Auswerten des ClientSignature Headers###
      else if (ParserHelper.isElement(HeaderTags.SupplierSignature, localName, uri))
      {
        parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                 this,
                                                                                                 attributes));
      }
      else if (ParserHelper.isElement(HeaderTags.responseToPartialStoreDelivery, localName, uri))
      {
        insideRspToPartialStoreDelivery = true;
      }
      else if (ParserHelper.isElement(CommonTags.ProcessCardBundle, localName, uri))
      {
        int[] check = {1, -1, -1, -1};
        processCardBuilder = new ProcessCardBundleBuilder(CommonTags.ProcessCardBundle,
                                                          parentBuilder.xmlReader, this, check);
        parentBuilder.xmlReader.setContentHandler(processCardBuilder);
      }
      else if (ParserHelper.isElement(CommonTags.Feedback, localName, uri))
      {
        parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
      }
      else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates, localName, uri))
      {
        int[] check = {0, -1};
        parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes,
                                                                                       check));
      }
      else if (ParserHelper.isElement(HeaderTags.FeatureDescription, localName, uri))
      {
        FeatureDescriptionHBuilder featureBuilder = new FeatureDescriptionHBuilder(this, attributes);
        parentBuilder.xmlReader.setContentHandler(featureBuilder);
        featureBuilder.startElement(uri, localName, qName, attributes);
      }
      else if (insideRspToPartialStoreDelivery)
      {

        if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
        {
          chunkInformationBuilder = new ChunkInformationBuilder(parentBuilder.xmlReader, this,
                                                                CheckInstance.ResponsePartialStoreDelivery);
          parentBuilder.xmlReader.setContentHandler(chunkInformationBuilder);
          chunkInformationBuilder.startElement(uri, localName, qName, attributes);
        }
        else if (ParserHelper.isElement(CommonTags.InsideFeedback, localName, uri))
        {
          feedbackBuilder = new FeedbackBuilder(this, true);
          parentBuilder.xmlReader.setContentHandler(feedbackBuilder);

        }
      }
      else
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
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    try
    {
      if (log.isDebugEnabled())
        log.debug("End-Element: " + localName);

      if ("Header".equals(localName))
      {
        if (processCardBuilder != null)
          ((ResponseToPartialStoreDelivery)msg).processCardBundle = processCardBuilder.getProcessCardBundleObject();

        insideHeader = false;
      }
      else if (ParserHelper.isElement(HeaderTags.responseToPartialStoreDelivery, localName, uri))
        insideRspToPartialStoreDelivery = false;
      else if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
      {
        rspToPartialStoreDelivery.setChunkInformation(chunkInformationBuilder.getChunkInformationObject());
      }
      else if (ParserHelper.isElement(CommonTags.InsideFeedback, localName, uri))
      {
        rspToPartialStoreDelivery.insideFeedBack = feedbackBuilder.getFeedback();
      }
      else
        super.endElement(uri, localName, qName);
    }
    catch (Exception ex)
    {
      log.error("Fehler End-Element!", ex);
      throw new SAXException(ex);
    }
    currentElement = null;
  }
}
