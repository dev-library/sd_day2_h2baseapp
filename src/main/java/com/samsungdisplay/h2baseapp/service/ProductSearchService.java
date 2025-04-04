package com.samsungdisplay.h2baseapp.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.samsungdisplay.h2baseapp.entity.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final EntityManager em;

    public SearchResult searchProducts(
            String keyword,
            Integer minPrice,
            Integer maxPrice,
            Double minRating,
            Double maxRating,
            int page,
            int size
    ) {
        Instant start = Instant.now();

        String jpql = "SELECT p FROM Product p WHERE 1=1";
        String countJpql = "SELECT COUNT(p) FROM Product p WHERE 1=1";

        StringBuilder where = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.isEmpty()) {
            where.append(" AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ");
            where.append(" OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) )");
            params.put("keyword", keyword);
        }
        if (minPrice != null) {
            where.append(" AND p.price >= :minPrice");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            where.append(" AND p.price <= :maxPrice");
            params.put("maxPrice", maxPrice);
        }
        if (minRating != null) {
            where.append(" AND p.rating >= :minRating");
            params.put("minRating", minRating);
        }
        if (maxRating != null) {
            where.append(" AND p.rating <= :maxRating");
            params.put("maxRating", maxRating);
        }

        jpql += where;
        countJpql += where;

        TypedQuery<Product> query = em.createQuery(jpql, Product.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);

        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
            countQuery.setParameter(key, params.get(key));
        }

        int total = countQuery.getSingleResult().intValue();
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<Product> resultList = query.getResultList();
        Page<Product> pageResult = new PageImpl<>(resultList, PageRequest.of(page, size), total);

        Instant end = Instant.now();
        long elapsedMillis = Duration.between(start, end).toMillis();

        return new SearchResult(pageResult, elapsedMillis);
    }

    public record SearchResult(Page<Product> products, long elapsedMillis) {}
}