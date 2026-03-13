package com.salesmanager.shop.store.controller.product.facade;

import java.util.List;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlistStats;

public interface WishlistFacade {

    ReadableWishlist getWishlist(String customerName, MerchantStore store, Language language);

    void addToWishlist(String customerName, Long productId, MerchantStore store);

    void removeFromWishlist(String customerName, Long productId, MerchantStore store);

    List<ReadableWishlistStats> getMostWishlisted(int limit, MerchantStore store, Language language);
}
