/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class ShimMessageListenerTest {

    ShimMessageListener messageListener;

    @Mock
    ConnectionAdapter switchConnection;

    @Mock
    ConnectionAdapter switchConnection2;

    @Mock
    ZeroMQBaseConnector connector;

    @Mock
    ShimSwitchConnectionHandlerImpl handshakeListener;

    @Mock
    ShimRelay shimRelay;

    @Mock
    EchoRequestMessage echo;

    @Mock
    ErrorMessage error;

    @Mock
    ExperimenterMessage experimenter;

    @Mock
    FlowRemovedMessage flowRemoved;

    @Mock
    HelloMessage hello;

    @Mock
    MultipartReplyMessage multipartReply;

    @Mock
    PacketInMessage packetIn;

    @Mock
    PortStatusMessage portStatus;

    @Mock
    DisconnectEvent disconnectEvent;

    ConnectionAdaptersRegistry registry;

    @Mock
    ShimSwitchConnectionHandlerImpl handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        messageListener = new ShimMessageListener(connector, switchConnection, shimRelay, handler);
        registry = new ConnectionAdaptersRegistry();
        registry.init();
        messageListener.registerConnectionAdaptersRegistry(registry);
        messageListener.registerHandshakeListener(handshakeListener);
    }

    @Test
    public void testOnEchoRequestMessage1() {
        messageListener.onEchoRequestMessage(echo);
        Mockito.verify(switchConnection).echoReply(Matchers.any(EchoReplyInput.class));
        Mockito.verify(handler).sendGetFeaturesToSwitch(echo.getVersion(), ShimSwitchConnectionHandlerImpl.DEFAULT_XID,
                switchConnection, 0);

    }

    @Test
    public void testOnEchoRequestMessage2() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onEchoRequestMessage(echo);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, echo, echo.getVersion(), echo.getXid(), 1L, 0);
    }

    @Test
    public void testOnErrorMessage() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onErrorMessage(error);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, error, error.getVersion(), error.getXid(), 1L,
                0);
    }

    @Test
    public void testOnExperimenterMessage() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onExperimenterMessage(experimenter);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, experimenter, experimenter.getVersion(),
                experimenter.getXid(), 1L, 0);
    }

    @Test
    public void testOnFlowRemovedMessage() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onFlowRemovedMessage(flowRemoved);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, flowRemoved, flowRemoved.getVersion(),
                flowRemoved.getXid(), 1L, 0);
    }

    @Test
    public void testOnHelloMessage1() {
        messageListener.onHelloMessage(hello);
        Mockito.verify(handshakeListener).onSwitchHelloMessage(hello.getXid(), hello.getVersion());
    }

    @Test
    public void testOnHelloMessage2() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onHelloMessage(hello);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, hello, hello.getVersion(), hello.getXid(), 1L,
                0);
    }

    @Test
    public void testOnMultipartReplyMessage() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onMultipartReplyMessage(multipartReply);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, multipartReply, multipartReply.getVersion(),
                multipartReply.getXid(), 1L, 0);
    }

    @Test
    public void testOnPacketInMessage() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onPacketInMessage(packetIn);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, packetIn, packetIn.getVersion(),
                packetIn.getXid(), 1L, 0);
    }

    @Test
    public void testOnPortStatusMessage() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onPortStatusMessage(portStatus);
        Mockito.verify(shimRelay).sendOpenFlowMessageToCore(connector, portStatus, portStatus.getVersion(),
                portStatus.getXid(), 1L, 0);
    }

    @Test
    public void testOnDisconnectEvent() {
        registry.registerConnectionAdapter(switchConnection, new BigInteger("1"));
        messageListener.onDisconnectEvent(disconnectEvent);
        Assert.assertNull(registry.getConnectionAdapter(1L));
    }

}
