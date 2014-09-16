package explorviz.visualization.clustering;

import java.util.Arrays;

public class Standardization {

    // calculate arithmetic Mean
    public static double arithmeticMean(double[] array) {

	double sum = 0;

	for (int i = 0; i < array.length; i++) {
	    sum += array[i];
	}

	return sum / array.length;

    }

    // calculate standard Deviation
    public static double standardDeviation(double[] array) {

	double sum = 0;
	double aMean = arithmeticMean(array);

	for (int i = 0; i < array.length; i++) {
	    sum += Math.pow((array[i] - aMean), 2);
	}

	return Math.sqrt(sum / array.length);

    }

    // calculate standardization for input
    public static double[] standardize(double[] array) {

	double[] standardizedArray = new double[array.length];
	double aMean = arithmeticMean(array);
	double sDeviation = standardDeviation(array);

	// if standard deviation = 0, then this parameter has no influence on
	// the distance between classes and therefore every value can be set to
	// 0 to avoid division by 0
	for (int i = 0; i < array.length; i++) {
	    if (sDeviation == 0) {
		standardizedArray[i] = 0;
	    } else {
		standardizedArray[i] = ((array[i] - aMean) / sDeviation);
	    }
	}
	return standardizedArray;
    }

    public static void main(String[] args) {

	// test with kieker.monitoring.core.controller data
	double[] methodCalls = new double[] { 6, 5, 1, 7, 1, 14, 1, 6, 5, 12, 6 };

	System.out.println("the arithmetic mean of " + Arrays.toString(methodCalls) + " is: " + arithmeticMean(methodCalls));
	System.out.println("standard deviation of " + Arrays.toString(methodCalls) + " is: " + standardDeviation(methodCalls));
	System.out.println("standardization of " + Arrays.toString(methodCalls) + " is: " + Arrays.toString(standardize(methodCalls)));

	double[] activeInstances = new double[] { 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1 };

	System.out.println("the arithmetic mean of " + Arrays.toString(activeInstances) + " is: " + arithmeticMean(activeInstances));
	System.out.println("standard deviation of " + Arrays.toString(activeInstances) + " is: " + standardDeviation(activeInstances));
	System.out.println("standardization of " + Arrays.toString(activeInstances) + " is: " + Arrays.toString(standardize(activeInstances)));

	double[] cNameDistances = new double[] { 5, 7, 4, 19, 9, 23, 8, 7, 4, 8, 8, 6, 20, 10, 18, 8, 7, 5, 9, 7, 18, 8, 26, 7, 8, 5, 8, 17, 7, 24, 6, 8, 4, 8,
		10, 25, 18, 17, 19, 19, 24, 8, 7, 9, 9, 26, 25, 23, 26, 8, 7, 8, 7, 9, 8 };

	System.out.println("the arithmetic mean of " + Arrays.toString(cNameDistances) + " is: " + arithmeticMean(cNameDistances));
	System.out.println("standard deviation of " + Arrays.toString(cNameDistances) + " is: " + standardDeviation(cNameDistances));
	System.out.println("standardization of " + Arrays.toString(cNameDistances) + " is: " + Arrays.toString(standardize(cNameDistances)));

    }
}
