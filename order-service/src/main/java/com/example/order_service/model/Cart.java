package com.example.order_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.util.Set;

@Entity
@Table(name = "Carts")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"orders"})
@Data
@Builder
public class Cart extends AbstractMappedEntity{
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "card_id", unique = true, nullable = false, updatable = false)
    private String cartId;
    @Column(name = "user_id")
    private long userId;
    @JsonIgnore
    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Orders> orders;
}
