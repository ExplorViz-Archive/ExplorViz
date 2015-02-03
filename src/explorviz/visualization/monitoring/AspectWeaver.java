package explorviz.visualization.monitoring;

public class AspectWeaver {
	public static native void weave() /*-{
		$wnd.WRITE_TIME_TRIGGERED = true
		$wnd.RECORD_BATCH_SIZE = 1 // increase for normal usage...
		
		function initMonitoring() {
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
			$wnd.moduleCacheJSLineLength = -1
			$wnd.functionLineMap = {}
			$wnd.classFilenameMap = {}
			$wnd.recordBatchStorage = []
			
			$wnd.currentStackDepth = 0
			$wnd.traceID = 0 // TODO init from server?
			$wnd.orderID = 0
	
			$wnd.lastWrite = 0
	
			$wnd.jQuery.get('http://localhost:9876/explorviz/' + $strongName
					+ '.cache.js', '', function(result) {
				$wnd.moduleCacheJSLines = result.split("\n");
				$wnd.moduleCacheJSLineLength = $wnd.moduleCacheJSLines.length;
			}, 'html');
		}
		
		function fetchStaticClazz(methodname) {
			var clazzname = null;
			if (methodname in $wnd.functionLineMap) {
				return $wnd.functionLineMap[methodname]
			} else {
				var toSeekName = 'function ' + methodname;
				var toSeekNameLength = toSeekName.length

				var lines = $wnd.moduleCacheJSLines
				var length = $wnd.moduleCacheJSLineLength
				var firstMethodChar = methodname[0]
				for (var i = 0; i < length; i++) {
					var line = lines[i];
					if (line.length >= toSeekNameLength
							&& line[9] == firstMethodChar
							&& line.startsWith(toSeekName)) {
						for (var j = i; j < length; j++) {
							var indexName = lines[j].indexOf("createForClass_0_g$")
							if (indexName >= 0) {
								clazzname = lines[j].substring(indexName + 21, lines[j].lastIndexOf(","));
								clazzname = clazzname.substring(0, clazzname.lastIndexOf(",")-1);
								clazzname = clazzname.substring(0, clazzname.indexOf(",")-1) + "." + clazzname.substring(clazzname.indexOf(",") + 3, clazzname.length);
								$wnd.functionLineMap[methodname] = clazzname;
								return clazzname;
							}
						}
					}
				}
			}
			return clazzname;
		}
		
		function finishTraceIfNecessary() {
			// LESS is if we started in the middle of the trace...
			if ($wnd.currentStackDepth <= 0) {
				// we finished the trace
				$wnd.traceID++;
				$wnd.orderID = 0;
				$wnd.currentStackDepth = 0;
			};
		}
		
		function aspectInvoc(invocation) {
			// JS source not yet fetched therefore only proceed
			if ($wnd.moduleCacheJSLineLength == -1)
				return invocation.proceed();
				
			// TODO replace method with StringRegistryID and send those records...
			
			// TODO operations:
//				eval("$wnd.currentClazz = (this.__proto__.___clazz$.packageName_1_g$ + '.' + this.__proto__.___clazz$.compoundName_1_g$)")
		
			var className = fetchStaticClazz(invocation.method)
			
			if (className == null) {
				return invocation.proceed();
			}
			
			if (invocation.method.indexOf("viewScene") >= 0) {
				console.log("here  className ! " + className)
			}
			
			if (className == null || className.startsWith("explorviz.visualization.monitoring") || !className.startsWith("explorviz"))
				return invocation.proceed()

			$wnd.recordBatchStorage.push("1;" + $wnd.performance.now() + ";" + $wnd.traceID + ";" + ($wnd.orderID++) + ";" + classFilename + ";" + invocation.method);
			
			$wnd.currentStackDepth++;
			var result = invocation.proceed();
			$wnd.currentStackDepth--;

			var afterTimestamp = $wnd.performance.now()
			$wnd.recordBatchStorage.push("3;" + afterTimestamp + ";" + $wnd.traceID + ";" + ($wnd.orderID++) + ";" + classFilename + ";" + invocation.method);

			if (!$wnd.WRITE_TIME_TRIGGERED) {
				if ($wnd.recordBatchStorage.length >= $wnd.RECORD_BATCH_SIZE) {
					@explorviz.visualization.monitoring.MonitoringManager::sendRecordBundle(Ljava/lang/String;)($wnd.recordBatchStorage.toString())
					$wnd.recordBatchStorage = [];
				}
			} else {
				if (afterTimestamp - $wnd.lastWrite > 1000) {
					$wnd.lastWrite = afterTimestamp
					@explorviz.visualization.monitoring.MonitoringManager::sendRecordBundle(Ljava/lang/String;)($wnd.recordBatchStorage.toString())
					$wnd.recordBatchStorage = [];
				}
			}

			finishTraceIfNecessary();

			return result;
		}
		
		initMonitoring();
		
		$wnd.prototypeDefinitions = []
		
		eval("for (var key in prototypesByTypeId_1_g$) { $wnd.prototypeDefinitions.push(prototypesByTypeId_1_g$[key]) };")
		
		$wnd.tempAttri = 0
		for (var i = 2; i < $wnd.prototypeDefinitions.length; i++) {
			$wnd.currentPrototypeElement = $wnd.prototypeDefinitions[i]
			$wnd.shouldInstrumentObject = false
			eval("if ($wnd.currentPrototypeElement.___clazz$ && $wnd.currentPrototypeElement.___clazz$.packageName_1_g$.startsWith('explorviz')) {$wnd.shouldInstrumentObject = true}")
			
			if ($wnd.shouldInstrumentObject) {
//				$wnd.jQuery.aop
//					.around(
//							{
//								target : $wnd.currentPrototypeElement,
//								method : /(^[a-z].*)/,
//							}, aspectInvoc);
			}
		}
		
		
		$wnd.prototypeDefinitions = []
		
		$wnd.jQuery.aop
				.around(
						{
							target : this,
							method : /(^[a-s].*)|(^[u-z].*)|(^[t][a-x].*)|(^[t][z].*)|(^[t][y][p][e][P].*)|(^[t][y][p][e][_].*)/,
						}, aspectInvoc);
	}-*/;
}
