package explorviz.visualization.experiment.services;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import explorviz.shared.experiment.Answer;
import explorviz.shared.experiment.Question;
import explorviz.shared.model.Landscape;

/**
 * @author Santje Finke
 * 
 */
@RemoteServiceRelativePath("questionservice")
public interface QuestionService extends RemoteService {

	/**
	 * Fetches the questions from the server.s
	 * 
	 * @return an array containing all questions for the questionnaire
	 * @throws IOException
	 */
	public Question[] getQuestions() throws IOException;

	/**
	 * Writes an answer to the server.
	 * 
	 * @param answer
	 *            The answer to be saved
	 * @throws IOException
	 */
	public void writeAnswer(Answer answer) throws IOException;

	/**
	 * Fetches the statistical and qualitative feedback questions from the
	 * server that are meant to be shown before and after the tasks.
	 * 
	 * @return The statistical and qualitative feedback questions
	 * @throws IOException
	 */
	public String[] getVocabulary() throws IOException;

	/**
	 * Writes an answer to the server under an explicit given userID.
	 * 
	 * @param string
	 *            The answer to be saved
	 * @param id
	 *            The id of the user that gave the answer
	 * @throws IOException
	 */
	public void writeStringAnswer(String string, String id) throws IOException;

	/**
	 * Sets the maximum timestamp for the replayer.
	 * 
	 * @param timestamp
	 *            The timestamp the replayer stops at.
	 */
	void setMaxTimestamp(long timestamp);

	/**
	 * Fetches all answers from the server for usage on the client side.
	 * 
	 * @return All answers in one string.
	 * @throws IOException
	 */
	String downloadAnswers() throws IOException;

	/**
	 * Adds a question to the questions that are saved on the server.
	 * 
	 * @param question
	 *            The question to be added
	 * @throws IOException
	 */
	void saveQuestion(Question question) throws IOException;

	/**
	 * Deletes all questions that are saved on the server and saves the given
	 * questions.
	 * 
	 * @param question
	 *            The question to be saved
	 * @throws IOException
	 */
	void overwriteQuestions(Question question) throws IOException;

	/**
	 * @return The "skip question" setting
	 */
	boolean allowSkip();

	/**
	 * @return The language setting
	 */
	String getLanguage();

	/**
	 * Fetches the statistical and qualitative feedback questions for Extravis
	 * from the server that are meant to be shown before and after the tasks.
	 * 
	 * @return The statistical and qualitative feedback questions
	 * @throws IOException
	 */
	String[] getExtravisVocabulary() throws IOException;

	/**
	 * @return and empty landscape
	 */
	Landscape getEmptyLandscape();

}
