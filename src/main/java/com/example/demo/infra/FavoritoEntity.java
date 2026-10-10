package com.example.demo.infra;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId; // Si en tu record original usaste String, cambialo acá también.

    private String nota;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id")
    private ListaEntity lista;

    // Constructor vacío exigido por JPA
    public FavoritoEntity() {
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public ListaEntity getLista() { return lista; }
    public void setLista(ListaEntity lista) { this.lista = lista; }
}