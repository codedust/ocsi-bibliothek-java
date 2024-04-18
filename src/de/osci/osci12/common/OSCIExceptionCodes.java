package de.osci.osci12.common;

/**
 * Diese Klasse definiert die OSCI Warning und Error Codes
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
 * @author R. Lindemann
 * @version 2.4.1
 */
public class OSCIExceptionCodes
{

  /**
   * Definition der verschiedenen Fehler, Warnung und Positivmeldungen.
   */
  public enum OSCIFeedBackTypes
  {
    Warning, OSCIException, SoapClient, SoapServer, Ok
  }

  /**
   * Definition der Positivmeldung. (Nuller Fehlercodes)
   */
  public enum OSCIOkCodes implements OSCIExceptionCodesI
  {
    Ok("0800"), OkFurther("0801");

    private String osciCode;

    OSCIOkCodes(String osciCode)

    {
      this.osciCode = osciCode;
    }

    /**
     * Liefert die Eingestellte OSCI Warnung
     *
     * @return osciCode
     */
    @Override
    public String getOSCICode()
    {
      return osciCode;
    }

    /**
     * Returns the type of the Error code from the type OSCIFeedBackTypes
     */
    @Override
    public OSCIFeedBackTypes getOSCIFeedBackTypes()
    {
      return OSCIFeedBackTypes.Ok;
    }

  }

  /**
   * Eine Liste von den Warnungen (Dreier Fehlercodes) auf Auftragsebene
   */
  public enum OSCIWarnings implements OSCIExceptionCodesI
  {
    EncCertClientOutOfTimeRange("3500"),
    EncCertCheckNotCompleted("3501"),
    SigCertClientOutOfTimeRange("3700"),
    SigCertAuthorOutOfTimeRange("3701"),
    SignatureOfAuthorSigCertInvalid("3702"),
    SigCertAuthorRevoked("3703"),
    EncCertAuthorOutOfTimeRange("3704"),
    EncCertRecipientOutOfTimeRange("3705"),
    EncCertReaderOutOfTimeRange("3706"),
    CertificateCheckNotCompleted("3707"),
    SigCerRecipientOutOfTimeRange("3708"),
    FurtherDeliveriesPresent("3800"),
    FurtherProcessCardsPresent("3801"),
    SignatureOfForwardingMsgRspMissing("3802"),
    UnencryptedResponseMessage("3803"),
    ContainingInsideFeedbackWarning("3950"),
    ChunkMessageTimeOutExceeded("3951"),
    PartialMessageInProcessing("3952");

    private String osciCode;

    OSCIWarnings(String osciCode)

    {
      this.osciCode = osciCode;
    }

    /**
     * Liefert die Eingestellte OSCI Warnung
     *
     * @return osciCode
     */
    @Override
    public String getOSCICode()
    {
      return osciCode;
    }

    /**
     * Returns the type of the Error code from the type OSCIFeedBackTypes
     */
    @Override
    public OSCIFeedBackTypes getOSCIFeedBackTypes()
    {
      return OSCIFeedBackTypes.Warning;
    }

  }

