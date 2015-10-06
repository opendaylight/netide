/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.serialization.NetIdeSerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShimMessageListener
        implements OpenflowProtocolListener, SystemNotificationsListener, ConnectionReadyListener {

    private static final Logger LOG = LoggerFactory.getLogger(ShimMessageListener.class);
    private ConnectionAdaptersRegistry connectionRegistry;
    public static final Long DEFAULT_XID = 0x01020304L;
    private ZeroMQBaseConnector coreConnector;
    private ConnectionAdapter switchConnection;
    private SerializationFactory factory;
    private IHandshakeListener handshakeListener;
    
    public ShimMessageListener(ZeroMQBaseConnector connector, ConnectionAdapter switchConnection) {
        this.coreConnector = connector;
        this.switchConnection = switchConnection;
        SerializerRegistry registry = new NetIdeSerializerRegistryImpl();
        registry.init();
        factory.setSerializerTable(registry);
    }
    
    public void registerConnectionAdaptersRegistry(ConnectionAdaptersRegistry connectionRegistry){
        this.connectionRegistry = connectionRegistry;
    }

    public void RegisterCoreListener(IHandshakeListener listener) {
        this.handshakeListener = listener;
    }
    
    /// OpenflowProtocolListener methods/////
    @Override
    public void onEchoRequestMessage(EchoRequestMessage arg0) {
        LOG.info("SHIM Echo request message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);
        if ( datapathId == null){
            EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
            builder.setVersion(arg0.getVersion());
            builder.setXid(arg0.getXid() + 1L);
            builder.setData(arg0.getData());
            this.switchConnection.echoReply(builder.build());
        }else{
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
            
    }

    @Override
    public void onErrorMessage(ErrorMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null){
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage arg0) {
        LOG.info("SHIM Experimenter message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null){
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage arg0) {
        LOG.info("SHIM Flow removed message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null){
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
    }

    @Override
    public void onHelloMessage(HelloMessage arg0) {
        LOG.info("SHIM Hello Message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);
        if (datapathId == null){
            handshakeListener.onSwitchHelloMessage(arg0.getXid(), arg0.getVersion());
        }else {
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
        
    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage arg0) {
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null){
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
    }

    @Override
    public void onPacketInMessage(PacketInMessage arg0) {
        LOG.info("SHIM Packet In message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null){
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage arg0) {
        LOG.info("SHIM Port Status message received: ", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null){
            ShimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(), datapathId.longValue());
        }
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onDisconnectEvent(DisconnectEvent arg0) {
        LOG.info("SHIM Disconnect event received: ", arg0);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent arg0) {
        LOG.info("SHIM Switch Idle event received: ", arg0);
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onConnectionReady() {
        LOG.info("SHIM Message: ConnectionReady"); 
    }
}
