Readme zu den mitgelieferten Bibliotheken der OSCI-Bibliothek in der Version 2.4.1

In diesem Verzeichnis sind sowohl die OSCI-Bibliothek, als auch die benötigten
Bibliotheken anderer Anbieter enthalten.

Die Dateien sind im Classpath einzubinden.

Folgende zusätzlichen Bibliotheken sind enthalten:

- commons-logging.jar
  Das Archiv commons-logging.jar in der Version 1.2
  stammt aus dem Apache Commons Project
  (siehe http://commons.apache.org/proper/commons-logging/)
  und ermöglicht die Anbindung verschiedener Logging-APIs.
  Die optionale Konfiguration kann der zugehörigen Dokumentation
  entnommen werden. Mit der Default-Einstellung wird der
  JAVA-eigene Logging-Mechanismus verwendet.

- bcprov-jdk18on.jar
  Das Archiv bcprov-jdk18on.jar in der Version 1.76 entstammt
  ebenfalls einem Open-Source Projekt (Bouncy Castle,
  http://www.bouncycastle.org) und beinhaltet einen JCE
  (Java Cryptography Extension)-Provider.

- javax.servlet-api.jar
  Das Archiv javax.servlet-api.jar in der Version 4.0.1 enthält
  die Java Servlet API (https://javaee.github.io/servlet-spec/)
  und wird ausschließlich für eines der Code-Beispiele benötigt.


Die folgenden Bibliotheken werden ausschließlich für die Verarbeitung des
MessageMetaData-Custom-Headers benötigt:

- xsd-osci-messagemetadata.jar
  Das Archiv enthält die generierten MessageMetaData-v2.0.2-Klassen, die für die programmatische
  Anbindung des Custom-Headers benötigt werden

- jakarta.activation-api.jar und jakarta.activation.jar
  Die Archive in der Version 1.2.2 enthalten die Jakarta-Activation-Bibliothek
  (https://jakarta.ee/specifications/activation/1.2/)

- jakarta.xml.bind-api.jar und jaxb-impl.jar
  Die Archive in den Versionen 2.3.3 (API) und 2.3.7 (Implementierung)
  enthalten die Jakarta-JAXB-API und deren Implementierung (https://javaee.github.io/jaxb-v2/)

- xsd-xoev-basisdatentypen.jar, xsd-ws-addr.jar und xsd-oasis-200401-wss-wssecurity-secext.jar
  Diese Archive enthalten weitere generierte Klassen, die für die Benutzung der
  MessageMetaData-Klassen benötigt werden

