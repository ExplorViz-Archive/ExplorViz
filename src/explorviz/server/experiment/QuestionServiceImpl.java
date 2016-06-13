package explorviz.server.experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.zeroturnaround.zip.ZipUtil;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import explorviz.server.main.Configuration;
import explorviz.server.main.FileSystemHelper;
import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;
import explorviz.shared.model.Landscape;
import explorviz.visualization.engine.Logging;
import explorviz.visualization.experiment.services.QuestionService;

/**
 * @author Santje Finke
 *
 */
public class QuestionServiceImpl extends RemoteServiceServlet implements QuestionService {

	private static final long serialVersionUID = 3071142731982595657L;
	private static final Logger log = Logger.getLogger("QuestionService");
	private static String answerFolder;
	private static String experimentFolder;

	@Override
	public Question[] getQuestions() throws IOException {
		final ArrayList<Question> questions = new ArrayList<Question>();
		try {
			final String filePath = FileSystemHelper.getExplorVizDirectory()
					+ "/experiment/questions.txt";
			String text, answers, corrects, procTime, time, free;
			final BufferedReader br = new BufferedReader(new FileReader(filePath));
			text = br.readLine(); // read text
			int i = 0;
			while (null != text) {
				answers = br.readLine(); // read answers
				corrects = br.readLine(); // read correct answers
				free = br.readLine(); // read amount of free inputs
				procTime = br.readLine(); // read processing time
				time = br.readLine(); // read timestamp
				questions.add(new Question(i, text, answers, corrects, free, procTime, time));
				text = br.readLine(); // read text of next question
				i++;
			}
			br.close();
		} catch (final FileNotFoundException e) {
			log.severe(e.getMessage());
		}
		return questions.toArray(new Question[0]);
	}

	@Override
	public void writeAnswer(final Answer answer) throws IOException {
		String id = answer.getUserID();
		if (id.equals("")) {
			id = "DummyUser";
		}
		writeStringAnswer(answer.toCSV(), id);
	}

	@Override
	public void writeStringAnswer(final String string, final String id) throws IOException {
		makeDirectories();

		try {
			final FileOutputStream answerFile = new FileOutputStream(new File(answerFolder + "/"
					+ id + ".csv"), true);
			final String writeString = id + "," + string;
			answerFile.write(writeString.getBytes("UTF-8"));
			answerFile.flush();
			answerFile.close();
		} catch (final FileNotFoundException e) {
			log.severe(e.getMessage());
		}
	}

	@Override
	public String[] getVocabulary() throws IOException {
		final List<String> vocab = new ArrayList<String>();
		try {
			final String filePath = getServletContext().getRealPath("/experiment/") + "/"
					+ "statisticalQuestions.txt";
			final BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			line = br.readLine();
			while (null != line) {
				vocab.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (final FileNotFoundException e) {
			log.severe(e.getMessage());
		}
		return vocab.toArray(new String[0]);
	}

	@Override
	public void setMaxTimestamp(final long timestamp) {
		LandscapeReplayer.getReplayerForCurrentUser().setMaxTimestamp(timestamp);
	}

	@Override
	public String downloadAnswers() throws IOException {
		final List<Byte> result = new ArrayList<Byte>();

		makeDirectories();

		final File folder = new File(answerFolder);
		final File zip = new File(experimentFolder + "answers.zip");
		ZipUtil.pack(folder, zip);

		final byte[] buffer = new byte[1024];

		final InputStream is = new FileInputStream(zip);
		int b = is.read(buffer);
		while (b != -1) {
			for (int i = 0; i < b; i++) {
				result.add(buffer[i]);
			}
			b = is.read(buffer);
		}
		is.close();

		final byte[] buf = new byte[result.size()];
		for (int i = 0; i < result.size(); i++) {
			buf[i] = result.get(i);
		}
		final String encoded = Base64.encodeBase64String(buf);
		return encoded;
	}

	@Override
	public void saveQuestion(final Question question) throws IOException {
		if (experimentFolder == null) {
			experimentFolder = FileSystemHelper.getExplorVizDirectory() + "/experiment/";
			new File(experimentFolder).mkdir();
		}
		final String filePath = experimentFolder + "questions.xml";

		final String xml = toXML(question);

		try {
			final FileOutputStream questionFile = new FileOutputStream(new File(filePath), true);
			questionFile.write(xml.getBytes("UTF-8"));
			questionFile.flush();
			questionFile.close();
		} catch (final FileNotFoundException e) {
			log.severe(e.getMessage());
		}

		// test akr
		createQuestionFromXML();

	}

	@Override
	public void overwriteQuestions(final Question question) throws IOException {
		// delet old questions
		final String filePath = FileSystemHelper.getExplorVizDirectory()
				+ "/experiment/questions.txt";
		final File file = new File(filePath);
		final boolean ret = file.delete();
		if (ret) {
			log.info("File successfully deleted");
		}
		// save question
		saveQuestion(question);
	}

	@Override
	public String getLanguage() {
		return Configuration.selectedLanguage;
	}

	@Override
	public boolean allowSkip() {
		return Configuration.skipQuestion;
	}

	/**
	 * creates experiment and experiment/answers folder if they don't exist
	 */
	public void makeDirectories() {
		if (experimentFolder == null) {
			experimentFolder = FileSystemHelper.getExplorVizDirectory() + "/experiment/";
			new File(experimentFolder).mkdir();
		}
		if (answerFolder == null) {
			answerFolder = FileSystemHelper.getExplorVizDirectory() + "/experiment/answers";
			new File(answerFolder).mkdir();
		}
	}

	@Override
	public String[] getExtravisVocabulary() throws IOException {
		final List<String> vocab = new ArrayList<String>();
		try {
			final String filePath = getServletContext().getRealPath("/experiment/") + "/"
					+ "statisticalQuestionsExtravis.txt";
			final BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			line = br.readLine();
			while (null != line) {
				vocab.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (final FileNotFoundException e) {
			log.severe(e.getMessage());
		}
		return vocab.toArray(new String[0]);
	}

	@Override
	public Landscape getEmptyLandscape() {
		return EmptyLandscapeCreator.createEmptyLandscape();
	}

	private String toXML(final Question question) {

		final StringWriter sw = new StringWriter();

		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Question.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
					"./war/xml/explorviz_question.xsd");

			jaxbMarshaller.marshal(question, sw);

			return sw.toString();

		} catch (final JAXBException e) {
			e.printStackTrace();
		}

		return "XML parsing failed";
	}

	private void createQuestionFromXML() {

		JAXBContext jaxbContext;
		if (experimentFolder == null) {
			return;
		}

		final String filePath = experimentFolder + "questions.xml";

		try {
			jaxbContext = JAXBContext.newInstance(Question.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			// final SchemaFactory factory = SchemaFactory
			// .newInstance("./war/xml/explorviz_question.xsd");
			// final Schema schema = factory.newSchema();
			// unmarshaller.setSchema(schema);
			final StreamSource stream = new StreamSource(filePath);
			final JAXBElement<Question> unmarshalledObject = unmarshaller.unmarshal(stream,
					Question.class);
			final Question question = unmarshalledObject.getValue();
			Logging.log("Question text after parsing from XML to Question: " + question.getText());
		} catch (final JAXBException e) {
			e.printStackTrace();
		}
		// } catch (final SAXException e) {
		// e.printStackTrace();
		// }
	}
}
