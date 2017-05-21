package explorviz.server.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import explorviz.server.main.FileSystemHelper;

@WebServlet("/uploadfileservice")
public class UploadFileServiceImpl extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String EXP_ANSWER_FOLDER = FileSystemHelper.getExplorVizDirectory()
			+ File.separator + "experiment/answers";
	private static final String UPLOAD_DIRECTORY = "screenRecords";
	private static final int THRESHOLD_SIZE = 1024 * 200; // 200KB
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 1024; // 1024MB
	private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 1024; // 1024MB

	/**
	 * Function which gets called in case of a POST request and expects an
	 * uplaod of a big file
	 *
	 * @param request
	 *            HTTPServletRequest containing data to upload to server
	 * @param response
	 *            contains either 'Upload successful.' or 'Error during upload.'
	 */
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		String partAndNumber = "";
		String fileName = "";

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
					fileName = new File(item.getName()).getName();
					final String experimentName = getExperimentName(fileName);
					final String questionnairePrefix = getQuestionnairePrefix(fileName);
					String recordsName = getRecordsName(fileName);
					partAndNumber = getPartAndNumber(fileName);
					recordsName = recordsName + "_" + partAndNumber;

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
			// return with response
			out.write("Upload successful.");
		} catch (final Exception ex) {
			System.out.println(ex);
			out.write("Error during upload.");
		}

		// check whether there exists parts that could be merged
		/*
		 * if (!(partAndNumber.equals("part0.webm")) &&
		 * !(partAndNumber.equals(""))) { mergeScreenRecordParts(fileName); } ->
		 * not working
		 */
	}

	// helper parser function for the pure experiment name
	private String getExperimentName(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String experimentName = tokens[0];
		return experimentName;
	}

	// helper parser function for the questionnairePrefix
	private String getQuestionnairePrefix(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String questionnairePrefix = tokens[1];
		return questionnairePrefix;
	}

	// helper parser function for the name of the file, the name of the userID
	private String getRecordsName(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String recordsName = tokens[2];

		return recordsName;
	}

	// helper parser function for the part of the file
	private String getPartAndNumber(final String fileName) {
		final String delims = "[_]";
		final String[] tokens = fileName.split(delims);
		final String partAndNumber = tokens[3];

		return partAndNumber;
	}

	private void mergeScreenRecordParts(final String fileName) {
		final String experimentName = getExperimentName(fileName);
		final String questionnairePrefix = getQuestionnairePrefix(fileName);
		final String userID = getRecordsName(fileName);
		final String partAndNumber = getPartAndNumber(fileName);
		final String uploadFolder = EXP_ANSWER_FOLDER + File.separator + experimentName + "_"
				+ questionnairePrefix + File.separator + UPLOAD_DIRECTORY;
		final String fileEnding = ".mp4";
		final String part0 = userID + "_part0" + fileEnding;

		// check again whether there are more parts than one
		if ((partAndNumber != "") && (partAndNumber != "part0.webm")) { // TODO
																		// change
																		// to
																		// equlas
			try {
				mergeParts(part0, userID + "_" + partAndNumber, uploadFolder);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void mergeParts(final String fileNamePart0, final String fileNamePartN,
			final String uploadFolder) throws Exception {
		final File merge = new File(uploadFolder + File.separator + fileNamePart0);
		final String f1 = uploadFolder + File.separator + fileNamePart0;
		final String f2 = uploadFolder + File.separator + fileNamePartN;
		if (!merge.exists()) {

			final InputStream in = new FileInputStream(new File(f2));
			final OutputStream out = new FileOutputStream(new File(f1));

			// Transfer bytes from in to out
			final byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			// Log.d("audio concatenation", "was copied");

		} else {

			Movie[] inMovies;

			inMovies = new Movie[] { MovieCreator.build(f1), MovieCreator.build(f2), };

			final List<Track> videoTracks = new LinkedList<Track>();
			final List<Track> audioTracks = new LinkedList<Track>();

			for (final Movie m : inMovies) {
				for (final Track t : m.getTracks()) {
					if (t.getHandler().equals("soun")) {
						audioTracks.add(t);
					}
					if (t.getHandler().equals("vide")) {
						videoTracks.add(t);
					}
				}
			}

			final Movie result = new Movie();
			if (videoTracks.size() > 0) {
				result.addTrack(
						new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
			}

			if (audioTracks.size() > 0) {
				result.addTrack(
						new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
			}

			final Container out = new DefaultMp4Builder().build(result);

			final RandomAccessFile ram = new RandomAccessFile(
					String.format(uploadFolder + "/tempOutput.webm"), "rw");
			final FileChannel fc = ram.getChannel();
			out.writeContainer(fc);
			ram.close();
			fc.close();
			// IsoFile out = (IsoFile) new DefaultMp4Builder().build(result);
			// FileOutputStream fos = new FileOutputStream(new File(
			// String.format(context.getCacheDir() + "/output.mp4")));
			// out.getBox(fos.getChannel());
			// fos.close();
			final String mergedFilepath = String.format(uploadFolder + "/tempOutput.webm");

			// copy(new File(mergedFilepath), new File(mFileNameToUse));
			FileUtils.copyFile(new File(mergedFilepath), new File(fileNamePart0));
			// Toast.makeText(getApplicationContext(), "success",
			// Toast.LENGTH_SHORT).show();
		}
	}

}