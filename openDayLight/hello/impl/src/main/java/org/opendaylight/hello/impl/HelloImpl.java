/*
 * Copyright © 2016 Wipro and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.hello.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.GetNameInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.GetNameInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.GetNameOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.test.rev150105.TestService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HelloImpl implements HelloService {

    private static final Logger LOG = LoggerFactory.getLogger(HelloImpl.class);
    public static TestService testService = null;

    public HelloImpl(TestService testService) {
        LOG.info("Testservice has been set");
        this.testService = testService;
    }


    @Override
    public Future<RpcResult<HelloOutput>> hello(HelloInput input) {
        LOG.info("RPC implementation started");
        HelloOutputBuilder helloBuilder = new HelloOutputBuilder();
        // Call another RPC
        try {
            if (testService != null) {
                GetNameInputBuilder getNameInputBuilder = new GetNameInputBuilder();
                getNameInputBuilder.setFirstName(input.getName());
                getNameInputBuilder.setLastName("Last name");
                Future<RpcResult<GetNameOutput>> response = testService.getName(getNameInputBuilder.build());
                try {
                    // Get output and send back
                    String fullName = response.get().getResult().getLinkdata().toString();
                    helloBuilder.setResult("Hello " + fullName);
                } catch (InterruptedException e) {
                    LOG.error("Exception " + e.getMessage());
                } catch (ExecutionException e) {
                    LOG.error("Exception " + e.getMessage());
                }

            } else {
                throw new Exception("test service is Null");
            }

        }catch (Exception exception) {
            helloBuilder.setResult(exception.getMessage());
            LOG.error("Exception " + exception.getMessage());
        }






        return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
    }

}

