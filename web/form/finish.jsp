<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>

<c:set var="exsession" value="${requestScope['net.gslsrc.dmex.exercise.ExerciseSession']}"/>
<c:if test="${exsession == null}">
    <c:redirect url="/"/>
</c:if>

<c:set var="eid" value="${exsession.exerciseId}"/>

<html>
<head>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/stylesheets.jsp" %>

<c:url var="finishCss" value="/style/finish.css"/>
<link type="text/css" rel="stylesheet" href="${finishCss}"/>
</head>

<body>

<h2>
    <fmt:message key="problem.title">
        <fmt:param>
            <dmex:exerciseMsg exercise="${eid}" key="exercise.title.${eid}"
                default="${eid}" locale="${pageContext.request.locale}"/>
        </fmt:param>
    </fmt:message>
</h2>

<%@ include file="/WEB-INF/template/showErrors.jsp" %>

<div id="finishMessage">
    <fmt:message key="finish.text">
        <fmt:param>
            <fmt:formatNumber value="${exsession.elapsedTime / 1000}"
                pattern="##0"/>
        </fmt:param>
    </fmt:message>
</div>

<table class="progress">
    <tbody>
        <tr class="odd">
            <th><fmt:message key="progress.submitted"/></th>
            <td>${exsession.submitted}</td>
        </tr>
        <tr class="even">
            <th><fmt:message key="progress.correct"/></th>
            <td>${exsession.correct}</td>
        </tr>
        <tr class="odd">
            <th><fmt:message key="progress.incorrect"/></th>
            <td>${exsession.incorrect}</td>
        </tr>
        <tr class="even">
            <th><fmt:message key="progress.skipped"/></th>
            <td>${exsession.skipped}</td>
        </tr>
    </tbody>
</table>

<ul class="bottomLinks">
    <li>
        <a href="${pageContext.servletContext.contextPath}/config/${eid}">
            <fmt:message key="link.new.session"/>
        </a>
    </li>
    <li>
        <a href="${pageContext.servletContext.contextPath}/">
            <fmt:message key="link.another.exercise"/>
        </a>
    </li>
</ul>

<%@ include file="/WEB-INF/template/footer.jspf" %>
