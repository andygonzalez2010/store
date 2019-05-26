package com.store.web.rest;

import com.store.service.CartService;
import com.store.web.rest.errors.BadRequestAlertException;
import com.store.service.dto.CartDTO;
import com.store.service.dto.CartCriteria;
import com.store.service.CartQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.store.domain.Cart}.
 */
@RestController
@RequestMapping("/api")
public class CartResource {

    private final Logger log = LoggerFactory.getLogger(CartResource.class);

    private static final String ENTITY_NAME = "cart";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CartService cartService;

    private final CartQueryService cartQueryService;

    public CartResource(CartService cartService, CartQueryService cartQueryService) {
        this.cartService = cartService;
        this.cartQueryService = cartQueryService;
    }

    /**
     * {@code POST  /carts} : Create a new cart.
     *
     * @param cartDTO the cartDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cartDTO, or with status {@code 400 (Bad Request)} if the cart has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/carts")
    public ResponseEntity<CartDTO> createCart(@Valid @RequestBody CartDTO cartDTO) throws URISyntaxException {
        log.debug("REST request to save Cart : {}", cartDTO);
        if (cartDTO.getId() != null) {
            throw new BadRequestAlertException("A new cart cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CartDTO result = cartService.save(cartDTO);
        return ResponseEntity.created(new URI("/api/carts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /carts} : Updates an existing cart.
     *
     * @param cartDTO the cartDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cartDTO,
     * or with status {@code 400 (Bad Request)} if the cartDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cartDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/carts")
    public ResponseEntity<CartDTO> updateCart(@Valid @RequestBody CartDTO cartDTO) throws URISyntaxException {
        log.debug("REST request to update Cart : {}", cartDTO);
        if (cartDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CartDTO result = cartService.save(cartDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cartDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /carts} : get all the carts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of carts in body.
     */
    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts(CartCriteria criteria, Pageable pageable, @RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get Carts by criteria: {}", criteria);
        Page<CartDTO> page = cartQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * {@code GET  /carts/count} : count all the carts.
    *
    * @param criteria the criteria which the requested entities should match.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
    */
    @GetMapping("/carts/count")
    public ResponseEntity<Long> countCarts(CartCriteria criteria) {
        log.debug("REST request to count Carts by criteria: {}", criteria);
        return ResponseEntity.ok().body(cartQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /carts/:id} : get the "id" cart.
     *
     * @param id the id of the cartDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cartDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/carts/{id}")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long id) {
        log.debug("REST request to get Cart : {}", id);
        Optional<CartDTO> cartDTO = cartService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cartDTO);
    }

    /**
     * {@code DELETE  /carts/:id} : delete the "id" cart.
     *
     * @param id the id of the cartDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/carts/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        log.debug("REST request to delete Cart : {}", id);
        cartService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
