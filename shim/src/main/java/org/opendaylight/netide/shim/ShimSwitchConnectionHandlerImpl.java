/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;


public class ShimSwitchConnectionHandlerImpl implements SwitchConnectionHandler, ICoreListener {  
	
	private static final Logger LOG = LoggerFactory.getLogger(ShimSwitchConnectionHandlerImpl.class);
	private static ZeroMQBaseConnector coreConnector;
	private List<ConnectionAdapter> connectionAdapterList; 
	
	public ShimSwitchConnectionHandlerImpl(ZeroMQBaseConnector connector){
		coreConnector = connector;
		connectionAdapterList = new ArrayList<ConnectionAdapter>();
	}
	
	@Override
	public boolean accept(InetAddress arg0) {
		return true;
	}

	@Override
	public void onSwitchConnected(ConnectionAdapter connectionAdapter) {
		LOG.info("SHIM: on Switch connected: " + connectionAdapter.getRemoteAddress().toString());
		connectionAdapterList.add(connectionAdapter);
		ShimMessageListener listener = new ShimMessageListener(coreConnector, connectionAdapter);
		connectionAdapter.setMessageListener(listener);
		connectionAdapter.setSystemListener(listener);
		connectionAdapter.setConnectionReadyListener(listener);
	}

	@Override
	public void onCoreMessage(ByteBuf input) {
		// TODO Auto-generated method stub
	}

}


