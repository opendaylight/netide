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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
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
    private IHandshakeListener handshakeListener;
    private ShimRelay shimRelay;

    public ShimMessageListener(ZeroMQBaseConnector connector, ConnectionAdapter switchConnection,
            ShimRelay _shimRelay) {
        this.coreConnector = connector;
        this.switchConnection = switchConnection;
        this.shimRelay = _shimRelay;
    }

    public void registerConnectionAdaptersRegistry(ConnectionAdaptersRegistry connectionRegistry) {
        this.connectionRegistry = connectionRegistry;
    }

    public void registerHandshakeListener(IHandshakeListener listener) {
        this.handshakeListener = listener;
    }

    /// OpenflowProtocolListener methods/////
    @Override
    public void onEchoRequestMessage(EchoRequestMessage arg0) {

        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);
        if (datapathId == null) {
            LOG.info("SHIM Echo request message received. Managed by shim.");
            EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
            builder.setVersion(arg0.getVersion());
            builder.setXid(arg0.getXid() + 1L);
            builder.setData(arg0.getData());
            this.switchConnection.echoReply(builder.build());
        } else {
            LOG.info("SHIM Echo request message received. Sent to core.");
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    @Override
    public void onErrorMessage(ErrorMessage arg0) {
        LOG.info("SHIM Message received");
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null) {
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage arg0) {
        LOG.info("SHIM Experimenter message received");
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null) {
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage arg0) {
        LOG.info("SHIM Flow removed message received");
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null) {
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    @Override
    public void onHelloMessage(HelloMessage arg0) {
        LOG.info("SHIM Hello Message received");
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);
        if (datapathId == null) {
            handshakeListener.onSwitchHelloMessage(arg0.getXid(), arg0.getVersion());
        } else {
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }

    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage arg0) {
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null) {
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    @Override
    public void onPacketInMessage(PacketInMessage arg0) {
        LOG.info("SHIM Packet In message received");
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);
        if (datapathId != null) {
            LOG.info("SHIM Packet In message send to core. DatapathId: {}", datapathId);
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage arg0) {
        LOG.info("SHIM Port Status message received: {}", arg0);
        BigInteger datapathId = this.connectionRegistry.getDatapathID(this.switchConnection);

        if (datapathId != null) {
            shimRelay.sendOpenFlowMessageToCore(coreConnector, arg0, arg0.getVersion(), arg0.getXid(),
                    datapathId.longValue(), 0);
        }
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onDisconnectEvent(DisconnectEvent arg0) {
        LOG.info("SHIM Disconnect event received: {}", arg0);
        this.connectionRegistry.removeConnectionAdapter(this.switchConnection);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent arg0) {
        LOG.info("SHIM Switch Idle event received: {}", arg0);
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onConnectionReady() {
        LOG.info("SHIM Message: ConnectionReady");
    }
}
