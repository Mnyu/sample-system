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

package com.ntw.common.security;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ntw.common.entity.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Created by anurag on 24/03/17.
 */

@Component
public class AuthenticationFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null) {
            logger.info("No Authorization header present in the request; requestContext={}", httpRequest.toString());
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().println("User does not have an authentication token");
            return;
        }

        logger.debug("Auth header : "+authHeader);

        UserAuth userAuth = getUserCred(authHeader);
        if (userAuth == null) {
            logger.info("Null access token");
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().println("User does not have a valid authentication token");
            return ;
        }

        AppAuthentication appAuthentication = new AppAuthentication(userAuth,
                httpRequest.isSecure(), authHeader);
        appAuthentication.setAuthenticated(true);
        AppSecurityContext appSecurityContext = new AppSecurityContext(appAuthentication);
        httpRequest.setAttribute("AppSecurityContext",appSecurityContext);
        SecurityContextHolder.setContext(appSecurityContext);
        logger.info("User authenticated; context={}", userAuth);

        chain.doFilter(request, response);

        return;
    }

    @Override
    public void destroy() {}

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    private UserAuth getUserCred(String authHeader) {
        if(authHeader==null || !authHeader.startsWith("Bearer")) {
            logger.error("Authorization not provided; authHeader={}", authHeader);
            return null;
        }

        String authToken = authHeader.substring("Bearer".length()).trim();

        JwtUtility jwtUtility = new JJwtUtility();
        UserAuth userAuth = jwtUtility.parseToken(authToken);

        return userAuth;
    }

}

