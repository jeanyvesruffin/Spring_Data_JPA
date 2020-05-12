# Spring_Data_JPA

Ce module nous montre comment est mise en oeuvre Spring_Data_JPA.

Java Persistence API, nous permet de maintenir les données persistantes. 
La couche Spring nous permet de Create Read Update Delete (CRUD) ces données.
De plus nous verrons comment ecrire les query à l'aide d'une syntaxe DSL (Domain Specific Language) utilisant des mots clé (keyword).
Enfin nous irons plus loin dans les query et ces possibilités.


## Installation Spring JPA et test

1 - Ajouter la dependence Spring-data-jpa

	<dependency>
	    <groupId>org.springframework.data</groupId>
	    <artifactId>spring-data-jpa</artifactId>
	    <version>2.2.7.RELEASE</version>
	</dependency>
	
2 - Ajouter dans le fichier de configuration du dossier META-INF le bean de configuration JPA

	...
	Dans l'entête
	...
	xmlns:jpa="http://www.springframework.org/schema/data/jpa"
	...
	http://www.springframework.org/schema/data/jpa http://www.springframework.org/schema/data/jpa/spring-jpa.xsd"
	...
	<jpa:repositories base-package="com.guitar.db.repository"/>
	
3 - Creation des interfaces JpaRepository (Location, Manufacturer, Model et ModelType)

Exemple LocationJpaRepository:
	
	...
	@Repository
	public interface LocationJpaRepository extends JpaRepository<Location, Long>{
	}
	...
	
4 - Creation des tests des l'interfaces (LocationPersistenceTests, ManufacturerPersistenceTests, ModelPersistenceTests et ModelTypePersistenceTests)

Exemple LocationPersistenceTests

	...
	
	import static org.junit.Assert.assertNotNull;	
	...
	@Autowired
	private LocationJpaRepository locationJpaRepository;
	...
	@Test
	public void testJpaFind()throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findAll();
		assertNotNull(locationsJpa);
	}
	...
	
## Spring Repositories test
Nous allons tester les methodes de CRUD disponibles à travers CrudRepository.

Exemple sur modelTypePersistanceTests:

	...
	@Test
	@Transactional
	public void testJpaSaveAndGetAndDelete() throws Exception {
		ModelType mt = new ModelType();
		mt.setName("Test Model Type");
		mt = modelTypeJpaRepository.save(mt);
		entityManager.clear();
		Optional<ModelType> otherModelType = modelTypeJpaRepository.findById(mt.getId());
		if(otherModelType.isPresent()) {
			assertEquals("Test Model Type", otherModelType.get().getName());
			modelTypeJpaRepository.deleteById(mt.getId());
		}
	}
	@Test
	public void testJpaFind() throws Exception{
		ModelType mt = modelTypeJpaRepository.findById(1L).orElse(null);
		assertEquals("Dreadnought Acoustic", mt.getName());
	}
	...
	
## Spring Jpa fonctionalités

- Query DSL
- CRUD operations
- Paging and sorting
- Helpers (count (), exists(Long id), flush(), deleteInBatch(Iterable entites))


## JpaRepository as proxy

Exemple sur ManufacturerJpaRepository et ModelJpaRepository

1 - On creer les interfaces qui extends JpaRepository<T,ID>

2 - On inject ces interface dans les class @Repository (cad, ManufacturerRepository et ModelRepository)

Exemple dans ManufacturerRepository.

	...
	@Autowired
	private ManufacturerJpaRepository manufacturerJpaRepository
	...
	
3 - Appel le proxy dans les methode du CRUD

Exemple dans ManufacturerRepository.

	...
	public Manufacturer create(Manufacturer man) {
		return manufacturerJpaRepository.saveAndFlush(man);
	}
	...
	public Manufacturer update(Manufacturer man) {
		return manufacturerJpaRepository.saveAndFlush(man);
	}
	...
	public void delete(Manufacturer man) {
		manufacturerJpaRepository.delete(man);
	}
	...
	public Optional<Manufacturer> find(Long id) {
		return manufacturerJpaRepository.findById(id);
	}
	...
	
## Query DSL (Domain Specific Language)

1 - Definition des contrats d'interfaces JpaRepository

Exemple dans LocationJpaRepository:

	...
	List<Location> findByStateLike(String stateName);
	...

2 - Modifier les tests. Remarquons que la méthode findByStateLike est possible car il existe un attribut membre de la classe Location "state". Si par exemple nous indiquions states avec "s" alors nous aurions l'exception levée: No property states found type Location!


	...
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
	...
	@Test
	public void testJpaProxyFindWithLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateLike("New%");
		assertEquals(50, locs.size());
	}	
	...
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
	...
	
## Query basic methode syntaxe

L'analyseur de requêtes correspondra aux éléments suivants find...By, query...By, read...By, count...By, get...By.

Les critères utilisent des noms d'attribut d'entité JPA.

Plusieurs critères peuvent être combinés avec "AND", "OR"...

## Query method return types

