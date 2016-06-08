//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2016.06.08 um 07:49:37 PM CEST
//

package explorviz_question_xsd;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * <p>
 * Java-Klasse für GreetingListType complex type.
 *
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser
 * Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="GreetingListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Greeting" type="{}GreetingType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GreetingListType", propOrder = { "greeting" })
public class GreetingListType {

	@XmlElement(name = "Greeting", required = true)
	protected List<GreetingType> greeting;

	/**
	 * Gets the value of the greeting property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the greeting property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getGreeting().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link GreetingType }
	 *
	 *
	 */
	public List<GreetingType> getGreeting() {
		if (greeting == null) {
			greeting = new ArrayList<GreetingType>();
		}
		return greeting;
	}

}
