package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>
 * Dieses Klasse repräsentiert die Antwort des Intermediärs auf einen Zustellungsabholauftrag. Clients
 * erhalten vom Intermediär eine Instanz dieser Klasse, die eine Rückmeldung über den Erfolg der Operation
 * (getFeedback()) sowie ggf. die angeforderten verschlüsselten und/oder unverschlüsselten Inhaltsdaten
 * einschl. des zugehörigen Laufzettels enthält.
 * </p>
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.FetchDelivery
 */
public class ResponseToFetchDelivery extends ResponseToFetchAbstract implements ContentPackageI
{

  /**
   * Erzeugt aus einem Stream ein ResponseToFetchDelivery Objekt
   *
   * @param streamRspStrDel OSCI Stream vom ResponseToFetchDelivery
   * @return Konstruiertes ResponseToFetchDelivery Objekt
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public static ResponseToFetchDelivery parseResponseToFetchDelivery(InputStream streamRspStrDel)
    throws NoSuchAlgorithmException, IOException, OSCIException
  {
    ResponseToFetchDeliveryMessageParser obj = new ResponseToFetchDeliveryMessageParser();
    return obj.parseStream(streamRspStrDel);
  }

  ResponseToFetchDelivery(FetchRequestAbstract fetchDel, StoreDelivery storeDel)
    throws NoSuchAlgorithmException, OSCIRoleException
  {
    super(fetchDel.dialogHandler);
    // hier werden die Rollenobjekte getauscht...
    messageType = RESPONSE_TO_FETCH_DELIVERY;
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    setSelectionMode(fetchDel.getSelectionMode());
    setSelectionRule(fetchDel.getSelectionRule());

    if (storeDel == null)
    {
      setFeedback(new String[]{OSCIErrorCodes.NoDeliveryPresent.getOSCICode()});
    }
    else
    {
      addressee = storeDel.addressee;
      originator = storeDel.originator;

      ContentContainer[] con = storeDel.getContentContainer();

      for ( int i = 0 ; i < con.length ; i++ )
        addContentContainer(con[i]);

      EncryptedDataOSCI[] enc = storeDel.getEncryptedData();

      for ( int i = 0 ; i < enc.length ; i++ )
        addEncryptedData(enc[i]);

      Attachment[] att = storeDel.getAttachments();

      for ( int i = 0 ; i < att.length ; i++ )
        addAttachment(att[i]);

      for ( int i = 0 ; i < storeDel.getOtherAuthors().length ; i++ )
        otherAutors.put(storeDel.getOtherAuthors()[i].id, storeDel.getOtherAuthors()[i]);

      for ( int i = 0 ; i < storeDel.getOtherReaders().length ; i++ )
        otherReaders.put(storeDel.getOtherReaders()[i].id, storeDel.getOtherReaders()[i]);
    }
  }

  ResponseToFetchDelivery(DialogHandler dh)
  {
    super(dh);
    originator = ((Originator)dh.getClient());
    messageType = RESPONSE_TO_FETCH_DELIVERY;
  }


  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws IOException undocumented
   */
  @Override
  protected void compose() throws OSCIException, IOException, NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(2, null);
    createNonIntermediaryCertificatesH();

    String selection = "";

    if (selectionMode == SELECT_BY_MESSAGE_ID)
    {
      try
      {
        selection = "<" + osciNSPrefix + ":fetchDelivery><" + osciNSPrefix + ":SelectionRule><" + osciNSPrefix
                    + ":MessageId>"
                    + de.osci.helper.Base64.encode(selectionRule.getBytes(Constants.CHAR_ENCODING)) + "</"
                    + osciNSPrefix + ":MessageId></" + osciNSPrefix + ":SelectionRule></" + osciNSPrefix
                    + ":fetchDelivery>";
      }
      catch (UnsupportedEncodingException ex)
      {

      }
    }
    else if (selectionMode == SELECT_BY_DATE_OF_RECEPTION)
      selection = "<" + osciNSPrefix + ":fetchDelivery><" + osciNSPrefix + ":SelectionRule><" + osciNSPrefix
                  + ":ReceptionOfDelivery>" + selectionRule + "</" + osciNSPrefix + ":ReceptionOfDelivery></"
                  + osciNSPrefix + ":SelectionRule></" + osciNSPrefix + ":fetchDelivery>";
    else
      selection = "<" + osciNSPrefix + ":fetchDelivery></" + osciNSPrefix + ":fetchDelivery>";

    if (processCardBundle == null)
      osciH = new OsciH(HeaderTags.responseToFetchDelivery.getElementName(), writeFeedBack() + selection);
    else
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      MessagePartsFactory.writeXML(processCardBundle, out);
      osciH = new OsciH(HeaderTags.responseToFetchDelivery.getElementName(),
                        writeFeedBack() + selection + out.toString(Constants.CHAR_ENCODING));
    }

    messageParts.add(osciH);
    messageParts.add(intermediaryCertificatesH);
    messageParts.add(nonIntermediaryCertificatesH);
    if (featureDescription != null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    body = new Body(getContentContainer(), getEncryptedData());
    stateOfMsg |= STATE_COMPOSED;
    messageParts.add(body);
  }
}
