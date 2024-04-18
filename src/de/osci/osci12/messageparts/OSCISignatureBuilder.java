package de.osci.osci12.messageparts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.CanParser;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;
import de.osci.osci12.messagetypes.OSCIRequest;
import de.osci.osci12.roles.Role;
import de.osci.osci12.signature.OSCISignatureException;


/**
 * Signature-Parser.
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
class OSCISignatureBuilder extends MessagePartParser
{

  private static Log log = LogFactory.getLog(OSCISignatureBuilder.class);

  OSCISignature sig;

  boolean insideSignature = false;

  Boolean insideSignedInfo;

  boolean insideReference = false;

  boolean insideObject = false;

  boolean insideXades = false;

  boolean insideKeyInfo=false;

  private OSCISignatureReference sr;

  private String signingTime;

  private String signingPropsId;

  /**
   * Constructor for the OSCISignatureBuilder object
   *
   * @param xmlReader Description of Parameter
   * @param parentHandler Description of Parameter
   * @param atts Description of Parameter
   * @throws SAXException
   */
  public OSCISignatureBuilder(XMLReader xmlReader, DefaultHandler parentHandler, Attributes atts,boolean signatureElementAvaliable)
    throws SAXException
  {
    super(xmlReader, parentHandler);
    insideSignature=signatureElementAvaliable;

    if (parentHandler instanceof OSCIMessageBuilder)
    {
      OSCIMessage msg = ((OSCIMessageBuilder)parentHandler).getOSCIMessage();
      String signatureName = ((((de.osci.osci12.messagetypes.OSCIMessageBuilder)parentHandler).getOSCIMessage() instanceof OSCIRequest)
        ? HeaderTags.ClientSignature.getElementName() : HeaderTags.SupplierSignature.getElementName());
      String name = "<" + msg.osciNSPrefix + ":";
      String soap = msg.soapNSPrefix;
      name += signatureName + " Id=\"";
      name += (atts.getValue("Id") + "\" ");
      name += (soap + ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soap
               + ":mustUnderstand=\"1\">");
      sig = new OSCISignature(name);
      sig.setNSPrefixes(soap, msg.osciNSPrefix, msg.dsNSPrefix, msg.xencNSPrefix, msg.xsiNSPrefix);
    }
    else
      sig = new OSCISignature();
  }

  /**
   * Description of the Method
   *
   * @param uri Description of Parameter
   * @param localName Description of Parameter
   * @param qName Description of Parameter
   * @param attributes Description of Parameter
   * @exception SAXException Description of Exception
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start Element" + uri + ":" + localName);

    if (uri.equals(DS_XMLNS) && localName.equals("Signature") && !(insideSignature || insideKeyInfo || insideObject || insideReference || (insideSignedInfo != null && insideSignedInfo)|| insideXades))
    {
      insideSignature = true;
    }
    else if (insideSignature)
    {
      if (insideSignedInfo !=null && insideSignedInfo && uri.equals(DS_XMLNS))
      {
        if (localName.equals("Reference"))
        {
          insideReference = true;
          sr = new OSCISignatureReference();

          String rf = attributes.getValue("URI");
          sr.setRefID(rf);
          try
          {
            sig.addSignatureReference(sr);
          }
          catch (OSCIErrorException ex)
          {
            log.error("Error add the reference!", ex);
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName, ex);
          }
        }
        else if (localName.equals("CanonicalizationMethod"))
        {
          // do nothing
        }
        else if (insideReference)
        {
          if (localName.equals("Transforms"))
          {
            // do nothing
          }
          else if (localName.equals("Transform"))
          {
            // sr.transformerAlgorithms.add(attributes.getValue("Algorithm"));
            try
            {
              CanParser cp = new CanParser(sr.transformerAlgorithms, xmlReader, this, qName);
              xmlReader.setContentHandler(cp);
              cp.startDocument();
              cp.startElement(uri, localName, qName, attributes);
            }
            catch (Exception ex)
            {
              throw new SAXException(DialogHandler.text.getString("sax_exception_transfomation"), ex);
            }
          }
          else if (localName.equals("DigestMethod"))
          {
            sr.setDigestMethodAlgorithm(attributes.getValue("Algorithm"));
          }
          else if (localName.equals("DigestValue"))
            currentElement = new StringBuffer();
          else
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        }
        else if (localName.equals("SignatureMethod"))
        {
          sig.signatureAlgorithm = attributes.getValue("Algorithm");

          if (Constants.JCA_JCE_MAP.get(attributes.getValue("Algorithm")) == null)
            throw new SAXException(DialogHandler.text.getString("invalid_signature_algorithm") + " "
                                   + attributes.getValue("Algorithm"));
        }
        else
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
      else
      {
        if (!insideKeyInfo && !insideObject && uri.equals(DS_XMLNS))
        {
          if (localName.equals("SignedInfo") && insideSignedInfo==null)
          {
            insideSignedInfo = true;
          }
          else if (localName.equals("KeyInfo"))
          {
            insideKeyInfo = true;
          }
          else if (localName.equals("Object"))
          {
            insideObject = true;
          }
          else if (localName.equals("SignatureValue") && sig.signatureValue == null)
            currentElement = new StringBuffer();
          else
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        }
        else if (insideKeyInfo && uri.equals(DS_XMLNS) && localName.equals("RetrievalMethod"))
        {
          String rf = attributes.getValue("URI");
          sig.signerId = rf;
        }
        else if (insideObject && uri.equals(XADES_XMLNS))
        {
          if (localName.equals("QualifyingProperties"))
          {
            insideXades = true;
          }
          else if (insideXades)
          {
            if (localName.equals("QualifyingProperties") || localName.equals("SignedSignatureProperties"))
              ;
            else if (localName.equals("SigningTime"))
              currentElement = new StringBuffer();
            else if (localName.equals("SignedProperties"))
            {
              signingPropsId = attributes.getValue("Id");
            }
          }
          else
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        }
        else
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   * Description of the Method
   *
   * @param uri Description of Parameter
   * @param localName Description of Parameter
   * @param qName Description of Parameter
   * @exception SAXException Description of Exception
   */
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: "+uri+":" + localName);

    try
    {
      if (insideSignature)
      {
        if (uri.equals(DS_XMLNS))
        {
          if (localName.equals("Signature"))
          {
            insideSignature = false;
            if (parentHandler instanceof ContentContainerBuilder)
            {
              if (log.isDebugEnabled())
                log.debug("##########" + sig.signerId);

              String sigId = sig.signerId;

              if (sigId.charAt(0) == '#')
                sigId = sigId.substring(1);

              Role signer = ((ContentContainerBuilder)parentHandler).msg.getRoleForRefID(sigId);

              if (signer == null)
              {
                log.error("Das referenzierte Signer-Objekt konnte nicht gefunden werden.");
                throw new OSCISignatureException("no_signature_cert", sigId);
              }

              sig.signer = signer;
              ((ContentContainerBuilder)parentHandler).getContentContainer().addSignature(sig);
              sig.signedInfo = (byte[])((ContentContainerBuilder)parentHandler).can.getSignedInfos().remove(1);
              if (signingPropsId != null)
              {
                sig.signingProperties = ((ContentContainerBuilder)parentHandler).can.getSignedProperties()
                                                                                    .remove(0);
                sig.signingPropsId = signingPropsId;
                sig.signingTime = signingTime;
              }
            }
            else if (parentHandler instanceof OSCIMessageBuilder)
            {
              if (((de.osci.osci12.messagetypes.OSCIMessageBuilder)parentHandler).getOSCIMessage().signatureHeader != null)
                throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
              ((de.osci.osci12.messagetypes.OSCIMessageBuilder)parentHandler).getOSCIMessage().signatureHeader = sig;
            }
            this.xmlReader.setContentHandler(this.parentHandler);
          }
          else if (localName.equals("Object"))
          {
            insideObject = false;
          }
          else if (localName.equals("Reference"))
          {
            insideReference = false;
            if (log.isDebugEnabled())
            {
              String[] tr = sr.getTransformerAlgorithms();

              for ( int i = 0 ; i < tr.length ; i++ )
              {
                log.debug("\nTRANS: " + sr.getRefID() + " - " + tr[i]);
              }
            }

            sr = null;
          }
          else if (localName.equals("SignedInfo"))
          {
            insideSignedInfo = false;
          }
          else if (localName.equals("SignatureValue"))
            sig.signatureValue = de.osci.helper.Base64.decode(currentElement.toString());
          else if (localName.equals("KeyInfo"))
            insideKeyInfo = false;
          else if (insideReference)
          {
            if (localName.equals("DigestValue"))
            {
              if (log.isDebugEnabled())
                log.debug("ID " + sr.getRefID());

              byte[] dv = de.osci.helper.Base64.decode(currentElement.toString());
              sr.setDigestValue(dv);
            }
          }
        }
        else if (insideXades && uri.equals(XADES_XMLNS))
        {
          if (localName.equals("QualifyingProperties"))
          {
            insideXades = false;
          }
          else if (localName.equals("SigningTime")
              && (parentHandler instanceof ContentContainerBuilder))
          {
            signingTime = currentElement.toString();
          }
          else if ((localName.equals("SignedProperties") || localName.equals("SignedSignatureProperties")))
          {
            // nothing to do
          }
          else
          {
            throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
          }
        }else
        {
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
        }
      }
      else
      {
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
    }
    catch (Exception ex)
    {
      log.error("Fehler im End-Element!", ex);
      throw new SAXException(ex);
    }

    currentElement = null;
  }
}
