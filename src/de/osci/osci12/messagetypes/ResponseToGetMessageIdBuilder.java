package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;


/**
 * ResponseToGetMessageId-Parser.
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
class ResponseToGetMessageIdBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(ResponseToGetMessageIdBuilder.class);
  private Boolean insideResponseToGetMessageId = null;

  /**
   *  Constructor for the ResponseToForwardDeliveryBuilder object
   *
   *@param  parentHandler  Description of Parameter
   */
  public ResponseToGetMessageIdBuilder(OSCIEnvelopeBuilder parentHandler)
                                throws NoSuchAlgorithmException
  {
    super(parentHandler);
    msg = new de.osci.osci12.messagetypes.ResponseToGetMessageId(parentHandler.dhHandler, true);
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
      log.debug("Start Element RspFwdDel: " + localName);

    if (insideBody)
    {
      if (ParserHelper.isElement(CommonTags.Feedback, localName, uri) && insideResponseToGetMessageId)
      {
        parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
      }
      else if (ParserHelper.isElement(CommonTags.MessageId,localName,uri) && insideResponseToGetMessageId)
      {
        currentElement = new StringBuffer();
      }
      else if (localName.equals("responseToGetMessageId") && uri.equals(OSCI_XMLNS))
      {
        if (insideResponseToGetMessageId != null)
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        insideResponseToGetMessageId = true;
      }
    }
    else if (insideHeader)
    {
      // ### Auswerten des ControlBlock Headers###
      if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
        parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes, new int[] { 1, -1, 1, 1 }));
      // ### Auswerten des ClientSignature Headers###
      else if (ParserHelper.isElement(HeaderTags.SupplierSignature,localName,uri))
        parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                 this, attributes));
      else if (localName.equals("responseToGetMessageId") && uri.equals(OSCI_XMLNS))
      {
        ;
        //nothing to do
      }
      else if (ParserHelper.isElement(HeaderTags.FeatureDescription,localName,uri))
      {
        FeatureDescriptionHBuilder featureBuilder= new FeatureDescriptionHBuilder(this,attributes);
        parentBuilder.xmlReader.setContentHandler(featureBuilder);
        featureBuilder.startElement(uri, localName, qName, attributes);
      }
      else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates,localName,uri))
      {
        int[] check = { 0, -1 };
        parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes, check));
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

    if (localName.equals("Body") && uri.equals(SOAP_XMLNS))
    {
      insideBody = false;
    }
    else if (ParserHelper.isElement(CommonTags.MessageId,localName,uri) && insideResponseToGetMessageId)
    {
      try
      {
        ((ResponseToGetMessageId) msg).setMessageId(new String(de.osci.helper.Base64.decode(currentElement.toString()),
                                                               Constants.CHAR_ENCODING));
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }
    }
    else if (localName.equals("responseToGetMessageId") && uri.equals(OSCI_XMLNS))
      insideResponseToGetMessageId = false;
    else if (!(ParserHelper.isElement(HeaderTags.IntermediaryCertificates,localName,uri)))
      super.endElement(uri, localName, qName);

    currentElement = null;
  }
}
