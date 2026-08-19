<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="proveedores"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Proveedores - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Gesti&oacute;n de Proveedores</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <form method="post" action="${pageContext.request.contextPath}/proveedores">
        <div class="campos">
            <label for="nitproveedor">NIT</label>
            <input type="text" id="nitproveedor" name="nitproveedor" value="${datos.nitproveedor}">

            <label for="telefono_proveedor">Tel&eacute;fono</label>
            <input type="text" id="telefono_proveedor" name="telefono_proveedor" value="${datos.telefono_proveedor}">

            <label for="nombre_proveedor">Nombre Proveedor</label>
            <input type="text" id="nombre_proveedor" name="nombre_proveedor" value="${datos.nombre_proveedor}">

            <label for="ciudad_proveedor">Ciudad</label>
            <input type="text" id="ciudad_proveedor" name="ciudad_proveedor" value="${datos.ciudad_proveedor}">

            <label for="direccion_proveedor">Direcci&oacute;n</label>
            <input type="text" id="direccion_proveedor" name="direccion_proveedor" value="${datos.direccion_proveedor}">
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
