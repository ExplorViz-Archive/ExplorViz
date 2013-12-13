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
package org.ofbiz.pos.event;

import java.util.Locale;

import org.ofbiz.pos.screen.PosScreen;
import org.ofbiz.pos.component.InputWithPassword;
import org.ofbiz.pos.component.Output;
import org.ofbiz.pos.PosTransaction;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.Debug;
import org.ofbiz.guiapp.xui.XuiSession;
import org.ofbiz.entity.GenericValue;

public class SecurityEvents {


    public static final String module = SecurityEvents.class.getName();

    public static synchronized void login(PosScreen pos) {
        pos.setWaitCursor();
        String[] func = pos.getInput().getFunction("LOGIN");
        if (func == null) {
            pos.getInput().setFunction("LOGIN", "");
        }
        baseLogin(pos, false);
        pos.setNormalCursor();
    }

    public static synchronized void logout(PosScreen pos) {
        pos.setWaitCursor();
        PosTransaction trans = PosTransaction.getCurrentTx(pos.getSession());
        XuiSession session = pos.getSession();
        trans.closeTx();
        session.logout();
        pos.showPage("pospanel");
        PosScreen.currentScreen.setLock(true);
        pos.setNormalCursor();
    }

    public static synchronized void mgrLogin(PosScreen pos) {
        pos.setWaitCursor();
        XuiSession session = pos.getSession();
        if (session.hasRole(session.getUserLogin(), "MANAGER")) {
            ManagerEvents.mgrLoggedIn = true;
            pos.showPage("mgrpanel");
            PosScreen.currentScreen.getInput().clear();
        } else {
            String[] func = pos.getInput().getFunction("MGRLOGIN");
            if (func == null) {
                pos.getInput().setFunction("MGRLOGIN", "");
            }
            baseLogin(pos, true);
        }
        pos.setNormalCursor();
    }

    public static synchronized void lock(PosScreen pos) {
        pos.setLock(true);
    }

    private static synchronized void baseLogin(PosScreen pos, boolean mgr) {
        XuiSession session = pos.getSession();
        Output output = pos.getOutput();
        InputWithPassword input = pos.getInput();

        String loginFunc = mgr ? "MGRLOGIN" : "LOGIN";
        String[] func = input.getLastFunction();
        String text = input.value();
        if (func != null && func[0].equals(loginFunc)) {
            if (UtilValidate.isEmpty(func[1]) && UtilValidate.isEmpty(text)) {
                output.print(UtilProperties.getMessage(PosTransaction.resource,"PosULogin",Locale.getDefault()));
                input.setFunction(loginFunc);
                input.setPasswordInput(false);
            } else if (UtilValidate.isEmpty(func[1])) {
                output.print(UtilProperties.getMessage(PosTransaction.resource,"PosUPassw",Locale.getDefault()));
                input.setFunction(loginFunc);
                input.setPasswordInput(true);
            } else {
                input.setPasswordInput(false);
                String username = func[1];
                String password = text;
                if (!mgr) {
                    boolean passed = false;
                    try {
                        session.login(username, password);
                        passed = true;
                    } catch (XuiSession.UserLoginFailure e) {
                        input.clear();
                        input.setFunction(loginFunc);
                        output.print(e.getMessage() + " " +  UtilProperties.getMessage(PosTransaction.resource,"PosULogin",Locale.getDefault()));
                    }
                    if (passed) {
                        input.clear();
                        pos.setLock(false);
                        pos.refresh();
                        return;
                    }
                } else {
                    GenericValue mgrUl = null;
                    try {
                        mgrUl = session.checkLogin(username, password);
                    } catch (XuiSession.UserLoginFailure e) {
                        output.print(e.getMessage());
                        input.clear();
                    }
                    if (mgrUl != null) {
                        boolean isMgr = session.hasRole(mgrUl, "MANAGER");
                        if (!isMgr) {
                            output.print(UtilProperties.getMessage(PosTransaction.resource,"PosUserNotManager",Locale.getDefault()));
                            input.clear();
                        } else {
                            ManagerEvents.mgrLoggedIn = true;
                            pos.showPage("mgrpanel");
                        }
                    }
                }
            }
        } else {
            Debug.logInfo("Login function called but not prepared as a function!", module);
        }
    }
}
