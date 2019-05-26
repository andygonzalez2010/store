package com.store.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.store.domain.Cart;
import com.store.domain.*; // for static metamodels
import com.store.repository.CartRepository;
import com.store.service.dto.CartCriteria;
import com.store.service.dto.CartDTO;
import com.store.service.mapper.CartMapper;

/**
 * Service for executing complex queries for {@link Cart} entities in the database.
 * The main input is a {@link CartCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CartDTO} or a {@link Page} of {@link CartDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CartQueryService extends QueryService<Cart> {

    private final Logger log = LoggerFactory.getLogger(CartQueryService.class);

    private final CartRepository cartRepository;

    private final CartMapper cartMapper;

    public CartQueryService(CartRepository cartRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    /**
     * Return a {@link List} of {@link CartDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CartDTO> findByCriteria(CartCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Cart> specification = createSpecification(criteria);
        return cartMapper.toDto(cartRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CartDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CartDTO> findByCriteria(CartCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Cart> specification = createSpecification(criteria);
        return cartRepository.findAll(specification, page)
            .map(cartMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CartCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Cart> specification = createSpecification(criteria);
        return cartRepository.count(specification);
    }

    /**
     * Function to convert CartCriteria to a {@link Specification}.
     */
    private Specification<Cart> createSpecification(CartCriteria criteria) {
        Specification<Cart> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Cart_.id));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Cart_.email));
            }
            if (criteria.getOrderId() != null) {
                specification = specification.and(buildSpecification(criteria.getOrderId(),
                    root -> root.join(Cart_.orders, JoinType.LEFT).get(Order_.id)));
            }
        }
        return specification;
    }
}
