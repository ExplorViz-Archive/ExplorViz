/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.base.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Configurable Debug logging wrapper class
 *
 */
public final class Debug {

    public static final boolean useLog4J = true;
    public static final String noModuleModule = "NoModule";  // set to null for previous behavior
    public static final Object[] emptyParams = new Object[0];

    static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public static final String SYS_DEBUG = System.getProperty("DEBUG");
    public static final int ALWAYS = 0;
    public static final int VERBOSE = 1;
    public static final int TIMING = 2;
    public static final int INFO = 3;
    public static final int IMPORTANT = 4;
    public static final int WARNING = 5;
    public static final int ERROR = 6;
    public static final int FATAL = 7;
    public static final int NOTIFY = 8;

    public static final String[] levels = {"Always", "Verbose", "Timing", "Info", "Important", "Warning", "Error", "Fatal", "Notify"};
    public static final String[] levelProps = {"", "print.verbose", "print.timing", "print.info", "print.important", "print.warning", "print.error", "print.fatal", "print.notify"};
    public static final Level[] levelObjs = {Level.INFO, Level.DEBUG, Level.INFO, Level.INFO, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, NotifyLevel.NOTIFY};

    protected static Map<String, Integer> levelStringMap = new HashMap<String, Integer>();

    protected static PrintStream printStream = System.out;
    protected static PrintWriter printWriter = new PrintWriter(printStream);

    protected static boolean levelOnCache[] = new boolean[9];
    protected static boolean packException = true;
    protected static final boolean useLevelOnCache = true;

    protected static Logger root = Logger.getRootLogger();

    static {
        levelStringMap.put("verbose", Debug.VERBOSE);
        levelStringMap.put("timing", Debug.TIMING);
        levelStringMap.put("info", Debug.INFO);
        levelStringMap.put("important", Debug.IMPORTANT);
        levelStringMap.put("warning", Debug.WARNING);
        levelStringMap.put("error", Debug.ERROR);
        levelStringMap.put("fatal", Debug.FATAL);
        levelStringMap.put("always", Debug.ALWAYS);
        levelStringMap.put("notify", Debug.NOTIFY);

        // initialize Log4J
        if (!UtilProperties.propertyValueEqualsIgnoreCase("debug.properties", "disable.log4j.config", "true")) {
            org.apache.log4j.xml.DOMConfigurator.configure(UtilURL.fromResource("log4j.xml"));
        }

        // initialize levelOnCache
        for (int i = 0; i < 9; i++) {
            levelOnCache[i] = (i == Debug.ALWAYS || UtilProperties.propertyValueEqualsIgnoreCase("debug.properties", levelProps[i], "true"));
        }

        if (SYS_DEBUG != null) {
            for (int x = 0; x < 8; x++) {
                levelOnCache[x] = true;
            }
            LoggerRepository repo = root.getLoggerRepository();
            Enumeration<Logger> en = UtilGenerics.cast(repo.getCurrentLoggers());
            while (en.hasMoreElements()) {
                Logger thisLogger = en.nextElement();
                thisLogger.setLevel(Level.DEBUG);
            }
        }

        // configure exception packing
        packException = UtilProperties.propertyValueEqualsIgnoreCase("debug.properties", "pack.exception", "true");
    }

    public static PrintStream getPrintStream() {
        return printStream;
    }

    public static void setPrintStream(PrintStream printStream) {
        Debug.printStream = printStream;
        Debug.printWriter = new PrintWriter(printStream);
    }

    public static PrintWriter getPrintWriter() {
        return printWriter;
    }

    public static Logger getLogger(String module) {
        if (UtilValidate.isNotEmpty(module)) {
            return Logger.getLogger(module);
        } else {
            return root;
        }
    }

    /** Gets an Integer representing the level number from a String representing the level name; will return null if not found */
    public static Integer getLevelFromString(String levelName) {
        if (levelName == null) return null;
        return levelStringMap.get(levelName.toLowerCase());
    }

    /** Gets an int representing the level number from a String representing the level name; if level not found defaults to Debug.INFO */
    public static int getLevelFromStringWithDefault(String levelName) {
        Integer levelInt = getLevelFromString(levelName);
        if (levelInt == null) {
            return Debug.INFO;
        } else {
            return levelInt;
        }
    }

    public static void log(int level, Throwable t, String msg, String module) {
        log(level, t, msg, module, "org.ofbiz.base.util.Debug", emptyParams);
    }

    public static void log(int level, Throwable t, String msg, String module, Object... params) {
        log(level, t, msg, module, "org.ofbiz.base.util.Debug", params);
    }

    public static void log(int level, Throwable t, String msg, String module, String callingClass) {
        log(level, t, msg, module, callingClass, new Object[0]);
    }

