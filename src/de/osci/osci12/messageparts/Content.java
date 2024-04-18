package de.osci.osci12.messageparts;

import de.osci.helper.Base64InputStream;
import de.osci.helper.Base64OutputStream;
import de.osci.helper.NullOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.extinterfaces.OSCIDataSource;
import de.osci.osci12.extinterfaces.OSCIDataSourceExt123;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <p>
 * Die Content-Klasse repräsentiert einen Content-Eintrag in einer OSCI- Nachricht. Die Content-Einträge
 * befinden sich in ContentContainer-Einträgen und enthalten die eigentlichen Inhaltsdaten, die in beliebigen
 * Daten, Refenrenzen auf Attachments oder wiederum in Inhaltsdatencontainern bestehen können.
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
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class Content extends MessagePart
{

  private static Log log = LogFactory.getLog(Content.class);

  private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

  private Attachment attachment = null;

  protected OSCIDataSource swapBuffer;

  private ContentContainer coco;

  private int contentType;

  private InputStream transformedDataStream;

  public static final int ATTACHMENT_REFERENCE = 0;

  public static final int CONTENT_CONTAINER = 1;

  public static final int DATA = 2;

  String coNS;

  /**
   * Legt ein Content-Objekt an. Die Daten werden aus dem übergebenen InputStream gelesen.
   *
   * @param ins InputStream
   * @throws IOException bei Lesefehlern
   */
  public Content(InputStream ins) throws IOException
  {
    if (ins == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " ins");

    setRefID("content" + NEXT_ID.getAndIncrement());
    contentType = DATA;
    load(ins);
    transformers.add(b64);
  }

  /**
   * Legt ein Content-Objekt an. Die Daten werden aus dem übergebenen InputStream gelesen. übergebene
   * Transformer-Strings werden in die XML-Signatur eingetragen, die Strings müssen die gesamten
   * Transformer-Einträge gemäß der XML-Signature-Spezifikation in kanonischer Form enthalten. Die zu
   * signierenden transformierten Daten werden aus dem als dritten Parameter übergebenen Stream gelesen.
   *
   * @param ins InputStream der Inhaltsdaten
   * @param transformer Array der TransformerEinträge
   * @param transformedData InputStream der transformierten Daten
   * @throws IOException bei Lesefehlern
   * @throws OSCIException bei Problemn beim Aufbau des OSCI-Signatureintrags
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider den erforderlichen
   *           Hash-Algorithmus nicht unterstützt.
   */
  public Content(InputStream ins, String[] transformer, InputStream transformedData)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    this(ins);

    if ((transformer != null) && (transformer.length > 0))
    {
      transformedDataStream = transformedData;

      for ( int i = 0 ; i < transformer.length ; i++ )
        transformers.add(transformer[i]);
    }
  }

  /*
   * Legt ein Content-Objekt an. Die Daten werden aus dem übergebenen OSCIDataSource-Objekt gelesen.
   * @param ins der InputStream
   */
  Content(de.osci.osci12.extinterfaces.OSCIDataSource dataSource)
  {
    if (log.isDebugEnabled())
      log.debug("Konstruktor");

    if (dataSource == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " dataSource");

    setRefID("content" + NEXT_ID.getAndIncrement());
    swapBuffer = dataSource;
    contentType = DATA;
  }

  /**
   * Legt ein Content-Objekt mit dem Inhalt des übergebenen Strings an. <b>Hinweis:</b> Die Übergabe von
   * Inhaltsdaten als String ist zur Bequemlichkeit für Daten vorgesehen, die empfängerseitig ebenfalls als
   * String der OSCI-Nachricht entnommen und weiterverarbeitet werden. Dies ist zu beachten, wenn z.B.
   * XML-Dokumente übergeben werden, die eine Zeichensatzdeklaration beinhalten und möglicherweise
   * empfängerseitig als Binärdaten einem Parser übergeben werden. Die OSCI-Bibliothek überträgt die Daten
   * UTF-8-codiert.
   *
   * @param data der Inhalt
   * @throws IOException undocumented
   */
  public Content(String data) throws IOException
  {
    if (data == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " data");

    setRefID("content" + NEXT_ID.getAndIncrement());
    load(new ByteArrayInputStream(data.getBytes(Constants.CHAR_ENCODING)));
    contentType = DATA;
    transformers.add(b64);
  }

  /**
   * Legt ein Content-Objekt mit dem Inhalt des übergebenen Strings an. übergebene Transformer-Strings werden
   * in die XML-Signatur eingetragen, die Strings müssen die gesamten Transformer-Einträge gemäß der
   * XML-Signature-Spezifikation in kanonischer Form enthalten. Die zu signierenden transformierten Daten
   * müssen in dem als dritten Parameter übergebenen String enthalten sein. <b>Man beachte den Hinweis unter
   * {@link #Content(String)} zur Übergabe von Inhaltsdaten als String.</b>
   *
   * @param data Inhaltsdaten
   * @param transformer Array der Transformer-Einträge
   * @param transformedData String mit den transformierten Daten
   * @throws IOException bei Lesefehlern
   * @throws OSCIException bei Problemn beim Aufbau des OSCI-Signatureintrags
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider den erforderlichen
   *           Hash-Algorithmus nicht unterstützt.
   */
  public Content(String data, String[] transformer, String transformedData)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    this(data);

    if ((transformer != null) && (transformer.length > 0))
    {
      transformedDataStream = new ByteArrayInputStream(transformedData.getBytes(Constants.CHAR_ENCODING));

      for ( int i = 0 ; i < transformer.length ; i++ )
        transformers.add(transformer[i]);
    }
  }

  /**
   * Legt ein Content-Objekt an, welches eine Referenz auf ein Attachment enthält.
   *
   * @param attachment das Attachmentobjekt
   */
  public Content(Attachment attachment)
  {
    transformers.add(can);

    if (attachment == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " attachment");

    this.attachment = attachment;
    this.setRefID("content" + NEXT_ID.getAndIncrement());
    contentType = ATTACHMENT_REFERENCE;
  }

  /**
   * Legt ein Content-Objekt an, welches ein ContentContainer-Objekt enthält. Eine solche Verschachtelung von
   * ContentContainern ist z.B. für serielle Signaturen erforderlich.
   *
   * @param contentContainer der Inhaltsdatencontainer
   */
  public Content(ContentContainer contentContainer)
  {
    transformers.add(can);

    if (contentContainer == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " contentContainer");

    this.coco = contentContainer;
    this.setRefID("content" + NEXT_ID.getAndIncrement());
    contentType = CONTENT_CONTAINER;
  }

  private void load(InputStream input) throws IOException
  {
    if (swapBuffer == null)
    {
      swapBuffer = DialogHandler.getNewDataBuffer();

      if (swapBuffer instanceof OSCIDataSourceExt123)
        ((OSCIDataSourceExt123)swapBuffer).setConfidential(true);

      Base64OutputStream b64out = new Base64OutputStream(swapBuffer.getOutputStream(), false);
      byte[] bytes = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
      int anz = 0;

      while ((anz = input.read(bytes)) > -1)
      {
        b64out.write(bytes, 0, anz);
      }

      b64out.close();
      input.close();
    }
  }

  /**
   * Bevor eine Signaturprüfung an dem ContentContainer-Objekt durchgeführt werden kann, welches dieses
   * Content-Objekt enthält, müssen mit dieser Methode die transformierten Daten übergeben werden. Dies
   * betrifft nur Content-Objekte, die unter Anwendung von Transformationen signiert wurden.
   *
   * @param transformedData transformierte Daten
   * @see #setTransformedData(String transformedData)
   * @see ContentContainer#checkSignature(de.osci.osci12.roles.Role)
   */
  public void setTransformedData(InputStream transformedData)
  {
    if (getTransformerForSignature() == null)
      throw new IllegalStateException(DialogHandler.text.getString("no_transformer_state"));

    if (transformedDataStream != null)
      throw new IllegalStateException(DialogHandler.text.getString("illegal_change_of_transformed_data"));

    transformedDataStream = transformedData;
  }

  /**
   * Bevor eine Signaturprüfung an dem ContentContainer-Objekt durchgeführt werden kann, welches dieses
   * Content-Objekt enthält, müssen mit dieser Methode die transformierten Daten übergeben werden. Dies
   * betrifft nur Content-Objekte, die unter Anwendung von Transformationen signiert wurden.
   *
   * @param transformedData transformierte Daten
   * @see #setTransformedData(InputStream transformedData)
   * @see ContentContainer#checkSignature(de.osci.osci12.roles.Role)
   */
  public void setTransformedData(String transformedData)
  {
    try
    {
      setTransformedData(new ByteArrayInputStream(transformedData.getBytes(Constants.CHAR_ENCODING)));
    }
    catch (UnsupportedEncodingException ex)
    {
      // Kann bei UTF-8 nicht auftreten
    }
  }

  /**
   * Liefert die TransformerEinträge in der Signatur.
   *
   * @return String-Array mit den TransformerEinträgen
   */
  public String[] getTransformerForSignature()
  {
    if (transformers.size() < 2)
      return null;

    String[] ret = new String[transformers.size() - 1];

    for ( int i = 1 ; i < transformers.size() ; i++ )
      ret[i - 1] = transformers.get(i);

    return ret;
  }

  /**
   * Liefert den Hashwert des Eintrags.
   *
   * @return The digestValue value
   */
  protected byte[] getDigestValue(String digestAlgorithm)
    throws NoSuchAlgorithmException, IOException, OSCIException
  {
    if (digestValues.get(digestAlgorithm) != null)
      return (byte[])digestValues.get(digestAlgorithm);

    MessageDigest md;

    if (DialogHandler.getSecurityProvider() == null)
      md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digestAlgorithm));
    else
      md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digestAlgorithm),
                                     DialogHandler.getSecurityProvider());

    NullOutputStream nos = new NullOutputStream();
    DigestOutputStream digestOut = new DigestOutputStream(nos, md);

    if (contentType == DATA)
    {
      if (transformedDataStream == null)
      {
        // Bibliothek akzeptiert nur Base64-codierte Daten. Workaround für den Fall,
        // dass dieser Transformer beim Laden zwischengespeicherter Daten verloren gegangen ist
        // (StoredMessage).
        // Signaturen mit Transformern können in diesem Fall nur mit Hilfe neuer Content-Instanzen
        // erzeugt werden.
        if (transformers.size() == 0)
          transformers.add(b64);

        if (transformers.size() > 1)
        {
          digestOut.close();
          throw new IllegalStateException(DialogHandler.text.getString("no_transformed_data"));
        }

        swapBuffer.getInputStream().reset();
        transformedDataStream = new Base64InputStream(swapBuffer.getInputStream());
      }

      if (log.isDebugEnabled() && (transformers != null))
      {
        for ( int i = 0 ; i < transformers.size() ; i++ )
          log.debug("Transfromer " + getRefID() + " - " + transformers.get(i));
      }

      byte[] tmp = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
      int i;

      while ((i = transformedDataStream.read(tmp)) > -1)
      {
        digestOut.write(tmp, 0, i);
      }

      transformedDataStream.close();
      digestOut.close();
      digestValues.put(digestAlgorithm, md.digest());
    }
    else
    {
      writeXML(digestOut, false);
      digestOut.close();
      digestValues.put(digestAlgorithm, md.digest());

      if (log.isDebugEnabled())
        log.debug("enter getDigestValue" + Crypto.toHex((byte[])digestValues.get(digestAlgorithm)));

      length = nos.getLength();
    }

    return (byte[])digestValues.get(digestAlgorithm);

  }

  /**
   * Liefert den InputStream der Daten zurück, wenn der Content einer empfangenen Nachricht entnommen wurde.
   *
   * @return den InputStream der Inhaltsdaten, oder null, wenn die Nachricht einen ContentContainer oder eine
   *         Attachmentreferenz enthält.
   * @throws IOException bei Lesefehlern
   */
  public InputStream getContentStream() throws IOException
  {
    if (swapBuffer != null)
    {
      swapBuffer.getInputStream().reset();

      return new Base64InputStream(swapBuffer.getInputStream());
    }
    else
    {
      return null;
    }
  }

  /**
   * Liefert die eingestellten Daten des Content als String zurück. Der zurückgegebene String ist eine
   * UTF-8-Interpretation der enthaltenen Binärdaten.
   *
   * @return den String der Inhaltsdaten oder null, wenn die Nachricht einen ContentContainer oder eine
   *         Attachmentreferenz enthält
   * @throws IOException bei Lesefehlern
   */
  public String getContentData() throws IOException
  {
    if (swapBuffer != null)
    {
      swapBuffer.getInputStream().reset();

      InputStream in = new Base64InputStream(swapBuffer.getInputStream());
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] bytes = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
      int anz;

      while ((anz = in.read(bytes)) > -1)
        bos.write(bytes, 0, anz);

      in.close();
      bos.close();

      return bos.toString(Constants.CHAR_ENCODING);
    }
    else

      return null;
  }

  /**
   * Liefert das referenzierte Attachment zurück.
   *
   * @return Attachment-Objekt oder null, wenn das Content-Objekt Nutzdaten oder einen Inhaltsdatencontainern
   *         enthält
   */
  public Attachment getAttachment()
  {
    return attachment;
  }

  /**
   * Liefert den Inhaltsdatencontainer zurück.
   *
   * @return ContentContainer-Objekt oder null, wenn das Content-Objekt Nutzdaten oder eine Referenz auf ein
   *         Attachment enthält
   */
  public ContentContainer getContentContainer()
  {
    return coco;
  }

  /**
   * Gibt die Art des Inhalts des Content-Objektes in Form eines Identifiers zurück. mögliche Werte sind
   * ATTACHMENT_REFERENCE, CONTENT_CONTAINER und DATA
   *
   * @return Inhaltstyp
   */
  public int getContentType()
  {
    return contentType;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String toString()
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try
    {
      writeXML(out);
    }
    catch (Exception ex)
    {
      if (log.isDebugEnabled())
        log.debug("Fehler beim writeXML", ex);
    }

    try
    {
      return out.toString(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      // kann nicht vorkommen
      return "";
    }
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  protected void writeXML(OutputStream out) throws IOException, OSCIException
  {
    writeXML(out, false);
  }

  /**
   * Interne Methode, wird von Anwendungen normalerweise nicht aufgerufen.
   *
   * @param out undocumented
   * @param inner undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out, boolean inner) throws IOException, OSCIException
  {
    out.write(("<" + osciNSPrefix + ":Content").getBytes(Constants.CHAR_ENCODING));

    if (attachment != null)
    {
      if (log.isDebugEnabled())
        log.debug("SCHREIBE Attachment." + getRefID());

      if (!inner)
      {
        if (coNS == null)
          out.write(ns);
        else
          out.write(coNS.getBytes(Constants.CHAR_ENCODING));
      }

      out.write((" Id=\"" + getRefID() + "\" href=\"cid:" + attachment.getRefID() + "\"></" + osciNSPrefix
                 + ":Content>").getBytes(Constants.CHAR_ENCODING));
    }
    else if (coco != null)
    {
      if (log.isDebugEnabled())
        log.debug("Schreibe ContentContainer." + getRefID());

      if (!inner)
        out.write(ns);

      out.write((" Id=\"" + getRefID() + "\">").getBytes(Constants.CHAR_ENCODING));
      coco.writeXML(out, true);
      out.write(("</" + osciNSPrefix + ":Content>").getBytes(Constants.CHAR_ENCODING));
    }
    else
    {
      if (log.isDebugEnabled())
        log.debug("Schreibe Daten.");

      out.write(("><" + osciNSPrefix + ":Base64Content").getBytes(Constants.CHAR_ENCODING));

      if (!inner)
        out.write(ns);

      out.write((" Id=\"" + getRefID() + "\">").getBytes(Constants.CHAR_ENCODING));

      int count;
      byte[] inBytes = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
      InputStream in = swapBuffer.getInputStream();
      in.reset();

      while ((count = in.read(inBytes)) > -1)
        out.write(inBytes, 0, count);

      in.close();
      out.flush();
      out.write(("</" + osciNSPrefix + ":Base64Content></" + osciNSPrefix
                 + ":Content>").getBytes(Constants.CHAR_ENCODING));
    }
  }
}
