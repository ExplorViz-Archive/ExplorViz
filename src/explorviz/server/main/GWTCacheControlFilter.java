package explorviz.server.main;

import java.io.IOException;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Filter} to add cache control headers for GWT generated files to ensure
 * that the correct files get cached.
 *
 * @author See Wah Cheng
 * @created 24 Feb 2009
 */
public class GWTCacheControlFilter implements Filter {

	public void destroy() {
	}

	public void init(final FilterConfig config) throws ServletException {
	}

	public void doFilter(final ServletRequest request, final ServletResponse response,
			final FilterChain filterChain) throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final String requestURI = httpRequest.getRequestURI();

		if (requestURI.contains(".nocache.")) {
			final Date now = new Date();
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setDateHeader("Date", now.getTime());
			// one day old
			httpResponse.setDateHeader("Expires", now.getTime() - 86400000L);
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
		}

		filterChain.doFilter(request, response);
	}
}