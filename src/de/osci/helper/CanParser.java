package de.osci.helper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;


/**
 * Handler für die Kanonisierung.
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
 * @author H. Tabrizi / N. Büngener
 * @version 2.4.1
 */

// Dieser Parser wird auch zum Parsen von zusätzlichen SOAP-Headern verwendet.
public class CanParser extends DefaultHandler implements LexicalHandler
{

  private static Log log = LogFactory.getLog(CanParser.class);

  private boolean checkIds = true;

  // Constants
  /** Lexical handler property id (http://xml.org/sax/properties/lexical-handler). */
  private static final String LEXICAL_HANDLER_PROPERTY_ID = "http://xml.org/sax/properties/lexical-handler";

  protected int elementDepth;

  protected boolean isFirstProcessing = true;

  private DigestOutputStream outs = null;

  private MessageDigest md;

  private XMLReader parser = null;

  private Stack<Map<String, String>> sampleNSStack = new Stack<Map<String, String>>();

  protected boolean useComment = false;

  private boolean insideHeader = false;
  
  private boolean headerAlreadyFound = false;

  private boolean insideContainerSignatureProps = false;

  private boolean insideTransportSignedInfo = false;

  private String currentTransportSignatureRef;

  private Hashtable<String, String> transportDigestMethods;

  private boolean readNS;

  private boolean signedElement;

  private Map<String, String> tmpMap;

  Hashtable<String, MessageDigest> messageDigests;

  private String id;

  Hashtable<String, byte[]> digestValues;

  // List of all found ids
  private HashSet<String> foundRefIds;

  Vector<byte[]> signedInfos;

  Vector<String> signedProperties;

  Vector<String> cocoNS;

  private OutputStream out;

  private Vector<String> tag;

  private ContentHandler parent;

  private ErrorHandler errorHandler;

  private String name;

  private StoreInputStream sis;

  private static SAXParserFactory spf;

  private Writer outWriter;

  private Writer tmpWriter;

  private byte[] controlBlock;

  private String controlBlockID;

  /**
   * SOAP-Namespace Identifier
   */
  private String soapId;

  static
  {
    spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    spf.setValidating(false);
  }

