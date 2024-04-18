package de.osci.osci12.messagetypes;

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
import de.osci.osci12.messageparts.ProcessCardBundleBuilder;
import de.osci.osci12.soapheader.ControlBlockHBuilder;
import de.osci.osci12.soapheader.DesiredLanguagesH;
import de.osci.osci12.soapheader.FeatureDescriptionHBuilder;
import de.osci.osci12.soapheader.IntermediaryCertificatesHBuilder;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesHBuilder;


/**
 * ProcessDelivery-Parser.
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
class ProcessDeliveryBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(ProcessDeliveryBuilder.class);
  private ProcessCardBundleBuilder processCardBuilder = null;
  private Boolean insideProcessDelivery = null;

  /**
   * Konstruktor für den aufrufenden Mime-Parser
   *
   *@param  parentBuilder  Parent Object in den meisten Fällen der OSCIEnvelopeBuilder
   */
  ProcessDeliveryBuilder(OSCIEnvelopeBuilder parentBuilder)
  {
    super(parentBuilder);

    if (log.isDebugEnabled())
      log.debug("Konstruktor");

    msg = new de.osci.osci12.messagetypes.ProcessDelivery();
  }

  /**
   *   Überschreibt die characters Methode des DefaultHandler
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
    try
    {
      if (log.isDebugEnabled())
        log.debug("Start-Element: " + localName);

      if (insideBody)
      {
        if (ParserHelper.isElement(CommonTags.ContentPackage, localName, uri)) {
          setContentPackageHandler(localName);
        }
      }
      else if (insideHeader)
      {
        if (log.isDebugEnabled())
          log.debug("inside Header");

        // ### Auswerten des ControlBlock Headers###
        if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
          parentBuilder.xmlReader.setContentHandler(new ControlBlockHBuilder(this, attributes, new int[] { 0, 1, 0, 0 }));
        // ### Auswerten des ClientSignature Headers###
        else if (ParserHelper.isElement(HeaderTags.ClientSignature, localName, uri))
          parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createOsciSignatureBuilder(parentBuilder.xmlReader,
                                                                                                   this, attributes));
        else if (ParserHelper.isElement(HeaderTags.DesiredLanguages, localName, uri))
        {
          msg.desiredLanguagesH = new DesiredLanguagesH(this, attributes.getValue("Id"),attributes.getValue("LanguagesList"));
          msg.desiredLanguagesH.setRefID(attributes.getValue("Id"));
          msg.desiredLanguagesH.setNSPrefixes(msg);
          msg.dialogHandler.setLanguageList(attributes.getValue("LanguagesList"));
        }
        else if (ParserHelper.isElement(HeaderTags.processDelivery,localName,uri))
        {
          addFoundMsgPartIds(attributes.getValue("Id"), HeaderTags.processDelivery.getNamespace().getUri() +":"+HeaderTags.processDelivery.getElementName());
          if (insideProcessDelivery != null)
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
          insideProcessDelivery = true;
        }
        else if (localName.equals("MessageIdResponse") && uri.equals(OSCI_XMLNS) && insideProcessDelivery)
        {
          currentElement = new StringBuffer();
        }
        else if (ParserHelper.isElement(CommonTags.ProcessCardBundle,localName,uri) && insideProcessDelivery)
        {
          int[] check = { 1, 1, 0, -1 };
          processCardBuilder = new ProcessCardBundleBuilder(CommonTags.ProcessCardBundle, parentBuilder.xmlReader, this, check);
          parentBuilder.xmlReader.setContentHandler(processCardBuilder);
        }
        else if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates,localName,uri))
        {
          int[] check = { -1, -1 };
          parentBuilder.xmlReader.setContentHandler(new IntermediaryCertificatesHBuilder(this, attributes, check));
        }
        else if (ParserHelper.isElement(HeaderTags.NonIntermediaryCertificates,localName,uri))
        {
          int[] check = { -1, -1, -1, -1, -1, -1, 0 };
          parentBuilder.xmlReader.setContentHandler(new NonIntermediaryCertificatesHBuilder(this, attributes, check));
        } else if (ParserHelper.isElement(HeaderTags.FeatureDescription,localName,uri))
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
    catch (Exception ex)
    {
      throw new SAXException(ex);
    }
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
    try
    {
      if (log.isDebugEnabled())
        log.debug("End-Element: " + localName);

      if (localName.equals("Header") && uri.equals(SOAP_XMLNS))
      {
        if (processCardBuilder != null)
          ((ProcessDelivery) msg).processCardBundle = processCardBuilder.getProcessCardBundleObject();

        insideHeader = false;
      }
      else if (ParserHelper.isElement(HeaderTags.processDelivery,localName,uri))
        insideProcessDelivery = false;
      else if (insideProcessDelivery != null && insideProcessDelivery && localName.equals("MessageIdResponse") && uri.equals(OSCI_XMLNS))
        msg.messageId = new String(de.osci.helper.Base64.decode(currentElement.toString()), Constants.CHAR_ENCODING);
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
