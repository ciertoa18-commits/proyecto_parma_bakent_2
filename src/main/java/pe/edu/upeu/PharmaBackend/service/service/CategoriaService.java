package pe.edu.upeu.PharmaBackend.service.service;

import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;

import java.util.List;


public interface CategoriaService {

    CategoriaResponseDTO crear(CategoriaRequestDTO request);

    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO request);

    CategoriaResponseDTO buscar(Long id);

    void eliminar(Long id);

    List<CategoriaResponseDTO> listar();
}
