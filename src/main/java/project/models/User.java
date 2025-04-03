package project.models;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "TUSER")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 40)
    private String cpf;

    @Column(name = "photo_path",length = 150)
    private String photoPath;

    @Column(length = 20)
    private String cep;

    @Column(length = 100)
    private String city;

    @Column(length = 3)
    private String state;

    @Column(length = 100)
    private String street;

    @Column(name = "number_house", length = 10)
    private String numberHouse;

    @Column(length = 100)
    private String neighborhood;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "company_email", length = 100)
    private String companyEmail;

    @Column(name = "company_phone", length = 20)
    private String companyPhone;

    @Column(name = "company_cnpj", length = 40)
    private String companyCnpj;
}
