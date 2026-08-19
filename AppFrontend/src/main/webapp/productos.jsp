<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="productos"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Productos - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Carga de Productos</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <form method="post" action="${pageContext.request.contextPath}/productos"
          enctype="multipart/form-data">
        <div class="campos-simple" style="grid-template-columns:170px 1fr">
            <label for="archivo">Nombre del Archivo</label>
            <input type="file" id="archivo" name="archivo" accept=".csv">
        </div>
        <div class="botones">
            <button type="submit">Cargar</button>
        </div>
    </form>

    <c:if test="${not empty productos}">
        <h2 style="margin-top:34px">Productos cargados</h2>
        <table>
            <tr>
                <th>C&oacute;digo</th><th>Nombre</th><th>NIT Proveedor</th>
                <th>Precio Compra</th><th>IVA %</th><th>Precio Venta</th>
            </tr>
            <c:forEach var="p" items="${productos}">
                <tr>
                    <td>${p.codigo_producto}</td>
                    <td><c:out value="${p.nombre_producto}"/></td>
                    <td>${p.nitproveedor}</td>
                    <td>${p.precio_compra}</td>
                    <td>${p.ivacompra}</td>
                    <td>${p.precio_venta}</td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</div>
</body>
</html>
