package se.edugrade.winerater.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"wine_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double score;
    private String comment;
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wine_id", nullable = false)
    private Wine wine;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Rating(double score, String comment, Wine wine, User user) {
        this.score = score;
        this.comment = comment;
        this.wine = wine;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
