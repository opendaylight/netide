package org.opendaylight.netide.netiplib;

/**
 * Created by arne on 02.11.15.
 */
class InvalidNetIdeMessage extends RuntimeException {
    InvalidNetIdeMessage(String msg) {
        super(msg);
    }
}