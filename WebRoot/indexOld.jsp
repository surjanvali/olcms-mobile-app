<html>
<head>
<style type="text/css">
BODY {
	color: #000000;
	background-color: white;
	font-family: Verdana;
	margin-left: 0px;
	margin-top: 0px;
}

#content {
	margin-left: 30px;
	font-size: .70em;
	padding-bottom: 2em;
}

A:link {
	color: #336699;
	font-weight: bold;
	text-decoration: underline;
}

A:visited {
	color: #6699cc;
	font-weight: bold;
	text-decoration: underline;
}

A:active {
	color: #336699;
	font-weight: bold;
	text-decoration: underline;
}

A:hover {
	color: cc3300;
	font-weight: bold;
	text-decoration: underline;
}

P {
	color: #000000;
	margin-top: 0px;
	margin-bottom: 12px;
	font-family: Verdana;
}

pre {
	background-color: #e5e5cc;
	padding: 5px;
	font-family: Courier New;
	font-size: x-small;
	margin-top: -5px;
	border: 1px #f0f0e0 solid;
}

td {
	color: #000000;
	font-family: Verdana;
	font-size: .7em;
}

h2 {
	font-size: 1.5em;
	font-weight: bold;
	margin-top: 25px;
	margin-bottom: 10px;
	border-top: 1px solid #003366;
	margin-left: -15px;
	color: #003366;
}

h3 {
	font-size: 1.1em;
	color: #000000;
	margin-left: -15px;
	margin-top: 10px;
	margin-bottom: 10px;
}

ul {
	margin-top: 10px;
	margin-left: 20px;
}

ol {
	margin-top: 10px;
	margin-left: 20px;
}

li {
	margin-top: 10px;
	color: #000000;
}

font.value {
	color: darkblue;
	font: bold;
}

font.key {
	color: darkgreen;
	font: bold;
}

font.error {
	color: darkred;
	font: bold;
}

.heading1 {
	color: #ffffff;
	font-family: Tahoma;
	font-size: 26px;
	font-weight: normal;
	background-color: #003366;
	margin-top: 0px;
	margin-bottom: 0px;
	margin-left: -30px;
	padding-top: 10px;
	padding-bottom: 3px;
	padding-left: 15px;
	width: 105%;
}

.button {
	background-color: #dcdcdc;
	font-family: Verdana;
	font-size: 1em;
	border-top: #cccccc 1px solid;
	border-bottom: #666666 1px solid;
	border-left: #cccccc 1px solid;
	border-right: #666666 1px solid;
}

.frmheader {
	color: #000000;
	background: #dcdcdc;
	font-family: Verdana;
	font-size: .7em;
	font-weight: normal;
	border-bottom: 1px solid #dcdcdc;
	padding-top: 2px;
	padding-bottom: 2px;
}

.frmtext {
	font-family: Verdana;
	font-size: .7em;
	margin-top: 8px;
	margin-bottom: 0px;
	margin-left: 32px;
}

.frmInput {
	font-family: Verdana;
	font-size: 1em;
}

.intro {
	margin-left: -15px;
}
</style>
<title>AP Finance Data Share Web Service</title>
</head>
<body>
	<div id="content">
		<p class="heading1">AP-FDP Web Service to get GO Number</p>
		<br> Construct a specific URL to access the service <span>
		</span>
	</div>


	<input type="button" name="checkService" id="checkService"
		value="checkService" />



	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js"></script>
	<script>
		$(document)
				.ready(
						function() {
							$("#checkService")
									.click(
											function(e) {
												alert("calling function");
												// var url = "http://localhost:8080/aprcrp-services/services/AppsListService/showApps";
												var url = "https://aprcrp.apcfss.in/aprcrp-services/services/AppsListService/showApps";
												var xhr = new XMLHttpRequest();
												xhr.open("POST", url);

												xhr.setRequestHeader(
														"Content-Type",
														"application/json");

												xhr.onreadystatechange = function() {
													if (xhr.readyState === 4) {
														console.log(xhr.status);
														console
																.log(xhr.responseText);
													}
												};

												var data = '{"REQUEST" : {"DIST":"12","CIRCLE":"","FROMDATE":"02-02-2019","TODATE":"31-12-2019"}}';

												xhr.send(data);
											});
						});
	</script>
</body>
</html>
