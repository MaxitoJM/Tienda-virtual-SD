package com.tiendagenerica.frontend.cliente;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente HTTP de la aplicacion de frontend hacia la API REST del backend.
 *
 * La direccion del backend se resuelve en este orden:
 *   1. variable de entorno BACKEND_URL
 *   2. propiedad de sistema backend.url
 *   3. valor por defecto http://localhost:5000
 *
 * De esta forma, al desplegar en Elastic Beanstalk basta con definir la
 * variable de entorno con la URL del entorno de backend, sin recompilar.
 */
public class ApiClient {

    private static final String URL_POR_DEFECTO = "http://localhost:5000";
    private static final int TIMEOUT_MS = 15000;

    private final String urlBase;
    private final ObjectMapper mapper;

    public ApiClient() {
        this(resolverUrlBase());
    }

    public ApiClient(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String resolverUrlBase() {
        String desdeEntorno = System.getenv("BACKEND_URL");
        if (desdeEntorno != null && !desdeEntorno.trim().isEmpty()) {
            return desdeEntorno.trim();
        }
        return System.getProperty("backend.url", URL_POR_DEFECTO);
    }

    public String getUrlBase() {
        return urlBase;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public RespuestaHttp get(String ruta) throws IOException {
        return ejecutar("GET", ruta, null, null);
    }

    public RespuestaHttp post(String ruta, Object cuerpo) throws IOException {
        return ejecutar("POST", ruta, mapper.writeValueAsString(cuerpo), "application/json; charset=UTF-8");
    }

    public RespuestaHttp put(String ruta, Object cuerpo) throws IOException {
        return ejecutar("PUT", ruta, mapper.writeValueAsString(cuerpo), "application/json; charset=UTF-8");
    }

    public RespuestaHttp delete(String ruta) throws IOException {
        return ejecutar("DELETE", ruta, null, null);
    }

    /** Envio de un archivo mediante multipart/form-data (carga de productos). */
    public RespuestaHttp subirArchivo(String ruta, String campo, String nombreArchivo, byte[] contenido)
            throws IOException {
        String frontera = "----TiendaGenerica" + UUID.randomUUID().toString().replace("-", "");
        String salto = "\r\n";
        StringBuilder encabezado = new StringBuilder();
        encabezado.append("--").append(frontera).append(salto);
        encabezado.append("Content-Disposition: form-data; name=\"").append(campo)
                  .append("\"; filename=\"").append(nombreArchivo).append("\"").append(salto);
        encabezado.append("Content-Type: text/csv").append(salto).append(salto);
        byte[] inicio = encabezado.toString().getBytes(StandardCharsets.UTF_8);
        byte[] cierre = (salto + "--" + frontera + "--" + salto).getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conexion = abrir(ruta, "POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + frontera);
        try (OutputStream salida = conexion.getOutputStream()) {
            salida.write(inicio);
            salida.write(contenido);
            salida.write(cierre);
        }
        return leerRespuesta(conexion);
    }

    private RespuestaHttp ejecutar(String metodo, String ruta, String cuerpo, String contentType)
            throws IOException {
        HttpURLConnection conexion = abrir(ruta, metodo);
        if (cuerpo != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", contentType);
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(cuerpo.getBytes(StandardCharsets.UTF_8));
            }
        }
        return leerRespuesta(conexion);
    }

    private HttpURLConnection abrir(String ruta, String metodo) throws IOException {
        HttpURLConnection conexion = (HttpURLConnection) new URL(urlBase + ruta).openConnection();
        conexion.setRequestMethod(metodo);
        conexion.setRequestProperty("Accept", "application/json");
        conexion.setConnectTimeout(TIMEOUT_MS);
        conexion.setReadTimeout(TIMEOUT_MS);
        return conexion;
    }

    private RespuestaHttp leerRespuesta(HttpURLConnection conexion) throws IOException {
        int codigo = conexion.getResponseCode();
        InputStream flujo = codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
        StringBuilder contenido = new StringBuilder();
        if (flujo != null) {
            try (BufferedReader lector = new BufferedReader(
                    new InputStreamReader(flujo, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    contenido.append(linea);
                }
            }
        }
        conexion.disconnect();
        return new RespuestaHttp(codigo, contenido.toString(), mapper);
    }

    /** Respuesta HTTP con utilidades para interpretar el JSON devuelto. */
    public static class RespuestaHttp {

        private final int codigo;
        private final String cuerpo;
        private final ObjectMapper mapper;

        public RespuestaHttp(int codigo, String cuerpo, ObjectMapper mapper) {
            this.codigo = codigo;
            this.cuerpo = cuerpo;
            this.mapper = mapper;
        }

        public int getCodigo() { return codigo; }
        public String getCuerpo() { return cuerpo; }
        public boolean esExitosa() { return codigo >= 200 && codigo < 300; }

        /**
         * Convierte la respuesta en una lista de mapas, apta para recorrerse
         * directamente desde las paginas JSP con la etiqueta forEach.
         */
        public List<Map<String, Object>> comoLista() throws IOException {
            if (cuerpo == null || cuerpo.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return mapper.readValue(cuerpo, new TypeReference<List<Map<String, Object>>>() { });
        }

        /** Convierte la respuesta en un mapa, apto para usarse desde las paginas JSP. */
        public Map<String, Object> comoMapa() throws IOException {
            if (cuerpo == null || cuerpo.trim().isEmpty()) {
                return new LinkedHashMap<>();
            }
            return mapper.readValue(cuerpo, new TypeReference<Map<String, Object>>() { });
        }

        public JsonNode comoJson() throws IOException {
            if (cuerpo == null || cuerpo.trim().isEmpty()) {
                return mapper.createObjectNode();
            }
            return mapper.readTree(cuerpo);
        }

        /** Extrae el mensaje de error devuelto por el backend. */
        public String mensaje() {
            try {
                JsonNode nodo = comoJson();
                if (nodo.has("mensaje") && !nodo.get("mensaje").isNull()) {
                    return nodo.get("mensaje").asText();
                }
            } catch (IOException ignorado) {
                // Se devuelve el mensaje generico de abajo.
            }
            return "No fue posible completar la operacion (codigo HTTP " + codigo + ")";
        }
    }
}
