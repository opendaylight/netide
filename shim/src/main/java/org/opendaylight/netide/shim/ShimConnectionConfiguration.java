/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShimConnectionConfiguration implements ConnectionConfiguration {
    
    private static final Logger LOG = LoggerFactory.getLogger(ShimConnectionConfiguration.class);
    
    private int port = 6633;

    @Override
    public InetAddress getAddress() {
        // TODO Auto-generated method stub
        InetAddress addr = null;

        try {
            addr = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            LOG.error(e.getMessage());
        }

        return addr;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Object getSslContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getSwitchIdleTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ThreadConfiguration getThreadConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TlsConfiguration getTlsConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getTransferProtocol() {
        // TODO Auto-generated method stub
        return TransportProtocol.TCP;
    }

}
