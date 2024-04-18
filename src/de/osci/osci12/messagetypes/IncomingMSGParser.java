package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Hashtable;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.osci.helper.Base64InputStream;
import de.osci.helper.Canonizer;
import de.osci.helper.MIMEParser;
import de.osci.helper.MIMEPartInputStream;
import de.osci.helper.ParserHelper;
import de.osci.helper.StoreInputStream;
import de.osci.helper.SymCipherInputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.OSCIFeatures;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.common.SoapClientException;
import de.osci.osci12.common.SoapServerException;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.encryption.OSCICipherException;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.OSCISignature;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Role;
import de.osci.osci12.soapheader.FeatureDescriptionH;


/**
 * Streamparser
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann / N. Büngener
 * @version 2.4.1
 */
abstract class IncomingMSGParser
{

  private static Log log = LogFactory.getLog(IncomingMSGParser.class);

  protected static Role[] defaultSupplier;

  protected int searchPointer;

  /**
   * Constructor for the IncomingMSGParser object
   */
  public IncomingMSGParser()
  {}

  abstract OSCIEnvelopeBuilder getParser(XMLReader reader, DialogHandler dh);

  OSCIMessage parse(InputStream is, DialogHandler dh, StoreInputStream storeInStream)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    XMLReader reader = null;
    OSCIEnvelopeBuilder builder = null;

    try
    {
      javax.xml.parsers.SAXParserFactory saxFactory = javax.xml.parsers.SAXParserFactory.newInstance();
      saxFactory.setNamespaceAware(true);
      saxFactory.setValidating(false);

      SAXParser parser = saxFactory.newSAXParser();
      reader = parser.getXMLReader();
      ParserHelper.setFeatures(reader);
      builder = getParser(reader, dh);
      reader.setContentHandler(builder);
      reader.setErrorHandler(builder);
      builder.hashNCanStream = new Canonizer(is, storeInStream);

      if (log.isDebugEnabled())
        log.debug("Aktivierung des SAXParsers. ");

      reader.parse(new InputSource(builder.hashNCanStream));

      if (builder.hashNCanStream.getCanException() != null)
        throw new IOException();
    }
    catch (SAXException ex)
    {
      if ((ex.getException() != null) && (ex.getException() instanceof IllegalStateException))
        throw (IllegalStateException)ex.getException();

      if ((ex.getException() != null) && (ex.getException() instanceof OSCIException))
        throw (OSCIException)ex.getException();
      else
      {
        log.error("", ex);

        if (ex instanceof SAXParseException)
          log.error("\nSPALTE: " + ((SAXParseException)ex).getColumnNumber() + "\nZEILE: "
                    + ((SAXParseException)ex).getLineNumber(),
                    ex);

        throw new SoapClientException(OSCIErrorCodes.OSCIMsgStructureNotValid);
      }
    }
    catch (FactoryConfigurationError | ParserConfigurationException ex)
    {
      throw new IllegalStateException(ex);
    }
    catch (IOException ex)
    {
      // Falls der kanonisierende Thread eine Exception geworfen hat,
      // sollte hier eine IOException mit der Meldung "Write end dead." kommen.
      Exception e = builder.hashNCanStream.getCanException();

      if (e != null)
      {
        if (e instanceof SAXException)
        {
          if ((((SAXException)e).getException() != null)
              && (((SAXException)e).getException() instanceof OSCIException))
            throw (OSCIException)((SAXException)e).getException();
          else
            throw new SoapClientException(OSCIErrorCodes.OSCIMsgStructureNotValid);
        }
        else
          throw (IOException)e;
      }
      else
        throw ex;
    }
    finally
    {
      // workaround f. jdk1.5-Bug #6219755
      if (null != builder)
      {
        final byte[] tmp = new byte[64];
        while (builder.hashNCanStream.read(tmp) > -1)
        {
          // nothing to do
        }
      }
    }

