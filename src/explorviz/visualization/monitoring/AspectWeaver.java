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
			$wnd.functionToClassnameMap = {}
			$wnd.methodnameToLinenumber = {}
			$wnd.recordBatchStorage = []
			
			$wnd.stringRegistry = {}
			$wnd.stringRegistryNextIndex = 0

			$wnd.currentStackDepth = 0
			$wnd.traceID = 0
			$wnd.orderID = 0

			$wnd.lastWrite = 0

			$wnd.jQuery.get('http://localhost:9876/explorviz/' + $strongName
					+ '.cache.js', '', function(result) {
				if (result) {
					$wnd.moduleCacheJSLines = result.split("\n");
					$wnd.moduleCacheJSLineLength = $wnd.moduleCacheJSLines.length;
					
					var lines = $wnd.moduleCacheJSLines
					var length = $wnd.moduleCacheJSLineLength
					
					for (var i = 10; i < length; i++) {
						var line = lines[i];
						if (line.length >= 9
								&& line.startsWith('function ')) {
							var methodname = line.substring(9, line.indexOf("("))
							$wnd.methodnameToLinenumber[methodname] = i
						}
					}
				}
			}, 'html');
		}
		
		function getStringRegistryId(str) {
			if (str in $wnd.stringRegistry) {
				return $wnd.stringRegistry[str];
			} else {
				$wnd.stringRegistry[str] = $wnd.stringRegistryNextIndex;
				$wnd.recordBatchStorage.push("4;" + $wnd.stringRegistryNextIndex + ";" + str);
				
				return $wnd.stringRegistryNextIndex++;
			}
		}

		function createBeforeRecord(clazzname, methodname) {
			$wnd.recordBatchStorage.push("1;" + $wnd.performance.now() + ";"
					+ $wnd.traceID + ";" + ($wnd.orderID++) + ";" + clazzname
					+ ";" + methodname);

			$wnd.currentStackDepth++;
		}
		
		function createAfterRecord(afterTimestamp,clazzname, methodname) {
			$wnd.currentStackDepth--;

			$wnd.recordBatchStorage.push("3;" + afterTimestamp + ";"
					+ $wnd.traceID + ";" + ($wnd.orderID++) + ";" + clazzname
					+ ";" + methodname);
		}
		
		function writeRecordsToServerIfNecessary(afterTimestamp) {
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
		}

		function finishTraceIfNecessary() {
			// LESS is if we started in the middle of the trace...
			if ($wnd.currentStackDepth <= 0) {
				// we finished the trace
				$wnd.traceID++;
				$wnd.orderID = 0;
				$wnd.currentStackDepth = 0;
			}
			;
		}

		function aspectInvocOperation(invocation) {
			// JS source not yet fetched therefore only proceed
			if ($wnd.moduleCacheJSLineLength == -1)
				return invocation.proceed();

			eval("$wnd.currentClazzname = (this.__proto__.___clazz$.packageName_1_g$ + '.' + this.__proto__.___clazz$.compoundName_1_g$)")

			var clazzname = $wnd.currentClazzname;
			if (clazzname == null) {
				return invocation.proceed();
			}
			
			var methodname = invocation.method
			
			var classnameId = getStringRegistryId(clazzname)
			var methodnameId = getStringRegistryId(methodname)
			
			createBeforeRecord(classnameId, methodnameId);
			
			var result = invocation.proceed();
			
			var afterTimestamp = $wnd.performance.now();
			createAfterRecord(afterTimestamp,classnameId, methodnameId);

			writeRecordsToServerIfNecessary(afterTimestamp);

			finishTraceIfNecessary();

			return result;
		}
		
		function fetchStaticClazz(methodname) {
			if (methodname in $wnd.functionToClassnameMap) {
				return $wnd.functionToClassnameMap[methodname]
			} else {
				var L1 = $wnd.performance.now();
				
				var lines = $wnd.moduleCacheJSLines
				var length = $wnd.moduleCacheJSLineLength
				
				if (methodname in $wnd.methodnameToLinenumber) {
					var linenumber = $wnd.methodnameToLinenumber[methodname]
					
					for (var i = linenumber; i < length; i++) {
						var indexName = lines[i]
								.indexOf("createForClass_0_g$")
						if (indexName >= 0) {
							var clazzname = lines[i].substring(indexName + 21,
									lines[i].lastIndexOf(","));
							clazzname = clazzname.substring(0, clazzname
									.lastIndexOf(",") - 1);
							clazzname = clazzname.substring(0, clazzname
									.indexOf(",") - 1)
									+ "."
									+ clazzname.substring(clazzname
											.indexOf(",") + 3,
											clazzname.length);
							$wnd.functionToClassnameMap[methodname] = clazzname;
							return clazzname;
						}
					}
				}
				
				$wnd.functionToClassnameMap[methodname] = null;
				return null;
			}
		}
		
		function aspectInvocStatic(invocation) {
			// JS source not yet fetched therefore only proceed
			if ($wnd.moduleCacheJSLineLength == -1)
				return invocation.proceed();

			var methodname = invocation.method
			var clazzname = fetchStaticClazz(methodname);

			if (clazzname == null) {
				return invocation.proceed();
			}
			
			if (!clazzname.startsWith("explorviz")
					|| clazzname
							.startsWith("explorviz.visualization.monitoring")) {
				// delete the value
				$wnd.functionToClassnameMap[methodname] = null;
				return invocation.proceed();
			}

			var classnameId = getStringRegistryId(clazzname)
			var methodnameId = getStringRegistryId(methodname)
			
			createBeforeRecord(classnameId, methodnameId);
			
			var result = invocation.proceed();
			
			var afterTimestamp = $wnd.performance.now();
			createAfterRecord(afterTimestamp,classnameId, methodnameId);

			writeRecordsToServerIfNecessary(afterTimestamp);

			finishTraceIfNecessary();

			return result;
		}

		initMonitoring();

		$wnd.prototypeDefinitions = []

		eval("for (var key in prototypesByTypeId_1_g$) { $wnd.prototypeDefinitions.push(prototypesByTypeId_1_g$[key]) };")

		for (var i = 2; i < $wnd.prototypeDefinitions.length; i++) {
			$wnd.currentPrototypeElement = $wnd.prototypeDefinitions[i]
			$wnd.shouldInstrumentObject = false
			eval("if ($wnd.currentPrototypeElement.___clazz$ && $wnd.currentPrototypeElement.___clazz$.packageName_1_g$.startsWith('explorviz') && !($wnd.currentPrototypeElement.___clazz$.packageName_1_g$.startsWith('explorviz.visualization.monitoring'))) {$wnd.shouldInstrumentObject = true}")

			if ($wnd.shouldInstrumentObject == true) {
				$wnd.jQuery.aop.around({
					target : $wnd.currentPrototypeElement,
					method : /(^[a-z].*)/,
				}, aspectInvocOperation);
			}
		}

		$wnd.shouldInstrumentObject = null
		$wnd.currentPrototypeElement = null
		$wnd.prototypeDefinitions = null

		$wnd.jQuery.aop
				.around(
						{
							target : this,
							method : /(^[a-s].*)|(^[u-z].*)|(^[t][a-x].*)|(^[t][z].*)|(^[t][y][p][e][P].*)|(^[t][y][p][e][_].*)/,
						}, aspectInvocStatic);
	}-*/;
}
