package de.osci.osci12.encryption;

import de.osci.helper.Base64InputStream;
import de.osci.helper.Base64OutputStream;
import de.osci.helper.SymCipherOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.extinterfaces.OSCIDataSource;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.MessagePartsFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class CipherValue
{
  private static Log log = LogFactory.getLog(CipherValue.class);
  private OSCIDataSource swapBuffer = null;
  private ContentContainer coco = null;
  private javax.crypto.SecretKey key = null;
  private byte[] iv;
  private String symAlgorithm;
  private int ivLength;

  /**
   * Creates a new CipherValue object.
   *
   * @param data undocumented
   *
   * @throws IOException undocumented
   */
  public CipherValue(String data) throws IOException
  {
    swapBuffer = DialogHandler.getNewDataBuffer();

    if (log.isDebugEnabled())
      log.debug("Konstruktor String Data");

    OutputStream out = swapBuffer.getOutputStream();
    out.write(data.getBytes(Constants.CHAR_ENCODING));
    out.close();
  }

  /**
   * Creates a new CipherValue object.
   *
   * @param dataBytes undocumented
   *
   * @throws IOException undocumented
   */
  public CipherValue(byte[] dataBytes) throws IOException
  {
    swapBuffer = DialogHandler.getNewDataBuffer();

    Base64OutputStream b64out = new Base64OutputStream(swapBuffer.getOutputStream(), false);
    b64out.write(dataBytes);
    b64out.close();
  }

  /**
   * Creates a new CipherValue object.
   *
   * @throws IOException undocumented
   */
  public CipherValue() throws IOException
  {
    swapBuffer = DialogHandler.getNewDataBuffer();
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public OutputStreamWriter getStreamToWrite() throws IOException
  {
    return new OutputStreamWriter(swapBuffer.getOutputStream(), Constants.CHAR_ENCODING);
  }
  
  public CipherValue(de.osci.osci12.messageparts.ContentContainer contentContainer, SecretKey key, String symAlgorithm, int ivLength)
  {
    this.coco = contentContainer;
    this.key = key;
    this.symAlgorithm=symAlgorithm;
    this.ivLength = ivLength;
  }

  /**
   * Creates a new CipherValue object.
   *
   * @param contentContainer undocumented
   * @param key undocumented
   */
  public CipherValue(de.osci.osci12.messageparts.ContentContainer contentContainer, SecretKey key, String symAlgorithm)
  {
    this(contentContainer, key, symAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH);
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public InputStream getCipherValueStream() throws IOException
  {
    swapBuffer.getInputStream().reset();

    return new Base64InputStream(swapBuffer.getInputStream());
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out, String xenc) throws IOException,
                                                             OSCIException
  {
    out.write(("<" + xenc + ":CipherValue>").getBytes(Constants.CHAR_ENCODING));

    if (coco != null)
    {
      Base64OutputStream b64out = new Base64OutputStream(out, false);
      SymCipherOutputStream scos = new SymCipherOutputStream(b64out, key, symAlgorithm, ivLength, iv);
      iv = scos.getIv();
      MessagePartsFactory.writeXML(coco, scos);
      scos.close();
      b64out.flush(true);
    }
    else
    {
      InputStream is = swapBuffer.getInputStream();
      is.reset();

      byte[] tmp = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
      int i;

      while ((i = is.read(tmp)) > -1)
        out.write(tmp, 0, i);

      is.close();
    }

    out.write(("</" + xenc + ":CipherValue>").getBytes(Constants.CHAR_ENCODING));
    out.flush();
  }
}
