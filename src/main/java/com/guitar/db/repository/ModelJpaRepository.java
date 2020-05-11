package com.guitar.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guitar.db.model.Model;

public interface ModelJpaRepository extends JpaRepository<Model, Long> {

}
