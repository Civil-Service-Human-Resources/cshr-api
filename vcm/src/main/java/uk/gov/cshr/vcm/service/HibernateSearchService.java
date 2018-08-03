package uk.gov.cshr.vcm.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.exception.EmptyQueryException;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyEligibility;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.repository.DepartmentRepository;

@Service
public class HibernateSearchService {

    private static final Logger log = LoggerFactory.getLogger(HibernateSearchService.class);
    private static final double MILES_KM_MULTIPLIER = 1.609343502101154;

    @Autowired
    private final EntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    public HibernateSearchService(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    public void initializeHibernateSearch() throws InterruptedException {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.createIndexer().startAndWait();
    }

    public void purge() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.purgeAll(VacancyLocation.class);
        fullTextEntityManager.purgeAll(Vacancy.class);
    }

    @Transactional
    public Page<Vacancy> search(SearchParameters searchParameters, Pageable pageable)
            throws LocationServiceException, IOException {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(VacancyLocation.class).get();

        BooleanJunction combinedQuery;

        try {

            Query searchtermQuery = getSearchTermQuery(searchParameters, qb);
            Query salaryQuery = getSalaryQuery(searchParameters, qb);
            Query closedQuery = getClosedVacanciesQuery(qb);
            Query openQuery = getOpenQuery(qb, searchParameters.getVacancyEligibility());
            Query departmentQuery = getDepartmentQuery(searchParameters, qb);
            Query locationQuery = getLocationQuery(searchParameters, qb);

            Query contractTypeQuery = getFieldQuery("vacancy.contractTypes",
                    getContractTypes(searchParameters), qb);

            Query workingPatternsQuery = getFieldQuery("vacancy.workingPatterns",
                    getWorkingPatterns(searchParameters), qb);

            Query activeQuery = getActiveQuery(qb);

            combinedQuery = qb.bool()
                    .must(closedQuery).not()
                    .must(openQuery)
                    .must(locationQuery)
                    .must(salaryQuery)
                    .must(departmentQuery)
                    .must(searchtermQuery)
                    .must(contractTypeQuery)
                    .must(workingPatternsQuery)
                    .must(activeQuery);
        } catch (EmptyQueryException e) {
            log.error(e.getMessage(), e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        log.debug("luceneQuery=" + combinedQuery.createQuery().toString());

        // projection means we only return IDs from search, not hitting the database
        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(
                combinedQuery.createQuery(), VacancyLocation.class)
                .setProjection("vacancyid");

        List<Object[]> vacancyIDs = jpaQuery.getResultList();
        LinkedHashSet<Long> uniqueVacancyIDs = new LinkedHashSet<>();

        // remove any duplicate results
        vacancyIDs.forEach((vacancyID) -> {
            uniqueVacancyIDs.add(Long.valueOf(vacancyID[0].toString()));
        });

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();

        List idList;

        // limit resultset to page size
        if (uniqueVacancyIDs.size() < pageSize) {
            idList = Arrays.asList(uniqueVacancyIDs.toArray());
        } else {

            int max = pageNumber * pageSize + pageSize;

            if (max > uniqueVacancyIDs.size()) {
                max = uniqueVacancyIDs.size();
            }

            idList = Arrays.asList(uniqueVacancyIDs.toArray()).subList(pageNumber * pageSize, max);
        }

        if (idList.isEmpty()) {
            return new PageImpl<>(new ArrayList<>());
        } else {
            List<Vacancy> vacancies = entityManager
                    .createQuery("SELECT v FROM Vacancy v WHERE v.id IN (:ids)")
                    .setParameter("ids", idList)
                    .getResultList();

            // rearrange the db results to match the lucene relevance order
            vacancies.sort(Comparator.comparingLong(item -> idList.indexOf(item.getId())));

            PageImpl<Vacancy> page = new PageImpl<>(vacancies, pageable, uniqueVacancyIDs.size());
            return page;
        }
    }

    private Query getLocationQuery(SearchParameters searchParameters, QueryBuilder qb) {

        boolean includeOverseasJobs = BooleanUtils.isTrue(
                searchParameters.getVacancySearchParameters().getOverseasJob());

        if (searchParameters.getCoordinates() != null) {

            double kms = searchParameters.getVacancySearchParameters().getLocation().getRadius() * MILES_KM_MULTIPLIER;

            BooleanJunction locationQuery = qb.bool();

            Query spatialQuery = qb
                    .spatial().boostedTo(01.f)
                    .within(kms, Unit.KM)
                    .ofLatitude(searchParameters.getCoordinates().getLatitude())
                    .andLongitude(searchParameters.getCoordinates().getLongitude())
                    .createQuery();

            Query regionQuery = qb.phrase()
                    .onField("vacancy.regions")
                    .sentence(searchParameters.getCoordinates().getRegion())
                    .createQuery();

            Query regionSpatialQuery = qb.bool()
                    .should(spatialQuery)
                    .should(regionQuery).createQuery();

            Query overseasQuery = qb.keyword().onField("vacancy.overseasJob")
                    .matching("true").createQuery();

            if (!includeOverseasJobs) {
                locationQuery.must(regionSpatialQuery);
            } else {
                Query regionSpatialOverseas = qb.bool().should(regionSpatialQuery).should(overseasQuery).createQuery();
                locationQuery.must(regionSpatialOverseas);
            }

            return locationQuery.createQuery();
        } else if (!includeOverseasJobs) {

            return qb.keyword().onField("vacancy.overseasJob")
                    .matching("false").createQuery();
        } else {
            return qb.all().createQuery();
        }
    }

    private Query getSearchTermQuery(SearchParameters searchParameters, QueryBuilder qb) {

        String searchTerm = searchParameters.getVacancySearchParameters().getKeyword();

        if (searchTerm != null && !searchTerm.isEmpty()) {

            searchTerm = searchTerm.toLowerCase();

            searchTerm = searchTerm.toLowerCase();

            Query titleFuzzyQuery = qb.keyword().fuzzy()
                    .withEditDistanceUpTo(1)
                    .withPrefixLength(1)
                    .onField("vacancy.title")
                    .boostedTo(1.5f)
                    .matching(searchTerm)
                    .createQuery();

            Query titleQuery = qb.keyword()
                    .onField("vacancy.titleOriginal")
                    .boostedTo(2f)
                    .matching(searchTerm)
                    .createQuery();

            Query titlePhraseQuery = qb.phrase()
                    .onField("vacancy.titleOriginal")
                    .boostedTo(2f)
                    .ignoreAnalyzer()
                    .sentence(searchTerm)
                    .createQuery();

            Query descriptiopnPhraseQuery = qb.phrase()
                    .onField("vacancy.description")
                    .ignoreAnalyzer()
                    .sentence(searchTerm)
                    .createQuery();


            Query descriptionQuery = qb.keyword().fuzzy()
                    .withEditDistanceUpTo(1)
                    .boostedTo(1.2f)
                    .withPrefixLength(1)
                    .onField("vacancy.description")
                    .matching(searchTerm)
                    .createQuery();

            Query wildcardQuery = qb.keyword()
                    .wildcard()
                    .onFields("vacancy.title", "vacancy.description")
                    .matching(searchTerm.replaceAll(" ", "* ") + "*")
                    .createQuery();

            Query keywordQuery = qb.bool()
                    .should(titleFuzzyQuery)
                    .should(descriptionQuery)
                    .should(titleQuery)
                    .should(titlePhraseQuery)
                    .should(descriptiopnPhraseQuery)
                    .should(wildcardQuery)
                    .createQuery();

            return keywordQuery;
        } else {
            return qb.all().createQuery();
        }
    }

    private Query getSalaryQuery(SearchParameters searchParameters, QueryBuilder qb) {

        Integer minSalary = searchParameters.getVacancySearchParameters().getMinSalary();
        if (minSalary == null) {
            minSalary = 0;
        }
        Integer maxSalary = searchParameters.getVacancySearchParameters().getMaxSalary();
        if (maxSalary == null) {
            maxSalary = Integer.MAX_VALUE;
        }

        Query minQuery = qb
                .range()
                .onField("vacancy.salaryMax")
                .above(minSalary)
                .createQuery();

        Query maxQuery = qb
                .range()
                .onField("vacancy.salaryMin")
                .below(maxSalary)
                .createQuery();

        Query salaryQuery = qb
                .bool()
                .must(minQuery)
                .must(maxQuery).boostedTo(0.1f)
                .createQuery();

        return salaryQuery;
    }

    private Query getOpenQuery(QueryBuilder qb, VacancyEligibility vacancyEligibility) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        Query publicQuery = qb
                .range()
                .onField("vacancy.publicOpeningDate")
                .ignoreFieldBridge()
                .below(sdf.format(new Date()))
                .excludeLimit()
                .createQuery();

        if (vacancyEligibility.equals(VacancyEligibility.PUBLIC)) {


            return qb.bool().must(publicQuery).createQuery();
        } else {

            Query internalQuery = qb
                    .range()
                    .onField("vacancy.internalOpeningDate")
                    .ignoreFieldBridge()
                    .below(sdf.format(new Date()))
                    .excludeLimit()
                    .createQuery();

            Department department = departmentRepository.findById(vacancyEligibility.getDepartmentID()).get();

            // include children
            List<Department> eligibleDepartments = departmentRepository.findByParent(department);

            // include children of parent
            if (department.getParent() != null) {
                eligibleDepartments.addAll(departmentRepository.findByParent(department.getParent()));
            }

            // include the department itself
            eligibleDepartments.add(department);

            // add the parent if there is one
            if (department.getParent() != null) {
                eligibleDepartments.add(department.getParent());
            }

            StringBuilder stringBuilder = new StringBuilder();
            eligibleDepartments.forEach((department1) -> {
                stringBuilder.append(department1.getId()).append(" ");
            });

            Query departmentQuery = qb
                    .keyword()
                    .onField("vacancy.departmentID")
                    .matching(stringBuilder.toString())
                    .createQuery();

            Query internalDepartmentQuery = qb.bool()
                    .must(internalQuery)
                    .must(departmentQuery)
                    .createQuery();

            Query governmentQuery = qb
                    .range()
                    .onField("vacancy.governmentOpeningDate")
                    .ignoreFieldBridge()
                    .below(sdf.format(new Date()))
                    .excludeLimit()
                    .createQuery();

            return qb.bool()
                    .should(internalDepartmentQuery)
                    .should(publicQuery)
                    .should(governmentQuery)
                    .createQuery();
        }
    }

    private Query getClosedVacanciesQuery(QueryBuilder qb) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");

        Query closedQuery = qb
                .range()
                .onField("vacancy.closingDate")
                .ignoreFieldBridge()
                .below(sdf.format(new Date()))
                .excludeLimit()
                .createQuery();

        return closedQuery;
    }

