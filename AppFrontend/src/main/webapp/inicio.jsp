<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Tienda Gen&eacute;rica Virtual</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<div class="login">
    <h1>Bienvenidos a la Tienda Gen&eacute;rica</h1>

    <c:if test="${not empty error}">
        <div class="mensaje error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="campos-simple">
            <label for="usuario">Usuario</label>
            <input type="text" id="usuario" name="usuario" autofocus>

            <label for="password">Contrase&ntilde;a</label>
            <input type="password" id="password" name="password">
        </div>
        <div class="botones">
            <button type="submit">Aceptar</button>
            <button type="reset">Cancelar</button>
        </div>
    </form>
</div>
</body>
</html>
