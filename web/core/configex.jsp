<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>

<c:set var="settings" value="${sessionScope['net.gslsrc.dmex.settings.Settings']}"/>
<c:if test="${settings == null}">
    <c:redirect url="/"/>
</c:if>
<c:set var="eid" value="${settings.exerciseId}"/>

<html>
<head>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/scripts.jsp" %>
<%@ include file="/WEB-INF/template/stylesheets.jsp" %>
</head>

<body>

<h2>
    <fmt:message key="configex.title">
        <fmt:param>
            <dmex:exerciseMsg exercise="${eid}" key="exercise.title.${eid}"
                default="${eid}" locale="${pageContext.request.locale}"/>
        </fmt:param>
    </fmt:message>
</h2>

<%@ include file="/WEB-INF/template/showErrors.jsp" %>

<form action='${pageContext.servletContext.contextPath}/config/${eid}' method='post'>
    <c:forEach var="setting" begin="0" items="${settings.settings}">
        <dmex:renderSetting setting="${setting}" exercise="${eid}"
            locale="${pageContext.request.locale}"/>
    </c:forEach>

    <div class="buttons">
        <input type='button' value='<fmt:message key="button.back"/>'
            onclick='location.href="${pageContext.servletContext.contextPath}/"'/>
        <input type='submit' value='<fmt:message key="button.next"/>'/>
    </div>
</form>

<%@ include file="/WEB-INF/template/footer.jspf" %>
