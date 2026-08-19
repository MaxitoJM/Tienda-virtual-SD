<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="usuarios"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Usuarios - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Gesti&oacute;n de Usuarios</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <form method="post" action="${pageContext.request.contextPath}/usuarios">
        <div class="campos">
            <label for="cedula_usuario">C&eacute;dula</label>
            <input type="text" id="cedula_usuario" name="cedula_usuario" value="${datos.cedula_usuario}">

            <label for="usuario_login">Usuario</label>
            <input type="text" id="usuario_login" name="usuario" value="${datos.usuario}">

            <label for="nombre_usuario">Nombre Completo</label>
            <input type="text" id="nombre_usuario" name="nombre_usuario" value="${datos.nombre_usuario}">

            <label for="password">Contrase&ntilde;a</label>
            <input type="password" id="password" name="password">

            <label for="email_usuario">Correo Electr&oacute;nico</label>
            <input type="text" id="email_usuario" name="email_usuario" value="${datos.email_usuario}">
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
