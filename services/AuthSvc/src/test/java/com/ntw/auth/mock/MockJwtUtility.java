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

package com.ntw.auth.mock;

import com.ntw.auth.config.TestConfig;
import com.ntw.common.entity.UserAuth;
import com.ntw.common.security.JwtUtility;

/**
 * Created by anurag on 16/05/17.
 */
public class MockJwtUtility implements JwtUtility {

    @Override
    public UserAuth parseToken(String token) {
        if (token.equals(TestConfig.DUMMY_TOKEN)) {
            return TestConfig.Test_Admin_Auth;
        }
        return null;
    }

    @Override
    public String generateToken(UserAuth userAuth) {
        return TestConfig.DUMMY_TOKEN;
    }

}
