package com.store.service.dto;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.store.domain.Order} entity.
 */
public class OrderDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(value = 1)
    private Integer quantity;


    private Long cartId;

    private Long itemId;

    private String itemTitle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrderDTO orderDTO = (OrderDTO) o;
        if (orderDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), orderDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", cart=" + getCartId() +
            ", item=" + getItemId() +
            ", item='" + getItemTitle() + "'" +
            "}";
    }
}
