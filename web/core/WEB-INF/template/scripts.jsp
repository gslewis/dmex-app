<%-- vim:set ft=jsp: --%>
<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="jquery" value="${requestScope['net.gslsrc.dmex.servlet.javascript.jquery']}"/>
<c:if test="${jquery == null or jquery == true}">
<script type="text/javascript" src="${pageContext.servletContext.contextPath}/javascript/@JQUERY@"></script>
</c:if>

<c:set var="scripts" value="${requestScope['net.gslsrc.dmex.servlet.resources']}"/>
<c:if test="${scripts != null and fn:length(scripts) gt 0}">
    <c:forEach var="script" begin="0" items="${scripts}">
        <c:out value="${script}" escapeXml="false"/>
    </c:forEach>
</c:if>

