/*
 * Copyright © 2016 Wipro and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.test.impl;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.data.Linkdata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.data.LinkdataBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;


import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public class TestProvider implements TestService, DataTreeChangeListener<Wipro> {

    private static final Logger LOG = LoggerFactory.getLogger(TestProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<TestService> serviceRegistration;
    private Registration listenerRegistration = null;

    public TestProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("TestProvider Session Initiated");
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(TestService.class, this);
        listenerRegistration = registerAsDataChangeListener();
    }

    private ListenerRegistration<TestProvider> registerAsDataChangeListener() {
        //1. Create instance identified
        InstanceIdentifier<Wipro> identifier = InstanceIdentifier.builder(Wipro.class).build();
        return dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,identifier),this);

    }


    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("TestProvider Closed");
        serviceRegistration.close();
    }



    @Override
    public Future<RpcResult<GetNameOutput>> getName(GetNameInput input) {
        LOG.info("getName is called ");
        if (input == null) {
            LOG.error("Error , Input is null ");
        }
        String firstName = input.getFirstName();
        String lastName = input.getLastName();
        String fullName = firstName + lastName;
        LOG.info("Name is  "+fullName);


        // Get Link details from Topology
       // List<Link> linkdetails = getLinksFromTopology();

        int id = getID();

        // Setting output
        List<Linkdata> outputLinks = new ArrayList<>();
        LinkdataBuilder linkdataBuilder = new LinkdataBuilder();
        outputLinks.add(linkdataBuilder.setName(""+id).build());

        GetNameOutputBuilder getNameOutputBuilder = new GetNameOutputBuilder();
        getNameOutputBuilder.setLinkdata(outputLinks);


        return RpcResultBuilder.success(getNameOutputBuilder.build()).buildFuture();
    }

    private int getID() {


        int id = 0;
        //1. Create instance identified
        InstanceIdentifier<Wipro> identifier = InstanceIdentifier.builder(Wipro.class).build();
        //2. Data Broker - Read Object
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        //3. Reading Topology details
        try {
            Optional<Wipro> wiproOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, identifier).get();
            if (wiproOptional.isPresent()) {
                id = wiproOptional.get().getId().intValue();
            }
        } catch (Exception e) {
            LOG.error("Error reading topology {}", identifier);
            readOnlyTransaction.close();
            throw new RuntimeException("Error reading from operational store, topology : " + identifier, e);
        }
        readOnlyTransaction.close();
        if (id == 0) {
            return 0;
        }
        return id;

    }

    /**
     * Method used to fetch link details from DataStore
     */
    private List<Link> getLinksFromTopology() {

        //1. Create instance identified
        InstanceIdentifier<Topology> topologyInstanceIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1"))).build();
        Topology topology = null;
        //2. Data Broker - Read Object
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();

        //3. Reading Topology details
        try {
            Optional<Topology> topologyOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, topologyInstanceIdentifier).get();
            if (topologyOptional.isPresent()) {
                topology = topologyOptional.get();
            }
        } catch (Exception e) {
            LOG.error("Error reading topology {}", topologyInstanceIdentifier);
            readOnlyTransaction.close();
            throw new RuntimeException("Error reading from operational store, topology : " + topologyInstanceIdentifier, e);
        }
        readOnlyTransaction.close();
        if (topology == null) {
            return null;
        }
        //4. Get links details from topology
        List<Link> links = topology.getLink();

        if (links == null || links.isEmpty()) {
            return null;
        }
//        List<Link> internalLinks = new ArrayList<>();
//        for (Link link : links) {
//            LOG.info("All link details : : "+internalLinks);
//            if (!(link.getLinkId().getValue().contains("host"))) {
//                internalLinks.add(link);
//            }
//        }
        LOG.info(" Returning Links from Topology");
        return links;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Wipro>> changes) {

        LOG.info(" Caution !! Id changed in the DataStore");


    }
}