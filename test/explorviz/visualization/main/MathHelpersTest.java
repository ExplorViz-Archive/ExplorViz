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

		assertEquals((Float) 0.0f, categories.get(0));
		assertEquals((Float) 2.5f, categories.get(1));
		assertEquals((Float) 4.0f, categories.get(2));
		assertEquals((Float) 6.5f, categories.get(3));
		assertEquals((Float) 6.5f, categories.get(4));

		arrayList.clear();

		arrayList.add(0);

		categories = MathHelpers.getCategoriesForCommunication(arrayList);

		assertEquals((Float) 0.0f, categories.get(0));

		arrayList.clear();

		arrayList.add(0);
		arrayList.add(10);
		arrayList.add(20);
		arrayList.add(30);
		arrayList.add(40);

		categories = MathHelpers.getCategoriesForCommunication(arrayList);

		assertEquals((Float) 0.0f, categories.get(0));
		assertEquals((Float) 2.5f, categories.get(10));
		assertEquals((Float) 4.0f, categories.get(20));
		assertEquals((Float) 6.5f, categories.get(30));
		assertEquals((Float) 6.5f, categories.get(40));
	}
}
