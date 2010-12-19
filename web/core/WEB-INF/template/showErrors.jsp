<%-- vim:set ft=jsp: --%>
<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="errors" value="${requestScope['net.gslsrc.dmex.servlet.errors']}"/>
<c:if test="${fn:length(errors) gt 0}">
<div class='errors'>
    <ul>
        <c:forEach var="error" begin="0" items="${errors}">
        <li>${error}</li>
        </c:forEach>
    </ul>
</div>
</c:if>

