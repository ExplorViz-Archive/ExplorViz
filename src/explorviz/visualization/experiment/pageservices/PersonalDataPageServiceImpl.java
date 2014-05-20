package explorviz.visualization.experiment.pageservices;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class PersonalDataPageServiceImpl extends RemoteServiceServlet implements
		TutorialMenuService {

	private static final long serialVersionUID = -1013985260361101696L;

	@Override
	public String getPage() {
		return "personaldata";
	}
}
