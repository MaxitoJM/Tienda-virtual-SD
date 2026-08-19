package com.tiendagenerica.backend.controlador;

import com.tiendagenerica.backend.dto.CargueProductosDto;
import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.modelo.Producto;
import com.tiendagenerica.backend.servicio.CargueProductosServicio;
import com.tiendagenerica.backend.servicio.ProductoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** API de Productos, incluida la carga masiva desde archivo CSV (HU-014). */
@RestController
@RequestMapping("/productos")
public class ProductoControlador {

    private final ProductoServicio servicio;
    private final CargueProductosServicio cargueServicio;

    public ProductoControlador(ProductoServicio servicio, CargueProductosServicio cargueServicio) {
        this.servicio = servicio;
        this.cargueServicio = cargueServicio;
    }

    @GetMapping("/listar")
    public List<Producto> listar() {
        return servicio.listar();
    }

    @GetMapping("/consultar/{id}")
    public Producto consultar(@PathVariable("id") Long id) {
        return servicio.consultar(id);
    }

    @PostMapping("/guardar")
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(producto));
    }

    @PutMapping("/actualizar")
    public Producto actualizar(@RequestBody Producto producto) {
        return servicio.actualizar(producto);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDto> eliminar(@PathVariable("id") Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok(RespuestaDto.ok("Producto eliminado correctamente"));
    }

    /**
     * HU-014: carga de los productos desde un archivo separado por comas.
     * El archivo se envia como multipart/form-data en el campo archivo.
     */
    @PostMapping("/cargar")
    public CargueProductosDto cargar(@RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        return cargueServicio.cargar(archivo);
    }
}
