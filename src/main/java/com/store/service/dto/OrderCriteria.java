package com.store.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.store.domain.Order} entity. This class is used
 * in {@link com.store.web.rest.OrderResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /orders?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class OrderCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private IntegerFilter quantity;

    private LongFilter cartId;

    private LongFilter itemId;

    public OrderCriteria(){
    }

    public OrderCriteria(OrderCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.quantity = other.quantity == null ? null : other.quantity.copy();
        this.cartId = other.cartId == null ? null : other.cartId.copy();
        this.itemId = other.itemId == null ? null : other.itemId.copy();
    }

    @Override
    public OrderCriteria copy() {
        return new OrderCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public IntegerFilter getQuantity() {
        return quantity;
    }

    public void setQuantity(IntegerFilter quantity) {
        this.quantity = quantity;
    }

    public LongFilter getCartId() {
        return cartId;
    }

    public void setCartId(LongFilter cartId) {
        this.cartId = cartId;
    }

    public LongFilter getItemId() {
        return itemId;
    }

    public void setItemId(LongFilter itemId) {
        this.itemId = itemId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OrderCriteria that = (OrderCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(quantity, that.quantity) &&
            Objects.equals(cartId, that.cartId) &&
            Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        quantity,
        cartId,
        itemId
        );
    }

    @Override
    public String toString() {
        return "OrderCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (quantity != null ? "quantity=" + quantity + ", " : "") +
                (cartId != null ? "cartId=" + cartId + ", " : "") +
                (itemId != null ? "itemId=" + itemId + ", " : "") +
            "}";
    }

}
