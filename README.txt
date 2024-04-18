Readme zur OSCI-Bibliothek in der Version 2.4.1
===============================================

Kontakt
-------
| Governikus GmbH & Co. KG
| Hochschulring 4
| 28359 Bremen
| osci@governikus.de


Lizenz
------
Mit der Spezifikation des OSCI 1.2-Protokolls wird ein sicheres,
herstellerunabhängiges und interoperables Datenaustauschformat beschrieben.
Um die Implementierung für Anwender in der öffentlichen Verwaltung sowie der
Fachverfahrenshersteller zu erleichtern, stellen wir im Kontext der Anwendung
Governikus des IT-Planungsrates die OSCI Bibliothek zum Download unter der EUPL-Lizenz
zur Verfügung. Erfolgt die Integration der OSCI Bibliothek in unveränderter Form,
liegt keine Bearbeitung im Sinne der EUPL bzw. des deutschen Urheberrechts vor.
Die Art und Weise der Verlinkung der OSCI Bibliothek innerhalb einer Implementierung
führt insbesondere nicht zur Schaffung eines abgeleiteten Werkes. Die unveränderte
Übernahme der OSCI Bibliothek in eine Fachverfahrens- und/oder Clientsoftware führt
damit nicht dazu, dass dies Fachverfahrens- und/oder Clientsoftware unter den
Bedingungen der EUPL zu lizenzieren ist. Für die Weitergabe der OSCI Bibliothek selbst,
in unveränderter oder bearbeiteter Form, als Quellcode oder ausführbares Programm,
gelten die Lizenzbedingungen der EUPL in unveränderter Weise.

Der mitgelieferte Quellcode wird unter der EUPL v1.2 
bereitgestellt, mit Ausnahme der Bibliotheken BouncyCastle 
und Commons-Logging. Informationen zu den Lizenzen sind der Datei 
LICENSE.txt zu entnehmen.


Inhalt des Archivs
------------------
In diesem Archiv finden Sie die OSCI-Bibliothek in der
Version 2.4.1. Die vorliegende OSCI-Bibliothek setzt die
OSCI-Transport 1.2-Spezifikation um. Die OSCI-Spezifikation wird
durch die Koordinierungsstelle für IT-Standards (KoSIT)
herausgegeben. Die Spezifikationsdokumente finden Sie auf den
Seiten der KoSIT unter
https://www.xoev.de/downloads-2316#Standards



Enthalten ist folgendes:
- die eigentliche Java-Version der OSCI-Bibliothek (/lib)
- die Quellen der OSCI-Bibliothek (/src)
- die API-Dokumentation (/apidoc)
- die Funktionsbeschreibung (Funktionsbeschreibung.pdf)
- die Versionshistorie (OSCI-Bibliothek_Versionshistorie.pdf)
- und Anwendungsbeispiele (/beispielanwendung).


Die vorliegende OSCI-Bibliothek ist in JAVA implementiert. Alle
benötigten JAVA-Archive befinden sich im Verzeichnis /lib.
Dieses Verzeichnis beinhaltet neben der eigentlichen
OSCI-Bibliothek (osci-bibliothek.jar) zusätzliche externe JAVA-Archive
(jar-Dateien), die für den Betrieb der OSCI-Bibliothek notwendig
sind und im Classpath der Anwendung eingetragen sein müssen:

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



Hinweise zum JDK
----------------

Die zur Ausführung benötigte Java-Umgebung (ab JDK 1.8 inkl. JDK 11 und 17)
ist über die Homepage von Oracle zu erhalten.
Bitte beachten Sie die Lizenzen für Drittanbietersoftware.

Für die sogenannte "starke Kryptographie" benötigt die
Java-Laufzeitumgebung die Installation zusätzlicher
"policy files". Diese sind ebenfalls auf der o.g. Java-
Downloadseite (s. "Java Cryptography Extension (JCE)
Unlimited Strength Jurisdiction Policy Files") verfügbar.

Die Quellen der OSCI-Bibliothek befinden sich im Verzeichnis
/src. Um sie zu übersetzen, sind die oben benannten
Voraussetzungen nötig.

Im Verzeichnis /beispielanwendung/de/osci/osci12/samples/
befinden sich mehrere Beispielanwendungen.
