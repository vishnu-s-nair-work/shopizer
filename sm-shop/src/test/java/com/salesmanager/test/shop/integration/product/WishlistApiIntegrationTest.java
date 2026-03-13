package com.salesmanager.test.shop.integration.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.model.customer.CustomerGender;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.catalog.category.Category;
import com.salesmanager.shop.model.catalog.category.CategoryDescription;
import com.salesmanager.shop.model.catalog.category.PersistableCategory;
import com.salesmanager.shop.model.catalog.product.product.PersistableProduct;
import com.salesmanager.shop.model.catalog.product.product.ProductSpecification;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlistStats;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.address.Address;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.shop.store.security.AuthenticationRequest;
import com.salesmanager.shop.store.security.AuthenticationResponse;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class WishlistApiIntegrationTest extends ServicesTestSupport {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private HttpHeaders customerHeader(String email, String password) {
        // Register customer (ignore 4xx — may already exist from previous run)
        PersistableCustomer c = new PersistableCustomer();
        c.setEmailAddress(email);
        c.setPassword(password);
        c.setGender(CustomerGender.M.name());
        c.setLanguage("en");
        Address billing = new Address();
        billing.setFirstName("Test");
        billing.setLastName("User");
        billing.setCountry("US");
        c.setBilling(billing);
        c.setStoreCode(Constants.DEFAULT_STORE);
        testRestTemplate.postForEntity("/api/v1/customer/register",
            new HttpEntity<>(c, getHeader()), PersistableCustomer.class);

        ResponseEntity<AuthenticationResponse> login = testRestTemplate.postForEntity(
            "/api/v1/customer/login",
            new HttpEntity<>(new AuthenticationRequest(email, password)),
            AuthenticationResponse.class);
        assertEquals(HttpStatus.OK, login.getStatusCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        headers.add("Authorization", "Bearer " + login.getBody().getToken());
        return headers;
    }

    private Long createProduct(String sku) {
        PersistableCategory cat = new PersistableCategory();
        cat.setCode("wl-cat-" + sku);
        cat.setSortOrder(1);
        cat.setVisible(true);
        cat.setDepth(1);
        CategoryDescription desc = new CategoryDescription();
        desc.setLanguage("en");
        desc.setName("wl-cat-" + sku);
        desc.setFriendlyUrl("wl-cat-" + sku);
        List<CategoryDescription> descs = new ArrayList<>();
        descs.add(desc);
        cat.setDescriptions(descs);
        testRestTemplate.postForEntity("/api/v1/private/category?store=" + Constants.DEFAULT_STORE,
            new HttpEntity<>(cat, getHeader()), PersistableCategory.class);

        PersistableProduct product = super.product(sku);
        product.setSku(sku);
        product.setPrice(new BigDecimal("49.99"));
        product.setAvailable(true);
        product.setQuantity(10);
        ProductSpecification spec = new ProductSpecification();
        spec.setManufacturer(
            com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer.DEFAULT_MANUFACTURER);
        product.setProductSpecifications(spec);
        Category c = new Category();
        c.setCode("wl-cat-" + sku);
        List<Category> cats = new ArrayList<>();
        cats.add(c);
        product.setCategories(cats);

        ResponseEntity<Entity> resp = testRestTemplate.postForEntity(
            "/api/v1/private/product?store=" + Constants.DEFAULT_STORE,
            new HttpEntity<>(product, getHeader()), Entity.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        return resp.getBody().getId();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    public void getWishlist_unauthenticated_returns401() {
        ResponseEntity<String> r = testRestTemplate.getForEntity("/api/v1/auth/wishlist", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
    }

    @Test
    public void getWishlist_authenticated_returnsEmptyWishlist() {
        HttpHeaders h = customerHeader("wl-empty@test.com", "Test1234!");
        ResponseEntity<ReadableWishlist> r = testRestTemplate.exchange(
            "/api/v1/auth/wishlist", HttpMethod.GET, new HttpEntity<>(h), ReadableWishlist.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertNotNull(r.getBody());
        assertTrue(r.getBody().getWishlistItems().isEmpty());
    }

    @Test
    public void addToWishlist_unauthenticated_returns401() {
        ResponseEntity<String> r = testRestTemplate.postForEntity(
            "/api/v1/auth/wishlist/1", new HttpEntity<>(new HttpHeaders()), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
    }

    @Test
    public void addToWishlist_nonExistentProduct_returns404() {
        HttpHeaders h = customerHeader("wl-404@test.com", "Test1234!");
        ResponseEntity<String> r = testRestTemplate.postForEntity(
            "/api/v1/auth/wishlist/999999", new HttpEntity<>(h), String.class);
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
    }

    @Test
    public void addToWishlist_validProduct_returns201AndAppearsInWishlist() {
        Long productId = createProduct("WL-ADD-" + System.currentTimeMillis());
        HttpHeaders h = customerHeader("wl-add@test.com", "Test1234!");

        ResponseEntity<Void> add = testRestTemplate.postForEntity(
            "/api/v1/auth/wishlist/" + productId, new HttpEntity<>(h), Void.class);
        assertEquals(HttpStatus.CREATED, add.getStatusCode());

        ResponseEntity<ReadableWishlist> wl = testRestTemplate.exchange(
            "/api/v1/auth/wishlist", HttpMethod.GET, new HttpEntity<>(h), ReadableWishlist.class);
        assertEquals(1, wl.getBody().getWishlistItems().size());
        assertEquals(productId, wl.getBody().getWishlistItems().get(0).getProductId());
        assertNotNull(wl.getBody().getWishlistItems().get(0).getPrice());
    }

    @Test
    public void addToWishlist_duplicate_isIdempotent() {
        Long productId = createProduct("WL-DUP-" + System.currentTimeMillis());
        HttpHeaders h = customerHeader("wl-dup@test.com", "Test1234!");

        testRestTemplate.postForEntity("/api/v1/auth/wishlist/" + productId, new HttpEntity<>(h), Void.class);
        testRestTemplate.postForEntity("/api/v1/auth/wishlist/" + productId, new HttpEntity<>(h), Void.class);

        ResponseEntity<ReadableWishlist> wl = testRestTemplate.exchange(
            "/api/v1/auth/wishlist", HttpMethod.GET, new HttpEntity<>(h), ReadableWishlist.class);
        assertEquals(1, wl.getBody().getWishlistItems().size());
    }

    @Test
    public void removeFromWishlist_existingItem_removesIt() {
        Long productId = createProduct("WL-REM-" + System.currentTimeMillis());
        HttpHeaders h = customerHeader("wl-rem@test.com", "Test1234!");

        testRestTemplate.postForEntity("/api/v1/auth/wishlist/" + productId, new HttpEntity<>(h), Void.class);
        testRestTemplate.exchange("/api/v1/auth/wishlist/" + productId,
            HttpMethod.DELETE, new HttpEntity<>(h), Void.class);

        ResponseEntity<ReadableWishlist> wl = testRestTemplate.exchange(
            "/api/v1/auth/wishlist", HttpMethod.GET, new HttpEntity<>(h), ReadableWishlist.class);
        assertTrue(wl.getBody().getWishlistItems().isEmpty());
    }

    @Test
    public void removeFromWishlist_nonExistentItem_returns200() {
        HttpHeaders h = customerHeader("wl-remne@test.com", "Test1234!");
        ResponseEntity<Void> r = testRestTemplate.exchange(
            "/api/v1/auth/wishlist/999999", HttpMethod.DELETE, new HttpEntity<>(h), Void.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
    }

    @Test
    public void adminStats_authenticated_returnsStats() {
        // Add a product to wishlist so stats are non-trivial
        Long productId = createProduct("WL-STAT-" + System.currentTimeMillis());
        HttpHeaders h = customerHeader("wl-stat@test.com", "Test1234!");
        testRestTemplate.postForEntity("/api/v1/auth/wishlist/" + productId, new HttpEntity<>(h), Void.class);

        ResponseEntity<ReadableWishlistStats[]> r = testRestTemplate.exchange(
            "/api/v1/private/wishlist/stats?limit=5",
            HttpMethod.GET, new HttpEntity<>(getHeader()), ReadableWishlistStats[].class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertNotNull(r.getBody());
        assertTrue(r.getBody().length > 0);
        assertNotNull(r.getBody()[0].getProductId());
        assertNotNull(r.getBody()[0].getWishlistCount());
    }

    @Test
    public void adminStats_unauthenticated_returns401() {
        ResponseEntity<String> r = testRestTemplate.getForEntity(
            "/api/v1/private/wishlist/stats", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
    }
}
