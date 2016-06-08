//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.06.08 um 07:49:37 PM CEST 
//


package explorviz_question_xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the explorviz_question_xsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Greetings_QNAME = new QName("", "Greetings");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: explorviz_question_xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GreetingListType }
     * 
     */
    public GreetingListType createGreetingListType() {
        return new GreetingListType();
    }

    /**
     * Create an instance of {@link GreetingType }
     * 
     */
    public GreetingType createGreetingType() {
        return new GreetingType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GreetingListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Greetings")
    public JAXBElement<GreetingListType> createGreetings(GreetingListType value) {
        return new JAXBElement<GreetingListType>(_Greetings_QNAME, GreetingListType.class, null, value);
    }

}
