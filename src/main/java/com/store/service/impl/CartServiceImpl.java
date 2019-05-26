package com.store.service.impl;

import com.store.domain.Item;
import com.store.domain.Order;
import com.store.repository.ItemRepository;
import com.store.service.CartService;
import com.store.domain.Cart;
import com.store.repository.CartRepository;
import com.store.service.MailService;
import com.store.service.dto.CartDTO;
import com.store.service.mapper.CartMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Cart}.
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final MailService mailService;
    private final ItemRepository itemRepository;

    private final CartRepository cartRepository;

    private final CartMapper cartMapper;

    public CartServiceImpl(MailService mailService, ItemRepository itemRepository, CartRepository cartRepository, CartMapper cartMapper) {
        this.mailService = mailService;
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    /**
     * Save a cart.
     *
     * @param cartDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public CartDTO save(CartDTO cartDTO) {
        log.debug("Request to save Cart : {}", cartDTO);
        if (cartDTO.getClosedAt() == null) return close(cartDTO);
        Cart cart = cartMapper.toEntity(cartDTO);
        cart = cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    /**
     * close a cart.
     *
     * @param cartDTO the entity to close.
     * @return the persisted entity.
     */
    @Override
    public CartDTO close(CartDTO cartDTO) {
        log.debug("Request to close Cart : {}", cartDTO);
        Cart cart = cartMapper.toEntity(cartDTO);
        if (cart.getClosedAt() == null) {
            List<Item> modified = new ArrayList<>();
            for (Order order : cart.getOrders()) {
                Item item = order.getItem();
                item.setCount(item.getCount() - order.getQuantity());
                modified.add(item);
            }
            itemRepository.saveAll(modified);
            cart.setClosedAt(LocalDate.now());
            cart = cartRepository.save(cart);
            mailService.sendTicket(cart);
        }
        return cartMapper.toDto(cart);
    }

    /**
     * Get all the carts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CartDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Carts");
        return cartRepository.findAll(pageable)
            .map(cartMapper::toDto);
    }


    /**
     * Get one cart by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CartDTO> findOne(Long id) {
        log.debug("Request to get Cart : {}", id);
        return cartRepository.findById(id)
            .map(cartMapper::toDto);
    }

    /**
     * Delete the cart by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Cart : {}", id);
        cartRepository.deleteById(id);
    }
}
