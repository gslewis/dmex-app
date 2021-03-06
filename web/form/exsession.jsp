<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>
<%@ taglib prefix="dmexform" uri="/WEB-INF/dmex-form.tld" %>

<c:set var="exsession" value="${sessionScope['net.gslsrc.dmex.exercise.ExerciseSession']}"/>
<c:set var="problem" value="${sessionScope['net.gslsrc.dmex.exercise.Problem']}"/>
<c:set var="problemAnswer" value="${requestScope['net.gslsrc.dmex.exercise.ProblemAnswer']}"/>
<c:if test="${exsession == null or problem == null}">
    <c:redirect url="/"/>
</c:if>
<c:set var="eid" value="${exsession.exerciseId}"/>
<c:set var="demonstrate" value="${requestScope['net.gslsrc.dmex.servlet.ProblemServlet.demonstrate']}"/>

<html>
<head>
<meta http-equiv="Content-Type" context="text/html;charset=UTF-8"></meta>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/scripts.jsp" %>

<c:url var="numpadJS" value="/javascript/numpad.js"/>
<script type="text/javascript" src="${numpadJS}"></script>
<c:if test="${demonstrate == true}">
    <c:url var="demonstrateJS" value="/javascript/demonstrate.js"/>
    <script type="text/javascript" src="${demonstrateJS}"></script>
</c:if>

<%@ include file="/WEB-INF/template/stylesheets.jsp" %>
<c:url var="exsessionCss" value="/style/exsession.css"/>
<link type="text/css" rel="stylesheet" href="${exsessionCss}"></link>
<c:url var="numpadCss" value="/style/numpad.css"/>
<link type="text/css" rel="stylesheet" href="${numpadCss}"></link>
<dmexform:problemStylesheets problem="${problem}"/>

</head>

<body>

<div id="worksheet">
<div id="leftcol">
<%@ include file="/WEB-INF/template/numpad.html" %>
<c:if test="${demonstrate == true}">
<input class='demoButton' type='button'
    value="<fmt:message key='button.demo'/>"
    onclick="demonstrate('${eid}')"/>
</c:if>
</div>

<div id="maincol">
<c:url var="infoUrl" value="/info/${eid}">
    <c:param name="back" value="${pageContext.request.requestURI}"/>
</c:url>
<h2>
    <fmt:message key="problem.title">
        <fmt:param>
            <dmex:exerciseMsg exercise="${eid}" key="exercise.title.${eid}"
                default="${eid}" locale="${pageContext.request.locale}"/>
        </fmt:param>
    </fmt:message>
    <span class="infoLink">
        [<a href="${infoUrl}"><fmt:message key="dmex.info"/></a>]
    </span>
</h2>

<form id="problemForm" method='post'
    action='${pageContext.servletContext.contextPath}/problem/${eid}'>

    <div id="problemContainer" class="${eid}">
        <dmexform:renderProblem problem="${problem}" answer="${problemAnswer}"
            locale="${pageContext.request.locale}"/>
    </div>

    <div class="buttons">
        <span class="leftButtons">
            <input type='submit' value='<fmt:message key="button.submit"/>'/>
        </span>
        <span class="rightButtons">
            <input type='button' value='<fmt:message key="button.skip"/>'
                onclick='location.href="${pageContext.servletContext.contextPath}/problem/${eid}"'/>
            <input type='button' value='<fmt:message key="button.show"/>'
                onclick='location.href="${pageContext.servletContext.contextPath}/show/${eid}"'/>
            <input type='button' value='<fmt:message key="button.finish"/>'
                onclick='location.href="${pageContext.servletContext.contextPath}/finish/${eid}"'/>
        </span>
    </div>
</form>

<%@ include file="/WEB-INF/template/showErrors.jsp" %>

<c:set var="attempts" value="${requestScope['net.gslsrc.dmex.exercise.Problem.attempts']}"/>
<c:if test="${attempts != null}">
<div class="problemReport">
    <fmt:message key="dmex.problem.report">
        <fmt:param value="${requestScope['net.gslsrc.dmex.exercise.Problem.number']}"/>
        <fmt:param value="${attempts}"/>
    </fmt:message>
</div>
</c:if>

<%@ include file="/WEB-INF/template/bottomPanel.jsp" %>

</div> <!-- maincol -->
<div style="clear: both;"/>
</div> <!-- worksheet -->

<%@ include file="/WEB-INF/template/footer.jspf" %>
