package project.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {

    @NotBlank
    private String name;
    @NotBlank
    private String email;
    private String phone;
    @NotBlank
    private String cpf;
    private String cep;
    private String city;
    private String state;
    private String street;
    private String numberHouse;
    private String neighborhood;
    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyCnpj;

}
