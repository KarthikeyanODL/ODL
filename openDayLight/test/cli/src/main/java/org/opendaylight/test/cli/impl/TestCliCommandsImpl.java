/*
 * Copyright © 2016 Wipro and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.test.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.test.cli.api.TestCliCommands;

public class TestCliCommandsImpl implements TestCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(TestCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public TestCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("TestCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}