package de.osci.osci12.soapheader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class QualityOfTimestampHBuilder extends de.osci.osci12.messageparts.MessagePartParser
{
  //  private static Log log = LogFactory.getLog(QualityOfTimestampHBuilder.class);
  QualityOfTimestampH quotsh;

  /**
   *  Constructor for the QualityOfTimestampHBuilder object
   *
   *@param  parentHandler                 Description of Parameter
   *@param  atts                          Description of Parameter
   *@exception  SAXException  Description of Exception
   */
  public QualityOfTimestampHBuilder(OSCIMessageBuilder parentHandler, Attributes atts) throws SAXException
  {
    super(parentHandler);
    parentHandler.addFoundMsgPartIds(atts.getValue("Id"), HeaderTags.QualityOfTimestamp.getNamespace().getUri() +":"+HeaderTags.QualityOfTimestamp.getElementName()+ '_' + atts.getValue("Service"));
    quotsh = new QualityOfTimestampH(atts.getValue("Service").equals("reception"),
                                     atts.getValue("Quality").equals("cryptographic"));

    if (quotsh.isServiceReception())
      msg.qualityOfTimestampTypeReception = quotsh;
    else
      msg.qualityOfTimestampTypeCreation = quotsh;

    if (atts.getValue("Id") != null)
      quotsh.setRefID(atts.getValue("Id"));

    OSCIMessage msg = parentHandler.getOSCIMessage();
    quotsh.setNSPrefixes(msg);
  }

  /**
   *  Gets the qualityOfTimestampH attribute of the QualityOfTimestampHBuilder
   *  object
   *
   *@return    The qualityOfTimestampH value
   */
  public QualityOfTimestampH getQualityOfTimestampH()
  {
    return quotsh;
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
    if (!(ParserHelper.isElement(HeaderTags.QualityOfTimestamp, localName, uri)))
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    xmlReader.setContentHandler(parentHandler);
  }
}
