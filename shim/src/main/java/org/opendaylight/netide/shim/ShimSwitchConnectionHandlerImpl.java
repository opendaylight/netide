/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShimSwitchConnectionHandlerImpl implements SwitchConnectionHandler, ICoreListener {
    private static final Logger LOG = LoggerFactory.getLogger(ShimSwitchConnectionHandlerImpl.class);
    private static ZeroMQBaseConnector coreConnector;
    HashMap<ConnectionAdapter, BigInteger> connectionAdapterMap;
    
    public ShimSwitchConnectionHandlerImpl(ZeroMQBaseConnector connector) {
        coreConnector = connector;
        connectionAdapterMap = new LinkedHashMap<ConnectionAdapter, BigInteger>();
    }

    @Override
    public boolean accept(InetAddress arg0) {
        return true;
    }

    @Override
    public void onSwitchConnected(ConnectionAdapter connectionAdapter) {
        LOG.info("SHIM: on Switch connected: ", connectionAdapter.getRemoteAddress());
        ShimMessageListener listener = new ShimMessageListener(coreConnector, connectionAdapter, connectionAdapterMap);
        connectionAdapter.setMessageListener(listener);
        connectionAdapter.setSystemListener(listener);
        connectionAdapter.setConnectionReadyListener(listener);
    }

    @Override
    public void onCoreMessage(Long datapathId, ByteBuf input) {
        LOG.info("SHIM: Core message received");
        ConnectionAdapter conn = getConnectionAdapter(datapathId);
        if ( conn != null){
            // TODO: deserialize message and sends to the switch
        }
    }
    
    private synchronized ConnectionAdapter getConnectionAdapter(Long datapathId){
        for(ConnectionAdapter conn : connectionAdapterMap.keySet()){
            if (connectionAdapterMap.get(conn).longValue() == datapathId)
                return conn;
        }
        return null;
    }
}