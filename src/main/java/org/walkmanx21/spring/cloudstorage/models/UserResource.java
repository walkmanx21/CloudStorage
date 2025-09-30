package org.walkmanx21.spring.cloudstorage.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Resources")
public class UserResource {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "resource")
    private String resource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UserResource(User user, String resource, LocalDateTime createdAt) {
        this.user = user;
        this.resource = resource;
        this.createdAt = createdAt;
    }
}
