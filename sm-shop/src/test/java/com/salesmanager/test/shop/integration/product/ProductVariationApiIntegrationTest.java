package com.salesmanager.test.shop.integration.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.catalog.category.Category;
import com.salesmanager.shop.model.catalog.category.CategoryDescription;
import com.salesmanager.shop.model.catalog.category.PersistableCategory;
import com.salesmanager.shop.model.catalog.product.ReadableProductPrice;
import com.salesmanager.shop.model.catalog.product.attribute.PersistableProductAttribute;
import com.salesmanager.shop.model.catalog.product.attribute.PersistableProductOptionValue;
import com.salesmanager.shop.model.catalog.product.attribute.ProductOptionDescription;
import com.salesmanager.shop.model.catalog.product.attribute.ProductOptionValueDescription;
import com.salesmanager.shop.model.catalog.product.attribute.ProductPropertyOption;
import com.salesmanager.shop.model.catalog.product.attribute.ReadableSelectedProductVariant;
import com.salesmanager.shop.model.catalog.product.attribute.ReadableProductVariantValue;
import com.salesmanager.shop.model.catalog.product.attribute.api.PersistableProductOptionEntity;
import com.salesmanager.shop.model.catalog.product.attribute.api.PersistableProductOptionValueEntity;
import com.salesmanager.shop.model.catalog.product.attribute.api.ReadableProductAttributeEntity;
import com.salesmanager.shop.model.catalog.product.attribute.api.ReadableProductOptionEntity;
import com.salesmanager.shop.model.catalog.product.attribute.api.ReadableProductOptionValue;
import com.salesmanager.shop.model.catalog.product.product.PersistableProduct;
import com.salesmanager.shop.model.catalog.product.product.ProductSpecification;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.test.shop.common.ServicesTestSupport;

/**
 * Integration tests for POST /api/v2/product/{id}/variation
 * Covers the calculateVariant endpoint and the getProductWithAttributes fix.
 */
