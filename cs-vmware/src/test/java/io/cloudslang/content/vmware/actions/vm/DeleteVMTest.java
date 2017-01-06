/*******************************************************************************
 * (c) Copyright 2017 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.content.vmware.actions.vm;

import io.cloudslang.content.vmware.entities.VmInputs;
import io.cloudslang.content.vmware.entities.http.HttpInputs;
import io.cloudslang.content.vmware.services.VmService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by Mihai Tusa.
 * 1/12/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DeleteVM.class)
public class DeleteVMTest {
    private DeleteVM deleteVM;

    @Before
    public void init() {
        deleteVM = new DeleteVM();
    }

    @After
    public void tearDown() {
        deleteVM = null;
    }

    @Mock
    private VmService vmServiceMock;

    @Test
    public void testDeletesVM() throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        whenNew(VmService.class).withNoArguments().thenReturn(vmServiceMock);
        when(vmServiceMock.deleteVM(any(HttpInputs.class), any(VmInputs.class))).thenReturn(resultMap);

        resultMap = deleteVM.deleteVM("", "", "", "", "", "", "");

        verify(vmServiceMock, times(1)).deleteVM(any(HttpInputs.class), any(VmInputs.class));

        assertNotNull(resultMap);
    }

    @Test
    public void testDeletesVMProtocolException() throws Exception {
        Map<String, String> resultMap = deleteVM.deleteVM("", "", "myProtocol", "", "", "", "");

        verify(vmServiceMock, never()).deleteVM(any(HttpInputs.class), any(VmInputs.class));

        assertNotNull(resultMap);
        assertEquals(-1, Integer.parseInt(resultMap.get("returnCode")));
        assertEquals("Unsupported protocol value: [myProtocol]. Valid values are: https, http.", resultMap.get("returnResult"));
    }
}
