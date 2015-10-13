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
import java.util.LinkedHashMap;
import java.util.Set;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class ConnectionAdaptersRegistry {
    
    private static HashMap<ConnectionAdapter, BigInteger> connectionAdapterMap;
    
    public void init(){
        connectionAdapterMap = new LinkedHashMap<ConnectionAdapter, BigInteger>();
    }
    
    public synchronized void setConnectionAdapterMap(HashMap<ConnectionAdapter, BigInteger> map){
        connectionAdapterMap = map;
    }
    
    public synchronized void registerConnectionAdapter(ConnectionAdapter connectionAdapter, BigInteger datapathID){
        connectionAdapterMap.put(connectionAdapter, datapathID);
    }
    
    public synchronized BigInteger getDatapathID(ConnectionAdapter connectionAdapter){
        return connectionAdapterMap.get(connectionAdapter);
    }
    
    public synchronized ConnectionAdapter getConnectionAdapter(Long datapathId){
        for(ConnectionAdapter conn : connectionAdapterMap.keySet()){
            if (connectionAdapterMap.get(conn).longValue() == datapathId)
                return conn;
        }
        return null;
    }
    
    public Set<ConnectionAdapter> getConnectionAdapters(){
        return connectionAdapterMap.keySet();
    }
    
    public synchronized boolean removeConnectionAdapter(ConnectionAdapter conn){
        BigInteger datapathID = connectionAdapterMap.remove(conn);
        if (datapathID != null)
            return true;
        return false;
    }
}
