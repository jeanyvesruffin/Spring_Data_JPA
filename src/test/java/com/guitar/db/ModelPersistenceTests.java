package com.guitar.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.guitar.db.model.Model;
import com.guitar.db.repository.ModelJpaRepository;
import com.guitar.db.repository.ModelRepository;

@ContextConfiguration(locations={"classpath:com/guitar/db/applicationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ModelPersistenceTests {
	@Autowired
	private ModelRepository modelRepository;
	
	@Autowired
	private ModelJpaRepository modelJpaRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void testJpafind() throws Exception {
		List<Model> modelsJpa = modelJpaRepository.findAll();
		assertNotNull(modelsJpa);
	}
	
	@Test
	@Transactional
	public void testSaveAndGetAndDelete() throws Exception {
		Model m = new Model();
		m.setFrets(10);
		m.setName("Test Model");
		m.setPrice(BigDecimal.valueOf(55L));
		m.setWoodType("Maple");
		m.setYearFirstMade(new Date());
		m = modelRepository.create(m);
		
		// clear the persistence context so we don't return the previously cached location object
		// this is a test only thing and normally doesn't need to be done in prod code
		entityManager.clear();

		Optional<Model> otherModel = modelRepository.find(m.getId());
		if(otherModel.isPresent()) {
			assertEquals("Test Model", otherModel.get().getName());
			assertEquals(10, otherModel.get().getFrets());
			
			//delete BC location now
			modelRepository.delete(otherModel.get());			
		}
	}
	
	@Test
	@Transactional
	public void testJpaSaveAndGetAndDelete() throws Exception {
		Model m = new Model();
		m.setFrets(10);
		m.setName("Test Model");
		m.setPrice(BigDecimal.valueOf(55L));
		m.setWoodType("Maple");
		m.setYearFirstMade(new Date());
		m = modelJpaRepository.save(m);
		entityManager.clear();
		Optional<Model> otherModel = modelJpaRepository.findById(m.getId());
		if(otherModel.isPresent()) {
			assertEquals("Test Model", otherModel.get().getName());
			assertEquals(10, otherModel.get().getFrets());
			modelJpaRepository.deleteById(otherModel.get().getId());			
		}
	}

	@Test
	public void testGetModelsInPriceRange() throws Exception {
		List<Model> mods = modelRepository.getModelsInPriceRange(BigDecimal.valueOf(1000L), BigDecimal.valueOf(2000L));
		assertEquals(9, mods.size());
	}

	@Test
	public void testGetModelsByPriceRangeAndWoodType() throws Exception {
		List<Model> mods = modelRepository.getModelsByPriceRangeAndWoodType(BigDecimal.valueOf(1000L), BigDecimal.valueOf(2000L), "Maple");
		assertEquals(3, mods.size());
	}

	@Test
	public void testGetModelsByType() throws Exception {
		List<Model> mods = modelRepository.getModelsByType("Electric");
		assertEquals(4, mods.size());
	}
	
//	@Test
//	public void testGetModelsByTypes() throws Exception {
//		List<String> types = new ArrayList<String>();
//		types.add("Electric");
//		types.add("Acoustic");
//		List<Model> mods = modelJpaRepository.findByModelTypeNameIn(types);
//		mods.forEach((model) -> {
//			assertTrue(model.getModelType().getName().equals("Electric") || model.getModelType().getName().equals("Acoustic"));
//		});
//	}
}
