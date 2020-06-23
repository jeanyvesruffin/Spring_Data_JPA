# Spring_Data_JPA

Ce module nous montre comment sont gérés les données avec Spring à travers Java Persistence API (JPA).

La couche Spring nous permettra de Create Read Update Delete (CRUD) nos données.

De plus nous verrons comment ecrire les query à l'aide d'une syntaxe DSL (Domain Specific Language) utilisant des mots clé (keyword).

Nous irons, enfin, plus loin dans les possibilités de query.

La vérification de la mise en oeuvre de Spring_Data_JPA sera réaliser dans les tests.
Nous utiliserons un serveur H2 en local.

<!-- TOC -->

- [Spring_Data_JPA](#spring_data_jpa)
    - [Rappel javax et definition](#rappel-javax-et-definition)
    - [Installation Spring JPA et test](#installation-spring-jpa-et-test)
    - [Spring Repositories test](#spring-repositories-test)
    - [Spring Jpa fonctionalités](#spring-jpa-fonctionalités)
    - [JpaRepository as proxy](#jparepository-as-proxy)
    - [Query DSL (Domain Specific Language)](#query-dsl-domain-specific-language)
    - [Query basic methode syntaxe](#query-basic-methode-syntaxe)
    - [Query method return types](#query-method-return-types)
    - [Keyword for query And et Or](#keyword-for-query-and-et-or)
    - [Keyword for query Equals, Is et Not](#keyword-for-query-equals-is-et-not)
    - [Keyword for query Like, NotLike](#keyword-for-query-like-notlike)
    - [Keyword for query startingWith, endingWith et containing](#keyword-for-query-startingwith-endingwith-et-containing)
    - [Keyword for query LessThan(Equal) et GreaterThan(Equal)](#keyword-for-query-lessthanequal-et-greaterthanequal)
    - [Keyword for query Before, After et Betweeen](#keyword-for-query-before-after-et-betweeen)
    - [Keyword for query True et False (Dans la theorie car je n'arrive pas a le faire fonctionner)](#keyword-for-query-true-et-false-dans-la-theorie-car-je-narrive-pas-a-le-faire-fonctionner)
    - [Keyword for query IsNull, IsNotNull et NotNull](#keyword-for-query-isnull-isnotnull-et-notnull)
    - [Keyword for query In et NotIn](#keyword-for-query-in-et-notin)
        - [Bug fixe et remarques](#bug-fixe-et-remarques)

<!-- /TOC -->

## Rappel javax et definition

**javax.persistence** classe mère nous permettant de maintenir des données persistantes à travers une instance d' EntityManager, annoté, @PersistenceContext

		@PersistenceContext
		private EntityManager entityManager;

Nous pourrons ainsi écrire nos requetes à travers l'api **JPA** qui maintiendra des données persistantes, avec **JPQL**: Java Persistence Query Language, qui sera la pour nous donner acces au méthode pour la declaration des syntaxe SQL.

Exemple:

	public List<Model> getModelsInPriceRange(BigDecimal lowest, BigDecimal highest) {
			List<Model> mods = entityManager //====>Acces JPA 
					.createQuery("select m from Model m where m.price >= :lowest and m.price <= :highest")//====>Syntaxe JPQL
					.setParameter("lowest", lowest)//====>Parametres requete SQL
					.setParameter("highest", highest).getResultList();//====>Parametres requete SQL
			return mods;
		}



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

Dans l'interface LocationJpaRepository:

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
	
Exemple dans l'interface LocationJpaRepository:

	...
	List<Location>findByStateOrCountry(String value,String value2); 
	List<Location>findByStateAndCountry(String state,String country); 
	...
	
Avec comme test associé dans LocationPersistenceTests

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
	
Et:

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

Exemple dans l'interface LocationJpaRepository:

	...
	List<Location>findByStateIsOrCountryEquals(String value,String value2);
	List<Location>findByStateNot(String state);
	...

Avec comme test associé dans LocationPersistenceTests:

	...
	@Test
	public void testJpaIsEquals() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateIsOrCountryEquals("Utah", "Utah");
		assertNotNull(locationsJpa);
		assertEquals("Utah", locationsJpa.get(43).getState());
	}
	...
	
Console log:

	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state = ? or l1_0.country = ?
	
Et :

	...
	@Test
	public void testJpaNot() throws Exception{
		List<Location> locationsJpa = locationJpaRepository.findByStateNot("Utah");
		assertNotNull(locationsJpa);
		assertNotSame("Utah", locationsJpa.get(43).getState());
	}
	...
	
Console log:

	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state != ?
	
## Keyword for query Like, NotLike

Utilisation: Utile lorsque vous essayez de faire correspondre ou de ne pas faire correspondre une partie de la valeur du filtre de critères.

Exemple Keyword :

	findByStateLike("CA%");
	findByStateNotLike("AI%");
	
Exemple JPQL :
	
	...WHERE a.state like?1
	...WHERE a.state not like?1

Exemple dans l'interface LocationJpaRepository:
	
	...
	List<Location> findByStateLike(String stateName);
	List<Location> findByStateNotLike(String stateName);
	...
	
Avec comme test associé dans LocationPersistenceTests:
	
	...
	@Test
	public void testJpaLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateLike("New%");
		assertEquals(50, locs.size());
	}
	...
	
Console log:

	Hibernate: select l1_0.id, l1_0.country, l1_0.state from Location as l1_0 where l1_0.state like ? escape ?

Et:

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

Exemple dans l'interface LocationJpaRepository:

	...
	List<Location> findByStateStartingWith(String stateName);
	...

Avec comme test associé dans LocationPersistenceTests:

	...
	@Test
	public void testJpaStartingWith() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateStartingWith("New");
		assertEquals(50, locs.size());
	}
	...

Console log:

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

Exemple dans l'interface ModelJpaRepository nous allons ajouter au contrat d'interface la methode:
	
	...
	List<Model> findByPriceGreaterThanEqualAndPriceLessThanEqual(BigDecimal lowest, BigDecimal highest);
	...

On refactorise le code suivant à l'aide du proxy dans ModelRepository:

	public List<Model> getModelsInPriceRange(BigDecimal lowest, BigDecimal highest) {
			@SuppressWarnings("unchecked")
			List<Model> mods = entityManager
					.createQuery("select m from Model m where m.price >= :lowest and m.price <= :highest")
					.setParameter("lowest", lowest)
					.setParameter("highest", highest).getResultList();
			return mods;
		}
		
*Deviens:*


	public List<Model> getModelsInPriceRange(BigDecimal lowest, BigDecimal highest) {
		return modelJpaRepository.findByPriceGreaterThanEqualAndPriceLessThanEqual(lowest, highest);
	}

Avec comme test associé dans ModelPersistenceTests:

	@Test
	public void testGetModelsInPriceRange() throws Exception {
		List<Model> mods = modelRepository.getModelsInPriceRange(BigDecimal.valueOf(1000L), BigDecimal.valueOf(2000L));
		assertEquals(4, mods.size());
	}

Console log:

	select m1_0.id, m1_0.frets, m1_0.manufacturer_id, m1_0.modelType_id, m1_0.name, m1_0.price, m1_0.woodType, m1_0.yearFirstMade from Model as m1_0 where m1_0.price >= ? and m1_0.price <= ?

## Keyword for query Before, After et Betweeen

Utilisation: Utile lorsque vous devez effectuer une comparaison inférieure, supérieure ou d'encadrement avec des types de données date / heure

Exemple Keyword :

	findByFoundedDateBefore(dateObj);
	findByFoundedDateAfter(dateObj);
	findByFoundedDateBetween(startDateObj, endDateObj);
	
Exemple JPQL :
	
	...WHERE a.foundedDate<?1
	...WHERE a.foundedDate>?1
	...WHERE a.foundedDate between?1 and ?2

Exemple dans l'interface ManufacturerRepository nous allons ajouter au contrat d'interface la methode:
	
	...
	List<Manufacturer> findByfoundedDateBefore(Date date);
	...

On refactorise le code suivant à l'aide du proxy dans ManufacturerRepository:

	public List<Manufacturer> getManufacturersFoundedBeforeDate(Date date) {
		@SuppressWarnings("unchecked")
		List<Manufacturer> mans = entityManager
				.createQuery("select m from Manufacturer m where m.foundedDate < :date")
				.setParameter("date", date).getResultList();
		return mans;
	}


*Deviens:*

	public List<Manufacturer> getManufacturersFoundedBeforeDate(Date date) {
			return manufacturerJpaRepository.findByfoundedDateBefore(date);
		}

Console log:
	
	Hibernate: select m1_0.id, m1_0.averageYearlySales, m1_0.foundedDate, m1_0.headquarters_id, m1_0.name from Manufacturer as m1_0 where m1_0.foundedDate < ?
	
## Keyword for query True et False (Dans la theorie car je n'arrive pas a le faire fonctionner)

Utilisation: Utile lors de la comparaison de valeurs booléennes avec vrai ou faux

Exemple Keyword :

	findByActiveTrue();
	findByActiveFalse();
	
Exemple JPQL :
	
	...WHERE a.active=true;
	...WHERE a.active=false;

Nous commencerons par ajouter une colonne à notre base de donnée H2 qui recevra une valeur boolean:

Dans /Spring_Data_JPA/src/main/resources/h2/data.sql

	alter table manufacturer add column active boolean not null;
	
Puis ajouter dans la colonne Manufacturer les key, valeur:


	insert into manufacturer (id, ..., active) values (1,..., true);
	insert into manufacturer (id, ..., active) values (2,..., false);
	
Apres l'execution des test la nouvelles colonne active est créé nous pouvons commenter la ligne

	--alter table manufacturer add column active boolean not null;

Exemple dans l'interface manufacturerJpaRepository


	List<Manufacturer> findByActiveTrue();
	List<Manufacturer> findByActiveFalse();
	
**IDE en erreur**

Nous devons alors crée l'attributs membre à la class Manufacturer Active, générer setter et getter.
Puis ajouter la colonne à la requete

Dans Manufacturer

	...
	@NamedNativeQuery(name = "Manufacturer.getAllThatSellAcoustics", 
	query = "SELECT m.id, m.name, m.foundedDate, m.averageYearlySales, m.location_id as headquarters_id, m.active "
		+ "FROM Manufacturer m "
	...
	private Boolean active;
	...
		public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	...
	
On cree le test à l'aide du proxy dans manufacturerPersistenceTests:

	...
	@Test
	public void testTrueFalse() throws Exception {
		List<Manufacturer> mans = manufacturerJpaRepository.findByActiveTrue();
		assertEquals("Fender Musical Instruments Corporation", mans.get(0).getName());
		mans = manufacturerJpaRepository.findByActiveFalse();
		assertEquals("Gibson Guitar Corporation", mans.get(0).getName());
		
	}

## Keyword for query IsNull, IsNotNull et NotNull

Utilisation : Utilisé pour vérifier si une valeur de critère est nulle ou non nulle.

Exemple Keyword :

	findByStateIsNull();
	findByStateIsNotNull();
	findByStateNotNull();
	
Exemple JPQL :
	
	...WHERE a.state is null
	...WHERE a.state not null
	...WHERE a.state not null

On ajoute dans notre base h2 une valuer null à tester:

	insert into modeltype (id, name) values (8, null)

On ajoute à notre contract d'interface ModelTypeJpaRepository:

	List<ModelType> findByNameIsNull();
	
Avec comme test associé dans  ModelTypePersistenceTests:

	@Test
	public void testForNull() throws Exception{
		List<ModelType> mts = modelTypeJpaRepository.findByNameIsNull();
		assertNull(mts.get(0).getName());
	}

Console log:

	Hibernate: select m1_0.id, m1_0.name from ModelType as m1_0 where m1_0.name is null

## Keyword for query In et NotIn

Utilisation : Utile lorsque vous devez tester si une valeur de colonne fait partie d'une collection ou d'un ensemble de valeurs ou non

Exemple Keyword :

	findByStateIn(Collection<String>states);
	findByStateNotIn(Collection<String>states);

Exemple JPQL :
	
	...WHERE a.state in ? 1
	...WHERE a.state not in ? 1
	


Exemple dans l'interface ModelJpaRepository:

	List<Model> findByModelTypeNameIn(List<String> types);

	
Avec comme test associé dans ModelPersistenceTests

	...
	import static ... assertTrue;
	...
	@Test
	public void testGetModelsByTypes() throws Exception {
		List<String> types = new ArrayList<String>();
		types.add("Electric");
		types.add("Acoustic");
		List<Model> mods = modelJpaRepository.findByModelTypeNameIn(types);
		mods.forEach((model) -> {
			assertTrue(model.getModelType().getName().equals("Electric") || model.getModelType().getName().equals("Acoustic"));
		});
	}







### Bug fixe et remarques

Lors de l'execution d'une succession de test il est parfois necessaire de les jouer un par un avant de tous les rejoind dans ce cas preciser le dans le code

		// clear the persistence context so we don't return the previously cached location object
		// this is a test only thing and normally doesn't need to be done in prod code

Bug non fixe:
Probleme multiple sur la query True/False

	Failed to create query for method public abstract java.util.List com.guitar.db.repository.ManufacturerJpaRepository.findByActiveTrue()! 
	
**ABANDON**
