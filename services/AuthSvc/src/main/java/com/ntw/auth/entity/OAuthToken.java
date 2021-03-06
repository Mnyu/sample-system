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

package com.ntw.auth.entity;

/**
 * Created by anurag on 27/03/17.
 */

/**
 * OAuthToken wraps access_token in OAuth2 kind of token response
 */
public class OAuthToken {
    private String access_token;
    private String token_type;
    private Integer expires_in;

    public OAuthToken() {
    }

    public OAuthToken(String access_token) {
        this.access_token = access_token;
        this.token_type = "Bearer";
        expires_in = 36000;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public Integer getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Integer expires_in) {
        this.expires_in = expires_in;
    }

    @Override
    public String toString() {
        return "{" +
                "\"access_token\":" + (access_token == null ? "null" : "\"" + access_token + "\"") + ", " +
                "\"token_type\":" + (token_type == null ? "null" : "\"" + token_type + "\"") + ", " +
                "\"expires_in\":" + (expires_in == null ? "null" : "\"" + expires_in + "\"") +
                "}";
    }
}
