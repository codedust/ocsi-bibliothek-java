package de.osci.osci12.messagetypes;

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
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesHBuilder;


/**
 * ResponseToMediateDelivery-Parser.
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
class ResponseToMediateDeliveryBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(ResponseToMediateDeliveryBuilder.class);
  /**
   *  Objekt ProcessCardBundle für ProcessCard Information
   */
  private ProcessCardBundleBuilder requestProcessCardBuilder = null;
  private ProcessCardBundleBuilder replyProcessCardBuilder = null;
  private Boolean insideResponseToMediateDelivery = null;

  /**
   *  Constructor for the ResponseToForwardDeliveryBuilder object
   *
   *@param  parentHandler  Description of Parameter
   */
  public ResponseToMediateDeliveryBuilder(OSCIEnvelopeBuilder parentHandler)
                                   throws OSCIRoleException
  {
    super(parentHandler);
    msg = new de.osci.osci12.messagetypes.ResponseToMediateDelivery(parentHandler.dhHandler);
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
      log.debug("Start Element RspFwdDel: " + localName + " : " + insideHeader + "/" + insideBody);

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
        parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes, new int[] { 1, 1, 1, 1 }));
      // ### Auswerten des ClientSignature Headers###
      else if (ParserHelper.isElement(HeaderTags.SupplierSignature,localName,uri))
        parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                 this, attributes));
      else if (ParserHelper.isElement(HeaderTags.responseToMediateDelivery,localName,uri))
      {
        addFoundMsgPartIds(attributes.getValue("Id"), HeaderTags.responseToMediateDelivery.getNamespace().getUri() +":"+HeaderTags.responseToMediateDelivery.getElementName());
        if (insideResponseToMediateDelivery != null)
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        insideResponseToMediateDelivery = true;
      }
      else if (ParserHelper.isElement(CommonTags.RequestProcessCardBundle, localName, uri) && insideResponseToMediateDelivery)
      {
        int[] check = { 1, -1, -1, -1 };
        requestProcessCardBuilder = new ProcessCardBundleBuilder(CommonTags.RequestProcessCardBundle, parentBuilder.xmlReader,
                                                                 this, check);
        parentBuilder.xmlReader.setContentHandler(requestProcessCardBuilder);
      }
      else if (ParserHelper.isElement(CommonTags.ReplyProcessCardBundle, localName, uri) && insideResponseToMediateDelivery)
      {
        int[] check = { 1, -1, -1, -1 };
        replyProcessCardBuilder = new ProcessCardBundleBuilder(CommonTags.ReplyProcessCardBundle, parentBuilder.xmlReader, this,
                                                               check);
        parentBuilder.xmlReader.setContentHandler(replyProcessCardBuilder);
      }
      else if (ParserHelper.isElement(CommonTags.Feedback, localName, uri) && insideResponseToMediateDelivery)
      {
        parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
      }
      else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates,localName,uri))
      {
        int[] check = { 0, -1 };
        parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes, check));
      }
      else if (ParserHelper.isElement(HeaderTags.NonIntermediaryCertificates,localName,uri))
      {
        int[] check = { -1, -1, -1, -1, -1, -1, 0 };
        parentBuilder.xmlReader.setContentHandler(new NonIntermediaryCertificatesHBuilder(this, attributes, check));
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

    if (localName.equals("Header") && uri.equals(SOAP_XMLNS))
    {
      ResponseToMediateDelivery rtmd = ((ResponseToMediateDelivery) msg);

      // Das RequestProcessCardBundle bestücken
      if (requestProcessCardBuilder != null)
      {
        rtmd.processCardBundleRequest = requestProcessCardBuilder.getProcessCardBundleObject();
        rtmd.messageId = requestProcessCardBuilder.getProcessCardBundleObject().getMessageId();

        if (replyProcessCardBuilder != null)
          rtmd.processCardBundleReply = replyProcessCardBuilder.getProcessCardBundleObject();
      }
      else if (ParserHelper.isElement(HeaderTags.responseToMediateDelivery,localName,uri))
        insideResponseToMediateDelivery = false;

      insideHeader = false;
    }
    else
      super.endElement(uri, localName, qName);

    currentElement = null;
  }
}
