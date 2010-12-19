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

<table id="progress">
    <thead>
        <tr>
            <th>Submitted</th>
            <th>Correct</th>
            <th>Incorrect</th>
            <th>Skipped</th>
            <th>Time</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>${exsession.submitted}</td>
            <td>${exsession.correct}</td>
            <td>${exsession.incorrect}</td>
            <td>${exsession.skipped}</td>
            <td>
                <fmt:formatNumber value="${exsession.elapsedTime / 1000}"
                    pattern="##0"/>&nbsp;secs
            </td>
        </tr>
    </tbody>
</table>

<p>
    <a href="${pageContext.servletContext.contextPath}/config/${eid}">
        <fmt:message key="link.new.session"/>
    </a>
    <br/>
    <a href="${pageContext.servletContext.contextPath}/">
        <fmt:message key="link.another.exercise"/>
    </a>
</p>

<%@ include file="/WEB-INF/template/footer.jspf" %>
