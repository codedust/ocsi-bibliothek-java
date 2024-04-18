package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import de.osci.osci12.common.Constants;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;


/**
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
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class NonIntermediaryCertificatesH extends CertificateH
{

  // private static Log log = LogFactory.getLog(NonIntermediaryCertificatesH.class);
  private Originator cipherCertificateOriginator;

  private Author[] cipherCertificatesOtherAuthors;

  private Addressee cipherCertificateAddressee;

  private Reader[] cipherCertificatesOtherReaders;

  private Originator signatureCertificateOriginator;

  private Addressee signatureCertificateAddressee;

  private Author[] signatureCertificatesOtherAuthors;

  /**
   * Creates a new NonIntermediaryCertificatesH object.
   */
  public NonIntermediaryCertificatesH()
  {}

  /**
   * undocumented
   *
   * @param cipherCertificateAddressee undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setCipherCertificateAddressee(Addressee cipherCertificateAddressee) throws OSCIRoleException
  {
    certificates.put(cipherCertificateAddressee.getCipherCertificateId(),
                     cipherCertificateAddressee.getCipherCertificate());
    this.cipherCertificateAddressee = cipherCertificateAddressee;
  }

  /**
   * undocumented
   *
   * @param cipherCertificateOriginator undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setCipherCertificateOriginator(Originator cipherCertificateOriginator) throws OSCIRoleException
  {
    certificates.put(cipherCertificateOriginator.getCipherCertificateId(),
                     cipherCertificateOriginator.getCipherCertificate());
    this.cipherCertificateOriginator = cipherCertificateOriginator;
  }

  /**
   * undocumented
   *
   * @param cipherCertificatesOtherAuthors undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setCipherCertificatesOtherAuthors(Author[] cipherCertificatesOtherAuthors)
    throws OSCIRoleException
  {
    Hashtable<String, Author> hs = new Hashtable<String, Author>();

    for ( int i = 0 ; i < cipherCertificatesOtherAuthors.length ; i++ )
    {
      // Nach Import von Rollen aus anderen Nachrichten könnte es vorkommen,
      // dass verschiedene Authors gleiche Ids haben.
      if (hs.containsKey(cipherCertificatesOtherAuthors[i].getCipherCertificateId()))
      {
        if (!hs.get(cipherCertificatesOtherAuthors[i].getCipherCertificateId())
               .getCipherCertificate()
               .equals(cipherCertificatesOtherAuthors[i].getCipherCertificate()))
        {
          throw new OSCIRoleException("id_conflict_cipher_author",
                                      cipherCertificatesOtherAuthors[i].getCipherCertificateId());
        }
      }
      else
      {
        hs.put(cipherCertificatesOtherAuthors[i].getCipherCertificateId(), cipherCertificatesOtherAuthors[i]);
        certificates.put(cipherCertificatesOtherAuthors[i].getCipherCertificateId(),
                         cipherCertificatesOtherAuthors[i].getCipherCertificate());
      }
    }

    this.cipherCertificatesOtherAuthors = hs.values().toArray(new Author[hs.size()]);
  }

  /**
   * undocumented
   *
   * @param cipherCertificatesOtherReaders undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setCipherCertificatesOtherReaders(Reader[] cipherCertificatesOtherReaders)
    throws OSCIRoleException
  {
    Hashtable<String, Reader> hs = new Hashtable<String, Reader>();

    for ( int i = 0 ; i < cipherCertificatesOtherReaders.length ; i++ )
    {
      // Nach Import von Rollen aus anderen Nachrichten könnte es vorkommen,
      // dass verschiedene Readers gleiche Ids haben.
      if (hs.containsKey(cipherCertificatesOtherReaders[i].getCipherCertificateId()))
      {
        if (!hs.get(cipherCertificatesOtherReaders[i].getCipherCertificateId())
               .getCipherCertificate()
               .equals(cipherCertificatesOtherReaders[i].getCipherCertificate()))
          throw new OSCIRoleException("id_conflict_reader",
                                      cipherCertificatesOtherReaders[i].getCipherCertificateId());
      }
      else
      {
        hs.put(cipherCertificatesOtherReaders[i].getCipherCertificateId(), cipherCertificatesOtherReaders[i]);
        certificates.put(cipherCertificatesOtherReaders[i].getCipherCertificateId(),
                         cipherCertificatesOtherReaders[i].getCipherCertificate());
      }
    }

    this.cipherCertificatesOtherReaders = hs.values().toArray(new Reader[hs.size()]);
  }

  /**
   * undocumented
   *
   * @param signatureCertificateOriginator undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setSignatureCertificateOriginator(Originator signatureCertificateOriginator)
    throws OSCIRoleException
  {
    certificates.put(signatureCertificateOriginator.getSignatureCertificateId(),
                     signatureCertificateOriginator.getSignatureCertificate());
    this.signatureCertificateOriginator = signatureCertificateOriginator;
  }

  /**
   * undocumented
   *
   * @param signatureCertificateAddressee undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setSignatureCertificateAddressee(Addressee signatureCertificateAddressee)
    throws OSCIRoleException
  {
    certificates.put(signatureCertificateAddressee.getSignatureCertificateId(),
                     signatureCertificateAddressee.getSignatureCertificate());
    this.signatureCertificateAddressee = signatureCertificateAddressee;
  }

  /**
   * undocumented
   *
   * @param signatureCertificatesOtherAuthors undocumented
   * @throws OSCIRoleException undocumented
   */
  public void setSignatureCertificatesOtherAuthors(Author[] signatureCertificatesOtherAuthors)
    throws OSCIRoleException
  {
    Hashtable<String, Author> hs = new Hashtable<String, Author>();

    for ( int i = 0 ; i < signatureCertificatesOtherAuthors.length ; i++ )
    {
      // Nach Import von Rollen aus anderen Nachrichten könnte es vorkommen,
      // dass verschiedene Authors gleiche Ids haben.
      if (hs.containsKey(signatureCertificatesOtherAuthors[i].getSignatureCertificateId()))
      {
        if (!hs.get(signatureCertificatesOtherAuthors[i].getSignatureCertificateId())
               .getSignatureCertificate()
               .equals(signatureCertificatesOtherAuthors[i].getSignatureCertificate()))
          throw new OSCIRoleException("id_conflict_signer_author",
                                      signatureCertificatesOtherAuthors[i].getSignatureCertificateId());
      }
      else
      {
        hs.put(signatureCertificatesOtherAuthors[i].getSignatureCertificateId(),
               signatureCertificatesOtherAuthors[i]);
        certificates.put(signatureCertificatesOtherAuthors[i].getSignatureCertificateId(),
                         signatureCertificatesOtherAuthors[i].getSignatureCertificate());
      }
    }

    this.signatureCertificatesOtherAuthors = (Author[])hs.values().toArray(new Author[hs.size()]);
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented
   * @throws OSCIRoleException undocumented
   */
  public void writeXML(OutputStream out) throws IOException, OSCIRoleException
  {
    out.write(("<" + osciNSPrefix + ":NonIntermediaryCertificates").getBytes(Constants.CHAR_ENCODING));
    out.write(ns);
    out.write((" Id=\"nonintermediarycertificates\" " + soapNSPrefix
               + ":actor=\"http://www.w3.org/2001/12/soap-envelope/actor/none\" " + soapNSPrefix
               + ":mustUnderstand=\"1\">").getBytes(Constants.CHAR_ENCODING));

    if (cipherCertificateOriginator != null)
      addCipherCertificate(cipherCertificateOriginator, out);

    if (cipherCertificatesOtherAuthors != null)
    {
      for ( int i = 0 ; i < cipherCertificatesOtherAuthors.length ; i++ )
        addCipherCertificate(cipherCertificatesOtherAuthors[i], out);
    }

    if (cipherCertificateAddressee != null)
      addCipherCertificate(cipherCertificateAddressee, out);

    if (cipherCertificatesOtherReaders != null)
    {
      for ( int i = 0 ; i < cipherCertificatesOtherReaders.length ; i++ )
        addCipherCertificate(cipherCertificatesOtherReaders[i], out);
    }

    if (signatureCertificateOriginator != null)
      addSignatureCertificate(signatureCertificateOriginator, out);

    if (signatureCertificateAddressee != null)
      addSignatureCertificate(signatureCertificateAddressee, out);

    if (signatureCertificatesOtherAuthors != null)
    {
      for ( int i = 0 ; i < signatureCertificatesOtherAuthors.length ; i++ )
        addSignatureCertificate(signatureCertificatesOtherAuthors[i], out);
    }

    out.write(("</" + osciNSPrefix + ":NonIntermediaryCertificates>").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Addressee getCipherCertificateAddressee()
  {
    return cipherCertificateAddressee;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Originator getCipherCertificateOriginator()
  {
    return cipherCertificateOriginator;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Author[] getCipherCertificatesOtherAuthors()
  {
    return cipherCertificatesOtherAuthors;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public de.osci.osci12.roles.Reader[] getCipherCertificatesOtherReaders()
  {
    return cipherCertificatesOtherReaders;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Addressee getSignatureCertificateAddressee()
  {
    return signatureCertificateAddressee;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Originator getSignatureCertificateOriginator()
  {
    return signatureCertificateOriginator;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Author[] getSignatureCertificatesOtherAuthors()
  {
    return signatureCertificatesOtherAuthors;
  }
}
