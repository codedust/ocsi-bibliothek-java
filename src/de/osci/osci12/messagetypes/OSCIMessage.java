package de.osci.osci12.messagetypes;

import de.osci.helper.ISO8601DateTimeFormat;
import de.osci.helper.CustomHeaderHelper;
import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.MessagePart;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.OSCISignature;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;
import de.osci.osci12.roles.Role;
import de.osci.osci12.signature.OSCISignatureException;
import de.osci.osci12.soapheader.ControlBlockH;
import de.osci.osci12.soapheader.CustomHeader;
import de.osci.osci12.soapheader.DesiredLanguagesH;
import de.osci.osci12.soapheader.FeatureDescriptionH;
import de.osci.osci12.soapheader.IntermediaryCertificatesH;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesH;
import de.osci.osci12.soapheader.OsciH;
import de.osci.osci12.soapheader.QualityOfTimestampH;
import eu.osci.ws._2014._10.transport.MessageMetaData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Die Klasse ist die Superklasse aller OSCI-Nachrichten-Objekte.
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
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public abstract class OSCIMessage
{

  private static Log log = LogFactory.getLog(OSCIMessage.class);

  public Hashtable<String, byte[]> hashableMsgPart;

  public Hashtable<String, String> parsedMsgPartsIds;
  // private String signatureAlgorithm = DialogHandler.getSignatureAlgorithm();

  private String digestAlgorithm = DialogHandler.getDigestAlgorithm();

  /** Konstante, die einen undefinierten Nachrichtentyp anzeigt. */
  public static final int TYPE_UNDEFINED = 0;

  /** Konstante, die einen Annahmeauftrag anzeigt. */
  public static final int ACCEPT_DELIVERY = 0x01;

  /** Konstante, die einen Dialogendeauftrag anzeigt. */
  public static final int EXIT_DIALOG = 0x02;

  /** Konstante, die einen Abbholauftrag anzeigt. */
  public static final int FETCH_DELIVERY = 0x03;

  /** Konstante, die einen Abbholauftrag anzeigt. */
  public static final int PARTIAL_FETCH_DELIVERY = 0x32;

  /** Konstante, die einen Laufzettelabholauftrag anzeigt. */
  public static final int FETCH_PROCESS_CARD = 0x04;

  /** Konstante, die einen Weiterleitungsauftrag anzeigt. */
  public static final int FORWARD_DELIVERY = 0x05;

  /** Konstante, die einen MessageId-Anforderungsauftrag anzeigt. */
  public static final int GET_MESSAGE_ID = 0x06;

  /** Konstante, die einen Dialoginitialisierungsauftrag anzeigt. */
  public static final int INIT_DIALOG = 0x07;

  /** Konstante, die einen Abwicklungsauftrag anzeigt. */
  public static final int MEDIATE_DELIVERY = 0x08;

  /** Konstante, die einen Bearbeitungsauftrag anzeigt. */
  public static final int PROCESS_DELIVERY = 0x09;

  /** Konstante, die einen Zustellungsauftrag anzeigt. */
  public static final int STORE_DELIVERY = 0x0A;

  /** Konstante, die einen Partial Zustellungsauftrag anzeigt. */
  public static final int PARTIAL_STORE_DELIVERY = 0x0B;

  /** Konstante, die eine Annahmeantwort anzeigt. */
  public static final int RESPONSE_TO_ACCEPT_DELIVERY = 0x10;

  /** Konstante, die eine Dialogendeantwort anzeigt. */
  public static final int RESPONSE_TO_EXIT_DIALOG = 0x20;

  /** Konstante, die eine Abbholantwort anzeigt. */
  public static final int RESPONSE_TO_FETCH_DELIVERY = 0x30;

  /** Konstante, die eine Partial Abbholantwort anzeigt. */
  public static final int RESPONSE_TO_PARTIAL_FETCH_DELIVERY = 0x31;

  /** Konstante, die eine Laufzettelabholantwort anzeigt. */
  public static final int RESPONSE_TO_FETCH_PROCESS_CARD = 0x40;

  /** Konstante, die eine Weiterleitungsantwort anzeigt. */
  public static final int RESPONSE_TO_FORWARD_DELIVERY = 0x50;

  /** Konstante, die eine MessageId-Anforderungsantwort anzeigt. */
  public static final int RESPONSE_TO_GET_MESSAGE_ID = 0x60;

  /** Konstante, die eine Dialoginitialisierungsantwort anzeigt. */
  public static final int RESPONSE_TO_INIT_DIALOG = 0x70;

  /** Konstante, die eine Abwicklungsantwort anzeigt. */
  public static final int RESPONSE_TO_MEDIATE_DELIVERY = 0x80;

  /** Konstante, die eine Bearbeitungsantwort anzeigt. */
  public static final int RESPONSE_TO_PROCESS_DELIVERY = 0x90;

  /** Konstante, die eine Zustellungsantwort anzeigt. */
  public static final int RESPONSE_TO_STORE_DELIVERY = 0xA0;

  /** Konstante, die eine Zustellungsantwort anzeigt. */
  public static final int RESPONSE_TO_PARTIAL_STORE_DELIVERY = 0xA1;

  /** Konstante, die eine verschlüsselte SOAP-Nachricht anzeigt. */
  public static final int SOAP_MESSAGE_ENCRYPTED = 0x100;

  /** Konstante, die eine Rückmeldung auf Nachrichtenebene (SOAP-Fault) anzeigt. */
  public static final int SOAP_FAULT_MESSAGE = 0xB0;

  /** Kein Auswahlmodus gesetzt. */
  public static final int NO_SELECTION_RULE = -1;

  /** Auswahlmodus für Nachrichten/Laufzettel nach Message-ID. */
  public static final int SELECT_BY_MESSAGE_ID = 0;

  /** Auswahlmodus für Nachrichten/Laufzettel nach Empfangsdatum. */
  public static final int SELECT_BY_DATE_OF_RECEPTION = 1;

  /** Auswahlmodus für Nachrichten/Laufzettel nach Datum der letzten Modifikation. */
  public static final int SELECT_BY_RECENT_MODIFICATION = 2;

  /** Auswahlmodus für alle Laufzettel (default). */
  public static final int SELECT_ALL = -1;

  /** Auswahlmodus für Laufzettel von Nachrichten an den Absender eines Laufzettelabholauftrags. */
  public static final int SELECT_ADDRESSEE = 0;

  /** Auswahlmodus für Laufzettel von Nachrichten vom Absender eines Laufzettelabholauftrags. */
  public static final int SELECT_ORIGINATOR = 1;

  protected String messageId;

  /** Content-Id der Nachricht. */
  public static final String contentID = "osci@message";

  protected Originator originator;

  protected Addressee addressee;

  Hashtable<String, Author> otherAutors = new Hashtable<String, Author>();

  Hashtable<String, Reader> otherReaders = new Hashtable<String, Reader>();

  /** Enthält die ContentContainer der Nachricht. */
  public Hashtable<String, ContentContainer> contentContainer = new Hashtable<String, ContentContainer>();

  /** Enthält die EncryptedData-Objekte der Nachricht. */
  public Hashtable<String, EncryptedDataOSCI> encryptedData = new Hashtable<String, EncryptedDataOSCI>();

  /** Enthält die Attachment-Objekte der Nachricht. */
  public Hashtable<String, Attachment> attachments = new Hashtable<String, Attachment>();

  protected XMLReader xmlReader = null;

  protected String currentElement;

  protected Hashtable<String, String> xmlns = new Hashtable<String, String>();

  protected int messageType = 0;

  /** Signatureintrag im Header (Client- oder Suppliersignatur. */
  public OSCISignature signatureHeader;

  /** DesiredLanguages-Header */
  public DesiredLanguagesH desiredLanguagesH; // Kommt zwar nur in Requests vor, kann so aber in sign()
                                              // abgefragt werden.

  /** QualityOfTimestampCreation-Headereintrag */
  public QualityOfTimestampH qualityOfTimestampTypeCreation;

  /** QualityOfTimestampReception-Headereintrag */
  public QualityOfTimestampH qualityOfTimestampTypeReception;

  OsciH osciH;

  /** NonIntermediaryCertificates-Headereintrag */
  public NonIntermediaryCertificatesH nonIntermediaryCertificatesH;

  /** IntermediaryCertificates-Headereintrag */
  public IntermediaryCertificatesH intermediaryCertificatesH;

  /** ControlBlock-Headereintrag, nur bei geparsten Nachrichten zugewiesen. */
  public ControlBlockH controlBlock;

  Body body = null;

  /** Verwendeter DialogHandler */
  public DialogHandler dialogHandler;

  /* Signaturzertifikat */
  X509Certificate signerCert;

  int stateOfMsg = 0;

  protected static final int STATE_COMPOSED = 0x01;

  protected static final int STATE_SIGNED = 0x02;

  protected static final int STATE_PARSED = 0x04;

  protected Vector<CustomHeader> customHeaders = new Vector<CustomHeader>();

  public String soapNSPrefix = Constants.Namespaces.SOAP.getPrefix();

  public String osciNSPrefix = Constants.Namespaces.OSCI.getPrefix();

  public String osci2017NSPrefix = Constants.Namespaces.OSCI2017.getPrefix();

  public String osci2019NSPrefix = Constants.Namespaces.OSCI128.getPrefix();

  public String dsNSPrefix = Constants.Namespaces.XML_SIG.getPrefix();

  public String xencNSPrefix =  Constants.Namespaces.XML_ENC.getPrefix();

  public String xsiNSPrefix = Constants.Namespaces.XML_SCHEMA.getPrefix();


  // String bodyId = "body";
  public String ns = " xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:osci=\"http://www.osci.de/2002/04/osci\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";

  protected String boundary_string;

  protected boolean base64 = false; // default = false since 08.03.2023

  protected Vector<MessagePart> messageParts = new Vector<MessagePart>();

  protected FeatureDescriptionH featureDescription= null;




  /**
   * Konstruktor for the OSCIMessage object
   */
  protected OSCIMessage()
  {}

  /**
   * Konstruktor für ein OSCIMessage-Objekt
   *
   * @param dh DialogHandler
   */
  protected OSCIMessage(DialogHandler dh)
  {
    dialogHandler = dh;
  }

  /**
   * Durchsucht die Nachrichtensignatur nach den verwendeten Algorithmen. Es wird true zurückgegeben, wenn
   * Referenzen der XML-Signatur oder die Signatur selbst mit Algorithmen erzeugt wurden, die zu dem
   * übergebenen Prüfzeitpunkt als unsicher eingestuft wurden. Wird als Prüfzeitpunkt null übergeben, wird
   * (unabhängig von Fristen) auf die Verwendung von Algorithmen geprüft, die zum Zeitpunkt der
   * Veröffentlichung dieser Implementierung als unsicher eingestuft wurden.
   *
   * @param date Prüfzeitpunkt
   * @return true, wenn unsichere Algorithmen zur Signatur verwendet wurden, andernfalls false
   * @throws IllegalStateException falls die Nachricht nicht signiert ist
   * @throws OSCIRoleException wenn beim Zugriff auf das Signatur-Rollenobjekt (z.B. Zertifikat) ein Problem
   *           auftritt
   */
  public boolean hasWeakSignature(Date date) throws OSCIRoleException
  {
    if (signatureHeader == null)
      throw new IllegalStateException("Message is not signed.");

    if (signerCert == null)
      throw new IllegalStateException("Role object referenced in signature not found.");

    if (date == null)
      date = Constants.ACTUAL_DATE;

    if (Crypto.isWeak(date, signerCert))
    {
      return true;
    }

    if (Constants.OUT_DATES.containsKey(signatureHeader.signatureAlgorithm)
        && !date.before((Date)Constants.OUT_DATES.get(signatureHeader.signatureAlgorithm)))
      return true;
    String[] digMeths = (String[])signatureHeader.getDigestMethods().values().toArray(new String[0]);
    for ( int i = 0 ; i < digMeths.length ; i++ )
      if (Constants.OUT_DATES.containsKey(digMeths[i])
          && !date.before((Date)Constants.OUT_DATES.get(digMeths[i])))
        return true;

    return false;

  }

  /**
   * Liefert Status der Nachricht (signiert/unsigniert)
   *
   * @return signierte Nachricht, true
   * @see DialogHandler#setCheckSignatures(boolean)
   */
  public boolean isSigned()
  {
    return (signatureHeader != null);
  }

  /**
   * <p>
   * Diese Methode ermöglicht es Anwendungen, zusätzliche Zertifikate in den
   * NonIntermediaryCertificates-Header einzustellen, die dann vom Intermediär mit geprüft werden. Die
   * Zertifikate werden in Form von Reader- oder Author-Objekten übergeben, die die entsprechenden Zertifikate
   * enthalten müssen.
   * </p>
   * Die Methoden ContentContainer.sign(Role) und EncryptedData.encrypt(Role) fügen die übergebenen
   * Rollenobjekte der Nachricht automatisch hinzu, so dass diese Methode in der Regel nicht benötigt wird.
   *
   * @param role Hinzuzufügendes Reader- oder Author-Objekt
   * @throws IllegalArgumentException undocumented
   * @see de.osci.osci12.messageparts.ContentContainer#sign(Role)
   * @see de.osci.osci12.messageparts.EncryptedDataOSCI#encrypt(Role)
   */
  public void addRole(Role role) throws IllegalArgumentException
  {
    if (role instanceof Originator)
      originator = (Originator)role;
    else if (role instanceof Addressee)
      addressee = (Addressee)role;
    else
    {
      if ((role instanceof Author) && (!otherAutors.containsValue(role)))
      {
        if (otherAutors.containsKey(role.id))
        {
          // Role.id wird nur noch intern verwendet
          role.cipherRefId = role.getCipherCertificateId();
          role.signatureRefId = role.getSignatureCertificateId();
          role.id += '0';
        }

        otherAutors.put(role.id, (Author)role);
      }
      else if ((role instanceof Reader) && (!otherReaders.containsValue(role)))
      {
        if (otherReaders.containsKey(role.id))
        {
          // Role.id wird nur noch intern verwendet
          role.cipherRefId = role.getCipherCertificateId();
          role.id += '0';
        }

        otherReaders.put(role.id, (Reader)role);
      }
    }
  }

  /**
   * <p>
   * Diese Methode ermöglicht es Anwendungen, zusätzliche Autoren und Leser aus einem anderen (z.B. geparsten)
   * NonIntermediaryCertificates-Header in den vorhandenen NonIntermediaryCertificates-Header einzustellen,
   * die dann vom Intermediär mit geprüft werden. Die Zertifikate werden in Form von Reader- oder
   * Author-Objekten übergeben, die die entsprechenden Zertifikate enthalten müssen.
   * </p>
   *
   * @param nonIntermediaryCertificates Hinzuzufügendes NonIntermediaryCertificates-Header-Objekt
   * @throws IllegalArgumentException undocumented
   * @see de.osci.osci12.messageparts.ContentContainer#sign(Role)
   * @see de.osci.osci12.messageparts.EncryptedDataOSCI#encrypt(Role)
   */
  public void addReadersAndAuthors(NonIntermediaryCertificatesH nonIntermediaryCertificates)
  {
    for ( Reader reader : nonIntermediaryCertificates.getCipherCertificatesOtherReaders() )
    {
      if (log.isDebugEnabled())
      {
        log.debug("Ein weiteres Leser-Verschlüsselungs-Objekt wird der Nachricht hinzugefügt.");
      }
      addRole(reader);
    }

    for ( Author author : nonIntermediaryCertificates.getSignatureCertificatesOtherAuthors() )
    {
      if (log.isDebugEnabled())
      {
        log.debug("Ein weiteres Autor-Signatur-Objekt wird der Nachricht hinzugefügt.");
      }
      addRole(author);
    }

    for ( Author author : nonIntermediaryCertificates.getCipherCertificatesOtherAuthors() )
    {
      if (log.isDebugEnabled())
      {
        log.debug("Ein weiteres Autor-Verschlüsselungs-Objekt wird der Nachricht hinzugefügt.");
      }
      addRole(author);
    }
  }

  void addContentContainer(ContentContainer container) throws OSCIRoleException
  {
    if (log.isDebugEnabled())
      log.debug("ContentContainer wird hinzugefügt.");

    // übernehmen der Role Objekte
    contentContainer.put(container.getRefID(), container);

    Role[] roles = container.getRoles();

    for ( int i = 0 ; i < roles.length ; i++ )
    {
      if (log.isDebugEnabled())
        log.debug("Ein weiteres Role-Objekt wird der Nachricht hinzugefügt.");

      if (roles[i] instanceof Originator)
      {
        if ((this.originator != null) && (this.originator != roles[i]))
          throw new OSCIRoleException("incompatible_role_error", "Originator");

        if (log.isDebugEnabled())
          log.debug("Eine Originator wird hinzugefügt.");

        this.originator = (Originator)roles[i];
      }
      else if (roles[i] instanceof Addressee)
      {
        if ((this.addressee != null) && (this.addressee != roles[i]))
          throw new OSCIRoleException("incompatible_role_error", "Addressee");

        this.addressee = (Addressee)roles[i];

        if (log.isDebugEnabled())
          log.debug("Eine Addressee wird hinzugefügt.");

        break;
      }

      if (roles[i] instanceof Author)
      {
        if (log.isDebugEnabled())
          log.debug("Eine Author wird hinzugefügt.\n" + roles[i].id + "\n"
                    + roles[i].getSignatureCertificateId());

        // this.otherAutors.put(roles[i].id, roles[i]);
        addRole(roles[i]);
      }

      if (roles[i] instanceof de.osci.osci12.roles.Reader)
      {
        if (log.isDebugEnabled())
          log.debug("Eine Reader wird hinzugefügt.");

        // this.otherReaders.put(roles[i].id, roles[i]);
        addRole(roles[i]);
      }
    }

    // Übernahme der Attachments
    Attachment[] atts = container.getAttachments();

    if ((atts != null) && (atts.length > 0))
    {
      for ( int i = 0 ; i < atts.length ; i++ )
        this.addAttachment(atts[i]);
    }
  }

  /**
   * Füge der Nachricht ein gegebenes {@link MessageMetaData}-Objekt als {@link CustomHeader} hinzu.
   *
   * @param mmd
   */
  void addMessageMetaDataXTA2V3(final MessageMetaData mmd)
  {
    addCustomHeader(CustomHeaderHelper.getMessageMetaDataAsCustomHeader(mmd));
  }

  /**
   * Füge der Nachricht ein gegebenes {@link MessageMetaData}-Objekt in Form einer Zeichenkette als
   * {@link CustomHeader} hinzu.
   *
   * @param mmd
   */
  void addMessageMetaDataXTA2V3(final String mmd)
  {
    addCustomHeader(CustomHeaderHelper.getMessageMetaDataStringAsCustomHeader(mmd));
  }



  /**
   * Liefert {@link MessageMetaData} aus CustomHeader-Liste, sofern vorhanden.
   *
   * @return MessageMetaData
   * @see #addMessageMetaDataXTA2V3(MessageMetaData)
   */
  MessageMetaData getMessageMetaDataXTA2V3()
  {
    for ( CustomHeader customHeader : customHeaders )
    {
      if (CustomHeaderHelper.MESSAGE_META_DATA_ID.equals(customHeader.getRefID()))
      {
        log.debug("Found MessageMetaData-CustomHeader");
        return CustomHeaderHelper.readMessageMetaDataFromOsciString(customHeader.getData());
      }
    }
    return null;
  }

  void removeContentContainer(ContentContainer container)
  {
    contentContainer.remove(container.getRefID());
  }

  ContentContainer[] getContentContainer()
  {
    return ((ContentContainer[])contentContainer.values().toArray(new ContentContainer[0]));
  }

  /**
   * Durchsucht <b>die unverschlüsselten</b> Inhaltsdaten nach dem ContentContainer mit der übergebenen RefID.
   *
   * @param refID zu suchende RefID
   * @return den zugehörigen ContentContainer oder null, wenn die Referenz nicht gefunden wurde.
   */
  protected ContentContainer getContentContainerByRefID(String refID)
  {
    ContentContainer[] containers = getContentContainer();
    MessagePart ret = null;

    for ( int i = 0 ; i < containers.length ; i++ )
    {
      if ((ret = searchMessagePart(containers[i], refID)) != null)
      {
        if (ret instanceof ContentContainer)
          return (ContentContainer)ret;
        else
        {
          log.info("RefID " + refID + " does not refer to a ContentContainer.");

          return null;
        }
      }
    }

    log.info("RefID " + refID + " not found.");

    return null;
  }

  /**
   * Durchsucht <b>die unverschlüsselten</b> ContentContainer nach dem Content mit der übergebenen RefID.
   *
   * @param refID zu suchende RefID
   * @return den zugehörigen Content oder null, wenn die Referenz nicht gefunden wurde.
   */
  public Content getContentByRefID(String refID)
  {
    ContentContainer[] containers = getContentContainer();
    MessagePart ret = null;

    for ( int i = 0 ; i < containers.length ; i++ )
    {
      if ((ret = searchMessagePart(containers[i], refID)) != null)
      {
        if (ret instanceof Content)
          return (Content)ret;
        else
        {
          log.info("RefID " + refID + " does not refer to a Content.");

          return null;
        }
      }
    }

    log.info("RefID " + refID + " not found.");

    return null;
  }

  public FeatureDescriptionH getFeatureDescription()
  {
    return featureDescription;
  }


  public void setFeatureDescription(FeatureDescriptionH featureDescription)
  {
    this.featureDescription = featureDescription;
  }
  private MessagePart searchMessagePart(ContentContainer cnt, String refID)
  {
    if (cnt.getRefID().equals(refID))
      return cnt;

    Content[] contents = cnt.getContents();

    for ( int i = 0 ; i < contents.length ; i++ )
    {
      if (contents[i].getRefID().equals(refID))
        return contents[i];

      if (contents[i].getContentType() == Content.CONTENT_CONTAINER)
      {
        MessagePart mp = searchMessagePart(contents[i].getContentContainer(), refID);

        if (mp != null)
          return mp;
      }
    }

    return null;
  }

  void addEncryptedData(EncryptedDataOSCI encData) throws OSCIRoleException
  {
    if ((encData.getRefID() == null) || encData.getRefID().equals(""))
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + " refId");

    encryptedData.put(encData.getRefID(), encData);

    // übernehmen der Role Objekte
    Role[] roles = encData.getRoles();

    for ( int i = 0 ; i < roles.length ; i++ )
    {
      if (roles[i] instanceof Originator)
      {
        if ((this.originator != null) && (this.originator != roles[i]))
          throw new OSCIRoleException("incompatible_role_error", "Originator");

        this.originator = (Originator)roles[i];
      }
      else if (roles[i] instanceof Addressee)
      {
        if ((this.addressee != null) && (this.addressee != roles[i]))
          throw new OSCIRoleException("incompatible_role_error", "Addressee");

        this.addressee = (Addressee)roles[i];
      }

      if (roles[i] instanceof Author)
        // this.otherAutors.put(roles[i].id, roles[i]);
        addRole(roles[i]);

      if (roles[i] instanceof de.osci.osci12.roles.Reader)
        // this.otherReaders.put(roles[i].id, roles[i]);
        addRole(roles[i]);
    }

    // Übernahme der Attachments
    Attachment[] atts = encData.getAttachments();

    for ( int i = 0 ; i < atts.length ; i++ )
      this.addAttachment(atts[i]);
  }

  void removeEncryptedData(EncryptedDataOSCI encData)
  {
    encryptedData.remove(encData.getRefID());
  }

  /**
   * Verschlüsselt die Nachricht auf Nachrichtenebene
   *
   * @return Verschlüsselte Transportdaten
   */
  EncryptedDataOSCI[] getEncryptedData()
  {
    return ((de.osci.osci12.messageparts.EncryptedDataOSCI[])encryptedData.values()
                                                                          .toArray(new de.osci.osci12.messageparts.EncryptedDataOSCI[0]));
  }

  /**
   * Interne Methode. Attachments werden automatisch einer Nachricht hinzugefügt, wenn der referenzierende
   * Content eingestellt wird. Diese Methode sollte von Anwendungen daher normalerweise nicht aufgerufen
   * werden.
   *
   * @param attachment Attachment
   * @see de.osci.osci12.messageparts.ContentContainer#addContent(de.osci.osci12.messageparts.Content)
   */
  void addAttachment(Attachment attachment)
  {
    if (attachment == null)
    {
      log.debug("Attachment to add is null!");
      return;
    }

    log.debug("Add attachment to msg: " + attachment.getRefID());

    attachments.put(attachment.getRefID(), attachment);

    if (boundary_string != null)
      attachment.setBoundary(boundary_string);
  }

  /**
   * Interne Methode. Attachments werden automatisch aus einer Nachricht entfernt, wenn der referenzierende
   * Content entfernt wird. Diese Methode sollte von Anwendungen daher normalerweise nicht aufgerufen werden.
   *
   * @param attachment Attachment
   * @see de.osci.osci12.messageparts.ContentContainer#removeContent(de.osci.osci12.messageparts.Content)
   */
  void removeAttachment(Attachment attachment)
  {
    attachments.remove(attachment.getRefID());
  }

  Attachment[] getAttachments()
  {
    return ((Attachment[])attachments.values().toArray(new Attachment[0]));
  }

  private boolean cmpRoles(Role org, Role next)
  {
    try
    {
      if (org.hasCipherCertificate())
      {
        if (next.hasCipherCertificate() && !org.getCipherCertificate().equals(next.getCipherCertificate()))
          return false;
      }
      else if (next.hasCipherCertificate())
        org.setCipherCertificate(next.getCipherCertificate());

      if (org.hasSignatureCertificate())
      {
        if (next.hasSignatureCertificate()
            && !org.getSignatureCertificate().equals(next.getSignatureCertificate()))
          return false;
      }
      else if (next.hasSignatureCertificate())
        org.setSignatureCertificate(next.getSignatureCertificate());

      return true;
    }
    catch (OSCIRoleException ex)
    {
      return false;
    }
  }

  private void importCertificates(Role[] roles) throws OSCIRoleException
  {
    for ( int i = 0 ; i < roles.length ; i++ )
    {
      if ((roles[i] instanceof Originator) && (originator != null) && (!cmpRoles(originator, roles[i])))
        throw new OSCIRoleException("incompatible_role_error", "Originator");
      else if ((roles[i] instanceof Addressee) && (addressee != null) && !cmpRoles(addressee, roles[i]))
        throw new OSCIRoleException("incompatible_role_error", "Addressee");
      else if (roles[i] instanceof de.osci.osci12.roles.Reader)
        // otherReaders.put(roles[i].id, roles[i]);
        addRole(roles[i]);
      else if (roles[i] instanceof Author)
        // otherAutors.put(roles[i].id, roles[i]);
        addRole(roles[i]);
    }
  }

  /**
   * undocumented
   *
   * @param coco undocumented
   * @throws OSCIRoleException undocumented
   */
  protected void collectCertificatesFromContentContainer(ContentContainer coco) throws OSCIRoleException
  {
    importCertificates(coco.getRoles());

    Content[] cnt = coco.getContents();

    for ( int i = 0 ; i < cnt.length ; i++ )
    {
      if (cnt[i].getContentType() == Content.CONTENT_CONTAINER)
        collectCertificatesFromContentContainer(cnt[i].getContentContainer());
    }

    EncryptedDataOSCI[] enc = coco.getEncryptedData();

    for ( int i = 0 ; i < enc.length ; i++ )
    {
      importCertificates(enc[i].getRoles());
    }
  }

  /**
   * undocumented
   *
   * @throws OSCIRoleException undocumented
   */
  protected void importAllCertificates() throws OSCIRoleException
  {
    for ( ContentContainer coco : contentContainer.values() )
    {
      collectCertificatesFromContentContainer(coco);
    }

    for ( EncryptedDataOSCI data : encryptedData.values() )
    {
      importCertificates(data.getRoles());
    }
  }

  /**
   * Setzt den Hash-Algorithmus für die Signatur der Nachrichten (Voreinstellung aus DialogHandler).
   *
   * @param _digestAlgorithm Hashalgorithmus-XML-Attribut
   * @see DialogHandler#setDigestAlgorithm(String)
   */
  public void setDigestAlgorithm(String _digestAlgorithm)
  {
    if (isSigned())
      throw new IllegalStateException("Message is already signed.");
    this.digestAlgorithm = _digestAlgorithm;
  }

  /**
   * undocumented
   *
   * @param role undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws de.osci.osci12.common.OSCICancelledException undocumented
   * @throws java.security.NoSuchAlgorithmException undocumented
   * @throws OSCISignatureException undocumented
   */
  protected void sign(Role role) throws IOException, OSCIException,
    de.osci.osci12.common.OSCICancelledException, java.security.NoSuchAlgorithmException
  {
    if ((stateOfMsg & STATE_COMPOSED) == 0)
      compose();

    String name = "";

    if (this instanceof OSCIRequest)
      name = "<" + osciNSPrefix + ":ClientSignature Id=\"clientsignature\" " + soapNSPrefix
             + ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soapNSPrefix
             + ":mustUnderstand=\"1\">";
    else
      name = "<" + osciNSPrefix + ":SupplierSignature Id=\"suppliersignature\" " + soapNSPrefix
             + ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soapNSPrefix
             + ":mustUnderstand=\"1\">";

    signatureHeader = MessagePartsFactory.createOSCISignature(name);
    signatureHeader.setNSPrefixes(this);
    signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(dialogHandler.getControlblock(),
                                                                              digestAlgorithm));

    if (desiredLanguagesH != null)
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(desiredLanguagesH,
                                                                                digestAlgorithm));

    // nur signieren, wenn mitgesendet wird
    if (featureDescription != null && dialogHandler.isSendFeatureDescription())
    {
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(featureDescription,
                                                                                             digestAlgorithm));
    }


    if (qualityOfTimestampTypeCreation != null)
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(qualityOfTimestampTypeCreation,
                                                                                digestAlgorithm));

    if (qualityOfTimestampTypeReception != null)
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(qualityOfTimestampTypeReception,
                                                                                digestAlgorithm));

    if (osciH != null)
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(osciH, digestAlgorithm));

    for ( int i = 0 ; i < customHeaders.size() ; i++ )
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(((CustomHeader)customHeaders.get(i)),
                                                                                digestAlgorithm));

    if (attachments.size() > 0)
    {
      Enumeration<Attachment> atts = attachments.elements();
      Attachment att;
      byte[] tmp = null;

      while (atts.hasMoreElements())
      {
        // workaround für das Problem, dass Attachments im Header verschlüsselt signiert werden.
        att = (Attachment)atts.nextElement();
        tmp = null;

        if (att.hasDigestValue(digestAlgorithm))
          tmp = att.getDigestValue(digestAlgorithm);

        MessagePartsFactory.setDigestValue(att,
                                           att.getEncryptedDigestValue(digestAlgorithm),
                                           digestAlgorithm);
        signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(att, digestAlgorithm));

        if (tmp != null)
          MessagePartsFactory.setDigestValue(att, tmp, digestAlgorithm);
      }
    }

    if (role instanceof Intermed)
    {
      if (intermediaryCertificatesH == null)
        intermediaryCertificatesH = new IntermediaryCertificatesH();

      intermediaryCertificatesH.setSignatureCertificateIntermediary((Intermed)role);
    }
    else
    {
      if (nonIntermediaryCertificatesH == null)
        nonIntermediaryCertificatesH = new NonIntermediaryCertificatesH();

      if (role instanceof Originator)
        nonIntermediaryCertificatesH.setSignatureCertificateOriginator((Originator)role);
      else
        nonIntermediaryCertificatesH.setSignatureCertificateAddressee((Addressee)role);
    }

    if (intermediaryCertificatesH != null)
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(intermediaryCertificatesH,
                                                                                digestAlgorithm));

    if (nonIntermediaryCertificatesH != null)
      signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(nonIntermediaryCertificatesH,
                                                                                digestAlgorithm));

    signatureHeader.addSignatureReference(MessagePartsFactory.createOSCISignatureReference(body, digestAlgorithm));

    try
    {
      signatureHeader.sign(role);
    }
    catch (SignatureException ex)
    {
      log.warn("Error:",ex);
      throw new OSCISignatureException("signature_creation_failed");
    }

    messageParts.set(1, signatureHeader);
  }

  // Diese Methode fügt alle in den Role-Objekten auffindbaren Zertifikate zum non-Intermed Header hinzu
  protected void createNonIntermediaryCertificatesH() throws de.osci.osci12.roles.OSCIRoleException
  {
    if ((stateOfMsg & STATE_PARSED) != 0)
      return;

    boolean empty = true;

    if (nonIntermediaryCertificatesH == null)
      nonIntermediaryCertificatesH = new NonIntermediaryCertificatesH();
    else
      empty = false;

    if (originator != null)
    {
      if (originator.hasSignatureCertificate())
      {
        nonIntermediaryCertificatesH.setSignatureCertificateOriginator(originator);
        empty = false;
      }
      else
        log.info("Kein Originator-Signaturzertifikat gefunden");

      if (originator.hasCipherCertificate())
      {
        nonIntermediaryCertificatesH.setCipherCertificateOriginator(originator);
        empty = false;
      }
      else
        log.info("Kein Originator-Verschlüsselungszertifikat gefunden");

      // empty = false;
    }

    Vector<Role> v;

    if ((v = searchCertificates(false, getOtherAuthors())).size() > 0)
      empty = false;

    nonIntermediaryCertificatesH.setCipherCertificatesOtherAuthors(v.toArray(new Author[v.size()]));

    if (addressee != null)
    {
      if (addressee.hasCipherCertificate())
      {
        nonIntermediaryCertificatesH.setCipherCertificateAddressee(addressee);
        empty = false;
      }
      else
        log.info("Kein Addressee-Verschlüsselungszertifikat gefunden");
    }

    if ((v = searchCertificates(false, getOtherReaders())).size() > 0)
      empty = false;

    nonIntermediaryCertificatesH.setCipherCertificatesOtherReaders(v.toArray(new Reader[v.size()]));

    if ((v = searchCertificates(true, getOtherAuthors())).size() > 0)
      empty = false;

    nonIntermediaryCertificatesH.setSignatureCertificatesOtherAuthors(v.toArray(new Author[v.size()]));

    if (empty)
      nonIntermediaryCertificatesH = null;
  }

  private Vector<Role> searchCertificates(boolean signature, Role[] roles)
  {
    Vector<Role> v = new Vector<Role>();

    for ( int i = 0 ; i < roles.length ; i++ )
    {
      if ((signature && roles[i].hasSignatureCertificate())
          || (!signature && roles[i].hasCipherCertificate()))
        v.add(roles[i]);
    }

    return v;
  }

  /**
   * Sets the dialogHandler attribute of the OSCIMessage object
   *
   * @param dialogHandler The new dialogHandler value
   */
  void setDialogHandler(DialogHandler dialogHandler)
  {
    this.dialogHandler = dialogHandler;
  }

  /**
   * Liefert den DialogHandler des Nachrichtenobjektes.
   *
   * @return den DialogHandler
   */
  public DialogHandler getDialogHandler()
  {
    return dialogHandler;
  }

  /**
   * Liefert den Identifier für den Nachrichtentyp (ACCEPT_DELIVERY, EXIT_DIALOG...).
   *
   * @return den Messagetyp-Identifier
   */
  public int getMessageType()
  {
    return messageType;
  }

  /**
   * Diese Methode liefert ein Role-Objekt passend zu der übergebenem RefID.
   *
   * @return Role Objekt oder Null
   * @param refId RefID
   */
  public Role getRoleForRefID(String refId)
  {

    for ( Author author : otherAutors.values() )
    {
      if (checkRole(refId, author))
        return author;
    }

    for ( Reader reader : otherReaders.values() )
    {
      if (checkRole(refId, reader))
        return reader;
    }

    if (checkRole(refId, getOriginator()))
      return getOriginator();

    if (checkRole(refId, getAddressee()))
      return getAddressee();

    if (checkRole(refId, dialogHandler.getSupplier()))
      return dialogHandler.getSupplier();

    if (checkRole(refId, dialogHandler.getClient()))
      return dialogHandler.getClient();

    return null;
  }

  private boolean checkRole(String uri, Role role)
  {
    try
    {
      if (uri.equals(role.getCipherCertificateId()))
        return true;
      else if (uri.equals(role.getSignatureCertificateId()))
        return true;
    }
    catch (NullPointerException ex)
    {}

    return false;
  }

  /**
   * Liefert das Addressee-Rollenobjekt des Nachrichtenobjektes.
   *
   * @return den Addressee
   */
  public Addressee getAddressee()
  {
    return addressee;
  }

  /**
   * Setzt das Addressee-Rollenobjekt des Nachrichtenobjektes.
   *
   * @param addressee Addresse
   * @see #getAddressee()
   */
  void setAddressee(Addressee addressee)
  {
    this.addressee = addressee;
  }

  /**
   * Liefert das Originator-Rollenobjekt des Nachrichtenobjektes.
   *
   * @return den Originator
   */
  public Originator getOriginator()
  {
    return originator;
  }

  /**
   * Setzt das Originator-Rollenobjekt des Nachrichtenobjektes.
   *
   * @param originator Originator
   * @see #getOriginator()
   */
  void setOriginator(Originator originator)
  {
    this.originator = originator;
  }

  /**
   * Liefert alle Author-Rollenobjekte, die für die Signatur von Inhaltsdaten in der Nachricht verwendet
   * wurden oder die mit der Methode addRole(Role) der Nachricht hinzugefügt wurden.
   *
   * @return Array von Author-Objekten
   * @see #addRole(Role)
   * @see ContentContainer#sign(Role)
   */
  public Author[] getOtherAuthors()
  {
    return otherAutors.values().toArray(new Author[0]);
  }

  /**
   * Liefert alle Reader-Rollenobjekte, für die Inhaltsdaten in der Nachricht verschlüsselt wurden oder die
   * mit der Methode addRole(Role) der Nachricht hinzugefügt wurden.
   *
   * @return Array von Reader-Objekten
   * @see #addRole(Role)
   * @see EncryptedDataOSCI#encrypt(Role)
   */
  public Reader[] getOtherReaders()
  {
    return otherReaders.values().toArray(new Reader[0]);
  }

  /**
   * Mit dieser Methode können beliebige Einträge dem SOAP-Header der Nachricht hinzugefügt werden. Die
   * übergebenen Strings müssen vollständige XML-Tags sein. Das unterste Element muss ein Id-Attribut
   * besitzen, welches innerhalb der Nachricht eindeutig sein muss. Die Bibliothek verwendet für die
   * Id-Attribute die Namen der Einträge (ohne Namespace) in Kleinschreibung, es sollten daher beispielsweise
   * nicht "desiredlanguages", "clientsignature" oder "body" verwendet werden. Bei zu signierenden Nachrichten
   * muß der Tag außerdem in kanonischer Form übergeben werden. Der äußerste Tag muß alle
   * Namespace-Deklarationen der OSCI-Nachricht enthalten (für diesen Fall
   * siehe auch {@link #addCustomHeader(String, boolean)}).
   *
   * @param xml XML-Tag
   * @see #getCustomHeaders()
   */
  public void addCustomHeader(String xml)
  {
    if (log.isDebugEnabled())
      log.debug("Custom-Header: " + xml);

    customHeaders.add(new CustomHeader(xml));
  }


  /**
   * Mit dieser Methode können beliebige Einträge dem SOAP-Header der Nachricht hinzugefügt werden. Zusätzlich
   * kann hier mit dem Setzen des zweiten Parameters "makeOsciStructure" auf "true" automatisch die
   * XML-Struktur des Headers für zu signierende Nachrichten erweitert werden. D.h. die
   * Namespace-Deklarationen der OSCI-Nachricht werden angehängt und die Struktur kanonisiert.
   * Die übergebenen Strings müssen vollständige XML-Tags sein. Das unterste Element muss ein
   * Id-Attribut besitzen, welches innerhalb der Nachricht eindeutig sein muss. Die Bibliothek verwendet für
   * die Id-Attribute die Namen der Einträge (ohne Namespace) in Kleinschreibung, es sollten daher
   * beispielsweise nicht "desiredlanguages", "clientsignature" oder "body" verwendet werden.
   *
   * @param xml               XML-Tag
   * @param makeOsciStructure XML-Struktur für OSCI-Nachrichtensignatur vervollständigen?
   * @see #getCustomHeaders()
   */
  public void addCustomHeader(String xml, boolean makeOsciStructure)
  {
    if (!makeOsciStructure)
    {
      addCustomHeader(xml);
    }
    else
    {
      if (log.isDebugEnabled())
        log.debug("Custom-Header vor Kanonisierung: " + xml);

      CustomHeader canonizedHeader = new CustomHeader(CustomHeaderHelper.makeOSCICustomHeader(xml));

      if (log.isDebugEnabled())
        log.debug("Custom-Header nach Kanonisierung: " + canonizedHeader.getData());

      customHeaders.add(canonizedHeader);
    }
  }

  /**
   * Liefert vorhandene SOAP-Header-Einträge.
   *
   * @return Array der SOAP-Header-Einträge
   * @see #addCustomHeader(String)
   */
  public String[] getCustomHeaders()
  {
    String[] headers = new String[customHeaders.size()];

    for ( int i = 0 ; i < headers.length ; i++ )
      headers[i] = customHeaders.get(i).getData();

    return headers;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented protected void writeCustomSOAPHeaders(OutputStream out) throws
   *           IOException { if (customHeaders.size() > 0) { for (int i = 0; i < customHeaders.size(); i++) {
   *           log.debug("\n\nWRITE CUSTOM-HEADER: " + ((CustomHeader) customHeaders.get(i)).getData());
   *           out.write(((CustomHeader) customHeaders.get(i)).getData().getBytes(Constants.CHAR_ENCODING)); }
   *           } }
   */
  /**
   * Helfermethode zum Formatieren eines Datums in das für OSCI erforderliche ISO-8601-Format.
   *
   * @param date Datum-Objekt
   * @return formatierten Datum-String
   */
  public static String formatISO8601(Date date)
  {
    return new ISO8601DateTimeFormat().format(date);
  }

  /**
   * Helfermethode zum Parsen eines Datumsstrings im ISO-8601-Format. ISO-8601-Format.
   *
   * @param date Datum-String
   * @return Datum-Objekt
   * @throws ParseException bei Parse-Problemen
   */
  public static Date parseISO8601(String date) throws ParseException
  {
    return new ISO8601DateTimeFormat().parse(date);
  }

  /**
   * Legt fest, ob der äußere verschlüsselte Transportumschlag Base64-codiert übertragen wird.
   *
   * @param b64 true, Daten werden Base64-codiert
   */
  public void setBase64Encoding(boolean b64)
  {
    base64 = b64;
  }

  /**
   * Gibt an, ob der äußere verschlüsselte Transportumschlag Base64-codiert übertragen wird / wurde.
   *
   * @return true Daten sind Base64-codiert
   */
  public boolean getBase64Encoding()
  {
    return base64;
  }

  /**
   * undocumented
   *
   * @return undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  protected long calcLength() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    if ((stateOfMsg & STATE_COMPOSED) == 0)
      compose();

    long len = 431;
    len += ((attachments.keySet().size() + 3) * boundary_string.getBytes(Constants.CHAR_ENCODING).length);
    len += contentID.getBytes(Constants.CHAR_ENCODING).length;
    len += (4 * soapNSPrefix.getBytes(Constants.CHAR_ENCODING).length);
    len += ns.getBytes(Constants.CHAR_ENCODING).length;
    len += getClass().getName()
                     .substring(getClass().getName().lastIndexOf('.') + 1)
                     .getBytes(Constants.CHAR_ENCODING).length;

    for ( Attachment att : attachments.values() )
    {
      len += 87;
      len += att.getContentType().getBytes(Constants.CHAR_ENCODING).length;
      len += att.getRefID().getBytes(Constants.CHAR_ENCODING).length;
      len += att.getLength();
      len += Long.toString(att.getLength()).getBytes(Constants.CHAR_ENCODING).length;
      len += (MessagePartsFactory.attachmentIsBase64(att) ? 6 : 5);
    }

    for ( int i = 0 ; i < messageParts.size() ; i++ )
    {
      if (messageParts.get(i) != null)
        len += ((MessagePart)messageParts.get(i)).getLength();
    }

    log.debug("Laenge der Nachricht: " + len);

    return len;
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  protected void compose() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    if (boundary_string == null)
      boundary_string = DialogHandler.boundary + "_" + Tools.createRandom(24);

    messageParts.clear();

    if (controlBlock != null)
      messageParts.add(controlBlock);
    else
      messageParts.add(dialogHandler.getControlblock());

    // maybe null
    messageParts.add(signatureHeader);

    for ( Attachment att : attachments.values() )
    {
      att.setBoundary(boundary_string);
    }
  }

  void writeXML(OutputStream out) throws IOException, de.osci.osci12.OSCIException, NoSuchAlgorithmException
  {
    if ((stateOfMsg & STATE_COMPOSED) == 0)
      compose();

    // Auf Anregung v. MS \r\n rausgenommen
    // out.write(("\r\nMIME-Version: 1.0\r\nContent-Type: Multipart/Related; boundary=" +
    // DialogHandler.boundary +
    out.write(("MIME-Version: 1.0\r\nContent-Type: Multipart/Related; boundary=" + boundary_string
               + "; type=text/xml\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\n--" + boundary_string
               + "\r\nContent-Type: text/xml; charset=UTF-8\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("Content-Transfer-Encoding: 8bit\r\nContent-ID: <" + contentID
               + ">\r\n\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<").getBytes(Constants.CHAR_ENCODING));
    out.write(soapNSPrefix.getBytes(Constants.CHAR_ENCODING));
    out.write(":Envelope".getBytes(Constants.CHAR_ENCODING));
//    if(this instanceof PartialStoreDelivery || this instanceof PartialFetchDelivery|| this instanceof ResponseToPartialStoreDelivery || this instanceof ResponseToPartialFetchDelivery)
//    {
//      out.write(ns2017.getBytes(Constants.CHAR_ENCODING));
//    }
//    else
//    {
      out.write(ns.getBytes(Constants.CHAR_ENCODING));
//    }
    out.write(" xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ soap".getBytes(Constants.CHAR_ENCODING));
    out.write((getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1)
               + ".xsd http://www.w3.org/2000/09/xmldsig# oscisig.xsd http://www.w3.org/2001/04/xmlenc# oscienc.xsd\"><").getBytes(Constants.CHAR_ENCODING));
    out.write(soapNSPrefix.getBytes(Constants.CHAR_ENCODING));
    out.write(":Header>".getBytes(Constants.CHAR_ENCODING));

    /*
     * // Empfangene/geparste, erneut serialisierte Nachrichten mit richtigen NS-Prefixes erhalten if
     * (controlBlock != null) de.osci.osci12.messageparts.MessagePartsFactory.writeXML(controlBlock, out);
     * else de.osci.osci12.messageparts.MessagePartsFactory.writeXML(dialogHandler.getControlblock(), out);
     */
    for ( int i = 0 ; i < (messageParts.size() - 1) ; i++ )
    {
      if (messageParts.get(i) != null)
        MessagePartsFactory.writeXML(messageParts.get(i), out);
    }

    out.write(("</" + soapNSPrefix + ":Header>").getBytes(Constants.CHAR_ENCODING));
    MessagePartsFactory.writeXML((Body)messageParts.lastElement(), out);
    out.write(("</" + soapNSPrefix + ":Envelope>").getBytes(Constants.CHAR_ENCODING));

    for ( Attachment att : attachments.values() )
    {
      MessagePartsFactory.writeXML(att, out);
    }

    out.write(("\r\n--" + boundary_string + "--\r\n").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  @Override
  public String toString()
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try
    {
      writeXML(out);
      out.close();

      return new String(out.toByteArray(), Constants.CHAR_ENCODING);
    }
    catch (Exception ex)
    {
      // kann nicht vorkommen
      return "";
    }
  }
}
