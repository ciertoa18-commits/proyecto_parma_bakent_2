package pe.edu.upeu.PharmaBackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PharmaBackend.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, long id);

    List<Producto> findByCategoriaId(Long categoriaId);
    boolean existsByCategoriaId(Long categoriaId);


    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Producto> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findById(Long id);


    @Modifying
    @Query("""
            update Producto p
               set p.stock = p.stock - :cantidad
             where p.id = :id
               and p.stock >= :cantidad
            """)
    int descontarStock(@Param("id") Long id,
                       @Param("cantidad") Integer cantidad);
}
