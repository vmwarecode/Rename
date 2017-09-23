/*
 * ****************************************************************************
 * Copyright VMware, Inc. 2010-2016.  All Rights Reserved.
 * ****************************************************************************
 *
 * This software is made available for use under the terms of the BSD
 * 3-Clause license:
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.vmware.general;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.Map;

/**
 * <pre>
 * Rename
 *
 * This sample demonstrates renaming a manged entity
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * entityname   [required] : name of the inventory object - a managed entity
 * newname      [required] : new name of the inventory object - a managed entity
 *
 * <b>Command Line:</b>
 * Rename a virtual machine VM to new name VM2
 * run.bat com.vmware.general --url [webserviceurl]
 * --username [username] --password [password] --entityname VM
 * --newname VM2
 *
 * Rename a resource pool named Rp to new name pool
 * java com.vmware.general --url [webserviceurl]
 * --username [username] --password [password] --entityname Rp
 * --newname pool
 * </pre>
 */

@Sample(name = "rename", description = "This sample demonstrates renaming a manged entity")
public class Rename extends ConnectedVimServiceBase {

    private String entityname;
    private String newentityname;

    @Option(name = "entityname", description = "name of the inventory object - a managed entity")
    public void setEntityname(String entityname) {
        this.entityname = entityname;
    }

    @Option(name = "newname", description = "new name of the inventory object - a managed entity")
    public void setNewentityname(String newentityname) {
        this.newentityname = newentityname;
    }

    /**
     * This method returns a boolean value specifying whether the Task is
     * succeeded or failed.
     *
     * @param task ManagedObjectReference representing the Task.
     * @return boolean value representing the Task result.
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public boolean getTaskResultAfterDone(ManagedObjectReference task)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InvalidCollectorVersionFaultMsg {

        boolean retVal = false;

        // info has a property - state for state of the task
        Object[] result =
                waitForValues.wait(task, new String[]{"info.state", "info.error"},
                        new String[]{"state"}, new Object[][]{new Object[]{
                        TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

        if (result[0].equals(TaskInfoState.SUCCESS)) {
            retVal = true;
        }
        if (result[1] instanceof LocalizedMethodFault) {
            throw new RuntimeException(
                    ((LocalizedMethodFault) result[1]).getLocalizedMessage());
        }
        return retVal;
    }

    @Action
    public void rename() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidNameFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg {
        Map<String, ManagedObjectReference> entities =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(),
                        "ManagedEntity");
        ManagedObjectReference memor = entities.get(entityname);
        if (memor == null) {
            System.out.println("Unable to find a Managed Entity '" + entityname
                    + "' in the Inventory");
            return;
        } else {
            ManagedObjectReference taskmor =
                    vimPort.renameTask(memor, newentityname);
            if (getTaskResultAfterDone(taskmor)) {
                System.out.println("ManagedEntity '" + entityname
                        + "' renamed successfully.");
            } else {
                System.out.println("Failure -: Managed Entity Cannot Be Renamed");
            }
        }
    }
}
