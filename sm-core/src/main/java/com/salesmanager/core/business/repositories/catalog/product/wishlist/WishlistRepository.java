package com.salesmanager.core.business.repositories.catalog.product.wishlist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.salesmanager.core.model.catalog.product.wishlist.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("select w from Wishlist w left join fetch w.items wi left join fetch wi.product where w.customer.id = :customerId")
    Optional<Wishlist> findByCustomerIdWithItems(Long customerId);
}
