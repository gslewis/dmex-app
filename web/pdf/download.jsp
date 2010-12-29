<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dmex" uri="/WEB-INF/dmex.tld" %>

<c:set var="exsession"
    value="${sessionScope['net.gslsrc.dmex.exercise.ExerciseSession']}"/>
<c:set var="outputTypes"
    value="${requestScope['net.gslsrc.dmex.render.xsl.ProblemTemplates.OutputType']}"/>

<c:if test="${exsession == null or outputTypes == null}">
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

<c:url var="infoUrl" value="/info/${eid}">
    <c:param name="back" value="/download/${eid}"/>
</c:url>
<h2>
    <fmt:message key="download.title">
        <fmt:param>
            <dmex:exerciseMsg exercise="${eid}" key="exercise.title.${eid}"
                default="${eid}" locale="${pageContext.request.locale}"/>
        </fmt:param>
    </fmt:message>
    <span class="infoLink">
        [<a href="${infoUrl}"><fmt:message key="dmex.info"/><a>]
    </span>
</h2>


<ul>
<c:forEach var="outputType" begin="0" items="${outputTypes}">
    <c:url var="url" value="/download/${eid}">
        <c:param name="type" value="${fn:toLowerCase(outputType)}"/>
    </c:url>
    <li><a href="${url}"><fmt:message key="download.link.${outputType}"/></a>
</c:forEach>
</ul>

<form>
    <input type="button" value='<fmt:message key="button.back"/>'
    onclick='location.href="${pageContext.servletContext.contextPath}/problem/${eid}"'/>
    <input type="button" value='<fmt:message key="button.finish"/>'
        onclick='location.href="${pageContext.servletContext.contextPath}/"'/>
</form>

<%@ include file="/WEB-INF/template/footer.jspf" %>
