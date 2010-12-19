<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>
<%@ taglib prefix="dmexform" uri="/WEB-INF/dmex-form.tld" %>

<c:set var="exsession" value="${sessionScope['net.gslsrc.dmex.exercise.ExerciseSession']}"/>
<c:set var="problem" value="${sessionScope['net.gslsrc.dmex.exercise.Problem']}"/>
<c:if test="${exsession == null or problem == null}">
    <c:redirect url="/"/>
</c:if>
<c:set var="eid" value="${problem.exerciseId}"/>

<html>
<head>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/stylesheets.jsp" %>

<c:url var="cssUrl" value="/style/show.css"/>
<link type="text/css" rel="stylesheet" href="${cssUrl}"></link>

<dmexform:problemStylesheets problem="${problem}"/>

</head>

<body>
<div id="leftcol">
</div>

<div id="maincol">
<h2>
    <fmt:message key="problem.title">
        <fmt:param>
            <dmex:exerciseMsg exercise="${eid}" key="exercise.title.${eid}"
                default="${eid}" locale="${pageContext.request.locale}"/>
        </fmt:param>
    </fmt:message>
</h2>

<div id="problemForm">
    <div id="problemContainer" class="${eid}">
        <dmexform:renderProblem problem="${problem}"
            locale="${pageContext.request.locale}" form="false"/>
    </div>

    <div class="buttons">
        <input type='button' value='<fmt:message key="button.next"/>'
            onclick='location.href="${pageContext.servletContext.contextPath}/problem/${eid}"'/>
        <span class="other">
            <input type='button' value='<fmt:message key="button.finish"/>'
                onclick='location.href="${pageContext.servletContext.contextPath}/finish/${eid}"'/>
        </span>
    </div>
</div>

<table class="progress">
    <tbody>
        <tr>
            <th>Submitted</th>
            <td>${exsession.submitted}</td>
        </tr>
        <tr>
            <th>Correct</th>
            <td>${exsession.correct}</td>
        </tr>
        <tr>
            <th>Incorrect</th>
            <td>${exsession.incorrect}</td>
        </tr>
        <tr>
            <th>Skipped</th>
            <td>${exsession.skipped}</td>
        </tr>
    </tbody>
</table>

<p>
<a href="${pageContext.servletContext.contextPath}/config/${eid}">
    <fmt:message key="link.new.session"/>
</a>
</p>
</div>

<%@ include file="/WEB-INF/template/footer.jspf" %>
