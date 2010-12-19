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

<%@ include file="/WEB-INF/template/footer.jspf" %>
