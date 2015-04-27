package explorviz.visualization.main;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

public class MathHelpersTest {

	@Test
	public void testGetCategoriesForCommunication() throws Exception {
		final ArrayList<Integer> arrayList = new ArrayList<Integer>();
		arrayList.add(0);
		arrayList.add(1);
		arrayList.add(2);
		arrayList.add(3);
		arrayList.add(4);

		Map<Integer, Float> categories = MathHelpers.getCategoriesForCommunication(arrayList);

		assertEquals((Integer) 0, categories.get(0));
		assertEquals((Integer) 1, categories.get(1));
		assertEquals((Integer) 2, categories.get(2));
		assertEquals((Integer) 3, categories.get(3));
		assertEquals((Integer) 4, categories.get(4));

		arrayList.clear();

		arrayList.add(0);

		categories = MathHelpers.getCategoriesForCommunication(arrayList);

		assertEquals((Integer) 0, categories.get(0));

		arrayList.clear();

		arrayList.add(0);
		arrayList.add(10);
		arrayList.add(20);
		arrayList.add(30);
		arrayList.add(40);

		categories = MathHelpers.getCategoriesForCommunication(arrayList);

		assertEquals((Integer) 0, categories.get(0));
		assertEquals((Integer) 1, categories.get(10));
		assertEquals((Integer) 2, categories.get(20));
		assertEquals((Integer) 3, categories.get(30));
		assertEquals((Integer) 4, categories.get(40));
	}
}
