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

<!-- Material Design -->
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.2.1/css/material.min.css" />
<script src="//cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.2.1/js/material.min.js"></script>

<!-- CSS -->
<template:addResources type="css" resources="bundle.min.css" />

<!-- Vars -->
<c:set var="type"       value="${currentNode.properties['type'].string}"        />
<c:set var="startDate"  value="${currentNode.properties['start_date'].string}"  />
<c:set var="name"       value="${currentNode.properties['name'].string}"        />
<c:set var="distance"   value="${currentNode.properties['distance'].string}"    />
<c:set var="movingTime" value="${currentNode.properties['moving_time'].string}" />

<!-- Content -->
<div class="content" style="display: none;">

    <div class="development"></div>

    <div class="panel panel-info">
        <div class="panel-heading">
            <h3 class="panel-title">Activity</h3>
        </div>
        <div class="panel-body">
            <form class="form-horizontal">
                <fieldset>
                    <legend>Details</legend>
                    <div class="form-group">
                        <label for="inputName" class="col-lg-2 control-label">Name</label>
                        <div class="col-lg-10">
                            <input type="text" class="form-control" id="inputName" value="${fn:escapeXml(name)}">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputDistance" class="col-lg-2 control-label">Distance</label>
                        <div class="col-lg-10">
                            <input type="text" class="form-control" id="inputDistance" value="${distance} kms">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputMovingTime" class="col-lg-2 control-label">Name</label>
                        <div class="col-lg-10">
                            <input type="text" class="form-control" id="inputMovingTime" value="${movingTime}">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputStartDate" class="col-lg-2 control-label">Date</label>
                        <div class="col-lg-10">
                            <input type="text" class="form-control" id="inputStartDate" value="${startDate}">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputName" class="col-lg-2 control-label">Type</label>
                        <div class="col-lg-10">
                            <div class="radio radio-primary">
                                <label>
                                    <input type="radio" name="optionsRadiosType" id="optionsRadiosRide" value="Ride">
                                    Ride
                                </label>
                            </div>
                            <div class="radio radio-primary">
                                <label>
                                    <input type="radio" name="optionsRadiosType" id="optionsRadiosRun" value="Run">
                                    Run
                                </label>
                            </div>
                            <div class="radio radio-primary">
                                <label>
                                    <input type="radio" name="optionsRadiosType" id="optionsRadiosWalk" value="Walk">
                                    Walk
                                </label>
                            </div>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>

    <p class="text-center">
        <button type="submit" class="btn btn-info" onclick="javascript:window.history.back();">Back</button>
    </p>

</div>

<div class="boxCenter">
    <div class="spinner"></div>
</div>

<script>
    $(document).ready(function() {
        // Init material design
        $.material.init();
        // Select the activity type
        var type = '<c:out value="${type}" />';
        $("#optionsRadios" + type).prop("checked", true);
        // Loading
        setTimeout(function() {
            $('.spinner').hide();
            $('.content').show();
        }, 2000);
    });
</script>
