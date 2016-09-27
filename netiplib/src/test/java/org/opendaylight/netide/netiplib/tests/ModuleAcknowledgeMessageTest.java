/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.netiplib.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.netide.netiplib.Message;
import org.opendaylight.netide.netiplib.MessageHeader;
import org.opendaylight.netide.netiplib.MessageType;
import org.opendaylight.netide.netiplib.ModuleAcknowledgeMessage;
import org.opendaylight.netide.netiplib.NetIDEProtocolVersion;
import org.opendaylight.netide.netiplib.NetIPConverter;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class ModuleAcknowledgeMessageTest {
    ModuleAcknowledgeMessage message;
    byte[] payload = ByteBufUtils.hexStringToBytes("70 61 79 6C 6F 61 64");
    byte[] expectedMessage = ByteBufUtils
            .hexStringToBytes("05 05 00 07 00 00 00 11 00 00 00 02 00 00 00 00 00 00 00 2A 70 61 79 6C 6F 61 64");

    @Before
    public void startUp() throws Exception {
        MessageHeader header = new MessageHeader();
        header.setNetIDEProtocolVersion(NetIDEProtocolVersion.VERSION_1_4);
        header.setMessageType(MessageType.MODULE_ACKNOWLEDGE);
        header.setPayloadLength((short) 7);
        header.setTransactionId(17);
        header.setModuleId(2);
        header.setDatapathId(42L);
        message = new ModuleAcknowledgeMessage();
        message.setHeader(header);
        message.setModuleName("payload");
    }

    @Test
    public void testSerialization() {
        Assert.assertArrayEquals("Wrong byte representation", expectedMessage, message.toByteRepresentation());
    }

    @Test
    public void testMessageParsingGeneral() {
        Message testMessage = NetIPConverter.parseRawMessage(expectedMessage);
        Assert.assertNotNull(testMessage);
        Assert.assertEquals(NetIDEProtocolVersion.VERSION_1_4, testMessage.getHeader().getNetIDEProtocolVersion());
        Assert.assertEquals(MessageType.MODULE_ACKNOWLEDGE, testMessage.getHeader().getMessageType());
        Assert.assertEquals(7, testMessage.getHeader().getPayloadLength());
        Assert.assertEquals(17, testMessage.getHeader().getTransactionId());
        Assert.assertEquals(2, testMessage.getHeader().getModuleId());
        Assert.assertEquals(42, testMessage.getHeader().getDatapathId());
        Assert.assertArrayEquals(testMessage.getPayload(), payload);
    }

    @Test
    public void testMessageParsingConcrete() {
        Message testMessage = NetIPConverter.parseConcreteMessage(expectedMessage);
        Assert.assertNotNull(testMessage);
        Assert.assertTrue(testMessage instanceof ModuleAcknowledgeMessage);
        ModuleAcknowledgeMessage moduleAck = (ModuleAcknowledgeMessage) testMessage;
        Assert.assertEquals(NetIDEProtocolVersion.VERSION_1_4, moduleAck.getHeader().getNetIDEProtocolVersion());
        Assert.assertEquals(MessageType.MODULE_ACKNOWLEDGE, moduleAck.getHeader().getMessageType());
        Assert.assertEquals(7, moduleAck.getHeader().getPayloadLength());
        Assert.assertEquals(17, moduleAck.getHeader().getTransactionId());
        Assert.assertEquals(2, moduleAck.getHeader().getModuleId());
        Assert.assertEquals(42, moduleAck.getHeader().getDatapathId());
        Assert.assertEquals("payload", moduleAck.getModuleName());
        Assert.assertArrayEquals(payload, moduleAck.getPayload());
    }
}
