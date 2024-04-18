package de.osci.osci12.soapheader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.common.SoapClientException;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;
import de.osci.osci12.messagetypes.OSCIRequest;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;


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
public class ControlBlockHBuilder extends de.osci.osci12.messageparts.MessagePartParser
{
  private static Log log = LogFactory.getLog(ControlBlockHBuilder.class);

  //  Array steht für {Response,challenge,conversationID,SequenzNumber}
  private int[] check;
  /*
   *  private int[][] checkRequest = {{0, 0, 0, 0},    // undef
   *  {0, 1, 0, 0},    // initdialog
   *  {1, 1, 1, 1},    // exitdialog
   *  {-1, 1, -1, 1},  // getmsgid
   *  {-1, 1, -1, 1},  // storedel
   *  {1, 1, 1, 1},    // fetchdel
   *  {1, 1, 1, 1},    // fetchproccard
   *  {-1, 1, -1, 1},  // forwarddel
   *  {0, 1, 0, 0},    // acceptdel
   *  {1, 1, 1, 1},    // mediatedel
   *  {0, 1, 0, 0}};   // processdel
   *  private int[][] checkResponse = {{0, 0, 0, 0},   // resptoundef
   *  {1, 1, 1, 0},    // resptoinitdialog
   *  {1, 0, 1, 1},    // resptoexitdialog
   *  {1, -1, 1, 1},   // resptogetmsgid
   *  {1, -1, 1, 1},   // resptostoredel
   *  {1, 1, 1, 1},    // resptofetchdel
   *  {1, 1, 1, 1},    // resptofetchproccard
   *  {1, -1, 1, 1},   // resptoforwarddel
                                       geändert, weil ConvId in impliziten Dialogen unsinnig
   *  {1, -1, -1, 1},   // resptoforwarddel
   *  {1, -1, 1, 1},   // resptoacceptdel
                                       geändert, weil ConvId in impliziten Dialogen unsinnig
   *  {1, -1, 0, 0},   // resptoacceptdel
   *  {1, 1, 1, 1},    // resptomediatedel
   *  {1, -1, -1, 1}};   // resptoprocessdel
   */
  private ControlBlockH cb = null;

  /**
   *  Constructor for the ControlBlockHBuilder object
   *
   *@param  parent     Description of Parameter
   *@param  atts       Description of Parameter
   *@param  check      Description of Parameter
   * @throws SAXException
   */
  public ControlBlockHBuilder(OSCIMessageBuilder parent, Attributes atts, int[] check) throws SAXException
  {
    super(parent);
    parent.addFoundMsgPartIds(atts.getValue("Id"),HeaderTags.ControlBlock.getNamespace().getUri() +":"+HeaderTags.ControlBlock.getElementName());
    this.check = check;
    cb = new ControlBlockH();
    cb.setNSPrefixes(msg);
    if (atts.getValue("Id") != null)
    {
      cb.setRefID(atts.getValue("Id"));
    }

    cb.setConversationID(atts.getValue("ConversationId"));

    if (atts.getValue("SequenceNumber") != null)
    {
      cb.setSequenceNumber(Integer.parseInt(atts.getValue("SequenceNumber")));
    }
  }

  /**
   *  Gets the controlBlock attribute of the ControlBlockHBuilder object
   *
   *@return    The controlBlock value
   */
  public ControlBlockH getControlBlock()
  {
    return cb;
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
      log.debug("init ControlBlock  Element: " + localName);

    if ((localName.equals("Response") && uri.equals(OSCI_XMLNS)) ||
            (localName.equals("Challenge") && uri.equals(OSCI_XMLNS)))
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
      log.debug("End-Element Von ControlBlock: " + localName);

    try
    {
      if (localName.equals("Response") && uri.equals(OSCI_XMLNS))
      {
        cb.setResponse(currentElement.toString());
      }
      else if (localName.equals("Challenge") && uri.equals(OSCI_XMLNS))
      {
        cb.setChallenge(currentElement.toString());
      }
      else if (ParserHelper.isElement(HeaderTags.ControlBlock, localName, uri))
      {
        if ((check[0] == 1) && (cb.getResponse() == null))
        {
          throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
        }
        else if ((check[0] == 0) && (cb.getResponse() != null))
        {
          throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
        }

        if ((check[1] == 1) && (cb.getChallenge() == null))
        {
          throw new SAXException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");
        }
        else if ((check[1] == 0) && (cb.getChallenge() != null))
        {
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": Challenge");
        }

        if ((check[2] == 1) && (cb.getConversationID() == null))
        {
          throw new OSCIErrorException(OSCIErrorCodes.NoExplicitDialog, msg);
        }
        else if ((check[2] == 0) && (cb.getConversationID() != null))
        {
          throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
        }

        if ((check[3] == 1) && (cb.getSequenceNumber() == -1))
        {
          throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
        }
        else if ((check[3] == 0) && (cb.getSequenceNumber() != -1))
        {
          throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
        }

        DialogHandler dh = msg.dialogHandler;

        if ((check[0] & check[1] & check[2] & check[3]) < 0)
        {
          log.debug("neuer Dialog wird erstellt.");
          dh = new DialogHandler((Originator) null, new Intermed(null, null, null),
                                 (de.osci.osci12.extinterfaces.TransportI) null);
          dh.getControlblock().setConversationID(cb.getConversationID());
          dh.getControlblock().setChallenge(cb.getChallenge());
          dh.getControlblock().setResponse(cb.getResponse());
          dh.getControlblock().setSequenceNumber(cb.getSequenceNumber());
        }
        else
        {
          if (dh == null)
          {
            if (log.isDebugEnabled())
              log.debug("Dialog wird gesucht: " + cb.getConversationID());

            if ((cb.getConversationID() != null) && !cb.getConversationID().equals(""))
              dh = DialogHandler.findDialog(cb);
            else
            {
              dh = new DialogHandler((Originator) null, (Intermed) null, (de.osci.osci12.extinterfaces.TransportI) null);
            }
          }

          // An dieser Stelle wird die Sequenz-Nummer auf der Supplierseite hochgezählt
          if (msg instanceof OSCIRequest)
            if (cb.getSequenceNumber() > -1)
              dh.getControlblock().setSequenceNumber(dh.getControlblock().getSequenceNumber() + 1);

          dh.checkControlBlock(cb);
        }

        msg.dialogHandler = dh;
        msg.controlBlock = cb;
        xmlReader.setContentHandler(this.parentHandler);
      }

      currentElement = null;
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      //            ex.printStackTrace();
      throw new SAXException(ex);
    }
  }
}
