package de.osci.osci12.soapheader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.osci.helper.Base64InputStream;
import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;
import de.osci.osci12.roles.Role;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


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
public class NonIntermediaryCertificatesHBuilder extends de.osci.osci12.messageparts.MessagePartParser
{
  private static Log log = LogFactory.getLog(NonIntermediaryCertificatesHBuilder.class);
  private static final int CIPHER_CERTIFICATE_ORIGINATOR = 0;
  private static final int CIPHER_CERTIFICATE_OTHER_AUTHOR = 1;
  private static final int CIPHER_CERTIFICATE_ADDRESSEE = 2;
  private static final int CIPHER_CERTIFICATE_OTHER_READER = 3;
  private static final int SIGNATURE_CERTIFICATE_ORIGINATOR = 4;
  private static final int SIGNATURE_CERTIFICATE_OTHER_AUTHOR = 5;
  private static final int SIGNATURE_CERTIFICATE_ADDRESSEE = 6;

  // für das Array relevant sind die oben aufgeführten statischen Variablen wie z.B. CIPHER_CERTIFICATE_ORIGINATOR
  private int[] check;
  private NonIntermediaryCertificatesH nic = null;
  private int typ;
  private Originator originator;
  private Addressee addressee;
  private Vector<Reader> reader = new Vector<Reader>();
  private Vector<Author> authors = new Vector<Author>();
  private String tmpId;
  private String refId;
  private int msgTyp;
  private boolean changeOrgsAndAdds = false;


  /**
   *  Constructor for the NonIntermediaryCertificatesHBuilder object
   *
   *@param  parentHandler             Description of Parameter
   *@param  atts                      Description of Parameter
   *@param  check                     Description of Parameter
   * @throws SAXException
   *@exception  CertificateException  Description of Exception
   */
  public NonIntermediaryCertificatesHBuilder(OSCIMessageBuilder parentHandler, Attributes atts, int[] check) throws SAXException
  {
    super(parentHandler);
    parentHandler.addFoundMsgPartIds(atts.getValue("Id"), HeaderTags.NonIntermediaryCertificates.getNamespace().getUri() +":"+HeaderTags.NonIntermediaryCertificates.getElementName());
    this.check = check;
    nic = new NonIntermediaryCertificatesH();

    if (atts.getValue("Id") != null)
      nic.setRefID(atts.getValue("Id"));

    msgTyp = msg.getMessageType();

    if ((msgTyp == OSCIMessage.RESPONSE_TO_FETCH_DELIVERY) || (msgTyp == OSCIMessage.RESPONSE_TO_PROCESS_DELIVERY) ||
        (msgTyp == OSCIMessage.RESPONSE_TO_MEDIATE_DELIVERY)|| (msgTyp == OSCIMessage.RESPONSE_TO_PARTIAL_FETCH_DELIVERY))
      changeOrgsAndAdds = true;

    OSCIMessage msg = parentHandler.getOSCIMessage();
    nic.setNSPrefixes(msg);
  }