@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ProductVariationApiIntegrationTest extends ServicesTestSupport {

    // -----------------------------------------------------------------------
    // Test 1: empty options list → returns base product price (no DB skip)
    // -----------------------------------------------------------------------
    @Test
    public void calculateVariant_emptyOptions_returnsBasePriceForExistingProduct() throws Exception {
        // Create a product to use for this test
        String sku = "VAR-EMPTY-" + System.currentTimeMillis();
        PersistableProduct product = super.product(sku);
        product.setPrice(new BigDecimal("99.00"));
        product.setSku(sku);
        product.setAvailable(true);
        product.setQuantity(5);
        ProductSpecification spec = new ProductSpecification();
        spec.setManufacturer(
                com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer.DEFAULT_MANUFACTURER);
        product.setProductSpecifications(spec);

        HttpEntity<PersistableProduct> productEntity = new HttpEntity<>(product, getHeader());
        ResponseEntity<Entity> productResp = testRestTemplate.postForEntity(
                "/api/v1/private/product?store=" + Constants.DEFAULT_STORE, productEntity, Entity.class);
        assertEquals(HttpStatus.CREATED, productResp.getStatusCode());
        Long productId = productResp.getBody().getId();

        // Empty options → should return base price, not null
        ReadableSelectedProductVariant request = new ReadableSelectedProductVariant();
        request.setOptions(new ArrayList<>());

        HttpEntity<ReadableSelectedProductVariant> entity = new HttpEntity<>(request, getHeader());
        ResponseEntity<ReadableProductPrice> response = testRestTemplate.postForEntity(
                "/api/v2/product/" + productId + "/variation",
                entity,
                ReadableProductPrice.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getFinalPrice());
    }

    // -----------------------------------------------------------------------
    // Test 1b: non-existent product with empty options → 404
    // -----------------------------------------------------------------------
    @Test
    public void calculateVariant_emptyOptions_nonExistentProduct_returns404() {
        ReadableSelectedProductVariant request = new ReadableSelectedProductVariant();
        request.setOptions(new ArrayList<>());

        HttpEntity<ReadableSelectedProductVariant> entity = new HttpEntity<>(request, getHeader());
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/v2/product/999999/variation",
                entity,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // -----------------------------------------------------------------------
    // Test 2: non-existent product with non-empty options → 404
    // -----------------------------------------------------------------------
    @Test
    public void calculateVariant_productNotFound_returns404() {
        ReadableProductVariantValue opt = new ReadableProductVariantValue();
        opt.setOption(1L);
        opt.setValue(1L);

        ReadableSelectedProductVariant request = new ReadableSelectedProductVariant();
        request.setOptions(Arrays.asList(opt));

        HttpEntity<ReadableSelectedProductVariant> entity = new HttpEntity<>(request, getHeader());

        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/v2/product/999999/variation",
                entity,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // -----------------------------------------------------------------------
    // Test 3: valid product with matching attribute → returns price
    // -----------------------------------------------------------------------
    @Test
    public void calculateVariant_validProductAndOption_returnsPrice() throws Exception {

        // 1. Create category
        PersistableCategory cat = buildCategory("var-test-cat-" + System.currentTimeMillis());
        HttpEntity<PersistableCategory> catEntity = new HttpEntity<>(cat, getHeader());
        ResponseEntity<PersistableCategory> catResp = testRestTemplate.postForEntity(
                "/api/v1/private/category?store=" + Constants.DEFAULT_STORE, catEntity, PersistableCategory.class);
        assertEquals(HttpStatus.CREATED, catResp.getStatusCode());

        // 2. Create product
        String sku = "VAR-TEST-" + System.currentTimeMillis();
        PersistableProduct product = super.product(sku);
        product.setPrice(new BigDecimal("50.00"));
        product.setSku(sku);
        product.setAvailable(true);
        product.setQuantity(10);
        List<Category> cats = new ArrayList<>();
        Category c = new Category();
        c.setCode(cat.getCode());
        cats.add(c);
        product.setCategories(cats);
        ProductSpecification spec = new ProductSpecification();
        spec.setManufacturer(
                com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer.DEFAULT_MANUFACTURER);
        product.setProductSpecifications(spec);

        HttpEntity<PersistableProduct> productEntity = new HttpEntity<>(product, getHeader());
        ResponseEntity<Entity> productResp = testRestTemplate.postForEntity(
                "/api/v1/private/product?store=" + Constants.DEFAULT_STORE, productEntity, Entity.class);
        assertEquals(HttpStatus.CREATED, productResp.getStatusCode());
        Long productId = productResp.getBody().getId();
        assertNotNull(productId);

        // 3. Create option
        PersistableProductOptionEntity option = new PersistableProductOptionEntity();
        option.setCode("size-" + System.currentTimeMillis());
        ProductOptionDescription optDesc = new ProductOptionDescription();
        optDesc.setLanguage("en");
        optDesc.setName("Size");
        option.getDescriptions().add(optDesc);

        HttpEntity<PersistableProductOptionEntity> optionEntity = new HttpEntity<>(option, getHeader());
        ResponseEntity<ReadableProductOptionEntity> optionResp = testRestTemplate.postForEntity(
                "/api/v1/private/product/option?store=" + Constants.DEFAULT_STORE,
                optionEntity, ReadableProductOptionEntity.class);
        assertEquals(HttpStatus.CREATED, optionResp.getStatusCode());
        Long optionId = optionResp.getBody().getId();

        // 4. Create option value
        PersistableProductOptionValueEntity optionValue = new PersistableProductOptionValueEntity();
        optionValue.setCode("medium-" + System.currentTimeMillis());
        ProductOptionValueDescription valDesc = new ProductOptionValueDescription();
        valDesc.setLanguage("en");
        valDesc.setName("Medium");
        optionValue.getDescriptions().add(valDesc);

        HttpEntity<PersistableProductOptionValueEntity> valueEntity = new HttpEntity<>(optionValue, getHeader());
        ResponseEntity<ReadableProductOptionValue> valueResp = testRestTemplate.postForEntity(
                "/api/v1/private/product/option/value?store=" + Constants.DEFAULT_STORE,
                valueEntity, ReadableProductOptionValue.class);
        assertEquals(HttpStatus.CREATED, valueResp.getStatusCode());
        Long optionValueId = valueResp.getBody().getId();

        // 5. Add attribute (option + value) to product
        PersistableProductAttribute attr = new PersistableProductAttribute();
        ProductPropertyOption propOption = new ProductPropertyOption();
        propOption.setId(optionId);
        attr.setOption(propOption);

        PersistableProductOptionValue pov = new PersistableProductOptionValue();
        pov.setId(optionValueId);
        attr.setOptionValue(pov);

        HttpEntity<PersistableProductAttribute> attrEntity = new HttpEntity<>(attr, getHeader());
        ResponseEntity<ReadableProductAttributeEntity> attrResp = testRestTemplate.postForEntity(
                "/api/v1/private/product/" + productId + "/attribute?store=" + Constants.DEFAULT_STORE,
                attrEntity, ReadableProductAttributeEntity.class);
        assertEquals(HttpStatus.CREATED, attrResp.getStatusCode());

        // 6. Call calculateVariant with the matching option/value ids
        ReadableProductVariantValue variantValue = new ReadableProductVariantValue();
        variantValue.setOption(optionId);
        variantValue.setValue(optionValueId);

        ReadableSelectedProductVariant variantRequest = new ReadableSelectedProductVariant();
        variantRequest.setOptions(Arrays.asList(variantValue));

        HttpEntity<ReadableSelectedProductVariant> variantEntity = new HttpEntity<>(variantRequest, getHeader());
        ResponseEntity<ReadableProductPrice> priceResp = testRestTemplate.postForEntity(
                "/api/v2/product/" + productId + "/variation",
                variantEntity, ReadableProductPrice.class);

        assertEquals(HttpStatus.OK, priceResp.getStatusCode());
        assertNotNull(priceResp.getBody());
        assertNotNull(priceResp.getBody().getFinalPrice());
    }

    // -----------------------------------------------------------------------
    // Test 4: valid product but options don't match any attribute → still returns price (base price)
    // -----------------------------------------------------------------------
    @Test
    public void calculateVariant_noMatchingAttribute_returnsBasePrice() throws Exception {

        // Create a product with no attributes
        String sku = "VAR-NOMATCH-" + System.currentTimeMillis();
        PersistableProduct product = super.product(sku);
        product.setPrice(new BigDecimal("30.00"));
        product.setSku(sku);
        product.setAvailable(true);
        product.setQuantity(5);
        ProductSpecification spec = new ProductSpecification();
        spec.setManufacturer(
                com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer.DEFAULT_MANUFACTURER);
        product.setProductSpecifications(spec);

        HttpEntity<PersistableProduct> productEntity = new HttpEntity<>(product, getHeader());
        ResponseEntity<Entity> productResp = testRestTemplate.postForEntity(
                "/api/v1/private/product?store=" + Constants.DEFAULT_STORE, productEntity, Entity.class);
        assertEquals(HttpStatus.CREATED, productResp.getStatusCode());
        Long productId = productResp.getBody().getId();

        // Send options that don't match any attribute (non-existent ids)
        ReadableProductVariantValue variantValue = new ReadableProductVariantValue();
        variantValue.setOption(99999L);
        variantValue.setValue(99999L);

        ReadableSelectedProductVariant variantRequest = new ReadableSelectedProductVariant();
        variantRequest.setOptions(Arrays.asList(variantValue));

        HttpEntity<ReadableSelectedProductVariant> variantEntity = new HttpEntity<>(variantRequest, getHeader());
        ResponseEntity<ReadableProductPrice> priceResp = testRestTemplate.postForEntity(
                "/api/v2/product/" + productId + "/variation",
                variantEntity, ReadableProductPrice.class);

        // No matching attributes → pricingService calculates base price
        assertEquals(HttpStatus.OK, priceResp.getStatusCode());
        assertNotNull(priceResp.getBody());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private PersistableCategory buildCategory(String code) {
        PersistableCategory cat = new PersistableCategory();
        cat.setCode(code);
        cat.setSortOrder(1);
        cat.setVisible(true);
        cat.setDepth(1);
        CategoryDescription desc = new CategoryDescription();
        desc.setLanguage("en");
        desc.setName(code);
        desc.setFriendlyUrl(code);
        List<CategoryDescription> descs = new ArrayList<>();
        descs.add(desc);
        cat.setDescriptions(descs);
        return cat;
    }
}
