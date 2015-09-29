package org.opendaylight.netide.netiplib;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Class representing a message of type OPENFLOW.
 * Note that this only serves as a convenience class - if the MessageType is manipulated, the class will not recognize that.
 */
public class OpenFlowMessage extends Message {
    private DataObject ofMessage;

    /**
     * Instantiates a new Open flow message.
     */
    public OpenFlowMessage() {
        super(new MessageHeader(), new byte[0]);
        header.setMessageType(MessageType.OPENFLOW);
    }

    /**
     * Gets of message.
     *
     * @return the OF message
     */
    public DataObject getOfMessage() {
        return ofMessage;
    }

    /**
     * Sets of message.
     *
     * @param ofMessage the OF message
     */
    public void setOfMessage(DataObject ofMessage) {
        this.ofMessage = ofMessage;
    }

    @Override
    public byte[] getPayload() { 
        SerializerRegistry sRegistry = new SerializerRegistryImpl();
        sRegistry.init();
        SerializationFactory sFactory = new SerializationFactory();
        sFactory.setSerializerTable(sRegistry);
        ByteBuf bufOut = PooledByteBufAllocator.DEFAULT.buffer();
        
        short ofVersion = Short.valueOf(bufOut.readByte());
        sFactory.messageToBuffer(ofVersion, bufOut, ofMessage);
        return bufOut.array();
    }
}
