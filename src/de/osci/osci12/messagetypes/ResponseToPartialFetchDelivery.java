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
import de.osci.osci12.encryption.OSCICipherException;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.ChunkInformation.CheckInstance;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>
 * Instanzen dieser Klasse werden als Antworten auf paketierte Zustellungsabholaufträge zurückgegeben. Das
 * Nachrichtenobjekt enthält einen Teil einer ResponseToFetchDelivery Nachricht als Payload der
 * ResponseToPartialFetchDelivery Nachricht. Sollte die Nachricht klein sein, wird sofort die
 * ResponseToFetchDelivery Nachricht als Ergebnis zurück gegeben.
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
 * @author R. Lindemann
 * @version 2.4.1
 * @since 1.8.0
 * @see de.osci.osci12.messagetypes.PartialFetchDelivery
 */
public class ResponseToPartialFetchDelivery extends ResponseToFetchAbstract
{

  ChunkInformation chunkInformation = MessagePartsFactory.creatChunkInformation(CheckInstance.ResponsePartialFetchDelivery);

  /**
   *
   * @param fetchReq
   * @param addressee
   * @param originator
   * @param chunkAttachment
   * @throws NoSuchAlgorithmException
   * @throws OSCIRoleException
   * @throws IllegalArgumentException
   * @throws IOException
   */
  ResponseToPartialFetchDelivery(FetchRequestAbstract fetchReq,
                                 Addressee addressee,
                                 Originator originator,
                                 Attachment chunkAttachment)
    throws NoSuchAlgorithmException, OSCIRoleException, IllegalArgumentException, IOException
  {
    super(fetchReq.dialogHandler);
    // hier werden die Rollenobjekte getauscht...
    messageType = RESPONSE_TO_PARTIAL_FETCH_DELIVERY;
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    setSelectionMode(fetchReq.getSelectionMode());
    setSelectionRule(fetchReq.getSelectionRule());
    setChunkBlob(chunkAttachment);
    if (addressee == null || originator == null)
    {
      setFeedback(new String[]{OSCIErrorCodes.NoDeliveryPresent.getOSCICode()});
    }
    else
    {
      this.addressee = addressee;
      this.originator = originator;
    }
  }

  ResponseToPartialFetchDelivery(FetchRequestAbstract fetchReq,
                                 Addressee addressee,
                                 Originator originator,
                                 InputStream chunkBlob)
    throws NoSuchAlgorithmException, OSCIRoleException, IllegalArgumentException, IOException
  {
    super(fetchReq.dialogHandler);
    // hier werden die Rollenobjekte getauscht...
    messageType = RESPONSE_TO_PARTIAL_FETCH_DELIVERY;
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    setSelectionMode(fetchReq.getSelectionMode());
    setSelectionRule(fetchReq.getSelectionRule());
    setChunkBlob(chunkBlob);
    if (addressee == null || originator == null)
    {
      setFeedback(new String[]{OSCIErrorCodes.NoDeliveryPresent.getOSCICode()});
    }
    else
    {
      this.addressee = addressee;
      this.originator = originator;
    }
  }

  ResponseToPartialFetchDelivery(DialogHandler dh)
  {
    super(dh);
    originator = ((Originator)dh.getClient());
    messageType = RESPONSE_TO_PARTIAL_FETCH_DELIVERY;
  }

  /**
   * Liefert den Payload der paketierten Nachricht zurück. Bei einer paketierten Nachricht wird eine
   * ResponseToFetchDelivery Nachricht in mehreren Bestandteilen abgeholt. Im zurückgegebenen InputStream ist
   * der in dieser Nachricht enthaltene Bestandteil.
   *
   * @return InputStream Payload der paketierten Nachricht
   * @throws OSCICipherException OSCI spezifische Fehler
   * @throws IOException Bei Ein- / Ausgabefehlern
   */
  public InputStream getChunkBlob() throws OSCICipherException, IOException

  {
    ContentContainer coco = (ContentContainer)super.contentContainer.get("ChunkContentContainer");
    Attachment att = coco.getContents()[0].getAttachment();
    return att.getStream();
  }

