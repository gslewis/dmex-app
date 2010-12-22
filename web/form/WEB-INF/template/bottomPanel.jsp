<%-- vim:set ft=jsp: --%>
<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div id="bottomPanel">

<table class="progress">
    <tbody>
        <tr class="odd">
            <th>Submitted</th>
            <td>${exsession.submitted}</td>
        </tr>
        <tr class="even">
            <th>Correct</th>
            <td>${exsession.correct}</td>
        </tr>
        <tr class="odd">
            <th>Incorrect</th>
            <td>${exsession.incorrect}</td>
        </tr>
        <tr class="even">
            <th>Skipped</th>
            <td>${exsession.skipped}</td>
        </tr>
    </tbody>
</table>

<div class="bottomLinks">
    <a href="${pageContext.servletContext.contextPath}/config/${eid}">
        <fmt:message key="link.new.session"/>
    </a>
</div>

</div>
