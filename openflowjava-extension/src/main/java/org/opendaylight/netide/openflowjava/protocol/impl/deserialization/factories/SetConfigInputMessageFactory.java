/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;

/**
 * Translates FlowModInput messages
 */
public class SetConfigInputMessageFactory implements OFDeserializer<SetConfigInput>, DeserializerRegistryInjector {

    private DeserializerRegistry registry;

    @Override
    public SetConfigInput deserialize(ByteBuf rawMessage) {
        SetConfigInputBuilder builder = new SetConfigInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        SwitchConfigFlag flag = createSwitchConfigFlagsFromBitmap(rawMessage.readUnsignedShort());
        builder.setFlags(flag);
        builder.setMissSendLen(rawMessage.readUnsignedShort());
        return builder.build();
    }

    private static SwitchConfigFlag createSwitchConfigFlagsFromBitmap(int input) {
        return SwitchConfigFlag.forValue(input);
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}