  /**
   * Eine Liste von den Error (Neuner Fehlercodes) auf Auftragsebene
   *
   * @author Lindemann
   */
  public enum OSCIErrorCodes implements OSCIExceptionCodesI
  {
    RequestNotSigned("9600", OSCIFeedBackTypes.OSCIException),
    SignatureInvalid("9601", OSCIFeedBackTypes.OSCIException),
    NotAllRelevantPartsSigned("9602", OSCIFeedBackTypes.OSCIException),
    InternalErrorSignatureCheck("9603", OSCIFeedBackTypes.OSCIException),
    SigCertClientInvalid("9700", OSCIFeedBackTypes.OSCIException),
    SigCertClientRevoked("9701", OSCIFeedBackTypes.OSCIException),
    SignatureOfAuthorEncCertInvalid("9704", OSCIFeedBackTypes.OSCIException),
    EncCertAuthorRevoked("9705", OSCIFeedBackTypes.OSCIException),
    SignatureOfRecipientEncCertInvalid("9706", OSCIFeedBackTypes.OSCIException),
    EncCertRecipientRevoked("9707", OSCIFeedBackTypes.OSCIException),
    SignatureOfReaderEncCertInvalid("9708", OSCIFeedBackTypes.OSCIException),
    EncCertReaderRevoked("9709", OSCIFeedBackTypes.OSCIException),
    InternalErrorWhileCertCheck("9710", OSCIFeedBackTypes.OSCIException),
    SigCertOfRecipientInvalid("9711", OSCIFeedBackTypes.OSCIException),
    SigCertOfRecipientRevoked("9712", OSCIFeedBackTypes.OSCIException),
    NoMessageID("9800", OSCIFeedBackTypes.OSCIException),
    IllegalMessageId("9801", OSCIFeedBackTypes.OSCIException),
    NoExplicitDialog("9802", OSCIFeedBackTypes.OSCIException),
    NoDeliveryPresent("9803", OSCIFeedBackTypes.OSCIException),
    NoProcessCardsPresent("9804", OSCIFeedBackTypes.OSCIException),
    DeliveryNotAcceptedByRecipient("9805", OSCIFeedBackTypes.OSCIException),
    NoResponseAvailableByRecipient("9806", OSCIFeedBackTypes.OSCIException),
    RecipientsSignatureWrong("9807", OSCIFeedBackTypes.OSCIException),
    InternalErrorByRecipient("9808", OSCIFeedBackTypes.OSCIException),
    InternalForwardingErrorNoDelivery("9809", OSCIFeedBackTypes.OSCIException),
    InternalForwardingErrorDelivered("9810", OSCIFeedBackTypes.OSCIException),
    InternalErrorSupplier("9811", OSCIFeedBackTypes.OSCIException),
    // Partial Chunk msg Errors
    DuplicateChunk("9900", OSCIFeedBackTypes.OSCIException),
    WrongChunkNumber("9901", OSCIFeedBackTypes.OSCIException),
    WrongMaxChunkSize("9902", OSCIFeedBackTypes.OSCIException),
    WrongMinChunkSize("9903", OSCIFeedBackTypes.OSCIException),
    WrongMaxMessageSize("9904", OSCIFeedBackTypes.OSCIException),
    WrongChunkInformation("9905", OSCIFeedBackTypes.OSCIException),
    ContainingInsideFeedbackError("9950", OSCIFeedBackTypes.OSCIException),
    // Soap Client exception
    OSCIMsgStructureNotValid("9100", OSCIFeedBackTypes.SoapClient),
    NoEncKeyPresentOnMessgeLevel("9200", OSCIFeedBackTypes.SoapClient),
    EncCertMessageLevelRevoked("9201", OSCIFeedBackTypes.SoapClient),
    CouldNotDecryptRequestData("9202", OSCIFeedBackTypes.SoapClient),
    NoValidRequestData("9300", OSCIFeedBackTypes.SoapClient),
    WrongControlBlock("9400", OSCIFeedBackTypes.SoapClient),
    NoEncCertInRequestData("9500", OSCIFeedBackTypes.SoapClient),
    SignatureOfClientEncCertInvalid("9501", OSCIFeedBackTypes.SoapClient),
    EncCertClientRevoked("9502", OSCIFeedBackTypes.SoapClient),
    // Soap server exception
    SoapServerInternalErrorSupplierOnEncCertClient("9503", OSCIFeedBackTypes.SoapServer),
    SoapServerInternalErrorSupplier("9000", OSCIFeedBackTypes.SoapServer);

    private String osciCode;

    private OSCIFeedBackTypes typeOfFeedBack;

    OSCIErrorCodes(String osciCode, OSCIFeedBackTypes typeOfFeedBack)

    {
      this.osciCode = osciCode;
      this.typeOfFeedBack = typeOfFeedBack;
    }

    /**
     * Liefert den Eingestellten OSCI Error Code
     *
     * @return osciCode
     */
    @Override
    public String getOSCICode()
    {
      return osciCode;
    }

    /**
     * Returns the type of the Error code from the type OSCIFeedBackTypes
     */
    @Override
    public OSCIFeedBackTypes getOSCIFeedBackTypes()
    {
      return typeOfFeedBack;
    }

    public static OSCIErrorCodes fromErrorCode(String errorCode) {
      for (OSCIErrorCodes b : OSCIErrorCodes.values()) {
        if (b.getOSCICode().equalsIgnoreCase(errorCode)) {
          return b;
        }
      }
      return null;
    }

  }
  /**
   * Interface für alle OSCIExceptionCode Enumerations
   *
   * @author Lindemann
   */
  public interface OSCIExceptionCodesI
  {

    public String getOSCICode();

    public OSCIFeedBackTypes getOSCIFeedBackTypes();
  }

}
