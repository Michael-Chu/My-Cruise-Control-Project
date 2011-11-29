/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2007, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package net.sourceforge.cruisecontrol.dashboard.web;

import java.io.File;
import java.util.HashMap;

import net.sourceforge.cruisecontrol.dashboard.service.BuildLoopQueryService;
import net.sourceforge.cruisecontrol.dashboard.service.ConfigurationService;
import net.sourceforge.cruisecontrol.dashboard.service.DashboardXmlConfigService;
import net.sourceforge.cruisecontrol.dashboard.service.EnvironmentService;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.DataUtils;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class ConfigInterceptorTest extends MockObjectTestCase {
    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private Mock mockConfiguration;

    private ConfigurationService configurationMock;

    private ConfigInterceptor interceptor;
    private ModelAndView modelAndView;

    protected void setUp() throws Exception {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        mockConfiguration =
                mock(ConfigurationService.class, new Class[] {EnvironmentService.class,
                        DashboardXmlConfigService.class, BuildLoopQueryService.class}, new Object[] {null,
                        null, null});
        configurationMock = (ConfigurationService) mockConfiguration.proxy();
        interceptor = new ConfigInterceptor(configurationMock);
        modelAndView = new ModelAndView("model", new HashMap());
    }

    public void testShouldSetHasDashboardConfigToFalseWhenTheDashboardConfigLocationIsEmpty() throws Exception {
        expectsIsForceBuildEnabledCalled(true);
        expectsGetDashboardConfigLocationCalled(null);
        interceptor.postHandle(request, response, null, modelAndView);
        assertEquals("false", modelAndView.getModel().get(ConfigInterceptor.HAS_DASHBOARD_CONFIG_KEY));
    }

    public void testShouldSetHasDashboardConfigToFalseWhenTheDashboardConfigLocationIsInvalid() throws Exception {
        expectsIsForceBuildEnabledCalled(true);
        expectsGetDashboardConfigLocationCalled("not.exist");
        interceptor.postHandle(request, response, null, modelAndView);
        assertEquals("false", modelAndView.getModel().get(ConfigInterceptor.HAS_DASHBOARD_CONFIG_KEY));
    }

    public void testShouldSetHasDashboardConfigToTrueWhenTheCruiseLogIsNotEmpty() throws Exception {
        File tempFile = DataUtils.createTempFile("config", "file");
        expectsIsForceBuildEnabledCalled(true);
        expectsGetDashboardConfigLocationCalled(tempFile.getPath());
        interceptor.postHandle(request, response, null, modelAndView);
        assertEquals("true", modelAndView.getModel().get(ConfigInterceptor.HAS_DASHBOARD_CONFIG_KEY));
    }

    public void testShouldSetGlobalForceBuildEnabledToTrueWhenForceBuildEnabled() throws Exception {
        expectsIsForceBuildEnabledCalled(true);
        expectsGetDashboardConfigLocationCalled(null);
        interceptor.postHandle(request, response, null, modelAndView);
        assertEquals("true", modelAndView.getModel().get(ConfigInterceptor.FORCE_BUILD_ENABLED_KEY));
    }

    public void testShouldSetGlobalForceBuildEnabledToFalseWhenForceBuildDisabled() throws Exception {
        expectsIsForceBuildEnabledCalled(false);
        expectsGetDashboardConfigLocationCalled(null);
        interceptor.postHandle(request, response, null, modelAndView);
        assertEquals("false", modelAndView.getModel().get(ConfigInterceptor.FORCE_BUILD_ENABLED_KEY));
    }

    private void expectsIsForceBuildEnabledCalled(boolean returnValue) {
        mockConfiguration.expects(once()).method("isForceBuildEnabled").will(returnValue(returnValue));
    }

    private void expectsGetDashboardConfigLocationCalled(String returnValue) {
        mockConfiguration.expects(once()).method("getDashboardConfigLocation").will(returnValue(returnValue));
    }

}
