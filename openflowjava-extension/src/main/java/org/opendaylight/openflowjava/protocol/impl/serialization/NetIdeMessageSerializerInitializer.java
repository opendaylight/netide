/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.BarrierReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.EchoRequestMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.ErrorMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.ExperimenterMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.FlowRemovedMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GetConfigReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.GetFeaturesOutputFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.HelloMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.MultipartReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.NetIdePacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PacketInMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PortStatusMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.QueueGetConfigReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.RoleReplyMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.CommonMessageRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public final class NetIdeMessageSerializerInitializer {
    
    private NetIdeMessageSerializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }
    
    public static void registerMessageSerializers(SerializerRegistry registry) {
        short version = EncodeConstants.OF13_VERSION_ID;
        CommonMessageRegistryHelper registryHelper = new CommonMessageRegistryHelper(version, registry);
        registryHelper.registerSerializer(PacketInMessage.class, new PacketInMessageFactory());
        registryHelper.registerSerializer(PacketOutInput.class, new NetIdePacketOutInputMessageFactory());
        registryHelper.registerSerializer(GetFeaturesOutput.class, new GetFeaturesOutputFactory());
        registryHelper.registerSerializer(EchoRequestMessage.class, new EchoRequestMessageFactory());
        registryHelper.registerSerializer(MultipartReplyMessage.class, new MultipartReplyMessageFactory());
        registryHelper.registerSerializer(HelloMessage.class, new HelloMessageFactory());
        registryHelper.registerSerializer(ErrorMessage.class, new ErrorMessageFactory());
        registryHelper.registerSerializer(ExperimenterMessage.class, new ExperimenterMessageFactory());
        registryHelper.registerSerializer(GetConfigOutput.class, new GetConfigReplyMessageFactory());
        registryHelper.registerSerializer(FlowRemovedMessage.class, new FlowRemovedMessageFactory());
        registryHelper.registerSerializer(PortStatusMessage.class, new PortStatusMessageFactory());
        registryHelper.registerSerializer(BarrierOutput.class, new BarrierReplyMessageFactory());
        registryHelper.registerSerializer(GetQueueConfigOutput.class, new QueueGetConfigReplyMessageFactory());
        registryHelper.registerSerializer(RoleRequestOutput.class, new RoleReplyMessageFactory());
    }
}
