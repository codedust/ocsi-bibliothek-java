package de.osci.helper;

import de.osci.osci12.common.Constants;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;


/**
   THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
   OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
   ARE DISCLAIMED.  IN NO EVENT S@version 2.4.1
   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
   OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
   OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.
   a utility class which wraps around any given inputstream and decodes the
 * inputstream as per the BASE64 encoding standards.
 * <p>This class reads data from the underlying inputstream in chunks of four. </p>
 * @author Gokul Singh , gokulsingh@123india.com
 */
public class Base64InputStream extends FilterInputStream
{
  //  private static Log log = LogFactory.getLog(Base64InputStream.class);
  /** This stores the three decoded bytes         */
  private final byte[] store = new byte[3];
  /** no of bytes read from the store */
  private int pos = 0;
  /** if end of stream */
  private boolean endOfStream;
  /** no of valid bytes in <code>store</code> */
  private int nos = 0;

  private byte[] buffer;
  private int read_pos = 0;
  private int end_pos = 0;


  /** constructor which takes the input stream around which it wraps.
   * @param   in  stream around which this wraps.
   */
  public Base64InputStream(InputStream in)
  {
    super(new BufferedInputStream(in));

    if (in == null)
      throw new NullPointerException();

    try
    {
    	buffer = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];

    	end_pos = internal_read(buffer, 0, buffer.length);
    }
    catch (IOException e)
    {
    	throw new UndeclaredThrowableException(e);
    }
  }

  public int available() throws IOException
  {
	  if (end_pos < 0)
		  return 0;
	  return end_pos - read_pos;
  }

  /** reads a single byte
   * @return     byte read.
   * @exception   IOException
   */
  public int read() throws IOException
  {
	  if (end_pos < 0)
		  return -1;

	  if (end_pos > read_pos)
	  {
		  int ret = ((int)buffer[read_pos++]) & 0xff;
		  if (end_pos == read_pos)
		  {
			  end_pos = internal_read(buffer, 0, buffer.length);
			  read_pos = 0;
		  }
		  return ret;
	  }

	  byte[] tmp = new byte[1];
	  if (read(tmp, 0, 1) < 0)
		  return -1;

	  return ((int)tmp[0]) & 0xff;
  }

  /** reads a single byte
   * @return     byte read.
   * @exception   IOException
   */
  public int internal_read() throws IOException
  {
    if (pos >= nos)
      getStore();

    if (endOfStream)
      return -1;

    return ((int) store[pos++]) & 0xff;
  }

  /** reads the whole array and then returns. Is equal to calling the method
   * <code> read(target, 0, target.length) </code>
   *
   * @param   target  array in which it has to be copied
   * @return     no of bytes read. <code>-1</code> if end of stream is reached.
   * @exception   IOException
   */
  public int read(byte[] target) throws IOException
  {
    return read(target, 0, target.length);
  }

  public int read(byte[] target, int start, int length) throws IOException
  {
	  if (end_pos < 0)
		  return -1;
	  if (length > end_pos - read_pos)
		  length = end_pos -read_pos;
	  System.arraycopy(buffer, read_pos, target, start, length);
	  read_pos += length;
	  if (end_pos == read_pos)
	  {
		  end_pos = internal_read(buffer, 0, buffer.length);
		  read_pos = 0;
	  }
	  return length;
  }

  /** tries to read length no of bytes into the array .
   *
   * @param   target  array in which data is to be stored.
   * @param   start  index from which to start storing data in array
   * @param   length  maximum no of bytes to be read.
   * @return     no of bytes read.
   * @exception   IOException
   */
  public int internal_read(byte[] target, int start, int length)
           throws IOException
  {
    if (endOfStream)
      return -1;

    int read = 0;

    while ((read < length) && (pos < nos))
      target[start + read++] = store[pos++];

    while ((length - read) >= nos)
    {
      getStore();

      if (endOfStream)
        break;

      while (pos < nos)
        target[start + read++] = store[pos++];
    }

    if (!endOfStream)
    {
      if (pos >= nos)
        getStore();

      while ((read < length) && (pos < nos))
        target[start + read++] = store[pos++];
    }
    else if (read == 0)
    	return -1;

    return read;
  }

  /** This does not support marking.
   *
   * @return     Always returns <code>false</code>.
   */
  public boolean markSupported()
  {
    return false;
  }

  /** This stream does not support marking. This method just returns.
   */
  public void mark()
  {
    return;
  }

  /** This stream does not support marking. This method just returns.
   */
  public void reset()
  {
    return;
  }

  /** gets bytes.
   * @exception   IOException
   */
  private void getStore() throws IOException
  {
    pos = 0;

    int buffer = 0;
    int bytesRead = 0;
    int data;

// get four bytes and put in one int.
outer:
    while (bytesRead < 4)
    {
      data = in.read();

      if (data == -1)
      {
        nos = 0;
        endOfStream = true;

        return;
      }

      switch (data = decodeInt(data))
      {
      case -1:

        continue;

      case -2:

        if ((bytesRead != 2) && (bytesRead != 3))
          throw new IOException("Invalid placement of '=' in the stream");

        break outer;

      default:
        buffer = (buffer << 6) | data;
        bytesRead++;
      }
    }

    if (bytesRead == 4)
    {
      nos = 3;
      store[2] = (byte) (buffer & 0xFF);
      buffer >>= 8;
      store[1] = (byte) (buffer & 0xFF);
      buffer >>= 8;
      store[0] = (byte) (buffer & 0xFF);
    }
    else
    {
      if (bytesRead == 2)
      {
        nos = 1;
        in.read();
        buffer >>= 4;
        store[0] = (byte) (buffer & 0xFF);
      }
      else
      {
        nos = 2;
        buffer >>= 2;
        store[1] = (byte) (buffer & 0xFF);
        buffer >>= 8;
        store[0] = (byte) (buffer & 0xFF);
      }
    }
  }

  /** codes int into base64 chars.
   * @param   src  in to be coded.
   * @return     coded value of <code>src</code>
   */
  private int decodeInt(int src)
  {
    if ((src >= 'A') && (src <= 'Z'))
      return src - 'A';

    if ((src >= 'a') && (src <= 'z'))
      return src - 'a' + 26;

    if ((src >= '0') && (src <= '9'))
      return src - '0' + 52;

    if (src == '+')
      return 62;

    if (src == '/')
      return 63;

    if (src == '=')
      return -2; // denotes padding

    return -1; // denotes just ignore it. newlines etc..
  }
}
