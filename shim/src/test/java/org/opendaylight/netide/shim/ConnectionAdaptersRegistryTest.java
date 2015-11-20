/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionAdapterImpl;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class ConnectionAdaptersRegistryTest {
    ConnectionAdaptersRegistry registry;

    @Mock
    ConnectionAdapterImpl conn;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registry = new ConnectionAdaptersRegistry();
    }

    @Test
    public void testNoInit() {
        try {
            registry.getConnectionAdapters();
        } catch (NullPointerException e) {
            Assert.assertNotNull(e.getMessage());
        }

    }

    @Test
    public void testInit() {
        registry.init();
        Set<ConnectionAdapter> result = new HashSet<ConnectionAdapter>();
        Assert.assertEquals("Wrong key set", result, registry.getConnectionAdapters());
    }

    @Test
    public void testSetConnectionAdapterMap() {
        registry.init();
        HashMap<ConnectionAdapter, BigInteger> map = new LinkedHashMap<ConnectionAdapter, BigInteger>();
        map.put(conn, new BigInteger("1"));
        registry.setConnectionAdapterMap(map);
        Assert.assertEquals("Wrong map", conn, registry.getConnectionAdapter(1L));
    }

    @Test
    public void testRegisterConnectionAdapter() {
        registry.init();
        registry.registerConnectionAdapter(conn, new BigInteger("1"));
        Assert.assertEquals("Wrong map", conn, registry.getConnectionAdapter(1L));
    }

    @Test
    public void testGetDatapathID() {
        registry.init();
        registry.registerConnectionAdapter(conn, new BigInteger("1"));
        Assert.assertEquals("Wrong map", new BigInteger("1"), registry.getDatapathID(conn));
    }

    @Test
    public void testGetConnectionAdapter() {
        registry.init();
        registry.registerConnectionAdapter(conn, new BigInteger("1"));
        Assert.assertEquals("Wrong map", conn, registry.getConnectionAdapter(1L));
    }

    @Test
    public void testGetConnectionAdapters() {
        registry.init();
        registry.registerConnectionAdapter(conn, new BigInteger("1"));
        Set<ConnectionAdapter> result = new HashSet<ConnectionAdapter>();
        result.add(conn);
        Assert.assertEquals("Wrong key set", result, registry.getConnectionAdapters());
    }

    @Test
    public void testRemoveConnectionAdapter() {
        registry.init();
        Assert.assertEquals(false, registry.removeConnectionAdapter(conn));
        registry.registerConnectionAdapter(conn, new BigInteger("1"));
        Assert.assertEquals(true, registry.removeConnectionAdapter(conn));
        Assert.assertNull(registry.getConnectionAdapter(1L));
    }

}
