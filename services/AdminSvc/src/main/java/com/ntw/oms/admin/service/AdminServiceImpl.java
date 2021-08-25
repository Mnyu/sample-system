//////////////////////////////////////////////////////////////////////////////
// Copyright 2020 Anurag Yadav (anurag.yadav@newtechways.com)               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//////////////////////////////////////////////////////////////////////////////

package com.ntw.oms.admin.service;

import com.ntw.common.config.ServiceID;
import com.ntw.common.status.ServiceStatus;
import com.ntw.oms.admin.db.DBAdminMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by anurag on 23/05/19.
 */
@Component
public class AdminServiceImpl {

    @Autowired
    private DBAdminMgr adminDBMgr;

    @Autowired
    private APIDataManager apiDataManager;

    public boolean createTestData(int userCount, int productCount, String authHeader) {
        return apiDataManager.createTestData(userCount, productCount, authHeader);
    }

    public boolean deleteTestData(String authHeader) {
        boolean success = true;
        if (!apiDataManager.deleteTestData(authHeader)) {
            success = false;
        }
        if (!apiDataManager.createBootstrapData(authHeader)) {
            success = false;
        }
        return success;
    }

    public String getDBStatus() {
        StringBuilder statusBuilder = new StringBuilder();
        String cqlStatus = adminDBMgr.getDBStatus("CQL");
        statusBuilder.append(cqlStatus).append("<br/>\n");
        String sqlStatus = adminDBMgr.getDBStatus("SQL");
        statusBuilder.append(sqlStatus).append("<br/>\n");
        return statusBuilder.toString();
    }

    public String getSystemStatus() {
        String serviceStatus = apiDataManager.getServiceStatus();
        StringBuilder statusBuilder = new StringBuilder(serviceStatus);
        String dbStatus = getDBStatus();
        statusBuilder.append(dbStatus);
        return statusBuilder.toString();
    }

}
