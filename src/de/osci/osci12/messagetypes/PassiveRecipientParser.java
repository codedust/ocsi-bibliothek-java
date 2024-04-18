package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.XMLReader;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.signature.OSCISignatureException;


/**
 * Die Klasse ist der Eintrittspunkt für Nachrichten, die bei einem passiven Empfänger
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
public class PassiveRecipientParser extends de.osci.osci12.messagetypes.IncomingMSGParser
{
  private static Log log = LogFactory.getLog(PassiveRecipientParser.class);
  OSCIEnvelopeBuilder getParser(XMLReader reader, DialogHandler dh)
  {
    return new OSCIEnvelopeBuilder(reader, dh);
  }

  /**
   * Diese Methode parst eine Nachricht, die über einen InputStream eingeht.
   * Sie wird in der Regel von einem Servlet (o.ä.) aufgerufen.
   *
   * @param input InputStream der eingehenden Daten
   * @return eingelesene OSCI-Auftragsnachricht (Annahme- oder Bearbeitungsauftrag)
   * @throws IOException bei Leseproblemen
   * @throws OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException undocumented
   */
  public OSCIRequest parseStream(InputStream input)
                          throws IOException,
                                 OSCIException,
                                 java.security.NoSuchAlgorithmException
  {
    try
    {
      return this.parseStream(input, null);
    }
    catch (OSCISignatureException ex)
    {
      log.error("Error: ",ex);
      throw new OSCIErrorException(OSCIErrorCodes.SignatureInvalid);
    }
  }

  /**
   * Diese Methode parst eine Nachricht, die über einen InputStream eingeht.
   * Die eingehenden Daten werden parallel in den übergebenen OutputStream
   * geschrieben.
   *
   * @param input InputStream der eingehenden Daten
   * @param storeStream Sicherungsstream
   * @return eingelesene OSCI-Auftragsnachricht (Annahme- oder Bearbeitungsauftrag)
   * @throws IOException bei Leseproblemen
   * @throws OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException undocumented
   * @see #parseStream(InputStream)
   */
  public OSCIRequest parseStream(InputStream input, OutputStream storeStream)
                          throws IOException,
                                 OSCIException,
                                 java.security.NoSuchAlgorithmException
  {
    try
    {
      defaultSupplier = DialogHandler.getDefaultSuppliers();

      return (OSCIRequest) super.parseStream(input, null, true, storeStream);
    }
    catch (OSCISignatureException ex)
    {
      log.error("Error: ",ex);
      throw new OSCIErrorException(OSCIErrorCodes.SignatureInvalid);
    }
  }
}
