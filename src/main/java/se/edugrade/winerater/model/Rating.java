package se.edugrade.winerater.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double score;
    private String comment;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "wine_id")
    private Wine wine;

    @ManyToOne
    @JoinColumn(name = "user_id")
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
