package com.tiendagenerica.backend.servicio;

import com.tiendagenerica.backend.excepcion.DatosInvalidosException;
import com.tiendagenerica.backend.excepcion.RecursoNoEncontradoException;
import com.tiendagenerica.backend.modelo.Cliente;
import com.tiendagenerica.backend.repositorio.ClienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Modulo de Gestion de Clientes (HU-006 a HU-009). */
@Service
public class ClienteServicio {

    private final ClienteRepositorio repositorio;

    public ClienteServicio(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repositorio.findAll();
    }

    /** HU-007: consulta de los datos del cliente por medio de la cedula. */
    @Transactional(readOnly = true)
    public Cliente consultar(Long cedula) {
        if (cedula == null) {
            throw new DatosInvalidosException("La cedula del cliente es obligatoria");
        }
        return repositorio.findById(cedula).orElseThrow(() -> new RecursoNoEncontradoException(
                "La cedula " + cedula + " no se encuentra registrada en la base de datos"));
    }

    /** HU-006: creacion de un nuevo cliente. */
    @Transactional
    public Cliente guardar(Cliente cliente) {
        validarCompletitud(cliente);
        if (repositorio.existsById(cliente.getCedulaCliente())) {
            throw new DatosInvalidosException(
                    "Ya existe un cliente registrado con la cedula " + cliente.getCedulaCliente());
        }
        return repositorio.save(cliente);
    }

    /** HU-008: actualizacion de los datos del cliente, previa consulta por cedula. */
    @Transactional
    public Cliente actualizar(Cliente cliente) {
        validarCompletitud(cliente);
        Cliente existente = consultar(cliente.getCedulaCliente());
        existente.setNombreCliente(cliente.getNombreCliente());
        existente.setDireccionCliente(cliente.getDireccionCliente());
        existente.setTelefonoCliente(cliente.getTelefonoCliente());
        existente.setEmailCliente(cliente.getEmailCliente());
        return repositorio.save(existente);
    }

    /** HU-009: borrado de los datos del cliente, previa consulta por cedula. */
    @Transactional
    public void eliminar(Long cedula) {
        Cliente existente = consultar(cedula);
        repositorio.delete(existente);
    }

    private void validarCompletitud(Cliente cliente) {
        if (cliente == null) {
            throw new DatosInvalidosException("No se recibieron los datos del cliente");
        }
        if (cliente.getCedulaCliente() == null) {
            throw new DatosInvalidosException("Datos incompletos: la cedula es obligatoria");
        }
        exigir(cliente.getNombreCliente(), "el nombre completo");
        exigir(cliente.getDireccionCliente(), "la direccion");
        exigir(cliente.getTelefonoCliente(), "el telefono");
        exigir(cliente.getEmailCliente(), "el correo electronico");
    }

    private void exigir(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("Datos incompletos: " + campo + " es obligatorio");
        }
    }
}
