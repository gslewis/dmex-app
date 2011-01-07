<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>

<jsp:useBean id="exlist" class="net.gslsrc.dmex.servlet.ExerciseList"
    scope="application"/>

<html>
<head>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/stylesheets.jsp" %>
<c:url var="indexCss" value="/style/index.css"/>
<link type="text/css" rel="stylesheet" href="${indexCss}"/>
</head>

<body>

<h1><fmt:message key="dmex.title"/></h1>

<jsp:include page="/WEB-INF/template/showErrors.jsp"/>

<ol>
<c:forEach var="ex" begin="0" items="${exlist.exercises}">
<li>
    <c:set var="exTitle">
        <dmex:exerciseMsg exercise="${ex.id}"
            key="exercise.title.${ex.id}" default="${ex.id}"
            locale="${pageContext.request.locale}"/>
    </c:set>

    <c:url var="url" value="config/${ex.id}"/>
    <c:url var="infoUrl" value="info/${ex.id}"/>

    <a href="${url}">${exTitle}</a>&nbsp;[<a href="${infoUrl}"><fmt:message key="dmex.info"/></a>]
</li>
</c:forEach>
</ol>

<c:set var="introUrl">
    <dmex:info path="intro" locale="${pageContext.request.locale}"/>
</c:set>
<c:set var="newsUrl">
    <dmex:info path="news" locale="${pageContext.request.locale}"/>
</c:set>
<c:if test="${not empty introUrl or not empty newsUrl}">
    <div id="text-content">
    <c:if test="${not empty introUrl}">
        <c:import var="intro" url="${introUrl}"/>
        <c:if test="${not empty intro}">
            <div id="intro">
            ${intro}
            </div>
        </c:if>
    </c:if>

    <c:if test="${not empty newsUrl}">
        <c:import var="news" url="${newsUrl}"/>
        <c:if test="${not empty news}">
            <div id="news">
            ${news}
            </div>
        </c:if>
    </c:if>
    <div style="clear:both"></div>
    </div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jspf" %>
