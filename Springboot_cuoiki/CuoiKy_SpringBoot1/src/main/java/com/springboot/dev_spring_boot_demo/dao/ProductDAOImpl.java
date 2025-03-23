package com.springboot.dev_spring_boot_demo.dao;

import com.springboot.dev_spring_boot_demo.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductDAOImpl implements ProductDAO {
    private EntityManager entityManager;

    @Autowired
    public ProductDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Product> findAll() {
        TypedQuery<Product> query = entityManager.createQuery("from Product", Product.class);
        return query.getResultList();
    }

    @Override
    public Product findById(Long id) {
        return entityManager.find(Product.class, id);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return entityManager.merge(product);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    @Override
    public List<Product> findByCategory(String category) {
        TypedQuery<Product> query = entityManager.createQuery(
                "from Product where category = :category", Product.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        TypedQuery<Product> query = entityManager.createQuery(
                "from Product where lower(name) like lower(:keyword) or lower(description) like lower(:keyword)",
                Product.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }
}