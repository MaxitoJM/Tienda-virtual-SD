<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="clientes"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Clientes - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Gesti&oacute;n de Clientes</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <form method="post" action="${pageContext.request.contextPath}/clientes">
        <div class="campos">
            <label for="cedula_cliente">C&eacute;dula</label>
            <input type="text" id="cedula_cliente" name="cedula_cliente" value="${datos.cedula_cliente}">

            <label for="telefono_cliente">Tel&eacute;fono</label>
            <input type="text" id="telefono_cliente" name="telefono_cliente" value="${datos.telefono_cliente}">

            <label for="nombre_cliente">Nombre Completo</label>
            <input type="text" id="nombre_cliente" name="nombre_cliente" value="${datos.nombre_cliente}">

            <label for="email_cliente">Correo Electr&oacute;nico</label>
            <input type="text" id="email_cliente" name="email_cliente" value="${datos.email_cliente}">

            <label for="direccion_cliente">Direcci&oacute;n</label>
            <input type="text" id="direccion_cliente" name="direccion_cliente" value="${datos.direccion_cliente}">
            <span></span><span></span>
        </div>
        <div class="botones">
            <button type="submit" name="accion" value="consultar">Consultar</button>
            <button type="submit" name="accion" value="crear">Crear</button>
            <button type="submit" name="accion" value="actualizar">Actualizar</button>
            <button type="submit" name="accion" value="borrar">Borrar</button>
        </div>
    </form>
</div>
</body>
</html>
