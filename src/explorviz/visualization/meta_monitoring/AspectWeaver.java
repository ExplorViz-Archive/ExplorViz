package explorviz.visualization.meta_monitoring;

public class AspectWeaver {
	public static native void weave() /*-{
		if (typeof String.prototype.startsWith != 'function') {
			String.prototype.startsWith = function(str) {
				for (var i = 0; i < str.length; i++) {
					if (this[i] != str[i]) {
						return false;
					}
				}
				return true;
			};
		}

		$wnd.moduleCacheJSLines = null
		$wnd.moduleCacheJSLineLength = 0
		$wnd.functionLineMap = {}
		$wnd.filenameMap = {}
		$wnd.monitoringCache = []

		$wnd.lastWrite = 0

		$wnd.jQuery.get('http://localhost:9876/explorviz/' + $strongName
				+ '.cache.js', '', function(result) {
			$wnd.moduleCacheJSLines = result.split("\n");
			$wnd.moduleCacheJSLineLength = $wnd.moduleCacheJSLines.length;
		}, 'html');

		$wnd.jQuery().readSourceMapURL(
				'http://localhost:9876/sourcemaps/explorviz/' + $strongName
						+ '_sourcemap.json');

		function aspectInvoc(invocation) {
			var toSeekName = 'function ' + invocation.method;

			var linenumber = -1;
			if (toSeekName in $wnd.functionLineMap) {
				linenumber = $wnd.functionLineMap[toSeekName]
			} else {
				//				var firstL1 = $wnd.performance.now()

				var toSeekNameLength = toSeekName.length

				var lines = $wnd.moduleCacheJSLines
				var length = $wnd.moduleCacheJSLineLength
				var firstMethodChar = invocation.method[0]
				for (var i = 0; i < length; i++) {
					var line = lines[i];
					if (line.length >= toSeekNameLength
							&& line[9] == firstMethodChar
							&& line.startsWith(toSeekName)) {
						linenumber = i + 1;
						$wnd.functionLineMap[toSeekName] = linenumber;
						//						console.log("lookup of " + toSeekName + " took "
						//								+ ($wnd.performance.now() - firstL1))
						break;
					}
				}
			}

			if (linenumber == -1)
				return invocation.proceed();

			var filename = null;
			if (linenumber in $wnd.filenameMap) {
				filename = $wnd.filenameMap[linenumber]
			} else {
				//				var look1 = $wnd.performance.now()
				var filename = $wnd.jQuery().lookupFilename(linenumber)
				$wnd.filenameMap[linenumber] = filename

				//				console.log("filename lookup of " + filename + " took "
				//						+ ($wnd.performance.now() - look1))
			}

			if (!filename.indexOf("explorviz") == 0)
				return invocation.proceed()

			$wnd.monitoringCache.push(filename + ";" + invocation.method + ";"
					+ $wnd.performance.now());
			var result = invocation.proceed();

			var afterT = $wnd.performance.now()
			$wnd.monitoringCache.push(afterT);

			if (afterT - $wnd.lastWrite > 1000) {
				$wnd.lastWrite = afterT
				@explorviz.visualization.meta_monitoring.MetaMonitoringManager::sendRecordBundle(Ljava/lang/String;)($wnd.monitoringCache.toString())
				$wnd.monitoringCache = [];
			}
			return result;
		}

		$wnd.jQuery.aop
				.around(
						{
							target : this,
							method : /(^[a-s].*)|(^[u-z].*)|(^[t][a-x].*)|(^[t][z].*)|(^[t][y][p][e][P].*)|(^[t][y][p][e][_].*)/,
						}, aspectInvoc);
	}-*/;
}
