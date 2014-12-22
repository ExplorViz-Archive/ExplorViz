package explorviz.plugin.capacitymanagement.cloud_control.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import explorviz.plugin.capacitymanagement.cloud_control.common.StringToMapParser;
import explorviz.plugin.capacitymanagement.cloud_control.openstack.OpenStackOutputParser;

/**
 * This class tests the StringToMapParser in slastic.lite.common package.
 * 
 * @author Soeren Mahmens, Erik Koppenhagen
 * 
 */
public class StringToMapParserTest {

	@Test
	public void testParsing() {
		StringToMapParser parser = new OpenStackOutputParser();
		final ArrayList<String> data = new ArrayList<String>();

		// first test
		data.add("| abc      |  a b c         |");
		data.add("|   def      |     d e f    |");
		data.add("|     ghi    |     g h i    |");

		parser.parseAndAddStringList(data);
		HashMap<String, String> map = parser.getMap();

		Assert.assertEquals("Numbers of entries should be 3.", 3, map.size());
		Assert.assertEquals("Checking entry of abc.", "a b c", map.get("abc"));
		Assert.assertEquals("Checking entry of def.", "d e f", map.get("def"));
		Assert.assertEquals("Checking entry of ghi.", "g h i", map.get("ghi"));
		Assert.assertEquals("Checking entry of mno.", null, map.get("mno"));

		// test deleting 'optical lines' and capital letters
		parser = new OpenStackOutputParser();
		data.clear();
		data.add("+-----------------------------------+");
		data.add("|   Property   |     Value   |");
		data.add("+--------------------------------+");
		data.add("|    abc     | ABC     |");
		data.add("|   DEF    |    DEF    |");
		data.add("|      ghi        |    GHI     |");
		data.add("|   JKL    |       |");
		data.add("+--------------------------------------+");

		parser.parseAndAddStringList(data);
		map = parser.getMap();

		Assert.assertEquals("Numbers of entries should be 5.", 5, map.size());
		Assert.assertEquals("Checking entry of abc.", "ABC", map.get("abc"));
		Assert.assertEquals("Checking entry of DEF.", "DEF", map.get("DEF"));
		Assert.assertEquals("Checking entry of ghi.", "GHI", map.get("ghi"));
		Assert.assertEquals("Checking entry of JKL.", "", map.get("JKL"));

		// test leaving out pipes
		parser = new StringToMapParser("\\|", new HashMap<String, String>());
		data.clear();
		data.add(" abc      |  a b c         |");
		data.add("   def      |     d e f    |");
		data.add("     ghi    |     g h i    |");

		parser.parseAndAddStringList(data);
		map = parser.getMap();

		Assert.assertEquals("Numbers of entries should be 3.", 3, map.size());
		Assert.assertEquals("Checking entry of abc.", "a b c", map.get("abc"));
		Assert.assertEquals("Checking entry of def.", "d e f", map.get("def"));
		Assert.assertEquals("Checking entry of ghi.", "g h i", map.get("ghi"));
		Assert.assertEquals("Checking entry of mno.", null, map.get("mno"));
	}

	@Test
	public void testMultiColumnParser() {
		final ArrayList<String> data = new ArrayList<String>();
		HashMap<String, String> map = null;

		data.add("|   abc    |   a1    |  a2 |   a3 |");
		data.add("|  def      |    b1     |     b2    |    b3     |");
		data.add("|  ghi     |    c1     |     c2     |     c3      |");
		data.add("|     jkl     |     d1    |     d2     |    d3      |");

		OpenStackOutputParser parser = new OpenStackOutputParser();
		parser.parseAndAddStringList(data, 0, 1);
		map = parser.getMap();

		Assert.assertEquals(4, map.size());

		Assert.assertEquals("a1", map.get("abc"));
		Assert.assertEquals("b1", map.get("def"));
		Assert.assertEquals("c1", map.get("ghi"));
		Assert.assertEquals("d1", map.get("jkl"));

		parser = new OpenStackOutputParser();
		parser.parseAndAddStringList(data, 1, 3);
		map = parser.getMap();

		Assert.assertEquals("a3", map.get("a1"));
		Assert.assertEquals("b3", map.get("b1"));
		Assert.assertEquals("c3", map.get("c1"));
		Assert.assertEquals("d3", map.get("d1"));

	}

}