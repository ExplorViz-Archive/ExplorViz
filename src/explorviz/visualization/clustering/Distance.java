package explorviz.visualization.clustering;

public class Distance {

    public static double calcDistance(double mCall1, double mCall2, double aInst1, double aInst2, double stringDist) {

	// calculate distance as sum of euclidian distance and standardized
	// string distance
	double distance = 0;

	distance = Math.sqrt(Math.pow((mCall1 - mCall2), 2) + Math.pow((aInst1 - aInst2), 2)) + stringDist;

	return distance;
    }

    public static void main(String[] args) {

	System.out.println("the distance of (0.045, -0.20, -0.47, -0.47, -0.99) is: " + calcDistance(0.045, -0.20, -0.47, -0.47, -0.99));
    }

}
