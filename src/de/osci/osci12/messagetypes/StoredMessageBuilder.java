package de.osci.osci12.messagetypes;

import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.Namespaces;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.ProcessCardBundleBuilder;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.DesiredLanguagesH;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesHBuilder;
import de.osci.osci12.soapheader.QualityOfTimestampHBuilder;


/**
 * StoredMessag-Parser.
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
 * @author R. Lindemann / N. Büngener
 * @version 2.4.1
 */
class StoredMessageBuilder extends OSCIMessageBuilder
{

  private static Log log = LogFactory.getLog(StoredMessageBuilder.class);

  /**
   * Objekt ProcessCardBundle für ProcessCard Information
   */
  private ProcessCardBundleBuilder processCardBuilder = null;

  private Vector<ProcessCardBundle> processCardBundles = new Vector<ProcessCardBundle>();

  private ProcessCardBundleBuilder replyProcessCardBuilder = null;

  FeedbackBuilder feedbackBuilder = null;

  /**
   * Constructor for the ResponseToForwardDeliveryBuilder object
   *
   * @param parentHandler Description of Parameter
   * @param msgType Description of Parameter
   */
  public StoredMessageBuilder(OSCIEnvelopeBuilder parentHandler, int msgType)
  {
    super(parentHandler);
    msg = new de.osci.osci12.messagetypes.StoredMessage(msgType);
  }

