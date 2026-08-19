<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Tienda Gen&eacute;rica Virtual</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h1>Bienvenido(a), ${sessionScope.nombreUsuario}</h1>
    <p style="text-align:center;color:#777">
        Seleccione un m&oacute;dulo del men&uacute; superior para comenzar a trabajar.
    </p>
</div>
</body>
</html>
