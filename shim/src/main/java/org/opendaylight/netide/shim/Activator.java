/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private SwitchConnectionProvider connectionProvider;
    private ZeroMQBaseConnector coreConnector;

    @Override
    public void start(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
        LOG.info("NetIDE SHIM ACTIVATOR: Bundle start");
        connectionProvider  = new SwitchConnectionProviderImpl();
        coreConnector = new ZeroMQBaseConnector();
        
        ShimSwitchConnectionHandlerImpl handler = new ShimSwitchConnectionHandlerImpl(coreConnector);
        coreConnector.RegisterCoreListener(handler);
        coreConnector.setPort(5555);
        
        connectionProvider.setSwitchConnectionHandler(handler);

        ConnectionConfiguration conf = new ShimConnectionConfiguration();
        
        connectionProvider.setConfiguration(conf);
        connectionProvider.startup();
        coreConnector.Start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
        LOG.info("NetIDE SHIM ACTIVATOR: Bundle stop");
    }

}