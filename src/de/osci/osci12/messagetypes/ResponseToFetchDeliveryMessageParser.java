package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.XMLReader;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.CommonFactory;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.signature.OSCISignatureException;


/**
 * Die Klasse ist für das parsen von ResponseToFetchDelivery Nachrichten zustaendig
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann
 * @since 1.7.1
 */
class ResponseToFetchDeliveryMessageParser extends de.osci.osci12.messagetypes.IncomingMSGParser
{
  private static final Log LOG = LogFactory.getLog(ResponseToFetchDeliveryMessageParser.class);
  @Override
  OSCIEnvelopeBuilder getParser(XMLReader reader, DialogHandler dh)
  {
    return new OSCIEnvelopeBuilder(reader,dh);
  }

  /**
   *
   *
   * @param input InputStream des ResponseToFetchDelivery
   *
   * @return Konstruiertes ResponseToFetchDelivery Objekt
   *
   * @throws IOException
   * @throws OSCIException
   * @throws NoSuchAlgorithmException
   */
  public ResponseToFetchDelivery parseStream(InputStream input) throws NoSuchAlgorithmException, IOException, OSCIException
  {
    try
    {
      DialogHandler dh=
        new DialogHandler((Originator)null,new Intermed(null, null),null);
      CommonFactory.setDisableControlBlockCheck(dh, true);
      dh.setEncryption(false);
      return (ResponseToFetchDelivery) super.parseStream(input, dh,false,null);
    }
    catch (OSCISignatureException ex)
    {
      LOG.error("OSCISignatureException exception.", ex);
      throw new OSCIErrorException(OSCIErrorCodes.SignatureInvalid);
    }
  }
}
