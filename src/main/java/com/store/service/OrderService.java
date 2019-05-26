package com.store.service;

import com.store.service.dto.OrderDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.store.domain.Order}.
 */
public interface OrderService {

    /**
     * Save a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    OrderDTO save(OrderDTO orderDTO);

    /**
     * Get all the orders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<OrderDTO> findAll(Pageable pageable);


    /**
     * Get the "id" order.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<OrderDTO> findOne(Long id);

    /**
     * Delete the "id" order.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