  /**
   * For parsing without surrounding message: creates a new EncryptedDataBuilder object without parent handler
   *
   * @param xmlReader
   * @throws SAXException
   */
  public NonIntermediaryCertificatesHBuilder(XMLReader xmlReader) throws SAXException
  {
    super(xmlReader, new OSCIMessageBuilder(null));

    msgTyp = OSCIMessage.TYPE_UNDEFINED; // no surrounding OSCI message

    // allow all message types (same as in StoredMessageLoader)
    check = new int[]{-1, -1, -1, -1, -1, -1, -1};

    nic = new NonIntermediaryCertificatesH();
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
      log.debug("Start-Element: " + localName);

    if (localName.equals("CipherCertificateOriginator") && uri.equals(OSCI_XMLNS))
      typ = CIPHER_CERTIFICATE_ORIGINATOR;
    else if (localName.equals("CipherCertificateOtherAuthor") && uri.equals(OSCI_XMLNS))
      typ = CIPHER_CERTIFICATE_OTHER_AUTHOR;
    else if (localName.equals("CipherCertificateAddressee") && uri.equals(OSCI_XMLNS))
      typ = CIPHER_CERTIFICATE_ADDRESSEE;
    else if (localName.equals("CipherCertificateOtherReader") && uri.equals(OSCI_XMLNS))
      typ = CIPHER_CERTIFICATE_OTHER_READER;
    else if (localName.equals("SignatureCertificateOriginator") && uri.equals(OSCI_XMLNS))
      typ = SIGNATURE_CERTIFICATE_ORIGINATOR;
    else if (localName.equals("SignatureCertificateOtherAuthor") && uri.equals(OSCI_XMLNS))
      typ = SIGNATURE_CERTIFICATE_OTHER_AUTHOR;
    else if (localName.equals("SignatureCertificateAddressee") && uri.equals(OSCI_XMLNS))
      typ = SIGNATURE_CERTIFICATE_ADDRESSEE;
    else if (localName.equals("X509Certificate") && uri.equals(DS_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if (!(localName.equals("X509Data") && uri.equals(DS_XMLNS))
             && !(localName.equals(HeaderTags.NonIntermediaryCertificates.getElementName())))
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }

    if (attributes.getValue("Id") != null)
    {
      refId = attributes.getValue("Id");

      if (refId.indexOf("_") >= 0)
        tmpId = refId.substring(0, refId.indexOf('_'));
    }

    if (check[typ] == 0)
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
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
      log.debug("End-Element: " + localName);

    if (ParserHelper.isElement(HeaderTags.NonIntermediaryCertificates,localName,uri))
    {
      for (int i = 0; i < check.length; i++)
      {
        if (check[i] > 0)
          throw new SAXException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": " + localName);
      }

      try
      {
        nic.setCipherCertificatesOtherReaders(reader.toArray(new Reader[0]));

        Vector<Author> vec = new Vector<Author>();

        for (int i = 0; i < authors.size(); i++)
        {
          if (authors.get(i).hasCipherCertificate())
            vec.add(authors.get(i));
        }

        nic.setCipherCertificatesOtherAuthors(vec.toArray(new Author[0]));
        vec = new Vector<Author>();

        for (int i = 0; i < authors.size(); i++)
        {
          if (((Role) authors.get(i)).hasSignatureCertificate())
            vec.add(authors.get(i));
        }

        nic.setSignatureCertificatesOtherAuthors(vec.toArray(new Author[0]));

        if (msg != null)
        {
          msg.addRole(originator);
          msg.addRole(addressee);


          if (msgTyp == OSCIMessage.RESPONSE_TO_ACCEPT_DELIVERY)
          {
            if (addressee != null)
              msg.dialogHandler.supplier = addressee;
          }
          else if (msgTyp == OSCIMessage.RESPONSE_TO_PROCESS_DELIVERY)
          {
            if (originator != null)
            {
              if (originator.hasSignatureCertificate())
                msg.dialogHandler.getSupplier().setSignatureCertificate(originator.getSignatureCertificate());

              if (originator.hasCipherCertificate())
                msg.dialogHandler.getSupplier().setCipherCertificate(originator.getCipherCertificate());
            }
          }
          else if (!changeOrgsAndAdds && (msgTyp != OSCIMessage.ACCEPT_DELIVERY) && (msgTyp
                                                                                                    != OSCIMessage.PROCESS_DELIVERY)
                   && (originator != null))
          {
            msg.dialogHandler.client = originator;
          }

          for ( int i = 0; i < authors.size(); i++ )
            msg.addRole((Author)authors.get(i));


          for ( int i = 0; i < reader.size(); i++ )
            msg.addRole(reader.get(i));

          msg.nonIntermediaryCertificatesH = nic;
        }

        this.xmlReader.setContentHandler(this.parentHandler);
      }
      catch (OSCIRoleException ex)
      {
        log.error("Fehler im End-Element", ex);
        throw new SAXException(ex);
      }
    }
    else if (localName.equals("X509Certificate") && uri.equals(DS_XMLNS))
    {
      int i;
      X509Certificate cert;

      try
      {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(currentElement.toString().getBytes(de.osci.osci12.common.Constants.CHAR_ENCODING));
        Base64InputStream base64In = new Base64InputStream(byteStream);
        cert = de.osci.helper.Tools.createCertificate(base64In);

        if (typ == CIPHER_CERTIFICATE_ORIGINATOR)
        {
          if (msg != null && !changeOrgsAndAdds && (msg.dialogHandler.getClient() instanceof Originator))
          {
            originator = (Originator)msg.dialogHandler.getClient();
          }

          if (originator != null)
          {
            if (!originator.hasCipherCertificate())
              originator.setCipherCertificate(cert);
            else if (!originator.getCipherCertificate().equals(cert))
              throw new IllegalStateException(DialogHandler.text.getString("not_matching_cipher_certs") +
                                              " Originator");
          }
          else
            originator = new Originator((X509Certificate) null, cert);

          if (tmpId != null)
            originator.id = tmpId;

          originator.cipherRefId = refId;
          nic.setCipherCertificateOriginator(originator);
          check[CIPHER_CERTIFICATE_ORIGINATOR] = 0;
        }
        else if (typ == CIPHER_CERTIFICATE_OTHER_AUTHOR)
        {
          Author au = new Author((X509Certificate) null, cert);

          if (tmpId != null)
            au.id = tmpId;

          au.cipherRefId = refId;
          authors.add(au);
          if(msg != null)
          {
            msg.addRole(au);
          }
        }
        else if (typ == CIPHER_CERTIFICATE_ADDRESSEE)
        {
          if (msg != null && !changeOrgsAndAdds && (msg.dialogHandler.getClient() instanceof Addressee))
            addressee = (Addressee)msg.dialogHandler.getClient();

          if (addressee != null)
          {
            if (addressee.hasCipherCertificate())
              addressee.setCipherCertificate(cert);
            else if (!addressee.getCipherCertificate().equals(cert))
              throw new IllegalStateException(DialogHandler.text.getString("not_matching_cipher_certs") + " Addressee");
          }
          else
            addressee = new Addressee((X509Certificate) null, cert);

          if (tmpId != null)
            addressee.id = tmpId;

          addressee.cipherRefId = refId;
          nic.setCipherCertificateAddressee(addressee);
          check[CIPHER_CERTIFICATE_ADDRESSEE] = 0;
        }
        else if (typ == CIPHER_CERTIFICATE_OTHER_READER)
        {
          de.osci.osci12.roles.Reader rd = new de.osci.osci12.roles.Reader(cert);

          if (tmpId != null)
            rd.id = tmpId;

          rd.cipherRefId = refId;
          reader.add(rd);
          if(msg != null)
          {
            msg.addRole(rd);
          }
        }
        else if (typ == SIGNATURE_CERTIFICATE_ORIGINATOR)
        {
          if (originator == null)
          {
            if (msg != null && !changeOrgsAndAdds && (msg.dialogHandler.getClient() instanceof Originator))
            {
              originator = (Originator) msg.dialogHandler.getClient();
              originator.setSignatureCertificate(cert);
            }
            else
              originator = new Originator(cert, (X509Certificate) null);
          }
          else
            originator.setSignatureCertificate(cert);

          if (tmpId != null)
            originator.id = tmpId;

          originator.signatureRefId = refId;
          nic.setSignatureCertificateOriginator(originator);
          check[SIGNATURE_CERTIFICATE_ORIGINATOR] = 0;
        }
        else if (typ == SIGNATURE_CERTIFICATE_OTHER_AUTHOR)
        {
          for (i = 0; i < authors.size(); i++)
          {
            if (authors.get(i).id.equals(tmpId))
            {
              try
              {
                // Test, ob bereits ein Author-Objekt mit dieser Vorwahl mit einem Signaturzertifikat
                // versehen wurde (sollte nur beim Import von Rollen aus anderen Nachrichten vorkommen)
                authors.get(i).getSignatureCertificate();

                // Role.id wird nur noch intern verwendet
                if (tmpId != null)
                  tmpId += '0';
              }
              catch (OSCIRoleException ex)
              {
                authors.get(i).setSignatureCertificate(cert);
                authors.get(i).signatureRefId = refId;

                break;
              }
            }
          }

          if (i == authors.size())
          {
            Author au = new Author(cert, (X509Certificate) null);

            if (tmpId != null)
              au.id = tmpId;

            au.signatureRefId = refId;
            authors.add(au);
            if(msg != null)
            {
              msg.addRole(au);
            }
          }
        }
        else if (typ == SIGNATURE_CERTIFICATE_ADDRESSEE)
        {
          if (addressee == null)
          {
            addressee = new Addressee(cert, (X509Certificate) null);
          }
          else
            addressee.setSignatureCertificate(cert);

          if (tmpId != null)
            addressee.id = tmpId;

          addressee.signatureRefId = refId;
          nic.setSignatureCertificateAddressee(addressee);
          check[SIGNATURE_CERTIFICATE_ADDRESSEE] = 0;
        }
      }
      catch (Exception ex)
      {
        log.error("Fehler im End-Element", ex);
        throw new SAXException(ex);
      }

      tmpId = null;
    }

    typ = -1;
    currentElement = null;
  }


