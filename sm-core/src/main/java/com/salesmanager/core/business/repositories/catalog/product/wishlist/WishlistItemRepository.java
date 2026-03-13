package com.salesmanager.core.business.repositories.catalog.product.wishlist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.salesmanager.core.model.catalog.product.wishlist.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    @Modifying
    @Query("delete from WishlistItem wi where wi.wishlist.id = :wishlistId and wi.product.id = :productId")
    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);

    @Query("select wi.product.id, count(wi) from WishlistItem wi group by wi.product.id order by count(wi) desc")
    List<Object[]> findMostWishlisted(Pageable pageable);
}
