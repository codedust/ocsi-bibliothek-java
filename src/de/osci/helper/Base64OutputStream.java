/*
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
   a utility class which wraps around any given OutputStream and encodes the
 * data before writing to the outputstream as per the BASE64 encoding standards.
 * @author Gokul Singh , gokulsingh@123india.com
 */
package de.osci.helper;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/*
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
   a utility class which wraps around any given OutputStream and encodes the
 * data before writing to the outputstream as per the BASE64 encoding standards.
 * @author Gokul Singh , gokulsingh@123india.com
 */
public class Base64OutputStream extends FilterOutputStream
{
  //  private static Log log = LogFactory.getLog(Base64OutputStream.class);
  /** flag to denote if the flush denotes the end of the stream
   * for Encoding and padding if required should be done
   */
  private boolean isEnd = false;
  /** no of bytes waited to be encoded */
  private int inWait = 0;
  /** integer to act as a buffer to store the bytes before encoding */
  private int buffer = 0;
  /** no of chars written to out */
  private int charsWritten = 0;
  /** char array storing all the chars for the encoding */
  private static final byte[] BASE64CHARS = 
                                            {
                                              (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
                                              (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
                                              (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R',
                                              (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
                                              (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                                              (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
                                              (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p',
                                              (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
                                              (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1',
                                              (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                                              (byte) '8', (byte) '9', (byte) '+', (byte) '/', (byte) '='
                                            };
  /** padding byte */
  private static final byte PAD = (byte) '=';
  /** CRLF */
  private static byte[] CRLF = { 0x0a };

  /** it takes the output stream around which it wraps and the
   * behaviour of the stream when flush is called upon this stream.
   * Also see <a href="#setFlushBehaviour(boolean)">setFlushBehaviour</a>.
   *
   * @param   out  outputstream around which it wraps
   * @param   isFlushEnd  flag denoting if the <code>flush </code> method
   * denotes end of stream.
   */
  public Base64OutputStream(OutputStream out, boolean isFlushEnd)
  {
    super(new BufferedOutputStream(out)
      {
        public synchronized void flush() throws IOException
        {
          if (count > 0)
          {
            this.out.write(buf, 0, count);
            count = 0;
          }
        }
      });

    if (out == null)
      throw new NullPointerException();

    isEnd = isFlushEnd;
  }

  /** writes a byte.
   * @param   b  byte to be written.
   * @exception   IOException
   */
  public void write(int b) throws IOException
  {
    buffer = (buffer << 8) | (b & 0xFF);

    if (++inWait == 3)
      writeBytes();
  }

  /** writes the whole array <code>b</code>. Equivalent to calling the method
   * <code> write(b, 0, b.length)</code>.
   *
   * @param   b  array to be written
   * @exception   IOException
   */
  public void write(byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }

  /** it flushes the underlying stream. The behaviour of this method depends upon the
   * flag <code>isFlushEnd</code>. If the flag is set then call to this method is taken
   * to indicate that no more data is to written to this stream with base64 encoding
   * and hence padding is done if required as per base64 rules. If the flag is not set,
   * then it just flushes the underlying stream.
   *
   * @exception   IOException
   */
  public void flush(boolean completeEncoding) throws IOException
  {
    if (completeEncoding)
    {
      completeEncoding();
      inWait = 0;
      buffer = 0;
      charsWritten = 0;
    }

    out.flush();
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   */
  public void flush() throws IOException
  {
    flush(isEnd);
  }

  /** completes the encoding by applying padding and closes the underlying stream.
   * @exception   IOException
   */
  public void close() throws IOException
  {
    completeEncoding();
    out.close();
  }

  /** returns the flag <code>isFlushEnd</code>
   *
   * @return     returns the flag <code>isFlushEnd</code>
   */
  public boolean getFlushBehaviour()
  {
    return isEnd;
  }

  /** sets the behavior of this stream as regards the call to the method flush.
   *        <p>the end of base64 encoded stream is required to be known to properly terminate
   * the encoded stream. If
   * the <code>isFlushEnd</code> flag is set then a call to flush will assume that
   * no more data is going to be encoded and will apply padding to the end of the
   * stream as required before flusing the underlying stream. If the flag not set,
   * the underlying stream is flushed without any padding being applied.</p>
   * <p> This is required if in the same stream one wants to write encoded values and
   * other data as well. A call to <code>close</close> will close the underlying stream
   * which may be undesirable under certain circumstances. </p>
   * <p> Applications may turn the flag off and call flush to achieve flushing of the
   * underlying stream. Then the flag may be turned on and flush may be called on this
   * stream to signify the end of the encoded stream and flushing of the underlying
   * stream. </p>
   * @param   isFlushEnd  flag to tell if flush denotes end of encoded stream.
   */
  public void setFlushBehaviour(boolean isFlushEnd)
  {
    isEnd = isFlushEnd;
  }

  /** a byte array to store the encoded bytes */
  private byte[] encodedBytes = new byte[4];

  /** write the bytes to the underlying stream.
   * @exception   IOException
   */
  private void writeBytes() throws IOException
  {
    encodedBytes[3] = BASE64CHARS[buffer & 0x3F];
    buffer >>= 6;
    encodedBytes[2] = BASE64CHARS[buffer & 0x3F];
    buffer >>= 6;
    encodedBytes[1] = BASE64CHARS[buffer & 0x3F];
    buffer >>= 6;
    encodedBytes[0] = BASE64CHARS[buffer & 0x3F];
    inWait = 0;
    buffer = 0;
    out.write(encodedBytes);

    if ((charsWritten += 4) == 76)
    {
      out.write(CRLF);
      charsWritten = 0;
    }
  }

  /** completes encoding by applying padding if required .
   * @exception   IOException
   */
  private void completeEncoding() throws IOException
  {
    switch (inWait)
    {
    case 0:
      return;

    case 1:
      buffer <<= 4;
      encodedBytes[2] = PAD;
      encodedBytes[1] = BASE64CHARS[buffer & 0x3F];
      buffer >>= 6;
      encodedBytes[0] = BASE64CHARS[buffer & 0x3F];

      break;

    case 2:
      buffer <<= 2;
      encodedBytes[2] = BASE64CHARS[buffer & 0x3F];
      buffer >>= 6;
      encodedBytes[1] = BASE64CHARS[buffer & 0x3F];
      buffer >>= 6;
      encodedBytes[0] = BASE64CHARS[buffer & 0x3F];

      break;
    }

    encodedBytes[3] = PAD;
    out.write(encodedBytes);

    if ((charsWritten += 4) == 76)
    {
      out.write(CRLF);
      charsWritten = 0;
    }

    inWait = 0;
    buffer = 0;
  }
}