  /**
   * Creates a new CanParser object.
   *
   * @param tag undocumented
   * @param xmlReader undocumented
   * @param parent undocumented
   * @param name undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public CanParser(Vector<String> tag, XMLReader xmlReader, ContentHandler parent, String name)
    throws NoSuchAlgorithmException
  {
    this(xmlReader, new ByteArrayOutputStream());
    this.tag = tag;
    this.parent = parent;
    this.name = name;
  }

  /**
   * Creates a new CanParser object.
   *
   * @deprecated Diese Methode wird nicht mehr lange verfügbar sein. Bitte Methode
   *             {@link #CanParser(OutputStream, StoreInputStream, boolean)} benutzen
   * @param out undocumented
   * @param sis undocumented
   * @throws SAXException undocumented
   * @throws ParserConfigurationException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  @Deprecated
  public CanParser(OutputStream out, StoreInputStream sis)
    throws SAXException, ParserConfigurationException, NoSuchAlgorithmException
  {
    this(spf.newSAXParser().getXMLReader(), out);
    this.sis = sis;
  }

  /**
   * Creates a new CanParser object.
   *
   * @param out undocumented
   * @param sis undocumented
   * @throws SAXException undocumented
   * @throws ParserConfigurationException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public CanParser(OutputStream out, StoreInputStream sis, boolean checkIds)
    throws SAXException, ParserConfigurationException, NoSuchAlgorithmException
  {
    this(spf.newSAXParser().getXMLReader(), out);
    this.sis = sis;
    this.checkIds = checkIds;
  }

  private CanParser(XMLReader xmlReader, OutputStream out) throws NoSuchAlgorithmException
  {
    this.out = out;

    parser = xmlReader;
    // save configuration
    this.errorHandler = parser.getErrorHandler();
    parser.setContentHandler(this);
    parser.setErrorHandler(this);
    parser.setDTDHandler(this);
    parser.setEntityResolver(this);

    try
    {
      // lex = parser.getProperty(LEXICAL_HANDLER_PROPERTY_ID);
      parser.setProperty(LEXICAL_HANDLER_PROPERTY_ID, this);
    }
    catch (SAXException e)
    {
      // ignore
    }
    ParserHelper.setFeatures(parser);
    outs = new DigestOutputStream(this.out, null);
    /*
     * { public void write(byte[] data, int off, int len) throws IOException { log.debug("\n\nSIGN: "+new
     * String(data, off, len)); super.write(data, off, len); } public void flush() throws IOException {
     * log.debug("\nFLUSH SIGN", new Exception()); super.flush(); } };
     */
    try
    {
      outWriter = new BufferedWriter(new OutputStreamWriter(outs, Constants.CHAR_ENCODING));
    }
    catch (UnsupportedEncodingException ex)
    {
      // kann nicht auftreten
    }
  }

  /**
   * undocumented
   *
   * @throws SAXException undocumented
   */
  public void startDocument() throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("startDocument");

    Map<String, String> sampleNSMap = new TreeMap<String, String>();
    elementDepth = 0;
    sampleNSMap.put("xmlns", "");
    sampleNSStack.push(sampleNSMap);
    digestValues = new Hashtable<String, byte[]>();
    foundRefIds = new HashSet<String>();
    transportDigestMethods = new Hashtable<String, String>();
    messageDigests = new Hashtable<String, MessageDigest>();
    currentTransportSignatureRef = "";

    if (signedInfos == null)
      signedInfos = new Vector<byte[]>();

    if (cocoNS == null)
      cocoNS = new Vector<String>();

    // md.reset();
    outs.on(false);
    soapId = null;
  }

  /** Processing instruction. */
  public void processingInstruction(String target, String data) throws SAXException
  {
    try
    {
      if ((elementDepth == 0) && isFirstProcessing)
      {
        outWriter.write("<?" + target + " ");
        normalizeProcessingInstruction(data);
        outWriter.write("?>\n");
      }

      if ((elementDepth == 0) && !isFirstProcessing)
      {
        outWriter.write("\n<?" + target + " ");
        normalizeProcessingInstruction(data);
        outWriter.write("?>");
      }

      if (isFirstProcessing)
        isFirstProcessing = false;
    }
    catch (IOException ex)
    {
      throw new SAXException(ex);
    }
  }

  private boolean isOSCISignatureHeader(String uri, String localName)
  {
    if (HeaderTags.SupplierSignature.getNamespace().getUri().equals(uri) && (HeaderTags.ClientSignature.getElementName().equals(localName) ||HeaderTags.SupplierSignature.getElementName().equals(localName)))
    {
       return true;
    }
    return false;
  }
  /** Start element. */
  public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("CAN START ELEMENT: " + qName);

    try
    {
      // Id checker
      String idAll = attrs.getValue("Id");
      if (log.isDebugEnabled())
        log.debug("Element: " + qName + " Id: " + idAll);
      if (idAll != null)
      {
        if (!foundRefIds.add("#" + idAll))
        {
          log.error("#### DOUBLE REF-ID FOUND!!! Element with Id: " + idAll + " already exist ###");
          if (checkIds && ParserHelper.isSecureTransportDataCheck())
          {
            throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
          }
        }
      }
      signedElement = false;
      readNS = false;

      int attCount = attrs.getLength();
      Map<String, String> outMap = new TreeMap<String, String>();
      Map<String, String> sampleNSMap = new TreeMap<String, String>(sampleNSStack.peek());
      elementDepth++;

      // SOAP-Namespacedefinition suchen
      if (soapId == null)
      {
        soapId = "";

        for ( int i = 0 ; i < attrs.getLength() ; i++ )
        {
          if (attrs.getValue(i).equals("http://schemas.xmlsoap.org/soap/envelope/"))
          {
            if (!soapId.isEmpty())
              throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
            soapId = attrs.getQName(i).substring(attrs.getQName(i).indexOf(':') + 1);
          }
        }
      }

      // Entscheiden, ob der StoreStream die jetzt gepufferten Bytes wegwerfen oder
      // wegschreiben soll.
      if ((sis != null) && (qName.equals(soapId + ":Envelope")))
        sis.setSave((attrs.getValue("xsi:schemaLocation") != null)
                    && (attrs.getValue("xsi:schemaLocation").indexOf("soapMessageEncrypted.xsd") < 0));

      if (qName.equals(soapId + ":Header"))
      {
        if(!headerAlreadyFound)
        {
          insideHeader = true;
          headerAlreadyFound = true;
        }
        else
        {
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") +": "+ soapId + ":Header"); 
        }
      }
      else if (insideHeader && localName.equals("ControlBlock"))
      {
        signedElement = true;
        controlBlockID = "#" + attrs.getValue("Id");
        outWriter = new SplitWriter(outWriter);
      }
      else if ((insideHeader && (elementDepth == 3) && !isOSCISignatureHeader(uri,localName))
               || (elementDepth == 2 && qName.equals(soapId + ":Body")))
      {
        id = attrs.getValue("Id");
        String mdm = transportDigestMethods.get("#" + id);
        if (mdm == null)
          mdm = Constants.DIGEST_ALGORITHM_SHA256;
        signedElement = true;
        try
        {
          outWriter.flush();
        }
        catch (Throwable e)
        {
          log.error(e);
        }
        if (messageDigests.get(mdm) == null)
        {
          if (DialogHandler.getSecurityProvider() == null)
            messageDigests.put(mdm, MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(mdm)));
          else
            messageDigests.put(mdm,
                               MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(mdm),
                                                         DialogHandler.getSecurityProvider()));
        }
        md = (MessageDigest)messageDigests.get(mdm);
        md.reset();
        // outs.flush();
        outs.setMessageDigest(md);
        outs.on(true);
      }
      else if (localName.equals("SignedInfo") || (!insideHeader && localName.equals("SignedProperties")))
      {
        signedElement = true;
        readNS = true;
        outWriter = new SplitWriter(outWriter);
        if (insideHeader && localName.equals("SignedInfo"))
          insideTransportSignedInfo = true;
        else if (localName.equals("SignedProperties"))
          insideContainerSignatureProps = true;
      }
      else if (insideTransportSignedInfo)
      {
        if (localName.equals("Reference"))
          currentTransportSignatureRef = attrs.getValue("URI");
        else if (localName.equals("DigestMethod"))
        {
          transportDigestMethods.put(currentTransportSignatureRef, attrs.getValue("Algorithm"));
        }
      }
      else if (localName.equals("ContentContainer"))
      {
        signedElement = true;
        readNS = true;
      }

      // Sorting Attributes
      for ( int i = 0 ; i < attCount ; i++ )
      {
        // checkout Namespace xmlns="xyz", xmlns="" or xmlns:x="xyz"
        if (attrs.getQName(i).startsWith("xmlns:") || attrs.getQName(i).equals("xmlns"))
        {
          // compares whether Namespace exists in the earlier Parent Element (Superfluous Namespaces)
          if (!(sampleNSMap.containsKey(attrs.getQName(i))
                && attrs.getValue(i).equals(sampleNSMap.get(attrs.getQName(i)))))
          {
            sampleNSMap.put(attrs.getQName(i), attrs.getValue(i));
            outMap.put(" ," + attrs.getQName(i), attrs.getValue(i));
          }
          else
          {
            sampleNSMap.put(attrs.getQName(i), attrs.getValue(i));
          }
        }
        else
        {
          // looking for Attributes with Namespace Prefix
          if (!attrs.getURI(i).equals(""))
          {
            outMap.put(attrs.getURI(i) + "," + attrs.getQName(i), attrs.getValue(i));
          }
          else
          {
            outMap.put("," + attrs.getQName(i), attrs.getValue(i));
          }
        }
      }

      this.sampleNSStack.push(sampleNSMap);

      if (readNS)
      {
        tmpMap = outMap;
        outMap = new TreeMap<String, String>();
      }

      outWriter.write("<" + qName);

      if (signedElement)
      {
        Map.Entry<String, String> entry;

        for ( Iterator<Map.Entry<String, String>> iterator = sampleNSMap.entrySet()
                                                                        .iterator() ; iterator.hasNext() ; )
        {
          entry = iterator.next();

          if (entry.getValue().length() > 0)
            outMap.put(" ," + entry.getKey(), entry.getValue());
        }
      }

      if (readNS)
      {
        tmpWriter = outWriter;
        outWriter = new StringWriter();

        Iterator<Map.Entry<String, String>> itr = outMap.entrySet().iterator();

        while (itr.hasNext())
        {
          Map.Entry<String, String> entry = itr.next();
          String sKey = entry.getKey();
          outWriter.write(" " + sKey.substring(sKey.indexOf(",") + 1) + "=\"");
          this.normalizeAttr(entry.getValue());
          outWriter.write("\"");
        }

        outMap = tmpMap;

        if (localName.equals("SignedInfo") || insideContainerSignatureProps)
          ((SplitWriter)tmpWriter).sb.append(((StringWriter)outWriter).getBuffer());
        else
          cocoNS.add(((StringWriter)outWriter).toString());

        outWriter = tmpWriter;
        tmpWriter = null;
      }

      // Normalize Attributes
      Iterator<Map.Entry<String, String>> it = outMap.entrySet().iterator();

      while (it.hasNext())
      {
        Map.Entry<String, String> entry = it.next();
        String sKey = entry.getKey();
        outWriter.write(" " + sKey.substring(sKey.indexOf(",") + 1) + "=\"");
        this.normalizeAttr(entry.getValue());
        outWriter.write("\"");
      }

      outWriter.write(">");
    }
    catch (Exception ex)
    {
      throw new SAXException(ex);
    }
  }

  /** Characters. */
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    try
    {
      normalizeText(ch, start, length);
    }
    catch (IOException ex)
    {
      throw new SAXException(ex);
    }
  }

  /**
   * Ignorable whitespace. Wurde wg. Schreibfehler ohnehin nie aufgerufen, deahalb entfernt. public void
   * ignorableWhitespace(char[] ch, int start, int length) throws SAXException { characters(ch, start,
   * length); }
   */
  /** End element. */
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("CAN END ELEMENT: " + qName);

    elementDepth--;

    try
    {
      outWriter.write("</" + qName + ">");

      if (localName.equals("SignedInfo"))
      {
        // Keine Nachrichtensignatur
        if (!insideHeader && (signedInfos.size() == 0))
          signedInfos.add(null);

        signedInfos.add(((SplitWriter)outWriter).sb.toString().getBytes(Constants.CHAR_ENCODING));
        outWriter = ((SplitWriter)outWriter).wr;

        if (insideTransportSignedInfo)
        {
          insideTransportSignedInfo = false;
          String digAlgo = transportDigestMethods.get(controlBlockID);
          if (digAlgo != null)
          {
            if (DialogHandler.getSecurityProvider() == null)
              md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digAlgo));
            else
              md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digAlgo),
                                             DialogHandler.getSecurityProvider());
            messageDigests.put(digAlgo, md);
            if (digestValues.containsKey(controlBlockID))
              throw new SAXException(DialogHandler.text.getString("unexpected_entry"));

            // id
            digestValues.put(controlBlockID, md.digest(controlBlock));
          }

        }

      }
      else if (insideContainerSignatureProps && localName.equals("SignedProperties"))
      {
        signedProperties.add(((SplitWriter)outWriter).sb.toString());
        outWriter = ((SplitWriter)outWriter).wr;
        insideContainerSignatureProps = false;
      }
      else if (insideHeader && localName.equals("ControlBlock"))
      {
        outWriter.flush();
        controlBlock = ((SplitWriter)outWriter).sb.toString().getBytes(Constants.CHAR_ENCODING);
        outWriter = ((SplitWriter)outWriter).wr;
        id = null;

      }
      else if ((insideHeader && (elementDepth == 2) && !isOSCISignatureHeader(uri, localName))
               || (elementDepth == 1 && qName.equals(soapId + ":Body")))
      {
        outWriter.flush();

        if (id != null)
        {
          if (digestValues.containsKey("#" + id))
            throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
          log.debug("######### add to msgPart digest to Hashtable: #" + id);
          digestValues.put("#" + id, md.digest());
          md = null;
        }

        outs.on(false);
        id = null;
      }

      if (!this.sampleNSStack.empty())
                                      // removes the last treated Element from the Stack
                                      this.sampleNSStack.pop();
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new SAXException(ex);
    }

    if (qName.equals(soapId + ":Header"))
      insideHeader = false;

    if ((name != null) && (qName.equals(name)))
      endDocument();
  }

  /** End document. */
  public void endDocument() throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("CAN END DOC ");

    isFirstProcessing = true;

    try
    {
      outWriter.flush();
      // outs.flush();
      outWriter.close();
    }
    catch (IOException ex)
    {
      throw new SAXException(ex);
    }

    if (tag != null)
    {
      try
      {
        tag.add(((ByteArrayOutputStream)out).toString(Constants.CHAR_ENCODING));
      }
      catch (UnsupportedEncodingException ex)
      {
        // Impossible
      }

      // restore previous configuration
      if (errorHandler != null)
        parser.setErrorHandler(errorHandler);

      if (parent != null)
        parser.setContentHandler(parent);
    }
  }

  /**
   * undocumented
   *
   * @param ex undocumented
   * @throws SAXException undocumented
   */
  public void warning(SAXParseException ex) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Warning: " + ex);

    throw ex;
  }

  /** Error. */
  public void error(SAXParseException ex) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Error: " + ex);

    throw ex;
  }

  /** Fatal error. */
  public void fatalError(SAXParseException ex) throws SAXException
  {
    log.error("Fatal Error: ", ex);
    throw ex;
  }

  /**
   * undocumented
   *
   * @param name undocumented
   * @param publicId undocumented
   * @param systemId undocumented
   * @throws SAXException undocumented
   */
  public void startDTD(String name, String publicId, String systemId) throws SAXException
  {}

  /** End DTD. */
  public void endDTD() throws SAXException
  {}

  /** Start entity. */
  public void startEntity(String name) throws SAXException
  {}

  /** End entity. */
  public void endEntity(String name) throws SAXException
  {}

  /** Start CDATA section. */
  public void startCDATA() throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("CDATA");
  }

  /** End CDATA section. */
  public void endCDATA() throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("CDATA");
  }

  /** Comment. */
  public void comment(char[] ch, int start, int length) throws SAXException
  {
    if (useComment)
    {
      try
      {
        if (elementDepth == 0)
          outWriter.write("\n");

        outWriter.write("<!--");
        normalizeText(ch, start, length);
        outWriter.write("-->");
      }
      catch (Exception ex)
      {
        throw new SAXException(ex);
      }
    }
  }

  /** Normalize Attribute */
  protected void normalizeAttr(String s) throws IOException
  {
    for ( int j = 0 ; j < s.length() ; j++ )
    {
      char c = s.charAt(j);

      switch (c)
      {
        case '&':
          outWriter.write("&amp;");

          break;

        case '<':
          outWriter.write("&lt;");

          break;

        case '>':
          outWriter.write("&gt;");

          break;

        case '"':
          outWriter.write("&quot;");

          break;

        case 0x09: // '\t'
          outWriter.write("&#x9;");

          break;

        case 0x0A: // '\n'
          outWriter.write("&#xA;");

          break;

        case 0x0D: // '\r'
          outWriter.write("&#xD;");

          break;

        default:
          outWriter.write(c);

          break;
      }
    }
  }

  /** Normalize Processing Instruction */
  protected void normalizeProcessingInstruction(String s) throws IOException
  {
    for ( int j = 0 ; j < s.length() ; j++ )
    {
      char c = s.charAt(j);

      switch (c)
      {
        case 0x0D:
          outWriter.write("&#xD;");

          break;

        default:
          outWriter.write(c);

          break;
      }
    }
  }

  /** Normalize Text */

  // protected String normalizeText(String s)
  protected void normalizeText(char[] ch, int off, int len) throws IOException
  {
    for ( int j = off ; j < (off + len) ; j++ )
    {
      switch (ch[j])
      {
        case '&':
          outWriter.write("&amp;");

          break;

        case '<':
          outWriter.write("&lt;");

          break;

        case '>':
          outWriter.write("&gt;");

          break;

        case 0xD:
          outWriter.write("&#xD;");

          break;

        default:
          outWriter.write(ch[j]);

          break;
      }
    }

    // outWriter.flush();
  }

  /** Start Canonicalization */
  public void startCanonicalization(InputStream in, boolean withComment) throws SAXException, IOException
  {
    org.xml.sax.InputSource ins = new org.xml.sax.InputSource(in);
    this.useComment = withComment;
    parser.parse(ins);
  }

  static class SplitWriter extends Writer
  {

    Writer wr;

    StringBuffer sb = new StringBuffer();

    SplitWriter(Writer w)
    {
      wr = w;
    }

    public void write(String s) throws IOException
    {
      sb.append(s);
      wr.write(s);
    }

    public void close() throws IOException
    {
      wr.close();
    }

    public void flush() throws IOException
    {
      wr.flush();
    }

    public void write(int c) throws IOException
    {
      sb.append((char)c);
      wr.write(c);
    }

    public void write(char[] cbuf, int off, int len) throws IOException
    {
      this.write(new String(cbuf, off, len));
    }
  }
  /*
   * public java.util.Hashtable getDigestValues() { return digestValues; }
   */

}