    private Query getDepartmentQuery(SearchParameters searchParameters, QueryBuilder qb) {

        if (searchParameters.getVacancySearchParameters().getDepartment() != null
                && searchParameters.getVacancySearchParameters().getDepartment().length > 0) {

            StringBuilder stringBuilder = new StringBuilder();

            for (String string : searchParameters.getVacancySearchParameters().getDepartment()) {
                stringBuilder.append(string).append(" ");
            }

            Query departmentQuery = qb.keyword()
                    .onField("vacancy.departmentID")
                    .matching(stringBuilder.toString())
                    .createQuery();

            return departmentQuery;
        } else {
            return qb.all().createQuery();
        }
    }

    private Query getFieldQuery(String field, String searchTerm, QueryBuilder qb) {

        if (StringUtils.isNotBlank(searchTerm)) {

            Query query = qb.keyword()
                    .onField(field)
                    .matching(searchTerm)
                    .createQuery();

            return query;

        }

        return qb.all().createQuery();
    }

    private Query getActiveQuery(QueryBuilder qb) {

        Query query = qb.keyword()
                .onField("vacancy.active")
                .matching("true")
                .createQuery();

        return query;
    }

    private String getContractTypes(SearchParameters searchParameters) {

        StringBuilder stringBuilder = new StringBuilder();

        if (searchParameters.getVacancySearchParameters().getContractTypes() != null) {
            concatenateStringArray(searchParameters.getVacancySearchParameters().getContractTypes(), stringBuilder);
        }

        return stringBuilder.toString();
    }

    private String getWorkingPatterns(SearchParameters searchParameters) {

        StringBuilder stringBuilder = new StringBuilder();

        if (searchParameters.getVacancySearchParameters().getWorkingPatterns() != null) {
            concatenateStringArray(searchParameters.getVacancySearchParameters().getWorkingPatterns(), stringBuilder);
        }

        return stringBuilder.toString();
    }

    private void concatenateStringArray(String[] stringArray, StringBuilder stringBuilder) {
        for (String string : stringArray) {
            if (!string.isEmpty()) {
                stringBuilder.append(string).append(" ");
            }
        }
    }
}
