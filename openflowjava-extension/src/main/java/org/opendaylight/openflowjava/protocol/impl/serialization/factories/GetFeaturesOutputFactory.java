/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class GetFeaturesOutputFactory  implements OFSerializer<GetFeaturesOutput>, SerializerRegistryInjector{
    
    private SerializerRegistry registry;
    private static final byte MESSAGE_TYPE = 6;
    private static final byte PADDING = 4;
    
    @Override
    public void serialize(GetFeaturesOutput message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeLong(message.getDatapathId().longValue());
        outBuffer.writeInt(message.getBuffers().intValue());
        outBuffer.writeByte(message.getTables().intValue());
        outBuffer.writeByte(message.getAuxiliaryId().intValue());
        outBuffer.writeZero(PADDING);
        outBuffer.writeInt(createCapabilities(message.getCapabilities().getValue()));
        outBuffer.writeInt(message.getReserved().intValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    
    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }
    
    private byte createCapabilities(boolean[] bool){
        byte b = (byte)((bool[0]?1<<7:0) + (bool[1]?1<<6:0) + (bool[2]?1<<5:0) + 
                (bool[3]?1<<4:0) + (bool[4]?1<<3:0) + (bool[5]?1<<2:0) + 
                (bool[6]?1<<1:0));
        
        return b;
    }
}
