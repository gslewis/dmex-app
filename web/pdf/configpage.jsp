<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>

<c:set var="exsession" value="${sessionScope['net.gslsrc.dmex.exercise.ExerciseSession']}"/>
<c:if test="${exsession == null}">
    <c:redirect url="/"/>
</c:if>
<c:set var="eid" value="${exsession.exerciseId}"/>

<html>
<head>
<title><fmt:message key="dmex.title"/></title>

<%@ include file="/WEB-INF/template/scripts.jsp" %>
<%@ include file="/WEB-INF/template/stylesheets.jsp" %>
</head>

<body>

<h2>
    <fmt:message key="configpage.title">
        <fmt:param>
            <dmex:exerciseMsg exercise="${eid}" key="exercise.title.${eid}"
                default="${eid}" locale="${pageContext.request.locale}"/>
        </fmt:param>
    </fmt:message>
</h2>

<jsp:include page="/WEB-INF/template/showErrors.jsp"/>

<c:if test="${exsession.problemCount < 500}">
    <p>
        <fmt:message key="configpage.text.count">
            <fmt:param value="${exsession.problemCount}"/>
        </fmt:message>
    </p>
</c:if>

<form action='${pageContext.servletContext.contextPath}/problem/${eid}'
    method='post'>
    <label>
        <fmt:message key="configpage.problemCount.prompt"/>
    </label>
    <input type='text' name='problemCount' size='5' maxlength='3'>
    <i>
        <fmt:message key="configpage.problemCount.suffix">
            <%-- This should come from the exercise, or somewhere --%>
            <fmt:param value="100"/>
        </fmt:message>
    </i><br/>

    <%--
    <label>How many work sheets would you like generated?</label>
    <input type='text' name='sheetCount' size='5' maxlength='2'> <i>(default is 1, limit 20)</i><br/>
    <br/>
    --%>

    <input type='button' value='<fmt:message key="button.back"/>'
        onclick='location.href="${pageContext.servletContext.contextPath}/config/${eid}"'/>
    <input type='submit' value='<fmt:message key="button.next"/>'/>
</form>

<%@ include file="/WEB-INF/template/footer.jspf" %>
