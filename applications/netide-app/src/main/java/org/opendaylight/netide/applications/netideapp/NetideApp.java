/*
 * Copyright (c) 2016 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.applications.netideapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetideApp implements AutoCloseable, Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(NetideApp.class);

    public NetideApp() {
    }

    @Override
    public void close() {
        LOG.info("Netide App closing");
    }

    @Override
    public void run() {
        LOG.info("Netide App running");
    }
}


