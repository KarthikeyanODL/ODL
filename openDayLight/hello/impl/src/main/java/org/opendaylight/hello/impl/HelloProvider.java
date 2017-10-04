/*
 * Copyright © 2016 Wipro and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.hello.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloProvider {

    private static final Logger LOG = LoggerFactory.getLogger(HelloProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<HelloService> serviceRegistration;


    public HelloProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        LOG.info("Constructor called ");
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry=rpcProviderRegistry;

    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("Added RPC implementation ");
        TestService testService = rpcProviderRegistry.getRpcService(TestService.class);
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(HelloService.class, new HelloImpl(testService));
        LOG.info("HelloProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        serviceRegistration.close();
        LOG.info("HelloProvider Closed");
    }
}