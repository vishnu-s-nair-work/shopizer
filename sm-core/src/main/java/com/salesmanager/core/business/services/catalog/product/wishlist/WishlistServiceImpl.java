package com.salesmanager.core.business.services.catalog.product.wishlist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.business.repositories.catalog.product.wishlist.WishlistItemRepository;
import com.salesmanager.core.business.repositories.catalog.product.wishlist.WishlistRepository;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.wishlist.Wishlist;
import com.salesmanager.core.model.catalog.product.wishlist.WishlistItem;
import com.salesmanager.core.model.customer.Customer;

@Service("wishlistService")
@Transactional
public class WishlistServiceImpl implements WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private WishlistItemRepository wishlistItemRepository;

    @Override
    public Wishlist getOrCreateWishlist(Customer customer) {
        return wishlistRepository.findByCustomerIdWithItems(customer.getId())
            .orElseGet(() -> {
                Wishlist w = new Wishlist();
                w.setCustomer(customer);
                return wishlistRepository.save(w);
            });
    }

    @Override
    public void addItem(Customer customer, Product product) {
        Wishlist wishlist = getOrCreateWishlist(customer);
        boolean exists = wishlistItemRepository
            .findByWishlistIdAndProductId(wishlist.getId(), product.getId())
            .isPresent();
        if (!exists) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setProduct(product);
            wishlistItemRepository.save(item);
        }
    }

    @Override
    public void removeItem(Customer customer, Long productId) {
        wishlistRepository.findByCustomerIdWithItems(customer.getId())
            .ifPresent(w -> wishlistItemRepository
                .deleteByWishlistIdAndProductId(w.getId(), productId));
    }

    @Override
    @Transactional(readOnly = true)
    public Wishlist getWishlist(Customer customer) {
        return wishlistRepository.findByCustomerIdWithItems(customer.getId()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMostWishlisted(int limit) {
        return wishlistItemRepository.findMostWishlisted(PageRequest.of(0, limit));
    }
}
