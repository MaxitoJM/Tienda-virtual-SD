<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="reportes"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Total de Ventas por Cliente - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Total de Ventas por Cliente</h2>

    <c:if test="${not empty reporte.mensaje}">
        <div class="mensaje info"><c:out value="${reporte.mensaje}"/></div>
    </c:if>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <c:if test="${not empty reporte.clientes}">
        <table>
            <tr><th>Cedula</th><th>Nombre</th><th>Valor Total Ventas</th></tr>
            <c:forEach var="fila" items="${reporte.clientes}">
                <tr>
                    <td>${fila.cedula_cliente}</td>
                    <td><c:out value="${fila.nombre_cliente}"/></td>
                    <td>${fila.valor_total_ventas}</td>
                </tr>
            </c:forEach>
        </table>
        <div class="total">Total Ventas $ <span>${reporte.total_general_ventas}</span></div>
    </c:if>

    <div class="botones">
        <a href="${pageContext.request.contextPath}/reportes"><button type="button">Volver</button></a>
    </div>
</div>
</body>
</html>
