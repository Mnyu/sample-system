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
import com.ntw.oms.admin.api.ApiClient;
import com.ntw.oms.admin.api.ApiClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag on 26/07/20.
 */
@Component
public class APIDataManager {

    private static final Logger logger = LoggerFactory.getLogger(APIDataManager.class);

    @Autowired
    private ApiClientFactory apiClientFactory;

    @Value("${GatewaySvc.client.threads.size:10}")
    private int apiThreadPoolSize;

    public boolean createTestData(int userCount, int productCount, String authHeader) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(apiThreadPoolSize);
        List<DataInsertTask> insertTasks = new LinkedList<>();
        for (ServiceID serviceID : ServiceID.values()) {
            if (serviceID == ServiceID.AdminSvc || serviceID == ServiceID.GatewaySvc)
                continue;
            ApiClient apiClient = apiClientFactory.createApiClient(serviceID, authHeader);
            int size;
            if (serviceID == ServiceID.AuthSvc || serviceID == ServiceID.UserProfileSvc) {
                size = userCount;
            } else if (serviceID == ServiceID.InventorySvc || serviceID == ServiceID.ProductSvc) {
                size = productCount;
            } else {
                size = 0;
            }
            for (int i = 1; i <= size; i++) {
                DataInsertTask task = new DataInsertTask(apiClient, i);
                insertTasks.add(task);
                executor.execute(task);
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (DataInsertTask task : insertTasks) {
            StringBuilder taskStatus = new StringBuilder();
            taskStatus.append(task.getApiClient().toString()).append(":").append(task.getIndex())
                    .append(":").append(task.isSuccess());
            System.out.println(taskStatus.toString());
        }
        return true;
    }

    public boolean createBootstrapData(String authHeader) {
        ApiClient apiClient = apiClientFactory.
                createApiClient(ServiceID.AuthSvc, authHeader);
        if (!apiClient.insertBootstrapData()) {
            logger.error("Unable to create bootstrap user auth data");
            return false;
        }
        apiClient = apiClientFactory.
                createApiClient(ServiceID.UserProfileSvc, authHeader);
        if (!apiClient.insertBootstrapData()) {
            logger.error("Unable to create bootstrap user profile data");
            return false;
        }
        logger.info("Created bootstrap data");
        return true;
    }

    public boolean deleteTestData(String authHeader) {
        boolean success = true;
        for (ServiceID serviceID : ServiceID.values()) {
            if (serviceID == ServiceID.AdminSvc || serviceID == ServiceID.GatewaySvc)
                continue;
            ApiClient apiClient = apiClientFactory.createApiClient(serviceID, authHeader);
            if (! apiClient.deleteData()) {
                success = false;
            }
        }
        return success;
    }

    public String getServiceStatus() {
        StringBuilder serviceStatusBuilder = new StringBuilder();
        for (ServiceID serviceID : ServiceID.values()) {
            ApiClient apiClient = apiClientFactory.createApiClient(serviceID, "");
            if (serviceID == ServiceID.AdminSvc || serviceID == ServiceID.CartSvc)
                continue;
            String serviceStatus = null;
            try {
                serviceStatus = apiClient.getStatus();
            } catch (Exception e) {
                logger.info("Service {} not reachable");
            }
            if (serviceStatus == null) {
                serviceStatus = serviceID.toString() + " - Not Reachable";
            }
            serviceStatusBuilder.append(serviceStatus);
            serviceStatusBuilder.append("<br/>\n");
        }
        return serviceStatusBuilder.toString();
    }

}

class DataInsertTask implements Runnable {

    private ApiClient apiClient;
    private int index;
    private boolean success;

    public DataInsertTask(ApiClient apiClient, int index) {
        this.apiClient = apiClient;
        this.index = index;
    }

    public void run() {
        success = apiClient.insertData(index);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public int getIndex() {
        return index;
    }

    public boolean isSuccess() {
        return success;
    }
}
