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

	public interface LocationJpaRepository extends JpaRepository<Location, Long>{
	}
	
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









	