package com.guitar.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.guitar.db.model.Location;

@Repository
public interface LocationJpaRepository extends JpaRepository<Location, Long>{
	//Exemple Query keyword or
	List<Location>findByStateOrCountry(String value,String value2); 
	List<Location>findByStateAndCountry(String state,String country); 
	//Exemple Query keyword Equals, Is, Not combinés
	List<Location>findByStateIsOrCountryEquals(String value,String value2);
	List<Location>findByStateNot(String state);
	//Exemple Query keyword like et not like
	List<Location> findByStateLike(String stateName);
	List<Location> findByStateNotLike(String stateName);
	//Exemple Query keyword Starting , end, containe
	List<Location> findByStateStartingWith(String stateName);

	
	
}
