/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group.desc._case.MultipartRequestGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group.features._case.MultipartRequestGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.features._case.MultipartRequestMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.desc._case.MultipartRequestPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class MultipartRequestInputMessageFactory implements OFDeserializer<MultipartRequestInput>, DeserializerRegistryInjector{
    private DeserializerRegistry registry;
    private static final byte PADDING = 4;
    private static final byte FLOW_PADDING_1 = 3;
    private static final byte FLOW_PADDING_2 = 4;
    private static final byte AGGREGATE_PADDING_1 = 3;
    private static final byte AGGREGATE_PADDING_2 = 4;
    
    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

    @Override
    public MultipartRequestInput deserialize(ByteBuf rawMessage) {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setVersion((short)EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        int type = rawMessage.readUnsignedShort();
        builder.setType(getMultipartType(type));
        builder.setFlags(getMultipartRequestFlags(rawMessage.readUnsignedShort()));
        rawMessage.skipBytes(PADDING);
        switch (MultipartType.forValue(type)) {
        case OFPMPFLOW:  builder.setMultipartRequestBody(setFlow(rawMessage));
                 break;
        case OFPMPAGGREGATE:  builder.setMultipartRequestBody(setAggregate(rawMessage));
                 break;
        case OFPMPTABLE:  builder.setMultipartRequestBody(setTable(rawMessage));
                 break;
        case OFPMPPORTSTATS:  builder.setMultipartRequestBody(setPortStats(rawMessage));
                 break;
        case OFPMPPORTDESC:   builder.setMultipartRequestBody(setPortDesc(rawMessage));
                 break;
        case OFPMPQUEUE:  builder.setMultipartRequestBody(setQueue(rawMessage));
                 break;
        case OFPMPGROUP:  builder.setMultipartRequestBody(setGroup(rawMessage));
                 break;
        case OFPMPGROUPDESC: builder.setMultipartRequestBody(setGroupDesc(rawMessage));
                 break;
        case OFPMPGROUPFEATURES: builder.setMultipartRequestBody(setGroupFeatures(rawMessage));
                 break;
        case OFPMPMETER:  builder.setMultipartRequestBody(setMeter(rawMessage));
                 break;
        case OFPMPMETERCONFIG: builder.setMultipartRequestBody(setMeterConfig(rawMessage));
                 break;
        case OFPMPMETERFEATURES: builder.setMultipartRequestBody(setMeterFeatures(rawMessage));
                 break;
        //case OFPMPTABLEFEATURES: builder.setMultipartRequestBody(setTableFeatures(rawMessage));
        //         break;
        case OFPMPEXPERIMENTER: builder.setMultipartRequestBody(setExperimenter(rawMessage));
                 break;
        default:
                 break;
        }
        
        return builder.build();
    }
    
    private static MultipartType getMultipartType(int input){
        return MultipartType.forValue(input);
    }
    
    private static MultipartRequestFlags getMultipartRequestFlags(int input){
        final Boolean _oFPMPFREQMORE = (input & (1 << 0)) > 0;
        MultipartRequestFlags flag = new MultipartRequestFlags(_oFPMPFREQMORE);
        return flag;
    }
    
    private MultipartRequestFlowCase setFlow(ByteBuf input){
        MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder flowBuilder = new MultipartRequestFlowBuilder();
        flowBuilder.setTableId(input.readUnsignedByte());
        input.skipBytes(FLOW_PADDING_1);
        flowBuilder.setOutPort(input.readUnsignedInt());
        flowBuilder.setOutGroup(input.readUnsignedInt());
        input.skipBytes(FLOW_PADDING_2);
        byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(cookie);
        flowBuilder.setCookie(new BigInteger(1, cookie));
        byte[] cookie_mask = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(cookie_mask);
        flowBuilder.setCookieMask(new BigInteger(1, cookie_mask));
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
        flowBuilder.setMatch(matchDeserializer.deserialize(input));
        caseBuilder.setMultipartRequestFlow(flowBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestAggregateCase setAggregate(ByteBuf input){
        MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder aggregateBuilder = new MultipartRequestAggregateBuilder();
        aggregateBuilder.setTableId(input.readUnsignedByte());
        input.skipBytes(AGGREGATE_PADDING_1);
        aggregateBuilder.setOutPort(input.readUnsignedInt());
        aggregateBuilder.setOutGroup(input.readUnsignedInt());
        input.skipBytes(AGGREGATE_PADDING_2);
        byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(cookie);
        aggregateBuilder.setCookie(new BigInteger(1, cookie));
        byte[] cookie_mask = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(cookie_mask);
        aggregateBuilder.setCookieMask(new BigInteger(1, cookie_mask));
        OFDeserializer<Match> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, EncodeConstants.EMPTY_VALUE, Match.class));
        aggregateBuilder.setMatch(matchDeserializer.deserialize(input));
        caseBuilder.setMultipartRequestAggregate(aggregateBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestPortDescCase setPortDesc(ByteBuf input){
        MultipartRequestPortDescCaseBuilder caseBuilder = new MultipartRequestPortDescCaseBuilder();
        MultipartRequestPortDescBuilder portBuilder = new MultipartRequestPortDescBuilder();
        portBuilder.setEmpty(true);
        caseBuilder.setMultipartRequestPortDesc(portBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestPortStatsCase setPortStats(ByteBuf input){
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder portBuilder = new MultipartRequestPortStatsBuilder();
        portBuilder.setPortNo(input.readUnsignedInt());
        caseBuilder.setMultipartRequestPortStats(portBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestQueueCase setQueue(ByteBuf input){
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder queueBuilder = new MultipartRequestQueueBuilder();
        queueBuilder.setPortNo(input.readUnsignedInt());
        queueBuilder.setQueueId(input.readUnsignedInt());
        caseBuilder.setMultipartRequestQueue(queueBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestGroupCase setGroup(ByteBuf input){
        MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        MultipartRequestGroupBuilder groupBuilder = new MultipartRequestGroupBuilder();
        groupBuilder.setGroupId(new GroupId(input.readUnsignedInt()));
        caseBuilder.setMultipartRequestGroup(groupBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestGroupDescCase setGroupDesc(ByteBuf input){
        MultipartRequestGroupDescCaseBuilder caseBuilder = new MultipartRequestGroupDescCaseBuilder();
        MultipartRequestGroupDescBuilder groupBuilder = new MultipartRequestGroupDescBuilder();
        groupBuilder.setEmpty(true);
        caseBuilder.setMultipartRequestGroupDesc(groupBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestGroupFeaturesCase setGroupFeatures(ByteBuf input){
        MultipartRequestGroupFeaturesCaseBuilder caseBuilder = new MultipartRequestGroupFeaturesCaseBuilder();
        MultipartRequestGroupFeaturesBuilder groupBuilder = new MultipartRequestGroupFeaturesBuilder();
        groupBuilder.setEmpty(true);
        caseBuilder.setMultipartRequestGroupFeatures(groupBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestMeterCase setMeter(ByteBuf input){
        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder();
        MultipartRequestMeterBuilder meterBuilder = new MultipartRequestMeterBuilder();
        meterBuilder.setMeterId(new MeterId(input.readUnsignedInt()));
        caseBuilder.setMultipartRequestMeter(meterBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestMeterConfigCase setMeterConfig(ByteBuf input){
        MultipartRequestMeterConfigCaseBuilder caseBuilder = new MultipartRequestMeterConfigCaseBuilder();
        MultipartRequestMeterConfigBuilder meterBuilder = new MultipartRequestMeterConfigBuilder();
        meterBuilder.setMeterId(new MeterId(input.readUnsignedInt()));
        caseBuilder.setMultipartRequestMeterConfig(meterBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestMeterFeaturesCase setMeterFeatures(ByteBuf input){
        MultipartRequestMeterFeaturesCaseBuilder caseBuilder = new MultipartRequestMeterFeaturesCaseBuilder();
        MultipartRequestMeterFeaturesBuilder meterBuilder = new MultipartRequestMeterFeaturesBuilder();
        meterBuilder.setEmpty(true);
        caseBuilder.setMultipartRequestMeterFeatures(meterBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestTableCase setTable(ByteBuf input){
        MultipartRequestTableCaseBuilder caseBuilder = new MultipartRequestTableCaseBuilder();
        MultipartRequestTableBuilder tableBuilder = new MultipartRequestTableBuilder();
        tableBuilder.setEmpty(true);
        caseBuilder.setMultipartRequestTable(tableBuilder.build());
        return caseBuilder.build();
    }
    
    private MultipartRequestExperimenterCase setExperimenter(ByteBuf input){
        MultipartRequestExperimenterCaseBuilder caseBuilder = new MultipartRequestExperimenterCaseBuilder();
        MultipartRequestExperimenterBuilder experimenterBuilder = new MultipartRequestExperimenterBuilder();
        caseBuilder.setMultipartRequestExperimenter(experimenterBuilder.build());
        return caseBuilder.build();
        
    }
}
