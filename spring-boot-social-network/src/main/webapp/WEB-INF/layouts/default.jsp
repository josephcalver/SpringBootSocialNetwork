<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<!-- Required meta tags -->
<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta name="_csrf" content="${_csrf.token}" />
<meta name="_csrf_header" content="${_csrf.headerName}" />

<!-- Bootstrap CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css"
	integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ"
	crossorigin="anonymous">

<!-- jQuery first, then Tether, then Bootstrap JS. -->
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>

<link href="${contextRoot}/css/main.css" rel="stylesheet">

<!-- For javascript tagging on profile.jsp -->
<link href="${contextRoot}/css/jquery.tagit.css" rel="stylesheet">
<link href="${contextRoot}/css/tagit.ui-zendesk.css" rel="stylesheet">
<script src="${contextRoot}/js/jquery-ui.min.js"></script>
<script src="${contextRoot}/js/tag-it.min.js"></script>

<title><tiles:insertAttribute name="title" /></title>

<c:set var="contextRoot" value="${pageContext.request.contextPath}" />

</head>
<body>

	<nav class="navbar navbar-toggleable-md navbar-light bg-faded">
	<button class="navbar-toggler navbar-toggler-right" type="button"
		data-toggle="collapse" data-target="#navbarNav"
		aria-controls="navbarNav" aria-expanded="false"
		aria-label="Toggle navigation">
		<span class="navbar-toggler-icon"></span>
	</button>
	<a class="navbar-brand" href="${contextRoot}/">Spring Boot Social
		Network</a>
	<div class="collapse navbar-collapse" id="navbarNav">
		<ul class="navbar-nav">

			<li class="nav-item active"><a class="nav-link"
				href="${contextRoot}/">Home <span class="sr-only">(current)</span></a>
			</li>
			<li class="nav-item"><a class="nav-link"
				href="${contextRoot}/about">About</a></li>
			<sec:authorize access="hasRole('ROLE_ADMIN')">
				<li class="nav-item"><a class="nav-link"
					href="${contextRoot}/addstatus">Add Status</a></li>
				<li class="nav-item"><a class="nav-link"
					href="${contextRoot}/viewstatus">View Statuses</a></li>
			</sec:authorize>
			<sec:authorize access="isAuthenticated()">
				<li class="nav-item"><a class="nav-link"
					href="${contextRoot}/profile">My Profile</a></li>
				<li class="nav-item"><a class="nav-link"
					href="javascript:$('#logoutForm').submit()">Log Out</a></li>
			</sec:authorize>
			<sec:authorize access="!isAuthenticated()">
				<li class="nav-item"><a class="nav-link"
					href="${contextRoot}/register">Register</a></li>
				<li class="nav-item"><a class="nav-link"
					href="${contextRoot}/login">Log In</a></li>
			</sec:authorize>
		</ul>
	</div>
	</nav>

	<c:url var="logoutLink" value="/logout" />
	<form action="${logoutLink}" method="post" id="logoutForm">
		<input type="hidden" name="${_csrf.parameterName}"
			value="${_csrf.token}" />
	</form>

	<div class="container">

		<div id="content">
			<tiles:insertAttribute name="content" />
		</div>
	</div>


	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.0/js/tether.min.js"
		integrity="sha384-DztdAPBWPRXSA/3eYEEUWrWCy7G5KFbe8fFjk5JAIxUYHKkDx6Qin1DkWx51bBrb"
		crossorigin="anonymous"></script>
	<script
		src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/js/bootstrap.min.js"
		integrity="sha384-vBWWzlZJ8ea9aCX4pEW3rVHjgjt7zpkNpZk+02D9phzyeVkE+jo0ieGizqPLForn"
		crossorigin="anonymous"></script>
</body>
</html>