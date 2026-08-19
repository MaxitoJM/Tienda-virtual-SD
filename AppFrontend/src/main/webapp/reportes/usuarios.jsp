<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="reportes"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Listado de Usuarios - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Listado de Usuarios</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <c:if test="${not empty usuarios}">
        <table>
            <tr><th>C&eacute;dula</th><th>Nombre</th><th>Email</th><th>Usuario</th><th>Password</th></tr>
            <c:forEach var="u" items="${usuarios}">
                <tr>
                    <td>${u.cedula_usuario}</td>
                    <td><c:out value="${u.nombre_usuario}"/></td>
                    <td><c:out value="${u.email_usuario}"/></td>
                    <td><c:out value="${u.usuario}"/></td>
                    <td><c:out value="${u.password}"/></td>
                </tr>
            </c:forEach>
        </table>
    </c:if>

    <div class="botones">
        <a href="${pageContext.request.contextPath}/reportes"><button type="button">Volver</button></a>
    </div>
</div>
</body>
</html>
