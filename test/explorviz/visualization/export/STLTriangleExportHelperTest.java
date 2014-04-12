package explorviz.visualization.export;

import static org.junit.Assert.*;

import org.junit.Test;

public class STLTriangleExportHelperTest {

	@Test
	public void testToIEEEFloat() {
		assertEquals("1.000000e+00", STLTriangleExportHelper.toIEEEFloat(1f));
		assertEquals("-1.000000e+00", STLTriangleExportHelper.toIEEEFloat(-1f));
		assertEquals("-1.000000e+06", STLTriangleExportHelper.toIEEEFloat(-1000000f));
		assertEquals("0.000000e+00", STLTriangleExportHelper.toIEEEFloat(0f));
	}

	@Test
	public void testRoundToSixPlaces() {
		assertEquals("1.000000", STLTriangleExportHelper.roundToSixPlaces(1f));
		assertEquals("-1.000000", STLTriangleExportHelper.roundToSixPlaces(-1f));
		assertEquals("0.000000", STLTriangleExportHelper.roundToSixPlaces(0f));
	}

}