Exemple:

	public interface LocationJpaRepository extends JpaRepository<Location, Long>{
		// Permet de retourner une ligne contenant Location
		Location findFirstByState(String stateName);
		// Permet de retourner une liste de ligne 
		List<Location> findByStateLike(String stateName);
		// Permet de retourner un compteur de valeurs
		Long countByStateLike(String stateName);
	}	

## Keyword for query And et Or

Utilisation : Combine plusieurs filtres de requête à critères en utilisant une condition And ou Or.

Exemple Keyword :

	findByStateAndCountry("CA","USA");
	findByStateOrState("CA","AZ");

Exemple JPQL :
	
	...WHERE a.state=?1 AND a.country=?2
	...WHERE a.state=?1 OR a.state=?2
	
Exemple dans le fichier LocationJpaRepository:

	...
	List<Location>findByStateOrCountry(String value,String value2); 
	List<Location>findByStateAndCountry(String state,String country); 
	...
	
Avec comme test associé

	...
	@Test
	public void testJpaAnd() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateAndCountry("Utah", "United States");
		assertNotNull(locationsJpa);
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	...
	
	Console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state = ? and l1_0.country = ?
	
	...
	@Test
	public void testJpaOr() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateOrCountry("Utah", "Utah");
		assertNotNull(locationsJpa);
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	...
	
	Console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state = ? or l1_0.country = ?
	
## Keyword for query Equals, Is et Not

Utilisation : La valeur par défaut pour comparer des criteres avec la valeur du filtre est '='. Utiliser 'Not' pour comparer des valeurs differentes.

Exemple Keyword :

	findByState("CA");
	findByStateIs("CA");
	findByStateEquals("CA");
	findByStateNot("CA");
	
Exemple JPQL :
	
	...WHERE a.state=?1
	...WHERE a.state=?1
	...WHERE a.state=?1
	...WHERE a.state<>?1

Exemple dans le fichier LocationJpaRepository:

	...
	List<Location>findByStateIsOrCountryEquals(String value,String value2);
	List<Location>findByStateNot(String state);
	...

Avec comme test associé:

	...
	@Test
	public void testJpaIsEquals() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateIsOrCountryEquals("Utah", "Utah");
		assertNotNull(locationsJpa);
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	...
	
	console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state = ? or l1_0.country = ?
	
	...
	@Test
	public void testJpaNot() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateNot("Utah");
		assertNotNull(locationsJpa);
		assertNotSame("Utah", locationsJpa.get(43).getState());
	}
	...
	
	console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state != ?
	
## Keyword for query Like, NotLike

Utilisation: Utile lorsque vous essayez de faire correspondre ou de ne pas faire correspondre une partie de la valeur du filtre de critères.

Exemple Keyword :

	findByStateLike("CA%");
	findByStateNotLike("AI%");
	
Exemple JPQL :
	
	...WHERE a.state like?1
	...WHERE a.state not like?1

Exemple dans le fichier LocationJpaRepository:
	
	...
	List<Location> findByStateLike(String stateName);
	List<Location> findByStateNotLike(String stateName);
	...
	
Avec comme test associé:
	
	...
	@Test
	public void testJpaLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateLike("New%");
		assertEquals(50, locs.size());
	}
	...
	
	console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state like ? escape ?
	
	...
	@Test
	public void testJpaNotLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateNotLike("New%");
		assertEquals(5, locs.size());
	}
	...
	
	console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state not like ? escape ?
	
## Keyword for query startingWith, endingWith et containing

Utilisation: Similaire au mot clé "Like" sauf que le % est automatiquement ajouté à la valeur du filtre.

Exemple Keyword :

	findByStateStartingWith("AI"); //AI%
	findByStateEndingWith("ia"); //%ia
	findByStateContaining("in"); //%in%
	
Exemple JPQL :
	
	...WHERE a.state like?1
	...WHERE a.state like?1
	...WHERE a.state like?1

Exemple dans le fichier LocationJpaRepository:

	...
	List<Location> findByStateStartingWith(String stateName);
	...

Avec comme test associé:

	...
	@Test
	public void testJpaStartingWith() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateStartingWith("New");
		assertEquals(50, locs.size());
	}
	...

	console log:
	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state like ? escape ?

## Keyword for query LessThan(Equal) et GreaterThan(Equal)

Utilisation: Utile lorsque vous devez effectuer une comparaison avec des types de données numériques <, <=,> ou> = 

Exemple Keyword :

	findByPriceLessThan(20);
	findByPriceLessThanEqual(20);
	findByPriceGreaterThan(20);
	findByPriceGreaterThanEqual(20);

	
Exemple JPQL :
	
	...WHERE a.price <?1
	...WHERE a.price <=?1
	...WHERE a.price >?1
	...WHERE a.price >=?1


**ENCORE PLUS FORT**: Utile lorsque vous devez effectuer un encadrement

Exemple Keyword :

	findByPriceGreaterThanAndLessThan(10,20)

Exemple dans le fichier LocationJpaRepository:
	
	...
	List<Location> findByStateLike(String stateName);
	List<Location> findByStateNotLike(String stateName);
	...
	
Avec comme test associé:

	