  /**
   * Description of the Method
   *
   * @param uri Description of Parameter
   * @param localName Description of Parameter
   * @param qName Description of Parameter
   * @param attributes Description of Parameter
   * @exception SAXException Description of Exception
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if (log.isDebugEnabled())
    {
      log.debug("Start-Element: " + localName + ":" + uri);
    }

    if (ParserHelper.isElement(CommonTags.MessageId, localName, uri)
        || (localName.equals("ReceptionOfDelivery") && uri.equals(OSCI_XMLNS)))
    {
      currentElement = new StringBuffer();
    }
    else if (insideHeader && localName.equals("MessageIdResponse") && uri.equals(OSCI_XMLNS)
             && (msg.getMessageType() == OSCIMessage.PROCESS_DELIVERY))
    {
      currentElement = new StringBuffer();
    }
    else if (localName.equals("SelectionRule") && uri.equals(OSCI_XMLNS))
    {}
    else if (localName.equals("Quantity") && uri.equals(OSCI_XMLNS))
    {
      ((StoredMessage)msg).quantityLimit = Long.parseLong(attributes.getValue("Limit"));
    }
    else if (ParserHelper.isElement(CommonTags.Feedback, localName, uri))
    {
      parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
    }
    else if (ParserHelper.isElement(CommonTags.InsideFeedback, localName, uri))
    {
      feedbackBuilder = new FeedbackBuilder(this, true);
      parentBuilder.xmlReader.setContentHandler(feedbackBuilder);
    }
    else if (ParserHelper.isElement(CommonTags.ProcessCardBundle, localName, uri))
    {
      if (processCardBuilder != null)
        processCardBundles.add(processCardBuilder.getProcessCardBundleObject());

      int[] check = {-1, -1, -1, -1};
      processCardBuilder = new ProcessCardBundleBuilder(CommonTags.ProcessCardBundle, parentBuilder.xmlReader,
                                                        this, check);
      parentBuilder.xmlReader.setContentHandler(processCardBuilder);
    }
    else if (insideBody)
    {
      if (ParserHelper.isElement(CommonTags.ContentPackage, localName, uri)) {
        setContentPackageHandler(localName);
      }
    }
    else if (insideHeader)
    {
      // ### Auswerten des ControlBlock Headers###
      if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
      {
        parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes,
                                                                           new int[]{-1, -1, -1, -1}));
      }
      // ### Auswerten des ClientSignature Headers###
      else if ((ParserHelper.isElement(HeaderTags.SupplierSignature, localName, uri))
               || (ParserHelper.isElement(HeaderTags.ClientSignature, localName, uri)))
        parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                 this,
                                                                                                 attributes));
      else if (ParserHelper.isElement(HeaderTags.DesiredLanguages, localName, uri))
      {
        msg.desiredLanguagesH = new DesiredLanguagesH(this, attributes.getValue("Id"),
                                                      attributes.getValue("LanguagesList"));
        msg.desiredLanguagesH.setRefID(attributes.getValue("Id"));
        msg.desiredLanguagesH.setNSPrefixes(msg);
        msg.dialogHandler.setLanguageList(attributes.getValue("LanguagesList"));
      }
      else if (ParserHelper.isElement(CommonTags.ChunkInformation, localName, uri))
      {
      }

      else if (ParserHelper.isElement(HeaderTags.QualityOfTimestamp, localName, uri))
        parentBuilder.xmlReader.setContentHandler(new QualityOfTimestampHBuilder(this, attributes));
      //
      else if (ParserHelper.isElement(CommonTags.RequestProcessCardBundle, localName, uri))
      {
        int[] check = {-1, -1, -1, -1};
        processCardBuilder = new ProcessCardBundleBuilder(CommonTags.RequestProcessCardBundle,
                                                          parentBuilder.xmlReader, this, check);
        parentBuilder.xmlReader.setContentHandler(processCardBuilder);
      }
      else if (ParserHelper.isElement(CommonTags.ReplyProcessCardBundle, localName, uri))
      {
        int[] check = {-1, -1, -1, -1};
        replyProcessCardBuilder = new ProcessCardBundleBuilder(CommonTags.ReplyProcessCardBundle,
                                                               parentBuilder.xmlReader, this, check);
        parentBuilder.xmlReader.setContentHandler(replyProcessCardBuilder);
      }
      else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates, localName, uri))
      {
        int[] check = {-1, -1};
        parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes,
                                                                                       check));
      }
      else if (ParserHelper.isElement(HeaderTags.NonIntermediaryCertificates, localName, uri))
      {
        int[] check = {-1, -1, -1, -1, -1, -1, -1};
        parentBuilder.xmlReader.setContentHandler(new NonIntermediaryCertificatesHBuilder(this, attributes,
                                                                                          check));
      }
      else if (localName.equals("Subject") && uri.equals(OSCI_XMLNS))
      {
        currentElement = new StringBuffer();
      } else if (ParserHelper.isElement(HeaderTags.FeatureDescription, localName, uri))
      {
        FeatureDescriptionHBuilder featureBuilder = new FeatureDescriptionHBuilder(this, attributes);
        parentBuilder.xmlReader.setContentHandler(featureBuilder);
        featureBuilder.startElement(uri, localName, qName, attributes);
      }
      else if (localName.equals("ContentReceiver") && uri.equals(OSCI_XMLNS))
      {
        try
        {
          ((StoredMessage)msg).uriReceiver = new java.net.URI(attributes.getValue("URI"));
        }
        catch (URISyntaxException ex)
        {
          throw new SAXException(ex);
        }
      }
      else if (localName.equals(HeaderTags.QualityOfTimestamp.getElementName()) && uri.equals(OSCI_XMLNS))
        parentBuilder.xmlReader.setContentHandler(new QualityOfTimestampHBuilder(this, attributes));
      else if ((localName.equals(HeaderTags.mediateDelivery.getElementName())
                || localName.equals(HeaderTags.responseToMediateDelivery.getElementName())
                || localName.equals(HeaderTags.storeDelivery.getElementName())
                || localName.equals(HeaderTags.responseToStoreDelivery.getElementName())
                || localName.equals(HeaderTags.fetchDelivery.getElementName())
                || localName.equals(HeaderTags.responseToFetchDelivery.getElementName())
                || localName.equals(HeaderTags.fetchProcessCard.getElementName())
                || localName.equals(HeaderTags.responseToFetchProcessCard.getElementName())
                || localName.equals(HeaderTags.forwardDelivery.getElementName())
                || localName.equals(HeaderTags.responseToForwardDelivery.getElementName())
                || localName.equals(HeaderTags.acceptDelivery.getElementName())
                || localName.equals(HeaderTags.responseToAcceptDelivery.getElementName())
                || localName.equals(HeaderTags.partialStoreDelivery.getElementName())
                || localName.equals(HeaderTags.responseToPartialStoreDelivery.getElementName())
                || localName.equals(HeaderTags.processDelivery.getElementName())
                || localName.equals(HeaderTags.responseToProcessDelivery.getElementName()))
               && uri.equals(OSCI_XMLNS))
      {}
      else if ((localName.equals(HeaderTags.partialStoreDelivery.getElementName())
               || localName.equals(HeaderTags.responseToPartialStoreDelivery.getElementName()))
                  && uri.equals(Namespaces.OSCI2017.getUri()))
      {}
      else
        startCustomSoapHeader(uri, localName, qName, attributes);
    }
    else
      super.startElement(uri, localName, qName, attributes);
  }

  /**
   * Description of the Method
   *
   * @param uri Description of Parameter
   * @param localName Description of Parameter
   * @param qName Description of Parameter
   * @exception SAXException Description of Exception
   */
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    if (localName.equals("Envelope") && uri.equals(SOAP_XMLNS))
    {
      StoredMessage sm = ((StoredMessage)msg);

      if (processCardBuilder != null)
      {
        processCardBundles.add(processCardBuilder.getProcessCardBundleObject());
        sm.processCardBundles = (processCardBundles.toArray(new ProcessCardBundle[0]));

        if (replyProcessCardBuilder != null)
          sm.processCardBundleReply = replyProcessCardBuilder.getProcessCardBundleObject();
      }

      for ( int i = 0 ; i < customSoapHeader.size() ; i++ )
        msg.addCustomHeader(customSoapHeader.get(i));

      msg.dialogHandler.setCheckSignatures(false);
      parentBuilder.xmlReader.setContentHandler(parentBuilder);
    } else if (feedbackBuilder!=null && ParserHelper.isElement(CommonTags.InsideFeedback, localName, uri))
    {
      ((StoredMessage)msg).insideFeedBack = feedbackBuilder.getFeedback();
    }
    else if (ParserHelper.isElement(CommonTags.MessageId, localName, uri)
             || (localName.equals("MessageIdResponse") && uri.equals(OSCI_XMLNS)))
    {
      try
      {
        if ((msg.getMessageType() == OSCIMessage.FETCH_DELIVERY)
            || (msg.getMessageType() == OSCIMessage.RESPONSE_TO_FETCH_DELIVERY)
            || (msg.getMessageType() == OSCIMessage.FETCH_PROCESS_CARD)
            || (msg.getMessageType() == OSCIMessage.RESPONSE_TO_FETCH_PROCESS_CARD))
        {
          ((StoredMessage)msg).selectionMode = OSCIMessage.SELECT_BY_MESSAGE_ID;
          ((StoredMessage)msg).selectionRule = new String(de.osci.helper.Base64.decode(currentElement.toString()),
                                                          Constants.CHAR_ENCODING);
        }
        else
        {
          msg.messageId = new String(de.osci.helper.Base64.decode(currentElement.toString()),
                                     Constants.CHAR_ENCODING);
        }
      }
      catch (Exception ex)
      {
        throw new SAXException(ex);
      }
    }
    else if (localName.equals("ReceptionOfDelivery") && uri.equals(OSCI_XMLNS))
    {
      ((StoredMessage)msg).selectionMode = OSCIMessage.SELECT_BY_DATE_OF_RECEPTION;
      ((StoredMessage)msg).selectionRule = currentElement.toString();
    }
    else if (localName.equals("Subject") && uri.equals(OSCI_XMLNS))
      ((StoredMessage)msg).subject = currentElement.toString();
    else
      super.endElement(uri, localName, qName);

    currentElement = null;
  }
}
