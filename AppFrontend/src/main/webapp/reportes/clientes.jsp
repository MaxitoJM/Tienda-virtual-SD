<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="reportes"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Listado de Clientes - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Listado de Clientes</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <c:if test="${not empty clientes}">
        <table>
            <tr><th>C&eacute;dula</th><th>Nombre</th><th>Email</th><th>Direcci&oacute;n</th><th>Tel&eacute;fono</th></tr>
            <c:forEach var="cl" items="${clientes}">
                <tr>
                    <td>${cl.cedula_cliente}</td>
                    <td><c:out value="${cl.nombre_cliente}"/></td>
                    <td><c:out value="${cl.email_cliente}"/></td>
                    <td><c:out value="${cl.direccion_cliente}"/></td>
                    <td><c:out value="${cl.telefono_cliente}"/></td>
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
