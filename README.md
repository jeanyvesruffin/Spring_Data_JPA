# Spring_Data_JPA

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
		// Permet de retourner plus de ligne 
		List<Location> findByStateLike(String stateName);
		// Permet de retourner un compteur de valeurs
		Long countByStateLike(String stateName);
	}	

## Keyword


	