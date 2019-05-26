package com.store.service.mapper;

import com.store.domain.*;
import com.store.service.dto.OrderDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring", uses = {CartMapper.class, ItemMapper.class})
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {

    @Mapping(source = "cart.id", target = "cartId")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.title", target = "itemTitle")
    OrderDTO toDto(Order order);

    @Mapping(source = "cartId", target = "cart")
    @Mapping(source = "itemId", target = "item")
    Order toEntity(OrderDTO orderDTO);

    default Order fromId(Long id) {
        if (id == null) {
            return null;
        }
        Order order = new Order();
        order.setId(id);
        return order;
    }
}
