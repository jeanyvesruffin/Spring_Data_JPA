package com.guitar.db.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.guitar.db.model.Manufacturer;

@Repository
public class ManufacturerRepository {
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ManufacturerJpaRepository manufacturerJpaRepository;
	
	
	/**
	 * Create sans proxy repository
	 */
	/*public Manufacturer create(Manufacturer man) {
		entityManager.persist(man);
		entityManager.flush();
		return man;
	}*/
	public Manufacturer create(Manufacturer man) {
		return manufacturerJpaRepository.saveAndFlush(man);
	}
	
	
	/**
	 * Update sans proxy repository
	 */
	/*public Manufacturer update(Manufacturer man) {
		man = entityManager.merge(man);
		entityManager.flush();
		return man;
	}*/
	
	public Manufacturer update(Manufacturer man) {
		return manufacturerJpaRepository.saveAndFlush(man);
	}

	/**
	 * Delete sans proxy repository
	 */
	/*public void delete(Manufacturer man) {
		entityManager.remove(man);
		entityManager.flush();
	}*/
	
	public void delete(Manufacturer man) {
		manufacturerJpaRepository.delete(man);
	}

	/**
	 * Find sans proxy repository
	 */
	/*public Manufacturer find(Long id) {
		return entityManager.find(Manufacturer.class, id);
	}*/
	public Optional<Manufacturer> find(Long id) {
		return manufacturerJpaRepository.findById(id);
	}

	/**
	 * Custom finder sans proxy
	 */
	/*public List<Manufacturer> getManufacturersFoundedBeforeDate(Date date) {
		@SuppressWarnings("unchecked")
		List<Manufacturer> mans = entityManager
				.createQuery("select m from Manufacturer m where m.foundedDate < :date")
				.setParameter("date", date).getResultList();
		return mans;
	}*/
	public List<Manufacturer> getManufacturersFoundedBeforeDate(Date date) {
		return manufacturerJpaRepository.findByfoundedDateBefore(date);
	}

	/**
	 * Custom finder
	 */
	public Manufacturer getManufacturerByName(String name) {
		Manufacturer man = (Manufacturer)entityManager
				.createQuery("select m from Manufacturer m where m.name like :name")
				.setParameter("name", name + "%").getSingleResult();
		return man;
	}

	/**
	 * Native Query finder
	 */
	public List<Manufacturer> getManufacturersThatSellModelsOfType(String modelType) {
		@SuppressWarnings("unchecked")
		List<Manufacturer> mans = entityManager
				.createNamedQuery("Manufacturer.getAllThatSellAcoustics")
				.setParameter(1, modelType).getResultList();
		return mans;
	}
}
