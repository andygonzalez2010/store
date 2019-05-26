package com.store.web.rest;

import com.store.StoreApp;
import com.store.domain.Item;
import com.store.repository.ItemRepository;
import com.store.service.ItemService;
import com.store.service.dto.ItemDTO;
import com.store.service.mapper.ItemMapper;
import com.store.web.rest.errors.ExceptionTranslator;
import com.store.service.dto.ItemCriteria;
import com.store.service.ItemQueryService;

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
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static com.store.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link ItemResource} REST controller.
 */
@SpringBootTest(classes = StoreApp.class)
public class ItemResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Double DEFAULT_PRICE = 0D;
    private static final Double UPDATED_PRICE = 1D;

    private static final Integer DEFAULT_COUNT = 0;
    private static final Integer UPDATED_COUNT = 1;

    private static final byte[] DEFAULT_IMAGE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_CONTENT_TYPE = "image/png";

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemQueryService itemQueryService;

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

    private MockMvc restItemMockMvc;

    private Item item;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ItemResource itemResource = new ItemResource(itemService, itemQueryService);
        this.restItemMockMvc = MockMvcBuilders.standaloneSetup(itemResource)
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
    public static Item createEntity(EntityManager em) {
        Item item = new Item()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .price(DEFAULT_PRICE)
            .count(DEFAULT_COUNT)
            .image(DEFAULT_IMAGE)
            .imageContentType(DEFAULT_IMAGE_CONTENT_TYPE);
        return item;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Item createUpdatedEntity(EntityManager em) {
        Item item = new Item()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .price(UPDATED_PRICE)
            .count(UPDATED_COUNT)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE);
        return item;
    }

    @BeforeEach
    public void initTest() {
        item = createEntity(em);
    }

    @Test
    @Transactional
    public void createItem() throws Exception {
        int databaseSizeBeforeCreate = itemRepository.findAll().size();

        // Create the Item
        ItemDTO itemDTO = itemMapper.toDto(item);
        restItemMockMvc.perform(post("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isCreated());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeCreate + 1);
        Item testItem = itemList.get(itemList.size() - 1);
        assertThat(testItem.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testItem.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testItem.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testItem.getCount()).isEqualTo(DEFAULT_COUNT);
        assertThat(testItem.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testItem.getImageContentType()).isEqualTo(DEFAULT_IMAGE_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void createItemWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = itemRepository.findAll().size();

        // Create the Item with an existing ID
        item.setId(1L);
        ItemDTO itemDTO = itemMapper.toDto(item);

        // An entity with an existing ID cannot be created, so this API call must fail
        restItemMockMvc.perform(post("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemRepository.findAll().size();
        // set the field null
        item.setTitle(null);

        // Create the Item, which fails.
        ItemDTO itemDTO = itemMapper.toDto(item);

        restItemMockMvc.perform(post("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemRepository.findAll().size();
        // set the field null
        item.setDescription(null);

        // Create the Item, which fails.
        ItemDTO itemDTO = itemMapper.toDto(item);

        restItemMockMvc.perform(post("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemRepository.findAll().size();
        // set the field null
        item.setPrice(null);

        // Create the Item, which fails.
        ItemDTO itemDTO = itemMapper.toDto(item);

        restItemMockMvc.perform(post("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCountIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemRepository.findAll().size();
        // set the field null
        item.setCount(null);

        // Create the Item, which fails.
        ItemDTO itemDTO = itemMapper.toDto(item);

        restItemMockMvc.perform(post("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllItems() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList
        restItemMockMvc.perform(get("/api/items?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].count").value(hasItem(DEFAULT_COUNT)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))));
    }
    
    @Test
    @Transactional
    public void getItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get the item
        restItemMockMvc.perform(get("/api/items/{id}", item.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(item.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.doubleValue()))
            .andExpect(jsonPath("$.count").value(DEFAULT_COUNT))
            .andExpect(jsonPath("$.imageContentType").value(DEFAULT_IMAGE_CONTENT_TYPE))
            .andExpect(jsonPath("$.image").value(Base64Utils.encodeToString(DEFAULT_IMAGE)));
    }

    @Test
    @Transactional
    public void getAllItemsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where title equals to DEFAULT_TITLE
        defaultItemShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the itemList where title equals to UPDATED_TITLE
        defaultItemShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllItemsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultItemShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the itemList where title equals to UPDATED_TITLE
        defaultItemShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllItemsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where title is not null
        defaultItemShouldBeFound("title.specified=true");

        // Get all the itemList where title is null
        defaultItemShouldNotBeFound("title.specified=false");
    }

    @Test
    @Transactional
    public void getAllItemsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where description equals to DEFAULT_DESCRIPTION
        defaultItemShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the itemList where description equals to UPDATED_DESCRIPTION
        defaultItemShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllItemsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultItemShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the itemList where description equals to UPDATED_DESCRIPTION
        defaultItemShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllItemsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where description is not null
        defaultItemShouldBeFound("description.specified=true");

        // Get all the itemList where description is null
        defaultItemShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    public void getAllItemsByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where price equals to DEFAULT_PRICE
        defaultItemShouldBeFound("price.equals=" + DEFAULT_PRICE);

        // Get all the itemList where price equals to UPDATED_PRICE
        defaultItemShouldNotBeFound("price.equals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllItemsByPriceIsInShouldWork() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where price in DEFAULT_PRICE or UPDATED_PRICE
        defaultItemShouldBeFound("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE);

        // Get all the itemList where price equals to UPDATED_PRICE
        defaultItemShouldNotBeFound("price.in=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllItemsByPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where price is not null
        defaultItemShouldBeFound("price.specified=true");

        // Get all the itemList where price is null
        defaultItemShouldNotBeFound("price.specified=false");
    }

    @Test
    @Transactional
    public void getAllItemsByCountIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where count equals to DEFAULT_COUNT
        defaultItemShouldBeFound("count.equals=" + DEFAULT_COUNT);

        // Get all the itemList where count equals to UPDATED_COUNT
        defaultItemShouldNotBeFound("count.equals=" + UPDATED_COUNT);
    }

    @Test
    @Transactional
    public void getAllItemsByCountIsInShouldWork() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where count in DEFAULT_COUNT or UPDATED_COUNT
        defaultItemShouldBeFound("count.in=" + DEFAULT_COUNT + "," + UPDATED_COUNT);

        // Get all the itemList where count equals to UPDATED_COUNT
        defaultItemShouldNotBeFound("count.in=" + UPDATED_COUNT);
    }

    @Test
    @Transactional
    public void getAllItemsByCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where count is not null
        defaultItemShouldBeFound("count.specified=true");

        // Get all the itemList where count is null
        defaultItemShouldNotBeFound("count.specified=false");
    }

    @Test
    @Transactional
    public void getAllItemsByCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where count greater than or equals to DEFAULT_COUNT
        defaultItemShouldBeFound("count.greaterOrEqualThan=" + DEFAULT_COUNT);

        // Get all the itemList where count greater than or equals to UPDATED_COUNT
        defaultItemShouldNotBeFound("count.greaterOrEqualThan=" + UPDATED_COUNT);
    }

    @Test
    @Transactional
    public void getAllItemsByCountIsLessThanSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where count less than or equals to DEFAULT_COUNT
        defaultItemShouldNotBeFound("count.lessThan=" + DEFAULT_COUNT);

        // Get all the itemList where count less than or equals to UPDATED_COUNT
        defaultItemShouldBeFound("count.lessThan=" + UPDATED_COUNT);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultItemShouldBeFound(String filter) throws Exception {
        restItemMockMvc.perform(get("/api/items?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].count").value(hasItem(DEFAULT_COUNT)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))));

        // Check, that the count call also returns 1
        restItemMockMvc.perform(get("/api/items/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultItemShouldNotBeFound(String filter) throws Exception {
        restItemMockMvc.perform(get("/api/items?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restItemMockMvc.perform(get("/api/items/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingItem() throws Exception {
        // Get the item
        restItemMockMvc.perform(get("/api/items/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        int databaseSizeBeforeUpdate = itemRepository.findAll().size();

        // Update the item
        Item updatedItem = itemRepository.findById(item.getId()).get();
        // Disconnect from session so that the updates on updatedItem are not directly saved in db
        em.detach(updatedItem);
        updatedItem
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .price(UPDATED_PRICE)
            .count(UPDATED_COUNT)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE);
        ItemDTO itemDTO = itemMapper.toDto(updatedItem);

        restItemMockMvc.perform(put("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isOk());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeUpdate);
        Item testItem = itemList.get(itemList.size() - 1);
        assertThat(testItem.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testItem.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testItem.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testItem.getCount()).isEqualTo(UPDATED_COUNT);
        assertThat(testItem.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testItem.getImageContentType()).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingItem() throws Exception {
        int databaseSizeBeforeUpdate = itemRepository.findAll().size();

        // Create the Item
        ItemDTO itemDTO = itemMapper.toDto(item);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restItemMockMvc.perform(put("/api/items")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        int databaseSizeBeforeDelete = itemRepository.findAll().size();

        // Delete the item
        restItemMockMvc.perform(delete("/api/items/{id}", item.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Item.class);
        Item item1 = new Item();
        item1.setId(1L);
        Item item2 = new Item();
        item2.setId(item1.getId());
        assertThat(item1).isEqualTo(item2);
        item2.setId(2L);
        assertThat(item1).isNotEqualTo(item2);
        item1.setId(null);
        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemDTO.class);
        ItemDTO itemDTO1 = new ItemDTO();
        itemDTO1.setId(1L);
        ItemDTO itemDTO2 = new ItemDTO();
        assertThat(itemDTO1).isNotEqualTo(itemDTO2);
        itemDTO2.setId(itemDTO1.getId());
        assertThat(itemDTO1).isEqualTo(itemDTO2);
        itemDTO2.setId(2L);
        assertThat(itemDTO1).isNotEqualTo(itemDTO2);
        itemDTO1.setId(null);
        assertThat(itemDTO1).isNotEqualTo(itemDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(itemMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(itemMapper.fromId(null)).isNull();
    }
}