    public static void log(int level, Throwable t, String msg, String module, String callingClass, Object... params) {
        if (isOn(level)) {
            if (msg != null && params.length > 0) {
                StringBuilder sb = new StringBuilder();
                Formatter formatter = new Formatter(sb);
                formatter.format(msg, params);
                msg = sb.toString();
            }
            // pack the exception
            if (packException && t != null) {
                msg = System.getProperty("line.separator") + ExceptionHelper.packException(msg, t, true);
                t = null;
            }

            // log
            if (useLog4J) {
                Logger logger = getLogger(module);
                if (SYS_DEBUG != null) {
                    logger.setLevel(Level.DEBUG);
                }
                logger.log(callingClass, levelObjs[level], msg, t);
            } else {
                StringBuilder prefixBuf = new StringBuilder();

                prefixBuf.append(dateFormat.format(new java.util.Date()));
                prefixBuf.append(" [OFBiz");
                if (module != null) {
                    prefixBuf.append(":");
                    prefixBuf.append(module);
                }
                prefixBuf.append(":");
                prefixBuf.append(levels[level]);
                prefixBuf.append("] ");
                if (msg != null) {
                    getPrintWriter().print(prefixBuf.toString());
                    getPrintWriter().println(msg);
                }
                if (t != null) {
                    getPrintWriter().print(prefixBuf.toString());
                    getPrintWriter().println("Received throwable:");
                    t.printStackTrace(getPrintWriter());
                }
            }
        }
    }

    public static boolean isOn(int level) {
        if (useLevelOnCache) {
            return levelOnCache[level];
        } else {
            return (level == Debug.ALWAYS || UtilProperties.propertyValueEqualsIgnoreCase("debug", levelProps[level], "true"));
        }
    }

    // leaving these here
    public static void log(String msg) {
        log(Debug.ALWAYS, null, msg, noModuleModule, emptyParams);
    }

    public static void log(String msg, Object... params) {
        log(Debug.ALWAYS, null, msg, noModuleModule, params);
    }

    public static void log(Throwable t) {
        log(Debug.ALWAYS, t, null, noModuleModule, emptyParams);
    }

    public static void log(String msg, String module) {
        log(Debug.ALWAYS, null, msg, module, emptyParams);
    }

    public static void log(String msg, String module, Object... params) {
        log(Debug.ALWAYS, null, msg, module, params);
    }

    public static void log(Throwable t, String module) {
        log(Debug.ALWAYS, t, null, module, emptyParams);
    }

    public static void log(Throwable t, String msg, String module) {
        log(Debug.ALWAYS, t, msg, module, emptyParams);
    }

    public static void log(Throwable t, String msg, String module, Object... params) {
        log(Debug.ALWAYS, t, msg, module, params);
    }

    public static boolean verboseOn() {
        return isOn(Debug.VERBOSE);
    }

    public static void logVerbose(String msg, String module) {
        log(Debug.VERBOSE, null, msg, module, emptyParams);
    }

    public static void logVerbose(String msg, String module, Object... params) {
        log(Debug.VERBOSE, null, msg, module, params);
    }

    public static void logVerbose(Throwable t, String module) {
        log(Debug.VERBOSE, t, null, module, emptyParams);
    }

    public static void logVerbose(Throwable t, String msg, String module) {
        log(Debug.VERBOSE, t, msg, module, emptyParams);
    }

    public static void logVerbose(Throwable t, String msg, String module, Object... params) {
        log(Debug.VERBOSE, t, msg, module, params);
    }

    public static boolean timingOn() {
        return isOn(Debug.TIMING);
    }

    public static void logTiming(String msg, String module) {
        log(Debug.TIMING, null, msg, module, emptyParams);
    }

    public static void logTiming(String msg, String module, Object... params) {
        log(Debug.TIMING, null, msg, module, params);
    }

    public static void logTiming(Throwable t, String module) {
        log(Debug.TIMING, t, null, module, emptyParams);
    }

    public static void logTiming(Throwable t, String msg, String module) {
        log(Debug.TIMING, t, msg, module, emptyParams);
    }

    public static void logTiming(Throwable t, String msg, String module, Object... params) {
        log(Debug.TIMING, t, msg, module, params);
    }

    public static boolean infoOn() {
        return isOn(Debug.INFO);
    }

    public static void logInfo(String msg, String module) {
        log(Debug.INFO, null, msg, module, emptyParams);
    }

    public static void logInfo(String msg, String module, Object... params) {
        log(Debug.INFO, null, msg, module, params);
    }

    public static void logInfo(Throwable t, String module) {
        log(Debug.INFO, t, null, module, emptyParams);
    }

    public static void logInfo(Throwable t, String msg, String module) {
        log(Debug.INFO, t, msg, module, emptyParams);
    }

    public static void logInfo(Throwable t, String msg, String module, Object... params) {
        log(Debug.INFO, t, msg, module, params);
    }

    public static boolean importantOn() {
        return isOn(Debug.IMPORTANT);
    }

    public static void logImportant(String msg, String module) {
        log(Debug.IMPORTANT, null, msg, module, emptyParams);
    }

    public static void logImportant(String msg, String module, Object... params) {
        log(Debug.IMPORTANT, null, msg, module, params);
    }

    public static void logImportant(Throwable t, String module) {
        log(Debug.IMPORTANT, t, null, module, emptyParams);
    }

    public static void logImportant(Throwable t, String msg, String module) {
        log(Debug.IMPORTANT, t, msg, module, emptyParams);
    }

