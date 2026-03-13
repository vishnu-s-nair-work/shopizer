package com.salesmanager.shop.model.catalog.product.wishlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReadableWishlist implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private List<ReadableWishlistItem> wishlistItems = new ArrayList<>();

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<ReadableWishlistItem> getWishlistItems() { return wishlistItems; }
    public void setWishlistItems(List<ReadableWishlistItem> wishlistItems) { this.wishlistItems = wishlistItems; }
}
