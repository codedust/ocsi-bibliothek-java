package de.osci.osci12.messagetypes;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.MessagePartParser;


/**
 * Feedback-Parser.
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
class FeedbackBuilder extends MessagePartParser
{
  private static Log log = LogFactory.getLog(FeedbackBuilder.class);
  private Vector<String[]> feedbacks = new Vector<String[]>();
  private String[] entry;
  private boolean isInsideFeedback=false;

  /**
   *  Constructor for the FeedbackBuilder object
   *
   *@param  parent  Description of Parameter
   */
  public FeedbackBuilder(de.osci.osci12.messagetypes.OSCIMessageBuilder parent)
  {
    super(parent);
  }

  /**
   *  Constructor for the FeedbackBuilder object
   *
   *@param  parent  Description of Parameter
   */
  public FeedbackBuilder(de.osci.osci12.messagetypes.OSCIMessageBuilder parent,boolean isInsideFeedback)
  {
    super(parent);
    this.isInsideFeedback=isInsideFeedback;
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
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start Element in Feedback: " + localName);

    if ("Entry".endsWith(localName) && uri.equals(OSCI_XMLNS))
    {
      entry = new String[3];
      entry[0] = attributes.getValue("xml:lang");
    }
    else if ("Code".equals(localName) && uri.equals(OSCI_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if ("Text".equals(localName) && uri.equals(OSCI_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
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
    if (log.isDebugEnabled())
      log.debug("End Element in Feedback: " + localName);

    if(isInsideFeedback && ParserHelper.isElement(CommonTags.InsideFeedback,localName,uri))
    {
      parentHandler.endElement(uri, localName, qName);
      xmlReader.setContentHandler(parentHandler);
    }
    else if (ParserHelper.isElement(CommonTags.Feedback, localName, uri))
    {
      ((OSCIResponseTo) msg).feedBack = feedbacks;
      xmlReader.setContentHandler(parentHandler);
    }
    else if (localName.equals("Entry") && uri.equals(OSCI_XMLNS))
    {
      feedbacks.add(entry);
    }
    else if (localName.equals("Code") && uri.equals(OSCI_XMLNS))
    {
      entry[1] = currentElement.toString();
    }
    else if (localName.equals("Text") && uri.equals(OSCI_XMLNS))
    {
      entry[2] = currentElement.toString();
    }
    else
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }

    currentElement = null;
  }
  /**
   *
   * @return return the parsed feedbacks
   */
  public Vector<String[]> getFeedback()
  {
    return feedbacks;
  }
}
