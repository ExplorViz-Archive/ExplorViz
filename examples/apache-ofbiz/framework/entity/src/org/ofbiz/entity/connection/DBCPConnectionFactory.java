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
package org.ofbiz.entity.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.transaction.TransactionManager;

import javolution.util.FastMap;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp.managed.ManagedDataSource;
import org.apache.commons.dbcp.managed.PoolableManagedConnectionFactory;
import org.apache.commons.dbcp.managed.XAConnectionFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.transaction.TransactionFactory;
import org.w3c.dom.Element;

import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javolution.util.FastMap;

/**
 * DBCPConnectionFactory
 */
public class DBCPConnectionFactory implements ConnectionFactoryInterface {

    public static final String module = DBCPConnectionFactory.class.getName();
    protected static Map<String, ManagedDataSource> dsCache = FastMap.newInstance();

    public Connection getConnection(GenericHelperInfo helperInfo, Element jdbcElement) throws SQLException, GenericEntityException {
        ManagedDataSource mds = dsCache.get(helperInfo.getHelperFullName());
        if (mds != null) {
            return TransactionFactory.getCursorConnection(helperInfo, mds.getConnection());
        }

        synchronized (DBCPConnectionFactory.class) {
            mds = dsCache.get(helperInfo.getHelperFullName());
            if (mds != null) {
                return TransactionFactory.getCursorConnection(helperInfo, mds.getConnection());
            }

            // connection properties
            TransactionManager txMgr = TransactionFactory.getTransactionManager();
            String driverName = jdbcElement.getAttribute("jdbc-driver");

            
            String jdbcUri = UtilValidate.isNotEmpty(helperInfo.getOverrideJdbcUri()) ? helperInfo.getOverrideJdbcUri() : jdbcElement.getAttribute("jdbc-uri");
            String jdbcUsername = UtilValidate.isNotEmpty(helperInfo.getOverrideUsername()) ? helperInfo.getOverrideUsername() : jdbcElement.getAttribute("jdbc-username");
            String jdbcPassword = UtilValidate.isNotEmpty(helperInfo.getOverridePassword()) ? helperInfo.getOverridePassword() : jdbcElement.getAttribute("jdbc-password");

            // pool settings
            int maxSize, minSize, timeBetweenEvictionRunsMillis;
            try {
                maxSize = Integer.parseInt(jdbcElement.getAttribute("pool-maxsize"));
            } catch (NumberFormatException nfe) {
                Debug.logError("Problems with pool settings [pool-maxsize=" + jdbcElement.getAttribute("pool-maxsize") + "]; the values MUST be numbers, using default of 20.", module);
                maxSize = 20;
            } catch (Exception e) {
                Debug.logError("Problems with pool settings [pool-maxsize], using default of 20.", module);
                maxSize = 20;
            }
            try {
                minSize = Integer.parseInt(jdbcElement.getAttribute("pool-minsize"));
            } catch (NumberFormatException nfe) {
                Debug.logError("Problems with pool settings [pool-minsize=" + jdbcElement.getAttribute("pool-minsize") + "]; the values MUST be numbers, using default of 2.", module);
                minSize = 2;
            } catch (Exception e) {
                Debug.logError("Problems with pool settings [pool-minsize], using default of 2.", module);
                minSize = 2;
            }
            // idle-maxsize, default to half of pool-maxsize
            int maxIdle = maxSize / 2;
            if (jdbcElement.hasAttribute("idle-maxsize")) {
                try {
                    maxIdle = Integer.parseInt(jdbcElement.getAttribute("idle-maxsize"));
                } catch (NumberFormatException nfe) {
                    Debug.logError("Problems with pool settings [idle-maxsize=" + jdbcElement.getAttribute("idle-maxsize") + "]; the values MUST be numbers, using calculated default of" + (maxIdle > minSize ? maxIdle : minSize) + ".", module);
                } catch (Exception e) {
                    Debug.logError("Problems with pool settings [idle-maxsize], using calculated default of" + (maxIdle > minSize ? maxIdle : minSize) + ".", module);
                }
            }
            // Don't allow a maxIdle of less than pool-minsize
            maxIdle = maxIdle > minSize ? maxIdle : minSize;

            try {
                timeBetweenEvictionRunsMillis = Integer.parseInt(jdbcElement.getAttribute("time-between-eviction-runs-millis"));
            } catch (NumberFormatException nfe) {
                Debug.logError("Problems with pool settings [time-between-eviction-runs-millis=" + jdbcElement.getAttribute("time-between-eviction-runs-millis") + "]; the values MUST be numbers, using default of 600000.", module);
                timeBetweenEvictionRunsMillis = 600000;
            } catch (Exception e) {
                Debug.logError("Problems with pool settings [time-between-eviction-runs-millis], using default of 600000.", module);
                timeBetweenEvictionRunsMillis = 600000;
            }

            // load the driver
            Driver jdbcDriver;
            try {
                jdbcDriver = (Driver) Class.forName(driverName, true, Thread.currentThread().getContextClassLoader()).newInstance();
            } catch (Exception e) {
                Debug.logError(e, module);
                throw new GenericEntityException(e.getMessage(), e);
            }

            // connection factory properties
            Properties cfProps = new Properties();
            cfProps.put("user", jdbcUsername);
            cfProps.put("password", jdbcPassword);

            // create the connection factory
            ConnectionFactory cf = new DriverConnectionFactory(jdbcDriver, jdbcUri, cfProps);

            // wrap it with a LocalXAConnectionFactory
            XAConnectionFactory xacf = new LocalXAConnectionFactory(txMgr, cf);

            // configure the pool settings
            GenericObjectPool pool = new GenericObjectPool();

            pool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            pool.setMaxActive(maxSize);
            pool.setMaxIdle(maxIdle);
            pool.setMinIdle(minSize);
            pool.setMaxWait(120000);


            // create the pool object factory
            PoolableManagedConnectionFactory factory = new PoolableManagedConnectionFactory(xacf, pool, null, null, true, true);
            factory.setValidationQuery("select example_type_id from example_type limit 1");
            factory.setDefaultReadOnly(false);

            String transIso = jdbcElement.getAttribute("isolation-level");
            if (UtilValidate.isNotEmpty(transIso)) {
                if ("Serializable".equals(transIso)) {
                    factory.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                } else if ("RepeatableRead".equals(transIso)) {
                    factory.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                } else if ("ReadUncommitted".equals(transIso)) {
                    factory.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                } else if ("ReadCommitted".equals(transIso)) {
                    factory.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                } else if ("None".equals(transIso)) {
                    factory.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
                }
            }
            pool.setFactory(factory);

            mds = new ManagedDataSource(pool, xacf.getTransactionRegistry());
            //mds = new DebugManagedDataSource(pool, xacf.getTransactionRegistry()); // Useful to debug the usage of connections in the pool
            mds.setAccessToUnderlyingConnectionAllowed(true);

            // cache the pool
            dsCache.put(helperInfo.getHelperFullName(), mds);

            return TransactionFactory.getCursorConnection(helperInfo, mds.getConnection());
        }
    }

    public void closeAll() {
        // no methods on the pool to shutdown; so just clearing for GC
        dsCache.clear();
    }
}
