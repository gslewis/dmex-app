<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>

<c:set var="infoUrl">
    <dmex:info path="${pageContext.request.pathInfo}" locale="${pageContext.request.locale}"/>
</c:set>
<c:if test="${empty infoUrl}">
    <c:redirect url="/"/>
</c:if>

<html>
<head>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/stylesheets.jsp" %>
</head>

<body>

<c:import var="info" url="${infoUrl}"/>
${info}

<c:choose>
    <c:when test="${param['back'] != null}">
        <c:url var="backUrl" value="${param['back']}"/>
    </c:when>
    <c:otherwise>
        <c:url var="backUrl" value="/"/>
    </c:otherwise>
</c:choose>
<p><a href="${backUrl}"><fmt:message key="button.back"/></a></p>

<%@ include file="/WEB-INF/template/footer.jspf" %>
