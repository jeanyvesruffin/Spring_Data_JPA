package com.guitar.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;


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

import com.guitar.db.model.Location;
import com.guitar.db.repository.LocationJpaRepository;
import com.guitar.db.repository.LocationRepository;

@ContextConfiguration(locations={"classpath:com/guitar/db/applicationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class LocationPersistenceTests {
	@Autowired
	private LocationRepository locationRepository;
	
	@Autowired
	private LocationJpaRepository locationJpaRepository;

	
	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void testJpaFind() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findAll();
		// Test affirmant que l'objet n'est pas null
		assertNotNull(locationsJpa);
	}
	
	@Test
	@Transactional
	public void testSaveAndGetAndDelete() throws Exception {
		Location location = new Location();
		location.setCountry("Canada");
		location.setState("British Columbia");
		location = locationRepository.create(location);
		
		// clear the persistence context so we don't return the previously cached location object
		// this is a test only thing and normally doesn't need to be done in prod code
		entityManager.clear();

		Location otherLocation = locationRepository.find(location.getId());
		assertEquals("Canada", otherLocation.getCountry());
		assertEquals("British Columbia", otherLocation.getState());
		
		//delete BC location now
		locationRepository.delete(otherLocation);
	}
	
	@Test
	@Transactional
	public void testJpaSaveAndGetAndDelete() throws Exception {
		Location location = new Location();
		location.setCountry("Canada");
		location.setState("British Columbia");
		location = locationJpaRepository.save(location);
		entityManager.clear();
		Optional<Location> otherLocation = locationJpaRepository.findById(location.getId());
		if(otherLocation.isPresent()) {
			assertEquals("Canada", otherLocation.get().getCountry());
			assertEquals("British Columbia", otherLocation.get().getState());
			locationJpaRepository.deleteById(otherLocation.get().getId());			
		}
	}
	@Test
	@Transactional
	public void testJpaProxySaveAndGetAndDelete() throws Exception {
		Location location = new Location();
		location.setCountry("Canada");
		location.setState("British Columbia");
		location = locationJpaRepository.saveAndFlush(location);
		entityManager.clear();
		Optional<Location> otherLocation = locationJpaRepository.findById(location.getId());
		if(otherLocation.isPresent()) {
			assertEquals("Canada", otherLocation.get().getCountry());
			assertEquals("British Columbia", otherLocation.get().getState());
			locationJpaRepository.deleteById(otherLocation.get().getId());			
		}
	}

	@Test
	public void testFindWithLike() throws Exception {
		List<Location> locs = locationRepository.getLocationByStateName("New");
		assertEquals(4, locs.size());
	}
	
	@Test
	public void testJpaProxyFindWithLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateLike("New%");
		assertEquals(50, locs.size());
	}

	@Test
	@Transactional  //note this is needed because we will get a lazy load exception unless we are in a tx
	public void testFindWithChildren() throws Exception {
		Location arizona = locationRepository.find(3L);
		assertEquals("United States", arizona.getCountry());
		assertEquals("Arizona", arizona.getState());
		assertEquals(1, arizona.getManufacturers().size());
		assertEquals("Fender Musical Instruments Corporation", arizona.getManufacturers().get(0).getName());
	}
	@Test
	@Transactional
	public void testJpaProxyFindWithChildren() throws Exception {
		Optional<Location> arizona = locationJpaRepository.findById(3L);
		if(arizona.isPresent()) {
			assertEquals("United States", arizona.get().getCountry());
			assertEquals("Arizona", arizona.get().getState());			
			assertEquals(1, arizona.get().getManufacturers().size());
			assertEquals("Fender Musical Instruments Corporation", arizona.get().getManufacturers().get(0).getName());
		}
	}
	
	// Test keyword query AND
	@Test
	public void testJpaAnd() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateAndCountry("Utah", "United States");
		// Test affirmant que l'objet n'est pas null
		assertNotNull(locationsJpa);
		// Test affirmant que l'objet state est Utah et country est United state . 1 parametre => valeur attendue et 2 parametre valeur à tester
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	// Test keyword query OR
	@Test
	public void testJpaOr() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateOrCountry("Utah", "Utah");
		// Test affirmant que l'objet n'est pas null
		assertNotNull(locationsJpa);
		// Test affirmant que l'objet est égale à Utah. 1 parametre => valeur attendue et 2 parametre valeur à tester
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	// Test keyword query Is Or Equals
	@Test
	public void testJpaIsEquals() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateIsOrCountryEquals("Utah", "Utah");
		// Test affirmant que l'objet n'est pas null
		assertNotNull(locationsJpa);
		// Test affirmant que l'objet est égale à Utah. 1 parametre => valeur attendue et 2 parametre valeur à tester
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	// Test keyword query Not
	@Test
	public void testJpaNot() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateNot("Utah");
		// Test affirmant que l'objet n'est pas null
		assertNotNull(locationsJpa);
		// Test affirmant que l'objet est égale à Utah. 1 parametre => valeur attendue et 2 parametre valeur à tester
		assertNotSame("Utah", locationsJpa.get(43).getState());
	}
	
	// Test keyword like et not like
	@Test
	public void testJpaLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateLike("New%");
		assertEquals(50, locs.size());
	}
	@Test
	public void testJpaNotLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateNotLike("New%");
		assertEquals(50, locs.size());
	}
	// Test keyword starttingWith
	@Test
	public void testJpaStartingWith() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateStartingWith("New");
		assertEquals(50, locs.size());
	}
	
}
