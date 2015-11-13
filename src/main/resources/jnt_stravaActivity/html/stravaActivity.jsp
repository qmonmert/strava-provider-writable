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

<!-- Vars -->
<c:set var="type"       value="${currentNode.properties['type'].string}"        />
<c:set var="startDate"  value="${currentNode.properties['start_date'].string}"  />
<c:set var="name"       value="${currentNode.properties['name'].string}"        />
<c:set var="distance"   value="${currentNode.properties['distance'].string}"    />
<c:set var="movingTime" value="${currentNode.properties['moving_time'].string}" />

<!-- Content -->
<div class="development"></div>
<div class="panel panel-info">
    <div class="panel-heading">Activity : ${name}</div>
    <div class="panel-body">
        <form class="form-horizontal">
            <div class="form-group">
                <label for="activityName" class="col-sm-2 control-label">Name</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="activityName" value="${name}" disabled>
                </div>
            </div>
            <div class="form-group">
                <label for="activityType" class="col-sm-2 control-label">Type</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="activityType" value="${type}" disabled>
                </div>
            </div>
            <div class="form-group">
                <label for="activityDistance" class="col-sm-2 control-label">Distance</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="activityDistance" value="${distance}" disabled>
                </div>
            </div>
            <div class="form-group">
                <label for="activityMovingTime" class="col-sm-2 control-label">Time</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="activityMovingTime" value="${movingTime}" disabled>
                </div>
            </div>
            <div class="form-group">
                <label for="activityStartDate" class="col-sm-2 control-label">Date</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" id="activityStartDate" value="${startDate}" disabled>
                </div>
            </div>
        </form>
    </div>
</div>

<p class="text-center">
    <a class="btn btn-warning btn-sm" href="#" role="button" onclick="javascript:window.history.back();">
        Back
    </a>
</p>