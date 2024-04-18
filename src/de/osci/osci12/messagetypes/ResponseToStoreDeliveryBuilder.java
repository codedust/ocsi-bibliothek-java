package de.osci.osci12.messagetypes;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundleBuilder;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;


/**
 * ResponseToStoreDelivery-Parser.
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
class ResponseToStoreDeliveryBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(ResponseToStoreDelivery.class);
  private Boolean insideResponseToStoreDelivery = null;
  /**
   *  Objekt ProcessCardBundle für ProcessCard Information
   */
  private ProcessCardBundleBuilder processCardBuilder = null;

  /**
   *  Constructor for the ResponseToForwardDeliveryBuilder object
   *
   *@param  parentHandler  Description of Parameter
   */
  public ResponseToStoreDeliveryBuilder(OSCIEnvelopeBuilder parentHandler)
                                 throws NoSuchAlgorithmException
  {
    super(parentHandler);
    msg = new de.osci.osci12.messagetypes.ResponseToStoreDelivery(parentHandler.dhHandler, true);
  }

  /**
   *  Description of the Method
   *
   *@param  uri                           Description of Parameter
   *@param  localName                     Description of Parameter
   *@param  qName                         Description of Parameter
   *@param  attributes                    Description of Parameter
   *@exception  SAXException  Description of Exception
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start Element RspStrDel: " + localName);

    if (insideBody)
    {
    }
    else if (insideHeader)
    {
      // ### Auswerten des ControlBlock Headers###
      if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
      {
        parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes, //                        new int[]{1, -1, 1, 1}));
                                                                           //geändert, s. ControlBlockH
        new int[] { 1, -1, 1, 1 }));
      }

      // ### Auswerten des ClientSignature Headers###
      else if (ParserHelper.isElement(HeaderTags.SupplierSignature,localName,uri))
      {
        parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                 this, attributes));
      }
      else if (ParserHelper.isElement(HeaderTags.responseToStoreDelivery,localName,uri))
      {
        addFoundMsgPartIds(attributes.getValue("Id"), HeaderTags.responseToStoreDelivery.getNamespace().getUri() +":"+HeaderTags.responseToStoreDelivery.getElementName());
        if (insideResponseToStoreDelivery != null)
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        insideResponseToStoreDelivery = true;
      }
      else if (ParserHelper.isElement(CommonTags.ProcessCardBundle,localName,uri) && insideResponseToStoreDelivery)
      {
        int[] check = { 1, -1, -1, -1 };
        processCardBuilder = new ProcessCardBundleBuilder(CommonTags.ProcessCardBundle, parentBuilder.xmlReader, this, check);
        parentBuilder.xmlReader.setContentHandler(processCardBuilder);
      }
      else if (ParserHelper.isElement(CommonTags.Feedback, localName, uri) && insideResponseToStoreDelivery)
      {
        parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
      }
      else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates,localName,uri))
      {
        int[] check = { 0, -1 };
        parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes, check));
      }
      else if (ParserHelper.isElement(HeaderTags.FeatureDescription,localName,uri))
      {
        FeatureDescriptionHBuilder featureBuilder= new FeatureDescriptionHBuilder(this,attributes);
        parentBuilder.xmlReader.setContentHandler(featureBuilder);
        featureBuilder.startElement(uri, localName, qName, attributes);
      }
      else
        startCustomSoapHeader(uri, localName, qName, attributes);
    }
    else
      super.startElement(uri, localName, qName, attributes);
  }

  /**
   *  Description of the Method
   *
   *@param  uri                           Description of Parameter
   *@param  localName                     Description of Parameter
   *@param  qName                         Description of Parameter
   *@exception  SAXException  Description of Exception
   */
  public void endElement(String uri, String localName, String qName)
                  throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    if (localName.equals("Header"))
    {
      if (processCardBuilder != null)
        ((ResponseToStoreDelivery) msg).processCardBundle = processCardBuilder.getProcessCardBundleObject();

      insideHeader = false;
    }
    else if (ParserHelper.isElement(HeaderTags.responseToStoreDelivery,localName,uri))
      insideResponseToStoreDelivery = false;
    else
      super.endElement(uri, localName, qName);

    currentElement = null;
  }
}
