<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="modulo" value="ventas"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ventas - Tienda Gen&eacute;rica</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<div class="contenido">
    <h2>Registro de Ventas</h2>
    <%@ include file="/WEB-INF/jspf/mensajes.jspf" %>

    <form method="post" action="${pageContext.request.contextPath}/ventas">
        <table style="border:none">
            <tr>
                <td style="border:none;width:80px">C&eacute;dula</td>
                <td style="border:none;width:150px">
                    <input type="text" name="cedula_cliente" value="${param.cedula_cliente}">
                </td>
                <td style="border:none;width:110px">
                    <button class="pequeno" type="submit" name="accion" value="consultarCliente">Consultar</button>
                </td>
                <td style="border:none;width:70px">Cliente</td>
                <td style="border:none"><input type="text" value="${nombre_cliente}" readonly></td>
                <td style="border:none;width:80px">Consec.</td>
                <td style="border:none;width:110px"><input type="text" value="${codigo_venta}" readonly></td>
            </tr>
        </table>

        <table style="margin-top:18px">
            <tr>
                <th>Cod. Producto</th><th></th><th>Nombre Producto</th><th>Cant.</th><th>Vlr. Total</th>
            </tr>
            <c:forEach var="i" begin="1" end="3">
                <tr>
                    <td style="width:150px">
                        <input type="text" name="codigo_producto${i}" value="${paramValues['codigo_producto'.concat(i)][0]}">
                    </td>
                    <td style="width:110px">
                        <button class="pequeno" type="submit" name="accion" value="consultarProducto${i}">Consultar</button>
                    </td>
                    <td>
                        <input type="text" value="${requestScope['nombre_producto'.concat(i)]}" readonly>
                    </td>
                    <td style="width:90px">
                        <input type="text" name="cantidad_producto${i}" value="${paramValues['cantidad_producto'.concat(i)][0]}">
                    </td>
                    <td style="width:140px">
                        <input type="text" value="${requestScope['valor_total'.concat(i)]}" readonly>
                    </td>
                </tr>
            </c:forEach>
        </table>

        <div style="display:grid;grid-template-columns:1fr 320px;gap:20px;margin-top:22px;align-items:start">
            <div class="botones" style="margin-top:24px">
                <button type="submit" name="accion" value="confirmar">Confirmar</button>
            </div>
            <div class="campos-simple" style="grid-template-columns:130px 1fr">
                <label>Total Venta</label>
                <input type="text" value="${total_venta_sin_iva}" readonly>
                <label>Total IVA</label>
                <input type="text" value="${total_iva}" readonly>
                <label>Total con IVA</label>
                <input type="text" value="${total_con_iva}" readonly>
            </div>
        </div>
    </form>
</div>
</body>
</html>
