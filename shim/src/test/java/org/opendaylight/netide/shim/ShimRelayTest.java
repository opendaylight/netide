/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.NetIdeDeserializationFactory;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ShimRelayTest {

    @Mock
    ZeroMQBaseConnector coreConnector;

    @Mock
    SerializationFactory factory;

    @Mock
    NetIdeDeserializationFactory deserializationFactory;

    @Mock
    ConnectionAdapter connectionAdapter;

    @Mock
    ShimRelay shimRelay;

    short ofVersion = EncodeConstants.OF13_VERSION_ID;

    DataObject msg;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(ShimRelayTest.class);
        HelloInputBuilder builder = new HelloInputBuilder();
        builder.setXid(1L);
        builder.setVersion(ofVersion);
        List<Elements> listElements = new ArrayList<>();
        ElementsBuilder elementsBuilder = new ElementsBuilder();
        elementsBuilder.setType(HelloElementType.VERSIONBITMAP);
        List<Boolean> bitmap = new ArrayList<>();
        bitmap.add(true);
        elementsBuilder.setVersionBitmap(bitmap);
        listElements.add(elementsBuilder.build());
        builder.setElements(listElements);
        msg = builder.build();
        Mockito.when(coreConnector.SendData(Matchers.any(byte[].class))).thenReturn(true);
        Mockito.when(deserializationFactory.deserialize(Matchers.any(ByteBuf.class), Mockito.eq(ofVersion)))
                .thenReturn(msg);
        Mockito.when(shimRelay.createNetideDeserializationFactory()).thenReturn(deserializationFactory);
        Mockito.when(shimRelay.createSerializationFactory()).thenReturn(factory);

    }

    @Test
    public void testSendOpenFlowMessageToCore() {
        Mockito.doCallRealMethod().when(shimRelay).sendOpenFlowMessageToCore(coreConnector, msg, ofVersion, 1L, 1, 1);
        shimRelay.sendOpenFlowMessageToCore(coreConnector, msg, ofVersion, 1L, 1, 1);
        Mockito.verify(factory).messageToBuffer(Mockito.eq(ofVersion), Matchers.any(ByteBuf.class), Mockito.eq(msg));
        Mockito.verify(coreConnector).SendData(Matchers.any(byte[].class));
    }

    @Test
    public void testSendToSwitch() {
        ByteBuf input = UnpooledByteBufAllocator.DEFAULT.buffer();
        Mockito.doCallRealMethod().when(shimRelay).sendToSwitch(connectionAdapter, input, ofVersion, coreConnector, 1L,
                1);
        shimRelay.sendToSwitch(connectionAdapter, input, ofVersion, coreConnector, 1L, 1);
        Mockito.verify(shimRelay).sendDataObjectToSwitch(connectionAdapter, msg, ofVersion, coreConnector, 1L, 1);
    }

}
