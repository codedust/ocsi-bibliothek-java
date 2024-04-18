package de.osci.osci12.samples;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.common.SoapClientException;
import de.osci.osci12.common.SoapServerException;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messagetypes.AcceptDelivery;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIRequest;
import de.osci.osci12.messagetypes.OSCIResponseTo;
import de.osci.osci12.messagetypes.PassiveRecipientParser;
import de.osci.osci12.messagetypes.ProcessDelivery;
import de.osci.osci12.messagetypes.ResponseToAcceptDelivery;
import de.osci.osci12.messagetypes.ResponseToProcessDelivery;
import de.osci.osci12.messagetypes.SOAPFault;
import de.osci.osci12.roles.Addressee;


/**
 * This is a sample implementation of servlet acting as a passive recipient
 * in the synchronous communication scenarios according to the OSCI 1.2-trensport
 * specification. This demo supports the request-response scenario and the
 * one-way message. In case of request-response, the received data contained
 * in Content-Objects (unencrypted) are returned, prefixed with the String
 * "Return received content data: ". In the one-way-scenario, the received
 * content data is written to a file located in the servlets context directory.
 * The filename is the message-Id, the extension ".txt".
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 *
 * @author N.Büngener
 * @version 0.9
 */
public class PassiveRecipient extends HttpServlet
{
  private PassiveRecipientParser userParser;

  // Helper method for sending SOAP-error messages
  private void sendSOAPError(OutputStream out, String code)
                      throws IOException
  {
    new SOAPFault(code).writeToStream(out);
    out.close();
  }

  /**
   * Servlets initialization-method. For documentation see JAVA-Servlet documents.
   */
  public void init() throws ServletException
  {
    try
    {
      // Create the Addressee representing the recipient
      Addressee addresse = new Addressee(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/bob_signature_4096.p12",
                                                                                             "123456"),
                                         new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.p12",
                                                                                                "123456"));

      // Install the Addressee as default supplier.
      // A later version will support an array of default suppliers for
      // use of more than one Signer/Decrypter
      DialogHandler.setDefaultSupplier(addresse);
      // An instance of the PassiveRecipientParser is needed for parsing the incoming messages
      userParser = new PassiveRecipientParser();
    }
    catch (Exception ex)
    {
      throw new ServletException(ex);
    }
  }

  /**
   * Servlets doPost-method. For documentation see JAVA-Servlet documents.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
              throws ServletException,
                     IOException
  {
    OSCIRequest requestMessage;
    OSCIResponseTo responseMessage = null;

    // error handling is not implemented completely here. Warnings are ignored.
    String feedback = "0800";
    requestMessage = null;

    // Parse the incoming message
    try
    {
      requestMessage = userParser.parseStream(request.getInputStream());
    }
    catch (SoapServerException ex)
    {
      // Send SOAP server error
      sendSOAPError(response.getOutputStream(), ex.getErrorCode());

      return;
    }
    catch (SoapClientException ex)
    {
      // Send SOAP client error
      sendSOAPError(response.getOutputStream(), ex.getErrorCode());

      return;
    }
    catch (OSCIErrorException ex)
    {
      ex.printStackTrace();
      feedback = ex.getErrorCode();
      requestMessage = (OSCIRequest)ex.getOSCIMessage();
    }
    catch (Exception ex)
    {
      // General fault
      ex.printStackTrace();
      feedback = OSCIErrorCodes.InternalErrorSupplier.getOSCICode();
      sendSOAPError(response.getOutputStream(),OSCIErrorCodes.SoapServerInternalErrorSupplier.getOSCICode());
      return;
    }

    try
    {
      switch (requestMessage.getMessageType())
      {
      // Request-response scenario
      case (OSCIMessage.PROCESS_DELIVERY):

        // For convenience, assign received message to a new name
        ProcessDelivery procDel = (ProcessDelivery) requestMessage;

        // Create the response message
        ResponseToProcessDelivery rsp2procDel = new ResponseToProcessDelivery(procDel);

        // optional: extract ProcessCardBundles....
        //        procDel.getProcessCardBundle();
        if (procDel.getMessageId() != null) // with protocol ?

          rsp2procDel.setMessageId(procDel.getMessageIdResponse());

        if (feedback.startsWith("0"))
        {
          // Extract the content-data and return it
          for (int i = 0; i < procDel.getContentContainer().length; i++)
          {
            // Create a new ContentContainer and add all Contents with data
            // that can be found
            ContentContainer cc = new ContentContainer();

            for (int j = 0; j < procDel.getContentContainer()[i].getContents().length; j++)
            {
              if (procDel.getContentContainer()[i].getContents()[j].getContentType() == Content.DATA)
                cc.addContent(new Content("Return received content data: " +
                                          procDel.getContentContainer()[i].getContents()[i].getContentData()));
            }

            // Add conatiner to response message
            rsp2procDel.addContentContainer(cc);
          }

          rsp2procDel.setSubject("Response data");
        }

        // Assing created message to response
        responseMessage = rsp2procDel;
        // Simplified feedback handling: return only one code
        rsp2procDel.setFeedback(new String[] { feedback });

        // If the request was signed, the supplier should sign the response as well
        if (requestMessage.isSigned())
          rsp2procDel.sign();

        break;

      // One-Way message scenario
      case (OSCIMessage.ACCEPT_DELIVERY):

        // For convenience, assign received message to a new name
        AcceptDelivery accDel = (AcceptDelivery) requestMessage;

        // Create the response message
        ResponseToAcceptDelivery rsp2accDel = new ResponseToAcceptDelivery(accDel);

        // optional: extract ProcessCardBundles....
        //        accDel.getProcessCardBundle();
        if (feedback.startsWith("0"))
        {
          // Extract the content-data and write it to a file
          FileWriter out = new FileWriter(getServletContext().getRealPath("/" + accDel.getMessageId() + ".txt"));

          for (int i = 0; i < accDel.getContentContainer().length; i++)
          {
            for (int j = 0; j < accDel.getContentContainer()[i].getContents().length; j++)
            {
              if (accDel.getContentContainer()[i].getContents()[j].getContentType() == Content.DATA)
                out.write(accDel.getContentContainer()[i].getContents()[i].getContentData());
            }
          }

          out.close();
        }

        // Simplified feedback handling: return only one code
        rsp2accDel.setFeedback(new String[] { feedback });

        // If the request was signed, the supplier should sign the response as well
        if (requestMessage.isSigned())
          rsp2accDel.sign();

        responseMessage = rsp2accDel;

        break;
      }

      // Get output stream to write response to
      OutputStream out = response.getOutputStream();

      // Select the writeToStream-method
      if (responseMessage instanceof ResponseToAcceptDelivery)
        ((ResponseToAcceptDelivery) responseMessage).writeToStream(out, null);
      else
        ((ResponseToProcessDelivery) responseMessage).writeToStream(out, null);

      // Ready
      out.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }
}
