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
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.NetIdePacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PacketInMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.CommonMessageRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOut;

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
        registryHelper.registerSerializer(PacketOut.class, new NetIdePacketOutInputMessageFactory());
        registryHelper.registerSerializer(PacketIn.class, new PacketInMessageFactory());
    }
}