    return builder.childBuilder.msg;
  }

  OSCIMessage parseStream(InputStream in, DialogHandler dial, boolean request, OutputStream storeStream)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    return parseStream(in, dial, request, false, storeStream);
  }

  OSCIMessage parseStream(InputStream in,
                          DialogHandler dial,
                          boolean request,
                          boolean decryptedStream,
                          OutputStream storeStream)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    OSCIMessage msg = null;
    MIMEParser incomingMsg = null;
    MIMEPartInputStream mimeStream = null;

    if (log.isDebugEnabled())
      log.debug("Starte Parser: " + in.available());

    StoreInputStream sis = null;

    if (storeStream != null)
    {
      sis = new StoreInputStream(in, storeStream);
      incomingMsg = new MIMEParser(sis);
    }
    else
      incomingMsg = new MIMEParser(in);

    mimeStream = incomingMsg.getNextStream();
    msg = parse(mimeStream, dial, sis);
    msg.boundary_string = incomingMsg.boundary;

    if (log.isDebugEnabled())
      log.debug("Fertig mit Parsen des Transport Objektes. Msgtype: " + msg.getClass().toString());

    if (msg.getMessageType() == OSCIMessage.SOAP_MESSAGE_ENCRYPTED)
    {
      try
      {
        de.osci.osci12.encryption.EncryptedData ed = ((SOAPMessageEncrypted)msg).encData;
        mimeStream = incomingMsg.getNextStream();

        String s = null;

        if (log.isDebugEnabled())
          log.debug("Mime ID:  " + mimeStream.getContentID() + " Encrypted ID: "
                    + ed.getCipherData().getCipherReference().getURI());

        if (!(s = ("cid:" + mimeStream.getContentID())).equals(ed.getCipherData()
                                                                 .getCipherReference()
                                                                 .getURI()))
          throw new IllegalArgumentException(DialogHandler.text.getString("msg_format_error") + s);

        de.osci.osci12.roles.Role role = null;
        java.security.cert.X509Certificate cert = null;

        if (dial == null)
        {
          role = null;

          for ( int i = 0 ; i < defaultSupplier.length ; i++ )
          {
            if (defaultSupplier[i].getCipherCertificate()
                                  .equals(ed.getKeyInfo().getEncryptedKeys()[0].getKeyInfo()
                                                                               .getX509Data()
                                                                               .getX509Certificate()))
            {
              role = defaultSupplier[i];

              break;
            }
          }

          if (role == null)
            throw new SoapClientException(OSCIErrorCodes.NoEncKeyPresentOnMessgeLevel);
        }
        else
        {
          try
          {
            if (request)
              cert = dial.getSupplier().getCipherCertificate();
            else
              cert = dial.getClient().getCipherCertificate();

            if (cert.equals(ed.getKeyInfo().getEncryptedKeys()[0].getKeyInfo()
                                                                 .getX509Data()
                                                                 .getX509Certificate()))
            {
              if (request)
                role = dial.getSupplier();
              else
                role = dial.getClient();
            }
          }
          catch (OSCIRoleException ex)
          {
            // wird unten abgefangen
          }
        }

        if (role == null)
          throw new OSCIRoleException("no_private_key");

        if (!ed.getKeyInfo().getEncryptedKeys()[0].getEncryptionMethodAlgorithm()
                                                  .equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5)
            && !ed.getKeyInfo().getEncryptedKeys()[0].getEncryptionMethodAlgorithm()
                                                     .equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP))
          throw new NoSuchAlgorithmException(DialogHandler.text.getString("encryption_algorithm_not_supported")
                                             + ed.getKeyInfo()
                                                 .getEncryptedKeys()[0].getEncryptionMethodAlgorithm());

        if ((!Constants.JCA_JCE_MAP.containsKey(ed.getEncryptionMethodAlgorithm())))
          throw new NoSuchAlgorithmException(DialogHandler.text.getString("encryption_algorithm_not_supported")
                                             + ed.getEncryptionMethodAlgorithm());

        InputStream inKey = ed.getKeyInfo().getEncryptedKeys()[0].getCipherData()
                                                                 .getCipherValue()
                                                                 .getCipherValueStream();
        inKey.reset();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int anz = 0;
        byte[] bytes = new byte[256];

        while ((anz = inKey.read(bytes)) > -1)
        {
          bos.write(bytes, 0, anz);
        }

        inKey.close();

        byte[] decryptedKey;
        if (ed.getKeyInfo().getEncryptedKeys()[0].getEncryptionMethodAlgorithm()
                                                 .equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP))
          decryptedKey = role.getDecrypter()
                             .decrypt(bos.toByteArray(),
                                      ed.getKeyInfo().getEncryptedKeys()[0].mgfAlgorithm,
                                      ed.getKeyInfo().getEncryptedKeys()[0].digestAlgorithm);
        else
          decryptedKey = role.getDecrypter().decrypt(bos.toByteArray());
        InputStream input;

        // Zweite Bedingung f. Abwärtskompatibilität
        boolean b64 = mimeStream.getEncoding().equalsIgnoreCase("base64")
                      || mimeStream.getContentType().equalsIgnoreCase("text/base64");

        if (b64)
          input = new Base64InputStream(mimeStream);
        else
          input = mimeStream;

        String symEncMethod = ed.getEncryptionMethodAlgorithm();
        
        
        if(!ed.isIvLengthParsed())
        {
          // Sonderfall: kein IVLength-Element in der Nachricht = alte Bibliothek bzw. alter Standard 16 Byte benutzt
          ed.setIvLength(16);    
        }
          
        SymCipherInputStream cin = new SymCipherInputStream(input,
                                                            Crypto.createSymKey(decryptedKey, symEncMethod),
                                                            symEncMethod, ed.getIvLength(), false);

        if (log.isDebugEnabled())
          log.debug("#################### Encrypted OSCI-Msg wurde komplett verarbeitet, nun wird der Transportumschlag geöffnet und die eigentliche OSCI-Nachricht betrachtet ####################");

        msg = parseStream(cin, dial, request, true, storeStream);
        msg.setBase64Encoding(b64);
        msg.dialogHandler.setEncryption(true);

        FeatureDescriptionH featureDesc = msg.getFeatureDescription();

        // Wird der veraltete CBC-Padding-Modus verwendet, stelle wenn möglich auf GCM um
        if (symEncMethod.endsWith("-cbc") && featureDesc != null && featureDesc.getSupportedFeatures() != null
            && featureDesc.getSupportedFeatures().contains(OSCIFeatures.GCMPaddingModus)
            && ParserHelper.isSwitchToGCM())
        {
          if (log.isDebugEnabled())
          {
            log.debug("GCM wird in aktueller OSCI-Kommunikation unterstützt, benutze GCM für symmetrische Transportverschlüsselung");
          }

          msg.dialogHandler.setSymmetricCipherAlgorithm(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM);
        }
        else
        {
          msg.dialogHandler.setSymmetricCipherAlgorithm(symEncMethod);
        }

        msg.dialogHandler.setAsymmetricCipherAlgorithm(ed.getKeyInfo()
                                                         .getEncryptedKeys()[0].getEncryptionMethodAlgorithm());
        incomingMsg.getNextStream();

        return msg;
      }
      catch (SoapClientException ex)
      {
        throw ex;
      }
      catch (SoapServerException ex)
      {
        throw ex;
      }
      catch (IllegalStateException ex)
      {
        throw ex;
      }
      catch (OSCIException ex)
      {
        log.error("SOAP", ex);
        throw ex;
      }
      catch (Exception ex)
      {
        log.error("SOAP", ex);
        throw new SoapClientException(OSCIErrorCodes.CouldNotDecryptRequestData);
      }
      finally
      {
        // incomingMsg.getNextStream();
      }
    }
    else
    {
      // Check, ob eine Antwort auf einen verschlüsselten request auch verschlüsselt war.
      if (!request && msg.dialogHandler.isEncryption() && !decryptedStream
          && !(msg.getMessageType() == OSCIMessage.SOAP_FAULT_MESSAGE))
        throw new OSCICipherException(DialogHandler.text.getString("unencrypted_msg"));

      msg.dialogHandler.setEncryption(false);

      // es handelt sich um eine nicht Verschlüsselte Nachricht
      // die Attachments werden der Nachricht hinzugefüge
      if (log.isDebugEnabled())
        log.debug("Es handelte sich um eine nicht Verschlüsselte Nachricht");

      // ## Lesen der Attachments
      if (log.isDebugEnabled())
        log.debug("Nächster Schritt: Verarbeitung der Attachments");

      readAttachment(msg, incomingMsg);

      if (log.isDebugEnabled())
        log.debug("Die Signaturen werden überprüft.");

      // Nachrich signiert?
      if ((msg.dialogHandler.isCheckSignatures()) && (msg.signatureHeader != null))
      {
        boolean sigErg = checkMsgHashes(msg);

        if (!sigErg)
        {
          log.error("Die Signaturen der XML-OSCI-Daten sind fehlerhaft.");
          throw new OSCIErrorException(OSCIErrorCodes.SignatureInvalid, msg);
        }
      }
      else if (log.isDebugEnabled())
        log.debug("Unsignierte-Nachricht");
    }

    if (log.isDebugEnabled())
      log.debug("Alles ist Fertig " + msg.getMessageType());

    return msg;
  }

  /**
   * undocumented
   *
   * @param msg undocumented
   * @return undocumented
   * @throws OSCIErrorException undocumented
   */
  protected boolean checkMsgHashes(OSCIMessage msg) throws OSCIErrorException
  {
    try
    {
      OSCISignature sig = msg.signatureHeader;

      if (sig.getDigestMethods().containsValue(Constants.DIGEST_ALGORITHM_SHA1))
        log.info("SHA-1 used as digest algorithm for message signature.");

      if (sig.signatureAlgorithm.equals(Constants.SIGNATURE_ALGORITHM_RSA_SHA1))
        log.info("SHA-1 with RSA used as signature algorithm for message signature.");

      // #### checks that all messageParts are signed
      for ( String id : msg.parsedMsgPartsIds.keySet() )
      {
        if (!msg.hashableMsgPart.containsKey(id))
        {
          log.error("MessagePart with id:  " + id + " not hashed");
          throw new OSCIErrorException(OSCIErrorCodes.NotAllRelevantPartsSigned, msg);
        }
      }
      // ### check the signature references and parsed hashes
      Hashtable<String, byte[]> refsHash = sig.getDigests();
      // check the sizes of hashes and references
      if (refsHash.size() != msg.hashableMsgPart.size())
      {
        log.error("The number of references and hashed parts are not equil");
        return false;
      }

      for ( String key : refsHash.keySet() )
      {
        if (msg.hashableMsgPart.get(key) == null)
        {
          log.error("Element zur Signatur-Referenz '" + key + "' nicht in Nachricht gefunden.");

          return false;
        }
        else if (!MessageDigest.isEqual(msg.hashableMsgPart.get(key), refsHash.get(key)))
        {
          log.error("Hashwerte der Signatur-Referenz '" + key + "' stimmen nicht überein.");

          return false;
        }
        else
          msg.hashableMsgPart.remove(key);
      }

      /*
       * if ((msg.getMessageType() & 0x0F) > 0) // OSCIRequest-Nachricht
       * msg.hashableMsgPart.remove("#clientsignature"); else
       * msg.hashableMsgPart.remove("#suppliersignature");
       */
      if (msg.hashableMsgPart.size() > 0)
      {
        log.error("Nachricht enthält " + msg.hashableMsgPart.size() + "unsignierte Elemente.");
        throw new OSCIErrorException(OSCIErrorCodes.NotAllRelevantPartsSigned, msg);
        // return false;
      }

      java.security.Signature sg;

      if (DialogHandler.getSecurityProvider() == null)
        sg = java.security.Signature.getInstance(Constants.JCA_JCE_MAP.get(sig.signatureAlgorithm));
      else
        sg = java.security.Signature.getInstance(Constants.JCA_JCE_MAP.get(sig.signatureAlgorithm),
                                                 DialogHandler.getSecurityProvider());

      X509Certificate c;

      if (msg instanceof OSCIRequest)
        c = msg.dialogHandler.getClient().getSignatureCertificate();
      else
        c = msg.dialogHandler.getSupplier().getSignatureCertificate();

      msg.signerCert = c;

      if ((c.getKeyUsage() != null) && !c.getKeyUsage()[0] && !c.getKeyUsage()[1])
      {
        log.error("Signature certificate has wrong key usage.");
        return false;
      }

      sg.initVerify(c.getPublicKey());
      sg.update(sig.getSignedInfoBytes());

      if (log.isDebugEnabled())
        log.debug("vor check signature" + new String(sig.getSignedInfoBytes()));

      if (!sg.verify(sig.signatureValue))
        return false;

      if (log.isDebugEnabled())
        log.debug("Nach check Signature");

      return true;
    }
    catch (OSCIErrorException ex)
    {
      log.error("Es ist ein Fehler beim überprüfen der Hashwerte aufgetreten.", ex);
      throw ex;
    }
    catch (Exception ex)
    {
      log.error("Es ist ein Fehler beim überprüfen der Hashwerte aufgetreten.", ex);

      return false;
    }
  }

  /**
   * Description of the Method
   *
   * @param msg Description of Parameter
   * @param incomingMsg Description of Parameter
   */
  private void readAttachment(OSCIMessage msg, MIMEParser incomingMsg)
    throws IOException, NoSuchAlgorithmException
  {
    MIMEPartInputStream mimeStream = null;
    Attachment[] atts = msg.getAttachments();
    HashSet<String> foundAtts = new HashSet<>();

    while ((mimeStream = incomingMsg.getNextStream()) != null)
    {
      Attachment att = null;
      String refId = mimeStream.getContentID();
      if (foundAtts.contains(refId))
        throw new IllegalArgumentException(DialogHandler.text.getString("unexpected_entry") + ": " + refId);
      foundAtts.add(refId);
      boolean b64 = mimeStream.getEncoding().equalsIgnoreCase("base64")
                    || mimeStream.getContentType().equalsIgnoreCase("text/base64");

      if (log.isDebugEnabled())
        log.debug("Attachment RefId: " + refId);

      for ( int i = 0 ; i < atts.length ; i++ )
      {
        if (atts[i].getRefID().equals(refId))
        {
          if (log.isDebugEnabled())
            log.debug("Vorbereitetes Attachment gefunden. Der Stream wird nun hinzugefügt.");

          att = atts[i];
          att.setBase64Encoding(b64);
          att.setBoundary(incomingMsg.boundary);
          MessagePartsFactory.attachmentSetState(att, Attachment.STATE_OF_ATTACHMENT_PARSING, false);

          break;
        }
      }

      String digestMethod = null;
      if (msg.isSigned())
        digestMethod = msg.signatureHeader.getDigestMethods().get("cid:" + refId);

      if (att == null)
      {
        // ## Verschl?sselte Attachachments
        if (log.isDebugEnabled())
          log.debug("Verschlüsseltes Attachment gefunden.");

        if (b64)
          att = MessagePartsFactory.attachment(new Base64InputStream(mimeStream),
                                               refId,
                                               mimeStream.getLength(),
                                               digestMethod);
        else
          att = MessagePartsFactory.attachment(mimeStream, refId, mimeStream.getLength(), digestMethod);

        att.setBase64Encoding(b64);
        att.setBoundary(incomingMsg.boundary);
        msg.addAttachment(att);
        MessagePartsFactory.attachmentSetState(att, Attachment.STATE_OF_ATTACHMENT_ENCRYPTED, true);
      }
      else
      {
        // ## Attachments wurden der Nachricht bereits hinzugef?gt
        if (log.isDebugEnabled())
          log.debug("Unverschlüsseltes Attachment gefunden.");

        if (b64)
          MessagePartsFactory.attachmentSetStream(att,
                                                  new Base64InputStream(mimeStream),
                                                  false,
                                                  mimeStream.getLength(),
                                                  digestMethod);
        else
          MessagePartsFactory.attachmentSetStream(att,
                                                  mimeStream,
                                                  false,
                                                  mimeStream.getLength(),
                                                  digestMethod);
      }

      if (log.isDebugEnabled())
        log.debug("Es wurde ein Attachment hinzugefügt!RefID: " + att.getRefID());

      att.setContentType(mimeStream.getContentType());
      att.setMimeHeaders(mimeStream.mime_headers);
      if (msg.isSigned())
        msg.hashableMsgPart.put("cid:" + att.getRefID(), att.getEncryptedDigestValue(digestMethod));
    }
  }
}
