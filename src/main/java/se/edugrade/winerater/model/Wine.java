package se.edugrade.winerater.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    private String type;
    private String grape;
    private String country;
    private int year;

    @JsonIgnore
    @OneToMany(mappedBy = "wine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    public Wine(String name, String type, String country, int year) {
        this.name = name;
        this.type = type;
        this.country = country;
        this.year = year;
    }

    public Wine(String name, String type, String grape, String country, int year) {
        this.name = name;
        this.type = type;
        this.grape = grape;
        this.country = country;
        this.year = year;
    }
}
