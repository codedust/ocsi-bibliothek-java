package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.XMLReader;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.signature.OSCISignatureException;


/**
 * Die Klasse ist der Einrittspunkt für Nachrichten, die bei einem passiven Empfänger
 * eingehen.
 *
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
class StoredMessageParser extends de.osci.osci12.messagetypes.IncomingMSGParser
{
  OSCIEnvelopeBuilder getParser(XMLReader reader, DialogHandler dh)
  {
    return new StoredEnvelopeBuilder(reader);
  }

  /**
   * undocumented
   *
   * @param input undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws java.security.NoSuchAlgorithmException undocumented
   * @throws OSCIErrorException undocumented
   */
  public StoredMessage parseStream(InputStream input)
                            throws IOException,
                                   OSCIException,
                                   java.security.NoSuchAlgorithmException
  {
    try
    {
      return (StoredMessage) super.parseStream(input, null, true, null);
    }
    catch (OSCISignatureException ex)
    {
      throw new OSCIErrorException(OSCIErrorCodes.SignatureInvalid);
    }
  }
}
