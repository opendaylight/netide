/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class MultipartReplyMessageFactory implements OFSerializer<MultipartReplyMessage>, SerializerRegistryInjector {

    private static final byte MESSAGE_TYPE = 19;
    private SerializerRegistry registry;
    private static final byte PADDING = 4;
    private static final byte PORT_DESC_PADDING_1 = 4;
    private static final byte PORT_DESC_PADDING_2 = 2;
    
    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }

    @Override
    public void serialize(MultipartReplyMessage message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeShort(message.getType().getIntValue());
        writeFlags(message.getFlags(), outBuffer);
        outBuffer.writeZero(PADDING);
        switch (message.getType()){
        case OFPMPPORTDESC: serializePortDescBody(message.getMultipartReplyBody(), outBuffer);
            break;
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }
    
    public void writeFlags(MultipartRequestFlags flags, ByteBuf outBuffer){
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, flags.isOFPMPFREQMORE());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeShort(bitmap);
    }
    
    private void serializePortDescBody(MultipartReplyBody body, ByteBuf outBuffer){
        MultipartReplyPortDescCase portCase = (MultipartReplyPortDescCase) body;
        MultipartReplyPortDesc portDesc = portCase.getMultipartReplyPortDesc();
        for ( Ports port : portDesc.getPorts()){
            outBuffer.writeInt(port.getPortNo().intValue());    // Assuming PortNo = PortId
            outBuffer.writeZero(PORT_DESC_PADDING_1);
            writeMacAddress(port.getHwAddr().getValue(), outBuffer);
            outBuffer.writeZero(PORT_DESC_PADDING_2);
            writeName(port.getName(), outBuffer);
            writePortConfig(port.getConfig(), outBuffer);
            writePortState(port.getState(), outBuffer);
            writePortFeatures(port.getCurrentFeatures(), outBuffer);
            writePortFeatures(port.getAdvertisedFeatures(), outBuffer);
            writePortFeatures(port.getSupportedFeatures(), outBuffer);
            writePortFeatures(port.getPeerFeatures(), outBuffer);
            outBuffer.writeInt(port.getCurrSpeed().intValue());
            outBuffer.writeInt(port.getMaxSpeed().intValue());
        }
    }
    
    private void writeName(String name, ByteBuf outBuffer){
        byte[] nameBytes = name.getBytes();
        if (nameBytes.length < 16){
            byte[] nameBytesPadding = new byte[16];
            int i = 0;
            for (byte b : nameBytes){
                nameBytesPadding[i] = b;
                i++;
            }
            for (; i< 16; i++){
                nameBytesPadding[i] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        }else{
            outBuffer.writeBytes(nameBytes);
        }
            
    }
    
    private void writeMacAddress(String macAddress, ByteBuf outBuffer){
        String[] macAddressParts = macAddress.split(":");
        byte[] macAddressBytes = new byte[6];
        for(int i=0; i<6; i++){
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }
        outBuffer.writeBytes(macAddressBytes);
    }
    private void writePortConfig(PortConfig config, ByteBuf outBuffer){
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, config.isPortDown());
        map.put(2, config.isNoRecv());
        map.put(5, config.isNoFwd());
        map.put(6, config.isNoPacketIn());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }
    
    private void writePortState(PortState state, ByteBuf outBuffer){
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, state.isLinkDown());
        map.put(1, state.isBlocked());
        map.put(2, state.isLive());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }
    
    private void writePortFeatures(PortFeatures features, ByteBuf outBuffer){
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, features.is_10mbHd());
        map.put(1, features.is_10mbFd());
        map.put(2, features.is_100mbHd());
        map.put(3, features.is_100mbFd());
        map.put(4, features.is_1gbHd());
        map.put(5, features.is_1gbFd());
        map.put(6, features.is_10gbFd());
        map.put(7, features.is_40gbFd());
        map.put(8, features.is_100gbFd());
        map.put(9, features.is_1tbFd());
        map.put(10, features.isOther());
        map.put(11, features.isCopper());
        map.put(12, features.isFiber());
        map.put(13, features.isAutoneg());
        map.put(14, features.isPause());
        map.put(15, features.isPauseAsym());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }
    
    
    
}
