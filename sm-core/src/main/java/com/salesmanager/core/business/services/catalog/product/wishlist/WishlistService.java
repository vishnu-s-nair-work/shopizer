package com.salesmanager.core.business.services.catalog.product.wishlist;

import java.util.List;

import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.wishlist.Wishlist;
import com.salesmanager.core.model.customer.Customer;

public interface WishlistService {

    Wishlist getOrCreateWishlist(Customer customer);

    /** Idempotent — silently ignores duplicates */
    void addItem(Customer customer, Product product);

    /** No-op if item or wishlist does not exist */
    void removeItem(Customer customer, Long productId);

    Wishlist getWishlist(Customer customer);

    /** Returns [productId, count] pairs ordered by count desc */
    List<Object[]> getMostWishlisted(int limit);
}
