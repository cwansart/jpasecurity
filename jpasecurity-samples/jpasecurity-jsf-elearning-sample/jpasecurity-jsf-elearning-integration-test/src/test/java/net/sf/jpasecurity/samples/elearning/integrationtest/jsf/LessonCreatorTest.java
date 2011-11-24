/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.samples.elearning.integrationtest.jsf;

import net.sf.jpasecurity.samples.elearning.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import net.sf.jpasecurity.samples.elearning.integrationtest.junit.Parameters;

import org.jaxen.JaxenException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @auhtor Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters("http://localhost:8282/elearning-jsf/")
public class LessonCreatorTest extends AbstractHtmlTestCase {

    public LessonCreatorTest(String url) {
        super(url);
    }


    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.GUEST);
    }

    @Ignore
    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.GUEST);
        ElearningAssert.assertLessonCreatorPage(authenticateAsTeacher("lessonCreator.xhtml"), Role.TEACHER);
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.TEACHER);
    }

    @Ignore
    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.GUEST);
        ElearningAssert.assertLessonCreatorPage(authenticateAsStudent("lessonCreator.xhtml"), Role.STUDENT);
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.TEACHER);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    //TODO parameterize data
    public void createLessonTest() throws JaxenException {
        HtmlPage createLessonLink = testInputLink(getHtmlPage("lessonCreator.xhtml"), "create new lesson");
        ElearningAssert.assertLoginPage(createLessonLink, Role.GUEST);   
    }
    
    @Ignore
    @Test
    public void cancelTest() throws JaxenException {
        HtmlPage cancelLink = testLink(authenticateAsTeacher("lessonCreator.xhtml?course=3"), "cancel");
        ElearningAssert.assertCoursePage(cancelLink, Role.TEACHER); 
    }


    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("lessonCreator.xhtml", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Ignore
    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("lessonCreator.xhtml"), "Logout");
        ElearningAssert.assertLessonCreatorPage(logoutLink, Role.GUEST);         
    }


    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("lessonCreator.xhtml", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);        
    }


    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("lessonCreator.xhtml"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT); 
    }
}