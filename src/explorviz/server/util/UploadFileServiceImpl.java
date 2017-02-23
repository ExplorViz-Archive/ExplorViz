package explorviz.server.util;

import java.io.*;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import explorviz.server.main.FileSystemHelper;

@WebServlet("/uploadfileservice")
public class UploadFileServiceImpl extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String EXP_ANSWER_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "experiment/answers";
	private static final String UPLOAD_DIRECTORY = "screenRecords";
	private static final int THRESHOLD_SIZE = 1024 * 200; // 200KB
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		// checks if the request actually contains upload file
		if (!ServletFileUpload.isMultipartContent(request)) {
			final PrintWriter writer = response.getWriter();
			writer.println("Request does not contain upload data");
			writer.flush();
			return;
		}

		// configures upload settings
		final DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(THRESHOLD_SIZE);
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

		final ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setFileSizeMax(MAX_FILE_SIZE);
		upload.setSizeMax(MAX_REQUEST_SIZE);

		// constructs the directory path to store upload file
		final String answerPath = EXP_ANSWER_FOLDER + File.separator;

		// writing a response
		response.setContentType("text/html");
		final PrintWriter out = response.getWriter();
		try {
			// parses the request's content to extract file data
			final List formItems = upload.parseRequest(request);
			final Iterator iter = formItems.iterator();

			// iterates over form's fields
			while (iter.hasNext()) {
				final FileItem item = (FileItem) iter.next();
				// processes only fields that are not form fields
				if (!item.isFormField()) {
					// parse the name of the file for information
					final String fileName = new File(item.getName()).getName();
					final String experimentName = getExperimentName(fileName);
					final String questionnairePrefix = getQuestionnairePrefix(fileName);
					final String recordsName = getRecordsName(fileName);

					final String filePath = answerPath + experimentName + "_" + questionnairePrefix
							+ File.separator + UPLOAD_DIRECTORY + File.separator;

					// creates the directory if it does not exist
					final File uploadPath = new File(filePath);
					if (!uploadPath.exists()) {
						uploadPath.mkdir();
					}
					final File storeFile = new File(filePath + recordsName);

					// saves the file on disk
					item.write(storeFile);
				}
			}

			out.write("Upload successful.");
		} catch (final Exception ex) {
			out.write("Error during upload.");
		}
	}

	private String getExperimentName(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String experimentName = tokens[0];
		return experimentName;
	}

	private String getQuestionnairePrefix(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String questionnairePrefix = tokens[1];
		return questionnairePrefix;
	}

	private String getRecordsName(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String recordsName = tokens[2];

		return recordsName;
	}

}