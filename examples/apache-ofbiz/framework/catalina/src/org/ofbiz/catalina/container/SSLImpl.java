/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package org.ofbiz.catalina.container;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.SSLUtil;
import org.ofbiz.base.util.UtilValidate;

/**
 * SSLImpl
 */
public class SSLImpl extends JSSEImplementation {

    public static final String module = SSLImpl.class.getName();
    protected ServerSocketFactory ssFactory = null;
    protected TrustManager[] allow;

    public SSLImpl() throws ClassNotFoundException {
        super();
        this.allow =  new TrustManager[] { new AllowTrustManager() };
        Debug.logInfo("SSLImpl loaded; using custom ServerSocketFactory", module);
    }

    @Override
    public ServerSocketFactory getServerSocketFactory(AbstractEndpoint endpoint) {
        if (UtilValidate.isEmpty(this.ssFactory)) {
            this.ssFactory = (new JSSEImplementation()).getServerSocketFactory(endpoint);
        }
        return ssFactory;
    }

    class AllowTrustManager implements X509TrustManager {

        private TrustManager[] tm;

        public AllowTrustManager() throws ClassNotFoundException {
            try {
                tm = SSLUtil.getTrustManagers();
            } catch (Exception e) {
                Debug.logError(e, module);
                throw new ClassNotFoundException(e.getMessage());
            }
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String string) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String string) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return ((X509TrustManager) tm[0]).getAcceptedIssuers();
        }
    }
}
