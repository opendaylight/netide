package org.opendaylight.netide.netiplib;

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
}
