<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SwiftRiver - Semantic Tagging Service</title>
</head>
<body>
	<form method="POST" action="entities.json">
		<textarea name="text" cols="80" rows="30"></textarea>
		<input type="submit" name="submit" value="Tag" />
	</form>
</body>
</html>