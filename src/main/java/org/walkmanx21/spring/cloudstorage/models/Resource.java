package org.walkmanx21.spring.cloudstorage.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "Resources")
public class Resource {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "object")
    private String object;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Column(name = "size")
    private long size;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
