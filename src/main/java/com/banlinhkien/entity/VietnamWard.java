package com.banlinhkien.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vietnam_wards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VietnamWard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ward_id", nullable = false, unique = true)
    private Long wardId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "district_id", nullable = false)
    private Long districtId;
}
