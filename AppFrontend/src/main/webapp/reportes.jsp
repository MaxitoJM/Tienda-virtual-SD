<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="reportes"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Reportes - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Consultas y Reportes</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <div class="botones" style="margin-top:40px">
        <a href="${pageContext.request.contextPath}/reportes?tipo=usuarios"><button type="button">Listado de Usuarios</button></a>
        <a href="${pageContext.request.contextPath}/reportes?tipo=clientes"><button type="button">Listado de Clientes</button></a>
        <a href="${pageContext.request.contextPath}/reportes?tipo=ventasporcliente"><button type="button">Total de Ventas por Cliente</button></a>
    </div>
</div>
</body>
</html>