  /**
   * Gib das {@link NonIntermediaryCertificatesH}-Objekt zurück (sofern es bereits geparsed wurde).
   *
   * @return
   */
  public NonIntermediaryCertificatesH getNonIntermediaryCertificatesH()
  {
    return nic;
  }



  /**
   * Parse {@link NonIntermediaryCertificatesH} element directly from XML representation given as byte array.
   *
   * @param xmlBytes
   * @return
   * @throws SAXException
   */
  public static NonIntermediaryCertificatesH createFromXmlBytes(byte[] xmlBytes) throws SAXException
  {
    try
    {
      SAXParser parser = ParserHelper.getNewSAXParser();
      XMLReader reader = parser.getXMLReader();
      ParserHelper.setFeatures(reader);

      NonIntermediaryCertificatesHBuilder nicBuilder = new NonIntermediaryCertificatesHBuilder(reader);

      parser.parse(new InputSource(new ByteArrayInputStream(xmlBytes)), nicBuilder);
      return nicBuilder.getNonIntermediaryCertificatesH();
    }
    catch (IOException | ParserConfigurationException ex)
    {
      log.error("Allgemeiner Fehler beim Parsen des eingelesenen NonIntermediaryCertificatesH-Elements");
      throw new SAXException(ex);
    }
  }
}
