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

package com.ntw.oms.gateway.filter;

/**
 * Created by anurag on 30/05/17.
 */

public class Route {

    private String protocol;
    private String url;
    private String uri;
    private String method;
    private String authHeader;
    private String queryString;
    private String contentType;
    private String body;

    public Route() {}

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUrl() {
        return url;
    }

    public String getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "{" +
                "\"protocol\":" + (protocol == null ? "null" : "\"" + protocol + "\"") + ", " +
                "\"url\":" + (url == null ? "null" : "\"" + url + "\"") + ", " +
                "\"uri\":" + (uri == null ? "null" : "\"" + uri + "\"") + ", " +
                "\"method\":" + (method == null ? "null" : "\"" + method + "\"") + ", " +
                "\"authHeader\":" + (authHeader == null ? "null" : "\"" + authHeader + "\"") + ", " +
                "\"queryString\":" + (queryString == null ? "null" : "\"" + queryString + "\"") + ", " +
                "\"contentType\":" + (contentType == null ? "null" : "\"" + contentType + "\"") + ", " +
                "\"body\":" + (body == null ? "null" : "\"" + body + "\"") +
                "}";
    }

}