    public static void logImportant(Throwable t, String msg, String module, Object... params) {
        log(Debug.IMPORTANT, t, msg, module, params);
    }

    public static boolean warningOn() {
        return isOn(Debug.WARNING);
    }

    public static void logWarning(String msg, String module) {
        log(Debug.WARNING, null, msg, module, emptyParams);
    }

    public static void logWarning(String msg, String module, Object... params) {
        log(Debug.WARNING, null, msg, module, params);
    }

    public static void logWarning(Throwable t, String module) {
        log(Debug.WARNING, t, null, module, emptyParams);
    }

    public static void logWarning(Throwable t, String msg, String module) {
        log(Debug.WARNING, t, msg, module, emptyParams);
    }

    public static void logWarning(Throwable t, String msg, String module, Object... params) {
        log(Debug.WARNING, t, msg, module, params);
    }

    public static boolean errorOn() {
        return isOn(Debug.ERROR);
    }

    public static void logError(String msg, String module) {
        log(Debug.ERROR, null, msg, module, emptyParams);
    }

    public static void logError(String msg, String module, Object... params) {
        log(Debug.ERROR, null, msg, module, params);
    }

    public static void logError(Throwable t, String module) {
        log(Debug.ERROR, t, null, module, emptyParams);
    }

    public static void logError(Throwable t, String msg, String module) {
        log(Debug.ERROR, t, msg, module, emptyParams);
    }

    public static void logError(Throwable t, String msg, String module, Object... params) {
        log(Debug.ERROR, t, msg, module, params);
    }

    public static boolean fatalOn() {
        return isOn(Debug.FATAL);
    }

    public static void logFatal(String msg, String module) {
        log(Debug.FATAL, null, msg, module, emptyParams);
    }

    public static void logFatal(String msg, String module, Object... params) {
        log(Debug.FATAL, null, msg, module, params);
    }

    public static void logFatal(Throwable t, String module) {
        log(Debug.FATAL, t, null, module, emptyParams);
    }

    public static void logFatal(Throwable t, String msg, String module) {
        log(Debug.FATAL, t, msg, module, emptyParams);
    }

    public static void logFatal(Throwable t, String msg, String module, Object... params) {
        log(Debug.FATAL, t, msg, module, params);
    }

    public static void logNotify(String msg, String module) {
        log(Debug.NOTIFY, null, msg, module, emptyParams);
    }

    public static void logNotify(String msg, String module, Object... params) {
        log(Debug.NOTIFY, null, msg, module, params);
    }

    public static void logNotify(Throwable t, String module) {
        log(Debug.NOTIFY, t, null, module, emptyParams);
    }

    public static void logNotify(Throwable t, String msg, String module) {
        log(Debug.NOTIFY, t, msg, module, emptyParams);
    }

    public static void logNotify(Throwable t, String msg, String module, Object... params) {
        log(Debug.NOTIFY, t, msg, module, params);
    }

    public static void set(int level, boolean on) {
        if (!useLevelOnCache)
            return;
        levelOnCache[level] = on;
    }

    public static synchronized Appender getNewFileAppender(String name, String logFile, long maxSize, int backupIdx, String pattern) {
        if (pattern == null) {
            pattern = "%-5r[%24F:%-3L:%-5p]%x %m%n";
        }

        PatternLayout layout = new PatternLayout(pattern);
        layout.activateOptions();

        RollingFileAppender newAppender = null;
        try {
            newAppender = new RollingFileAppender(layout, logFile, true);
        } catch (IOException e) {
            logFatal(e, Debug.class.getName());
        }

        if (newAppender != null) {
            if (backupIdx > 0) {
                newAppender.setMaxBackupIndex(backupIdx);
            }
            if (maxSize > 0) {
                newAppender.setMaximumFileSize(maxSize);
            }
            newAppender.setThreshold(Level.DEBUG);
            newAppender.activateOptions();
            newAppender.setName(name);
        }

        return newAppender;
    }

    public static boolean registerFileAppender(String module, String name, String logFile, long maxSize, int backupIdx, String pattern) {
        Logger logger = Logger.getLogger(module);
        boolean found = false;

        Appender foundAppender = logger.getAppender(name);
        if (foundAppender == null) {
            Enumeration<Logger> currentLoggerEnum = UtilGenerics.cast(Logger.getRootLogger().getLoggerRepository().getCurrentLoggers());
            while (currentLoggerEnum.hasMoreElements() && foundAppender == null) {
                Logger log = currentLoggerEnum.nextElement();
                foundAppender = log.getAppender(name);
            }
        } else {
            return true;
        }

        if (foundAppender == null) {
            if (logFile != null) {
                foundAppender = getNewFileAppender(name, logFile, maxSize, backupIdx, pattern);
                if (foundAppender != null) {
                    found = true;
                }
            }
        } else {
            found = true;
        }

        logger.addAppender(foundAppender);
        return found;
    }

    public static boolean registerFileAppender(String module, String name, String logFile) {
        return registerFileAppender(module, name, logFile, 0, 10, null);
    }

    public static boolean registerFileAppender(String module, String name) {
        return registerFileAppender(module, name, null, -1, -1, null);
    }
}
