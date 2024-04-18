package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.StoreOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.Constants.OSCIFeatures;
import de.osci.osci12.soapheader.DesiredLanguagesH;
import de.osci.osci12.soapheader.FeatureDescriptionH;


/**
 * Die Klasse ist die Superklasse aller OSCI-Auftragsnachrichtenobjekte.
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
public abstract class OSCIRequest extends OSCIMessage
{
  //  private static Log log = LogFactory.getLog(OSCIRequest.class);
  protected java.net.URI uriReceiver;

  // todo: falls ein statisches Parserobjekt unperformant ist, evtl. eine getInstance()-Methode in den Parser einbauen
  static IncomingMSGParser parser = new PassiveRecipientParser();

  OSCIRequest()
  {
  }

  OSCIRequest(DialogHandler dh)
  {
    super(dh);
    featureDescription = new FeatureDescriptionH();
    
    // setze FeatureDescription mit aktuellen Features
    for ( OSCIFeatures feature : OSCIFeatures.values() )
    {
      featureDescription.getSupportedFeatures().add(feature);
    }
    desiredLanguagesH = new DesiredLanguagesH(dh.getLanguageList());
  }

  /**
   * Diese Methode liefert die im DialogHandler gesetzte Liste der gewünschten
   * Sprachen.
   * @return Liste der Sprachkürzel, getrennt durch Leerzeichen, z.B. "de en-US fr"
   * @see DialogHandler#getLanguageList()
   */
  public String getDesiredLanguages()
  {
    return dialogHandler.getLanguageList();
  }

  /**
   * undocumented
   *
   * @param outp undocumented
   * @param inp undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   * @throws de.osci.osci12.OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  protected OSCIMessage transmit(OutputStream outp, OutputStream inp)
                          throws IOException,
                                 OSCIException,
                                 NoSuchAlgorithmException
  {
    de.osci.osci12.extinterfaces.TransportI transport;

    synchronized (dialogHandler.getTransportModule())
    {
      transport = dialogHandler.getTransportModule().newInstance();
    }

    boolean intermed = ((this instanceof AcceptDelivery) || (this instanceof ProcessDelivery));
    URI uri;

    if (!intermed)
      uri = ((de.osci.osci12.roles.Intermed) dialogHandler.getSupplier()).getUri();
    else
      uri = uriReceiver;

    dialogHandler.fireEvent(Constants.EVENT_CONNECT);
    dialogHandler.fireEvent(Constants.EVENT_SIGN_MSG);

    if (dialogHandler.isCreateSignatures())
      sign();

    OutputStream out = null;
    dialogHandler.fireEvent(Constants.EVENT_SEND_MSG);

    try
    {
    	if (dialogHandler.isEncryption())
    	{
    		SOAPMessageEncrypted sme = new SOAPMessageEncrypted(this, outp);
    		out = transport.getConnection(uri, sme.calcLength());
    		sme.writeXML(out);
    	}
    	else
    	{
    		out = transport.getConnection(uri, calcLength());

    		if (outp != null)
    		{
    			StoreOutputStream sos = new StoreOutputStream(out, outp);
    			writeXML(sos);
    			sos.close();
    		}
    		else
    			writeXML(out);
    	}
    }
    finally
    {
    	if (out != null)
    		out.close();
    	if (outp != null)
    	  outp.close();
    }
    dialogHandler.fireEvent(Constants.EVENT_RECEIVE_MSG);

    InputStream in = null;
    OSCIMessage rsp;

    try
    {
      in = transport.getResponseStream();
      rsp = parser.parseStream(in, dialogHandler, false, inp);
      dialogHandler.fireEvent(Constants.EVENT_ACTION_COMPLETE);
    }
    finally
    {
      if (in != null)
        in.close();
      if (inp != null)
        inp.close();
    }

    return rsp;
  }

  /**
   * Bringt eine Client-Signatur an.
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws OSCIException wenn Zusammenstellen der Daten ein Problem auftritt.
   * @throws de.osci.osci12.common.OSCICancelledException bei Abbruch durch den
   * Benutzer
   */
  void sign()
     throws IOException,
            OSCIException,
            de.osci.osci12.common.OSCICancelledException,
            java.security.NoSuchAlgorithmException
  {
    super.sign(dialogHandler.getClient());
    //    messageParts.set(1, signatureHeader);
    // Dazwischen liegt der DesiredLanguages-Header
    messageParts.set(3, nonIntermediaryCertificatesH);
  }

  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   */
  protected void compose() throws OSCIException,
                                  NoSuchAlgorithmException,
                                  IOException
  {
    super.compose();
    messageParts.add(desiredLanguagesH);
    messageParts.add(nonIntermediaryCertificatesH); // maybe null
  }
}
