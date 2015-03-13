package explorviz.plugin_server.rootcausedetection.algorithm;

import java.util.ArrayList;

import org.junit.Test;

import explorviz.plugin_server.rootcausedetection.util.Maths;

public class MathTest {

	@Test
	public void test() {
		ArrayList<Double> l = new ArrayList<>();
		l.add(-1.0d);
		l.add(-1.0d);
		l.add(-0.5d);
		l.add(1.0d);
		l.add(1.0d);
		l.add(1.0d);
		l.add(1.0d);
		System.out.println(Maths.unweightedPowerMean(l, 2.0d));
	}

}
