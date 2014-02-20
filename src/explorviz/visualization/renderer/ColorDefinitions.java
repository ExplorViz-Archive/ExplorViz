package explorviz.visualization.renderer;

import java.util.ArrayList;
import java.util.List;

import explorviz.visualization.engine.math.Vector4f;

public class ColorDefinitions {
	public static final Vector4f pipeColor = new Vector4f(1f, 0.596078f, 0.11372549f, 1f);

	public static final Vector4f systemPlusColor = new Vector4f(1f, 0.596078f, 0.11372549f, 1f);
	public static final Vector4f systemForegroundColor = new Vector4f(0f, 0f, 0f, 1f);
	public static final Vector4f systemBackgroundColor = new Vector4f(0.8f, 0.8f, 0.8f, 1f);
	// new Vector4f(1f, 1f, 1f, 1f);

	public static final Vector4f nodeGroupPlusColor = new Vector4f(1f, 0.596078f, 0.11372549f, 1f);
	public static final Vector4f nodeGroupBackgroundColor = new Vector4f(0.08235f, 0.6f,
			0.16470588f, 1f);
	// 5,6 new Vector4f(78 / 255f, 0 / 255f, 0 / 255f, 1f)
	// 3,4: new Vector4f(0 / 255f, 30 / 255f, 78 / 255f, 1f)
	// 1,2: new Vector4f(0.08235f, 0.6f, 0.16470588f, 1f)

	public static final Vector4f nodeForegroundColor = new Vector4f(1f, 1f, 1f, 1f);
	public static final Vector4f nodeBackgroundColor = new Vector4f(0f, 0.7568627f, 0.247058f, 1f);
	// 5,6 new Vector4f(176 / 255f, 30 / 255f, 0 / 255f, 1f)
	// 3,4: new Vector4f(0 / 255f, 106 / 255f, 193 / 255f, 1f)
	// 1,2: new Vector4f(0f, 0.7568627f, 0.247058f, 1f)

	public static final Vector4f applicationForegroundColor = new Vector4f(1f, 1f, 1f, 1f);
	public static final Vector4f applicationBackgroundColor = new Vector4f(0.2745098f, 0.090196f,
			0.705882f, 1f);
	public static final Vector4f applicationBackgroundRightColor = new Vector4f(111 / 255f,
			82 / 255f, 180 / 255f, 1f);
	// 6: new Vector4f(0 / 255f, 106 / 255f, 193 / 255f, 1f)
	// 5: new Vector4f(25 / 255f, 153 / 255f, 0 / 255f, 1f)
	// 4: new Vector4f(170 / 255f, 64 / 255f, 255 / 255f, 1f)
	// 3: new Vector4f(193 / 255f, 0 / 255f, 79 / 255f, 1f)
	// vorschlag1: new Vector4f(0.145098039f, 0.4470588f, 0.9215686274f, 1f)
	// vorschlag2: new Vector4f(0.2745098f, 0.090196f, 0.705882f, 1f)

	public static final List<Vector4f> componentColors = new ArrayList<Vector4f>();
	public static final Vector4f clazzColor = applicationBackgroundColor;

	static {
		createComponentColors();
	}

	private static void createComponentColors() {
		componentColors.add(systemBackgroundColor);
		componentColors.add(nodeGroupBackgroundColor);
		componentColors.add(nodeBackgroundColor);
		componentColors.add(nodeGroupBackgroundColor);
		componentColors.add(nodeBackgroundColor);
		componentColors.add(nodeGroupBackgroundColor);
		componentColors.add(nodeBackgroundColor);
		componentColors.add(nodeGroupBackgroundColor);
		componentColors.add(nodeBackgroundColor);
	}
}