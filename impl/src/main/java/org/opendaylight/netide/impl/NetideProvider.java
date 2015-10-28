/*
 * Copyright(c) Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.rev151001.NetideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetideProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NetideProvider.class);
    private RpcRegistration<NetideService> netideService;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("NetideProvider Session Initiated");
        netideService = session.addRpcImplementation(NetideService.class, new StatusImpl());
    }

    @Override
    public void close() throws Exception {
        LOG.info("NetideProvider Closed");
        if (netideService != null) {
            netideService.close();
        }
    }

}