package com.store.service.dto;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.store.domain.Cart} entity.
 */
public class CartDTO implements Serializable {

    private Long id;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")
    private String email;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CartDTO cartDTO = (CartDTO) o;
        if (cartDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), cartDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CartDTO{" +
            "id=" + getId() +
            ", email='" + getEmail() + "'" +
            "}";
    }
}
