/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.opendaylight.netide.netiplib.Message;
import org.opendaylight.netide.netiplib.MessageType;
import org.opendaylight.netide.netiplib.NetIPUtils;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PacketInMessageFactory;
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
    private ZeroMQBaseConnector coreConnector;
    private ConnectionAdapter switchConnection;

    public ShimMessageListener(ZeroMQBaseConnector connector, ConnectionAdapter switchConnection) {
        this.coreConnector = connector;
        this.switchConnection = switchConnection;
    }

    private void sendToCore(byte[] data) {
        Message message = new Message(NetIPUtils.StubHeaderFromPayload(data), data);
        message.getHeader().setMessageType(MessageType.OPENFLOW);
        // TODO: find the correct values
        message.getHeader().setDatapathId(0);
        message.getHeader().setModuleId(0);
        message.getHeader().setTransactionId(0);
        coreConnector.SendData(message.toByteRepresentation());
    }

    /// OpenflowProtocolListener methods/////
    @Override
    public void onEchoRequestMessage(EchoRequestMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onErrorMessage(ErrorMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onHelloMessage(HelloMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
        HelloInputBuilder builder = new HelloInputBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.fieldsFrom(arg0);
        switchConnection.hello(builder.build());
    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onPacketInMessage(PacketInMessage pIn) {
        LOG.info("SHIM Message received: ", pIn);
        ByteBuf out = Unpooled.buffer();
        PacketInMessageFactory factory = new PacketInMessageFactory();
        factory.serialize(pIn, out);
        sendToCore(out.array());
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onDisconnectEvent(DisconnectEvent arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onConnectionReady() {
        LOG.info("SHIM Message: ConnectionReady");
    }
}
