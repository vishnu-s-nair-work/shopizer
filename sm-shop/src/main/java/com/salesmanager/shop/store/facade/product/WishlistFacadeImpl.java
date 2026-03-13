package com.salesmanager.shop.store.facade.product;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.wishlist.WishlistService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;
import com.salesmanager.core.model.catalog.product.wishlist.Wishlist;
import com.salesmanager.core.model.catalog.product.wishlist.WishlistItem;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.ReadableProductPrice;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlistItem;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlistStats;
import com.salesmanager.shop.populator.catalog.ReadableFinalPricePopulator;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.controller.product.facade.WishlistFacade;
import com.salesmanager.shop.utils.ImageFilePath;

@Service("wishlistFacade")
public class WishlistFacadeImpl implements WishlistFacade {

    @Inject private WishlistService wishlistService;
    @Inject private ProductService productService;
    @Inject private PricingService pricingService;
    @Inject private CustomerFacade customerFacade;
    @Inject @Qualifier("img") private ImageFilePath imageUtils;

    @Override
    public ReadableWishlist getWishlist(String customerName, MerchantStore store, Language language) {
        Customer customer = getCustomer(customerName, store);
        ReadableWishlist dto = new ReadableWishlist();
        dto.setCustomerId(customer.getId());

        Wishlist wishlist = wishlistService.getWishlist(customer);
        if (wishlist == null) return dto;

        List<ReadableWishlistItem> items = new ArrayList<>();
        for (WishlistItem item : wishlist.getItems()) {
            Product p = item.getProduct();
            if (p == null || !p.isAvailable()) continue;
            items.add(toReadableItem(p, store, language));
        }
        dto.setWishlistItems(items);
        return dto;
    }

    @Override
    public void addToWishlist(String customerName, Long productId, MerchantStore store) {
        Customer customer = getCustomer(customerName, store);
        Product product = productService.getProductWithAttributes(productId);
        if (product == null) {
            throw new ResourceNotFoundException("Product [" + productId + "] not found");
        }
        wishlistService.addItem(customer, product);
    }

    @Override
    public void removeFromWishlist(String customerName, Long productId, MerchantStore store) {
        Customer customer = getCustomer(customerName, store);
        wishlistService.removeItem(customer, productId);
    }

    @Override
    public List<ReadableWishlistStats> getMostWishlisted(int limit, MerchantStore store, Language language) {
        List<Object[]> rows = wishlistService.getMostWishlisted(limit);
        List<ReadableWishlistStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long pid = (Long) row[0];
            Long count = (Long) row[1];
            Product p = productService.getById(pid);
            ReadableWishlistStats stats = new ReadableWishlistStats();
            stats.setProductId(pid);
            stats.setWishlistCount(count);
            if (p != null) {
                stats.setProductName(getProductName(p, language));
            }
            result.add(stats);
        }
        return result;
    }

    private Customer getCustomer(String userName, MerchantStore store) {
        Customer customer = customerFacade.getCustomerByUserName(userName, store);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer [" + userName + "] not found");
        }
        return customer;
    }

    private ReadableWishlistItem toReadableItem(Product p, MerchantStore store, Language language) {
        ReadableWishlistItem item = new ReadableWishlistItem();
        item.setProductId(p.getId());
        item.setSku(p.getSku());
        item.setProductName(getProductName(p, language));
        try {
            FinalPrice fp = pricingService.calculateProductPrice(p);
            ReadableProductPrice price = new ReadableProductPrice();
            ReadableFinalPricePopulator populator = new ReadableFinalPricePopulator();
            populator.setPricingService(pricingService);
            populator.populate(fp, price, store, language);
            item.setPrice(price);
        } catch (Exception ignored) {}
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            ProductImage img = p.getImages().iterator().next();
            item.setImage(imageUtils.buildProductImageUtils(store, p.getSku(), img.getProductImage()));
        }
        return item;
    }

    private String getProductName(Product p, Language language) {
        if (p.getDescriptions() == null) return "";
        return p.getDescriptions().stream()
            .filter(d -> d.getLanguage() != null && d.getLanguage().getCode().equals(language.getCode()))
            .map(ProductDescription::getName)
            .findFirst()
            .orElse(p.getDescriptions().stream().map(ProductDescription::getName).findFirst().orElse(""));
    }
}
