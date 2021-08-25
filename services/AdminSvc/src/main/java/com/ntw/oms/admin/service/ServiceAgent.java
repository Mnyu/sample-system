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

import com.ntw.common.config.AppConfig;
import com.ntw.common.config.ServiceID;
import com.ntw.common.status.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by anurag on 23/05/19.
 */
@RestController
public class ServiceAgent {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAgent.class);

    @Autowired
    private AdminServiceImpl adminServiceBean;

    @GetMapping(path = AppConfig.STATUS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getServiceStatus() {
        logger.debug("Status request received");
        String status = ServiceStatus.getServiceStatus(ServiceID.AdminSvc);
        logger.debug("Status request response is {}",status);
        return ResponseEntity.ok().body(status);
    }

    @GetMapping(path = AppConfig.ADMIN_RESOURCE_PATH + AppConfig.STATUS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSystemStatus() {
        logger.debug("System status request received");
        String status = adminServiceBean.getSystemStatus();
        logger.debug("System status request response is {}",status);
        return ResponseEntity.ok().body(status);
    }

}
