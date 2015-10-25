<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<!-- CSS -->
<template:addResources type="css" resources="bundle.min.css" />

<!-- JS -->
<template:addResources type="javascript" resources="nav.min.js" />

<!-- Navigation bar -->
<a class="mobile" href="#">&#9776;</a>
<nav>


    <div class="strava-logo">Strava</div>

    <ul>
        <li>
            <a href="#">Tableau de bord</a>
            <ul>
                <li><a href="#">Flux d'actualités</a></li>
                <li><a href="#">Mes segments</a></li>
                <li><a href="#">Mes itinéraires</a></li>
                <li><a href="#"></a></li>
            </ul>
        </li>
        <li>
            <a href="#">Entraînement</a>
            <ul>
                <li><a href="#">Journal</a></li>
                <li><a href="#">Calendrier</a></li>
                <li><a href="#">Mes activités</a></li>
            </ul>
        </li>
        <li>
            <a href="#">Explorer</a>
            <ul>
                <li><a href="#">Explorateur de segments</a></li>
                <li><a href="#">Recherche de segments</a></li>
                <li><a href="#">Recherche d'athlètes</a></li>
                <li><a href="#">Clubs</a></li>
                <li><a href="#">Courses à pieds</a></li>
            </ul>
        </li>
        <li>
            <a href="#">Challenges</a>
        </li>
        <li>
            <a href="#">Boutiques</a>
        </li>
    </ul>
    <div style="clear:both;"></div>
</nav>

<!-- SQL-2 query -->
<jcr:sql var="res" sql="select * from [jnt:stravaActivity]"/>

<table id="activitiesTable" class="table table-striped table-bordered">
    <thead>
        <th class="strava-align">Type</th>
        <th class="strava-align">Date</th>
        <th>Activity name</th>
        <th class="strava-align">Distance</th>
        <th class="strava-align">Time</th>
    </thead>
    <tbody>
        <c:forEach items="${res.nodes}" var="stravaActivity" varStatus="status">
            <tr>
                <td class="strava-align">${stravaActivity.properties['type'].string}</td>
                <td class="strava-align">${stravaActivity.properties['start_date'].string}</td>
                <td>
                    <a href="<c:url value="${url.base}${stravaActivity.path}.html"/>">
                        ${stravaActivity.properties['name'].string}
                    </a>
                </td>
                <td class="strava-align">${stravaActivity.properties['distance'].string}</td>
                <td class="strava-align">${stravaActivity.properties['moving_time'].string}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>

