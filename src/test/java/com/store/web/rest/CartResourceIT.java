package com.store.web.rest;

import com.store.StoreApp;
import com.store.domain.Cart;
import com.store.domain.Order;
import com.store.repository.CartRepository;
import com.store.service.CartService;
import com.store.service.dto.CartDTO;
import com.store.service.mapper.CartMapper;
import com.store.web.rest.errors.ExceptionTranslator;
import com.store.service.dto.CartCriteria;
import com.store.service.CartQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.store.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link CartResource} REST controller.
 */
@SpringBootTest(classes = StoreApp.class)
public class CartResourceIT {

    private static final String DEFAULT_EMAIL = "*P@p";
    private static final String UPDATED_EMAIL = "W@G";

    private static final LocalDate DEFAULT_CLOSED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CLOSED_AT = LocalDate.now(ZoneId.systemDefault());

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartQueryService cartQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restCartMockMvc;

    private Cart cart;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CartResource cartResource = new CartResource(cartService, cartQueryService);
        this.restCartMockMvc = MockMvcBuilders.standaloneSetup(cartResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cart createEntity(EntityManager em) {
        Cart cart = new Cart()
            .email(DEFAULT_EMAIL)
            .closedAt(DEFAULT_CLOSED_AT);
        return cart;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cart createUpdatedEntity(EntityManager em) {
        Cart cart = new Cart()
            .email(UPDATED_EMAIL)
            .closedAt(UPDATED_CLOSED_AT);
        return cart;
    }

    @BeforeEach
    public void initTest() {
        cart = createEntity(em);
    }

    @Test
    @Transactional
    public void createCart() throws Exception {
        int databaseSizeBeforeCreate = cartRepository.findAll().size();

        // Create the Cart
        CartDTO cartDTO = cartMapper.toDto(cart);
        restCartMockMvc.perform(post("/api/carts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cartDTO)))
            .andExpect(status().isCreated());

        // Validate the Cart in the database
        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(databaseSizeBeforeCreate + 1);
        Cart testCart = cartList.get(cartList.size() - 1);
        assertThat(testCart.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testCart.getClosedAt()).isEqualTo(DEFAULT_CLOSED_AT);
    }

    @Test
    @Transactional
    public void createCartWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = cartRepository.findAll().size();

        // Create the Cart with an existing ID
        cart.setId(1L);
        CartDTO cartDTO = cartMapper.toDto(cart);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCartMockMvc.perform(post("/api/carts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cartDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Cart in the database
        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = cartRepository.findAll().size();
        // set the field null
        cart.setEmail(null);

        // Create the Cart, which fails.
        CartDTO cartDTO = cartMapper.toDto(cart);

        restCartMockMvc.perform(post("/api/carts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cartDTO)))
            .andExpect(status().isBadRequest());

        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCarts() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList
        restCartMockMvc.perform(get("/api/carts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cart.getId().intValue())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL.toString())))
            .andExpect(jsonPath("$.[*].closedAt").value(hasItem(DEFAULT_CLOSED_AT.toString())));
    }
    
    @Test
    @Transactional
    public void getCart() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get the cart
        restCartMockMvc.perform(get("/api/carts/{id}", cart.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(cart.getId().intValue()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL.toString()))
            .andExpect(jsonPath("$.closedAt").value(DEFAULT_CLOSED_AT.toString()));
    }

    @Test
    @Transactional
    public void getAllCartsByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where email equals to DEFAULT_EMAIL
        defaultCartShouldBeFound("email.equals=" + DEFAULT_EMAIL);

        // Get all the cartList where email equals to UPDATED_EMAIL
        defaultCartShouldNotBeFound("email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void getAllCartsByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where email in DEFAULT_EMAIL or UPDATED_EMAIL
        defaultCartShouldBeFound("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL);

        // Get all the cartList where email equals to UPDATED_EMAIL
        defaultCartShouldNotBeFound("email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void getAllCartsByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where email is not null
        defaultCartShouldBeFound("email.specified=true");

        // Get all the cartList where email is null
        defaultCartShouldNotBeFound("email.specified=false");
    }

    @Test
    @Transactional
    public void getAllCartsByClosedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where closedAt equals to DEFAULT_CLOSED_AT
        defaultCartShouldBeFound("closedAt.equals=" + DEFAULT_CLOSED_AT);

        // Get all the cartList where closedAt equals to UPDATED_CLOSED_AT
        defaultCartShouldNotBeFound("closedAt.equals=" + UPDATED_CLOSED_AT);
    }

    @Test
    @Transactional
    public void getAllCartsByClosedAtIsInShouldWork() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where closedAt in DEFAULT_CLOSED_AT or UPDATED_CLOSED_AT
        defaultCartShouldBeFound("closedAt.in=" + DEFAULT_CLOSED_AT + "," + UPDATED_CLOSED_AT);

        // Get all the cartList where closedAt equals to UPDATED_CLOSED_AT
        defaultCartShouldNotBeFound("closedAt.in=" + UPDATED_CLOSED_AT);
    }

    @Test
    @Transactional
    public void getAllCartsByClosedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where closedAt is not null
        defaultCartShouldBeFound("closedAt.specified=true");

        // Get all the cartList where closedAt is null
        defaultCartShouldNotBeFound("closedAt.specified=false");
    }

    @Test
    @Transactional
    public void getAllCartsByClosedAtIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where closedAt greater than or equals to DEFAULT_CLOSED_AT
        defaultCartShouldBeFound("closedAt.greaterOrEqualThan=" + DEFAULT_CLOSED_AT);

        // Get all the cartList where closedAt greater than or equals to UPDATED_CLOSED_AT
        defaultCartShouldNotBeFound("closedAt.greaterOrEqualThan=" + UPDATED_CLOSED_AT);
    }

    @Test
    @Transactional
    public void getAllCartsByClosedAtIsLessThanSomething() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        // Get all the cartList where closedAt less than or equals to DEFAULT_CLOSED_AT
        defaultCartShouldNotBeFound("closedAt.lessThan=" + DEFAULT_CLOSED_AT);

        // Get all the cartList where closedAt less than or equals to UPDATED_CLOSED_AT
        defaultCartShouldBeFound("closedAt.lessThan=" + UPDATED_CLOSED_AT);
    }


    @Test
    @Transactional
    public void getAllCartsByOrderIsEqualToSomething() throws Exception {
        // Initialize the database
        Order order = OrderResourceIT.createEntity(em);
        em.persist(order);
        em.flush();
        cart.addOrder(order);
        cartRepository.saveAndFlush(cart);
        Long orderId = order.getId();

        // Get all the cartList where order equals to orderId
        defaultCartShouldBeFound("orderId.equals=" + orderId);

        // Get all the cartList where order equals to orderId + 1
        defaultCartShouldNotBeFound("orderId.equals=" + (orderId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCartShouldBeFound(String filter) throws Exception {
        restCartMockMvc.perform(get("/api/carts?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cart.getId().intValue())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].closedAt").value(hasItem(DEFAULT_CLOSED_AT.toString())));

        // Check, that the count call also returns 1
        restCartMockMvc.perform(get("/api/carts/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCartShouldNotBeFound(String filter) throws Exception {
        restCartMockMvc.perform(get("/api/carts?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCartMockMvc.perform(get("/api/carts/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingCart() throws Exception {
        // Get the cart
        restCartMockMvc.perform(get("/api/carts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCart() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        int databaseSizeBeforeUpdate = cartRepository.findAll().size();

        // Update the cart
        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        // Disconnect from session so that the updates on updatedCart are not directly saved in db
        em.detach(updatedCart);
        updatedCart
            .email(UPDATED_EMAIL)
            .closedAt(UPDATED_CLOSED_AT);
        CartDTO cartDTO = cartMapper.toDto(updatedCart);

        restCartMockMvc.perform(put("/api/carts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cartDTO)))
            .andExpect(status().isOk());

        // Validate the Cart in the database
        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate);
        Cart testCart = cartList.get(cartList.size() - 1);
        assertThat(testCart.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testCart.getClosedAt()).isEqualTo(UPDATED_CLOSED_AT);
    }

    @Test
    @Transactional
    public void updateNonExistingCart() throws Exception {
        int databaseSizeBeforeUpdate = cartRepository.findAll().size();

        // Create the Cart
        CartDTO cartDTO = cartMapper.toDto(cart);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(put("/api/carts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(cartDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Cart in the database
        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCart() throws Exception {
        // Initialize the database
        cartRepository.saveAndFlush(cart);

        int databaseSizeBeforeDelete = cartRepository.findAll().size();

        // Delete the cart
        restCartMockMvc.perform(delete("/api/carts/{id}", cart.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Cart.class);
        Cart cart1 = new Cart();
        cart1.setId(1L);
        Cart cart2 = new Cart();
        cart2.setId(cart1.getId());
        assertThat(cart1).isEqualTo(cart2);
        cart2.setId(2L);
        assertThat(cart1).isNotEqualTo(cart2);
        cart1.setId(null);
        assertThat(cart1).isNotEqualTo(cart2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CartDTO.class);
        CartDTO cartDTO1 = new CartDTO();
        cartDTO1.setId(1L);
        CartDTO cartDTO2 = new CartDTO();
        assertThat(cartDTO1).isNotEqualTo(cartDTO2);
        cartDTO2.setId(cartDTO1.getId());
        assertThat(cartDTO1).isEqualTo(cartDTO2);
        cartDTO2.setId(2L);
        assertThat(cartDTO1).isNotEqualTo(cartDTO2);
        cartDTO1.setId(null);
        assertThat(cartDTO1).isNotEqualTo(cartDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(cartMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(cartMapper.fromId(null)).isNull();
    }
}