  /**
   * Setzt den Payload der paketierten Nachricht
   *
   * @param chunkAttachment Attachment des Chunk
   * @throws OSCIRoleException Im Fehlerfall
   * @throws NoSuchAlgorithmException Im Fehlerfall
   * @throws IOException Im Fehlerfall
   */
  void setChunkBlob(Attachment chunkAttachment)
    throws OSCIRoleException, NoSuchAlgorithmException, IOException
  {
    ContentContainer container = new ContentContainer();
    container.setRefID("ChunkContentContainer");
    chunkAttachment.setBase64Encoding(false);
    Content content = new Content(chunkAttachment);
    content.setRefID("ChunkContent");
    container.addContent(content);
    super.addContentContainer(container);
  }

  /**
   * Setzt den Payload der paketierten Nachricht
   *
   * @param chunkAttachment Attachment des Chunk
   * @throws OSCIRoleException Im Fehlerfall
   * @throws NoSuchAlgorithmException Im Fehlerfall
   * @throws IOException Im Fehlerfall
   */
  void setChunkBlob(InputStream chunkBlob) throws OSCIRoleException, NoSuchAlgorithmException, IOException
  {
    ContentContainer container = new ContentContainer();
    container.setRefID("ChunkContentContainer");
    Attachment atta = new Attachment(chunkBlob, "ChunkBlobStoreDelivery");
    atta.setBase64Encoding(false);
    Content content = new Content(atta);
    content.setRefID("ChunkContent");
    container.addContent(content);
    super.addContentContainer(container);
  }


  /**
   * @return Liefert die eingestellte ChunkInformation
   */
  public ChunkInformation getChunkInformation()
  {
    return chunkInformation;
  }


  /**
   * @param chunkInformation Setzt die ChunkInformation
   */
  public void setChunkInformation(ChunkInformation chunkInformation)
  {
    this.chunkInformation = chunkInformation;
  }

  /**
   * Setzt die Nachricht zusammen
   *
   * @throws OSCIException Im Fehlerfall
   * @throws IOException Im Fehlerfall
   * @throws NoSuchAlgorithmException Im Fehlerfall
   */
  @Override
  protected void compose() throws OSCIException, IOException, NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(2, null);
    createNonIntermediaryCertificatesH();
    ByteArrayOutputStream chunkInformationXml = new ByteArrayOutputStream();
    MessagePartsFactory.writeXML(chunkInformation, chunkInformationXml);
    String selection = "";
    String msgIdElement = "";

    if (getSelectionMode() == SELECT_BY_MESSAGE_ID)
    {
      try
      {
        selection = "<" + osciNSPrefix + ":fetchDelivery><" + osciNSPrefix + ":SelectionRule><" + osciNSPrefix
                    + ":MessageId>"
                    + de.osci.helper.Base64.encode(getSelectionRule().getBytes(Constants.CHAR_ENCODING))
                    + "</" + osciNSPrefix + ":MessageId></" + osciNSPrefix + ":SelectionRule></"
                    + osciNSPrefix + ":fetchDelivery>";
      }
      catch (UnsupportedEncodingException ex)
      {}
    }
    else if (getSelectionMode() == SELECT_BY_DATE_OF_RECEPTION)
      selection = "<" + osciNSPrefix + ":fetchDelivery><" + osciNSPrefix + ":SelectionRule><" + osciNSPrefix
                  + ":ReceptionOfDelivery>" + getSelectionRule() + "</" + osciNSPrefix
                  + ":ReceptionOfDelivery></" + osciNSPrefix + ":SelectionRule></" + osciNSPrefix
                  + ":fetchDelivery>";
    else
      selection = "<" + osciNSPrefix + ":fetchDelivery></" + osciNSPrefix + ":fetchDelivery>";
    if (messageId != null)
    {

      msgIdElement = "<" + osciNSPrefix + ":MessageId>"
                     + de.osci.helper.Base64.encode(messageId.getBytes(Constants.CHAR_ENCODING)) + "</"
                     + osciNSPrefix + ":MessageId>";
    }

    osciH = new OsciH(HeaderTags.responseToPartialFetchDelivery.getElementName(),
                      writeFeedBack() + selection + chunkInformationXml.toString(Constants.CHAR_ENCODING)
                                                                                  + msgIdElement,
                      osci2017NSPrefix);
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

  /**
   * Liefert die Message-ID der Nachricht.
   * @param messageId MessageId
   */
  public void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  /**
   * Liefert die Message-ID der Nachricht.
   *
   * @return Message-ID
   */
  @Override
  public String getMessageId()
  {
    return messageId;
  }
}
