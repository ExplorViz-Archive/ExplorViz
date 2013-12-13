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
package org.ofbiz.base.start;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * StartupException
 *
 */
@SuppressWarnings("serial")
public class StartupException extends Exception {

    Throwable nested = null;

    /**
     * Creates new <code>StartupException</code> without detail message.
     */
    public StartupException() {
        super();
    }

    /**
     * Constructs an <code>StartupException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StartupException(String msg) {
        super(msg);
    }

    /**
     * Constructs an <code>StartupException</code> with the specified detail message and nested Exception.
     * @param msg the detail message.
     * @param nested the chained exception.
     */
    public StartupException(String msg, Throwable nested) {
        super(msg);
        this.nested = nested;
    }

    /**
     * Constructs an <code>StartupException</code> with the specified detail message and nested Exception.
     * @param nested the chained exception.
     */
    public StartupException(Throwable nested) {
        super();
        this.nested = nested;
    }

    /** Returns the detail message, including the message from the nested exception if there is one. */
    @Override
    public String getMessage() {
        if (nested != null) {
            return super.getMessage() + " (" + nested.getMessage() + ")";
        } else {
            return super.getMessage();
        }
    }

    /** Returns the detail message, NOT including the message from the nested exception. */
    public String getNonNestedMessage() {
        return super.getMessage();
    }

    /** Returns the nested exception if there is one, null if there is not. */
    public Throwable getNested() {
        if (nested == null) {
            return this;
        }
        return nested;
    }

    /** Prints the composite message to System.err. */
    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (nested != null) {
            nested.printStackTrace();
        }
    }

    /** Prints the composite message and the embedded stack trace to the specified stream ps. */
    @Override
    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (nested != null) {
            nested.printStackTrace(ps);
        }
    }

    /** Prints the composite message and the embedded stack trace to the specified print writer pw. */
    @Override
    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (nested != null) {
            nested.printStackTrace(pw);
        }
    }
}
