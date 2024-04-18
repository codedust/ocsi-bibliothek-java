package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.util.Vector;

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
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.ProcessCardBundleBuilder;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;


/**
 * ResponseToFetchProcessCard-Parser.
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
class ResponseToFetchProcessCardBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(ResponseToFetchProcessCardBuilder.class);
  /**
   *  Objekt ProcessCardBundle für ProcessCard Information
   */
  private ProcessCardBundleBuilder processCardBuilder = null;
  private Vector<ProcessCardBundle> processCardBundles = new Vector<ProcessCardBundle>();
  private StringBuffer messageIds = new StringBuffer();
  private Boolean insideResponseToFetchProcessCard = null;
  private Boolean insideFetchProcessCard = null;

  /**
   *  Constructor for the ResponseToForwardDeliveryBuilder object
   *
   *@param  parentHandler  Description of Parameter
   */
  public ResponseToFetchProcessCardBuilder(OSCIEnvelopeBuilder parentHandler)
  {
    super(parentHandler);
    msg = new de.osci.osci12.messagetypes.ResponseToFetchProcessCard(parentHandler.dhHandler);
  }

  private void setSelectionAttributes(Attributes att)
  {
    if ("true".equals(att.getValue("NoReception")))
      ((ResponseToFetchProcessCard) msg).setSelectNoReceptionOnly(true);

    if ("Addressee".equals(att.getValue("Role")))
      ((ResponseToFetchProcessCard) msg).setRoleForSelection(OSCIMessage.SELECT_ADDRESSEE);
    else if ("Originator".equals(att.getValue("Role")))
      ((ResponseToFetchProcessCard) msg).setRoleForSelection(OSCIMessage.SELECT_ORIGINATOR);
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
      log.debug("Start Element RspFtProc: " + localName);

    if (insideBody)
    {
      if (ParserHelper.isElement(CommonTags.Feedback, localName, uri) && insideResponseToFetchProcessCard)
      {
        parentBuilder.xmlReader.setContentHandler(new FeedbackBuilder(this));
      }
      else if (ParserHelper.isElement(CommonTags.MessageId,localName,uri) && insideFetchProcessCard)
      {
        ((ResponseToFetchProcessCard) msg).setSelectionMode(OSCIMessage.SELECT_BY_MESSAGE_ID);
        setSelectionAttributes(attributes);
        currentElement = new StringBuffer();
      }
      else if (localName.equals("ReceptionOfDelivery") && uri.equals(OSCI_XMLNS) && insideFetchProcessCard)
      {
        ((ResponseToFetchProcessCard) msg).setSelectionMode(OSCIMessage.SELECT_BY_DATE_OF_RECEPTION);
        setSelectionAttributes(attributes);
        currentElement = new StringBuffer();
      }
      else if (localName.equals("RecentModification") && uri.equals(OSCI_XMLNS) && insideFetchProcessCard)
      {
        ((ResponseToFetchProcessCard) msg).setSelectionMode(OSCIMessage.SELECT_BY_RECENT_MODIFICATION);
        currentElement = new StringBuffer();
      }
      else if (localName.equals("Quantity") && uri.equals(OSCI_XMLNS) && insideFetchProcessCard)
      {
        ((ResponseToFetchProcessCard) msg).setQuantityLimit(Long.parseLong(attributes.getValue("Limit")));
      }
      else if (ParserHelper.isElement(HeaderTags.responseToFetchProcessCard, localName, uri) && uri.equals(OSCI_XMLNS))
      {
        if (insideResponseToFetchProcessCard != null)
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        insideResponseToFetchProcessCard = true;
//        addFoundMsgPartIds(attributes.getValue("Id"), OSCI_XMLNS+":responseToFetchProcessCard");
      }
      else if (ParserHelper.isElement(HeaderTags.fetchProcessCard, localName, uri) && insideResponseToFetchProcessCard)
      {
        if (insideFetchProcessCard != null)
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        insideFetchProcessCard = true;
      }
      else if (ParserHelper.isElement(CommonTags.ProcessCardBundle,localName,uri) && insideResponseToFetchProcessCard)
      {
        if (processCardBuilder != null)
          processCardBundles.add(processCardBuilder.getProcessCardBundleObject());

        int[] check = { 1, -1, -1, -1 };
        processCardBuilder = new ProcessCardBundleBuilder(CommonTags.ProcessCardBundle, parentBuilder.xmlReader, this, check);
        parentBuilder.xmlReader.setContentHandler(processCardBuilder);
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

    if (ParserHelper.isElement(CommonTags.MessageId,localName,uri))
    {
      if (messageIds.length() > 0)
        messageIds.append(';');

      try
      {
        messageIds.append(new String(de.osci.helper.Base64.decode(currentElement.toString()), Constants.CHAR_ENCODING));
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }
    }
    else if ((localName.equals("ReceptionOfDelivery") && uri.equals(OSCI_XMLNS)) ||
                 (localName.equals("RecentModification") && uri.equals(OSCI_XMLNS)) && insideFetchProcessCard)
    {
      ((ResponseToFetchProcessCard) msg).setSelectionRule(currentElement.toString());
    }
    else if (localName.equals("SelectionRule") && uri.equals(OSCI_XMLNS) && insideFetchProcessCard)
    {
      if (((ResponseToFetchProcessCard) msg).getSelectionMode() == OSCIMessage.SELECT_BY_MESSAGE_ID)
        ((ResponseToFetchProcessCard) msg).setSelectionRule(messageIds.toString());
    }
    else if (ParserHelper.isElement(HeaderTags.responseToFetchProcessCard, localName, uri))
      insideResponseToFetchProcessCard = false;
    else if (ParserHelper.isElement(HeaderTags.fetchProcessCard, localName, uri))
      insideFetchProcessCard = false;
    else if (localName.equals("Body") && uri.equals(SOAP_XMLNS))
    {
      ResponseToFetchProcessCard rtmd = ((ResponseToFetchProcessCard) msg);

      if (processCardBuilder != null)
        processCardBundles.add(processCardBuilder.getProcessCardBundleObject());

      rtmd.processCardBundles = ((ProcessCardBundle[]) processCardBundles.toArray(new ProcessCardBundle[0]));
      insideBody = false;
    }
    else
      super.endElement(uri, localName, qName);

    currentElement = null;
  }
}
