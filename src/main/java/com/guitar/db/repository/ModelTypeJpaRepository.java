package com.guitar.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guitar.db.model.ModelType;

public interface ModelTypeJpaRepository extends JpaRepository<ModelType, Long> {

}
