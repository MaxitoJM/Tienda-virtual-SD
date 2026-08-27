package com.tiendagenerica.frontend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Backend simulado para las pruebas del frontend.
 *
 * Levanta un servidor HTTP real en un puerto libre y devuelve respuestas
 * preparadas de antemano. De esta forma las pruebas ejercitan el camino
 * completo del cliente HTTP (cabeceras, cuerpos, codigos de estado y
 * multipart) sin necesidad de simular la biblioteca de red.
 */
public class BackendSimulado {

    /** Respuesta preparada para una ruta concreta. */
    private static class Respuesta {
        final int codigo;
        final String cuerpo;

        Respuesta(int codigo, String cuerpo) {
            this.codigo = codigo;
            this.cuerpo = cuerpo;
        }
    }

    /** Registro de una peticion recibida, para poder verificarla. */
    public static class PeticionRecibida {
        public final String metodo;
        public final String ruta;
        public final String cuerpo;

        PeticionRecibida(String metodo, String ruta, String cuerpo) {
            this.metodo = metodo;
            this.ruta = ruta;
            this.cuerpo = cuerpo;
        }
    }

    private final HttpServer servidor;
    private final Map<String, Respuesta> respuestas = new LinkedHashMap<>();
    private final List<PeticionRecibida> recibidas = new ArrayList<>();

    public BackendSimulado() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", this::atender);
        servidor.setExecutor(null);
        servidor.start();
    }

    public String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    public void detener() {
        servidor.stop(0);
    }

    /** Prepara la respuesta que se devolvera para el metodo y la ruta indicados. */
    public BackendSimulado responder(String metodo, String ruta, int codigo, String cuerpo) {
        respuestas.put(metodo + " " + ruta, new Respuesta(codigo, cuerpo));
        return this;
    }

    public List<PeticionRecibida> peticiones() {
        return recibidas;
    }

    public PeticionRecibida ultimaPeticion() {
        return recibidas.isEmpty() ? null : recibidas.get(recibidas.size() - 1);
    }

    private void atender(HttpExchange intercambio) throws IOException {
        String metodo = intercambio.getRequestMethod();
        String ruta = intercambio.getRequestURI().getPath();
        String cuerpoPeticion = leerTodo(intercambio.getRequestBody());
        recibidas.add(new PeticionRecibida(metodo, ruta, cuerpoPeticion));

        Respuesta respuesta = respuestas.get(metodo + " " + ruta);
        if (respuesta == null) {
            respuesta = new Respuesta(404,
                    "{\"exitoso\":false,\"mensaje\":\"Ruta no preparada: " + metodo + " " + ruta + "\"}");
        }

        byte[] datos = respuesta.cuerpo == null
                ? new byte[0]
                : respuesta.cuerpo.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(respuesta.codigo, datos.length == 0 ? -1 : datos.length);
        if (datos.length > 0) {
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(datos);
            }
        }
        intercambio.close();
    }

    private String leerTodo(InputStream entrada) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        byte[] bloque = new byte[4096];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            salida.write(bloque, 0, leidos);
        }
        return new String(salida.toByteArray(), StandardCharsets.UTF_8);
    }
}
