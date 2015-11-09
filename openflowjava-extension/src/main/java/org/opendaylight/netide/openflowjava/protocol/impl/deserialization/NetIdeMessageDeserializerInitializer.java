/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.openflowjava.protocol.impl.deserialization;

import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.BarrierInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.EchoInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.EchoReplyInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.FlowModInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.GetAsyncRequestMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.GetConfigInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.GetFeaturesInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.GetQueueConfigInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.GroupModInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.HelloInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.MeterModInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.MultipartRequestInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.PacketOutInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.PortModInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.RoleRequestInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.SetAsyncInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.SetConfigInputMessageFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories.TableModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.SimpleDeserializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;

public final class NetIdeMessageDeserializerInitializer {

    private NetIdeMessageDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    public static void registerMessageDeserializers(DeserializerRegistry registry) {
        // register OF v1.3 NEW message deserializers
        SimpleDeserializerRegistryHelper helper;
        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
        //helper.registerDeserializer(0, null, HelloInput.class, new HelloInputMessageFactory());
        //helper.registerDeserializer(2, null, EchoInput.class, new EchoInputMessageFactory());
        //helper.registerDeserializer(3, null, EchoReplyInput.class, new EchoReplyInputMessageFactory());
        helper.registerDeserializer(5, null, GetFeaturesInput.class, new GetFeaturesInputMessageFactory());
        helper.registerDeserializer(7, null, GetConfigInput.class, new GetConfigInputMessageFactory());
        helper.registerDeserializer(9, null, SetConfigInput.class, new SetConfigInputMessageFactory());
        helper.registerDeserializer(13, null, PacketOutInput.class, new PacketOutInputMessageFactory());
        helper.registerDeserializer(14, null, FlowModInput.class, new FlowModInputMessageFactory());
        helper.registerDeserializer(15, null, GroupModInput.class, new GroupModInputMessageFactory());
        helper.registerDeserializer(16, null, PortModInput.class, new PortModInputMessageFactory());
        helper.registerDeserializer(17, null, TableModInput.class, new TableModInputMessageFactory());
        helper.registerDeserializer(18, null, MultipartRequestInput.class, new MultipartRequestInputMessageFactory());
        helper.registerDeserializer(20, null, BarrierInput.class, new BarrierInputMessageFactory());
        helper.registerDeserializer(22, null, GetQueueConfigInput.class, new GetQueueConfigInputMessageFactory());
        helper.registerDeserializer(24, null, RoleRequestInput.class, new RoleRequestInputMessageFactory());
        helper.registerDeserializer(26, null, GetAsyncInput.class, new GetAsyncRequestMessageFactory());
        helper.registerDeserializer(28, null, SetAsyncInput.class, new SetAsyncInputMessageFactory());
        helper.registerDeserializer(29, null, MeterModInput.class, new MeterModInputMessageFactory());
        
    }
}
