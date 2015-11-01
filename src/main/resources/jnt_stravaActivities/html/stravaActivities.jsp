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

    <div class="development"></div>

    <div class="strava-logo">Strava</div>

    <ul>
        <li>
            <a target="_blank" href="https://www.strava.com/dashboard">Tableau de bord</a>
            <ul>
                <li><a target="_blank" href="https://www.strava.com/dashboard">Flux d'actualités</a></li>
                <li><a target="_blank" href="https://www.strava.com/athlete/segments/starred">Mes segments</a></li>
                <li><a target="_blank" href="https://www.strava.com/athlete/routes">Mes itinéraires</a></li>
            </ul>
        </li>
        <li>
            <a href="https://www.strava.com/athlete/training/log">Entraînement</a>
            <ul>
                <li><a target="_blank" href="https://www.strava.com/athlete/training/log">Journal</a></li>
                <li><a target="_blank" href="https://www.strava.com/athlete/calendar">Calendrier</a></li>
                <li><a target="_blank" href="https://www.strava.com/athlete/training">Mes activités</a></li>
            </ul>
        </li>
        <li>
            <a target="_blank" href="https://www.strava.com/segments/explore">Explorer</a>
            <ul>
                <li><a target="_blank" href="https://www.strava.com/segments/explore">Explorateur de segments</a></li>
                <li><a target="_blank" href="https://www.strava.com/segments/search">Recherche de segments</a></li>
                <li><a target="_blank" href="https://www.strava.com/athletes/search">Recherche d'athlètes</a></li>
                <li><a target="_blank" href="https://www.strava.com/clubs/search">Clubs</a></li>
                <li><a target="_blank" href="https://www.strava.com/featured-running-races">Courses à pieds</a></li>
            </ul>
        </li>
        <li>
            <a target="_blank" href="https://www.strava.com/challenges">Challenges</a>
        </li>
        <li>
            <a target="_blank" href="https://www.strava.com/shop?utm_campaign=evergreen&utm_medium=web&utm_source=top-nav">Boutiques</a>
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

