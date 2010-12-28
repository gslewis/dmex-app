<%-- vim:set ft=jsp: --%>
<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div id="bottomPanel">

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
        <c:url var="restartUrl" value="/problem/${eid}">
            <c:param name="action" value="restart"/>
        </c:url>
        <a href="${restartUrl}">
            <fmt:message key="link.restart.session"/>
        </a>
    </li>
    <li>
        <a href="${pageContext.servletContext.contextPath}/config/${eid}">
            <fmt:message key="link.new.session"/>
        </a>
    </li>
</ul>

</div>
