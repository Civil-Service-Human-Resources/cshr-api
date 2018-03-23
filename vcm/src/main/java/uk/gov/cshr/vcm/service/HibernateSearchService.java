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
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;

@Service
public class HibernateSearchService {

    @Autowired
    private final EntityManager entityManager;

    @Autowired
    public HibernateSearchService(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    public void initializeHibernateSearch() {

        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager.createIndexer().startAndWait();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void purge() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.purgeAll(VacancyLocation.class);
        fullTextEntityManager.purgeAll(Vacancy.class);
    }

    @Transactional
    public Page<Vacancy> search(SearchParameters searchParameters, Pageable pageable) throws LocationServiceException, IOException {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(VacancyLocation.class).get();

        Query searchtermQuery = getSearchTermQuery(searchParameters.getVacancySearchParameters().getKeyword(), qb);
        Query salaryQuery = getSalaryQuery(searchParameters, qb);
        Query openClosed = getOpenClosedQuery(qb);
        Query departmentQuery = getDepartmentQuery(searchParameters, qb);
        Query locationQuery = getLocationQuery(searchParameters, qb);

        BooleanJunction everythingElse = qb.bool()
                .must(locationQuery)
                .must(salaryQuery)
                .must(openClosed)
                .must(departmentQuery)
                .must(searchtermQuery);


        System.out.println("luceneQuery=" + everythingElse.createQuery().toString());
        System.out.println("test");

        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(
                everythingElse.createQuery(), VacancyLocation.class).setProjection("vacancyid");

//        FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(
//                searchtermQuery, VacancyLocation.class).setProjection("vacancyid");
//        jpaQuery.enableFullTextFilter("openFilter");

        // execute search
        try {
            List<Object[]> vacancyIDs = jpaQuery.getResultList();

            LinkedHashSet<Long> uniqueVacancyIDs = new LinkedHashSet<>();

            for (Object[] vacancyID : vacancyIDs) {
                System.out.println("id=" + Long.valueOf(vacancyID[0].toString()));
                uniqueVacancyIDs.add(Long.valueOf(vacancyID[0].toString()));
            }

            int pageSize = pageable.getPageSize();
            int offSet = pageable.getOffset();
            int pageNumber = pageable.getPageNumber();

            List idList;

            if (uniqueVacancyIDs.size() < pageSize) {
                idList = Arrays.asList(uniqueVacancyIDs.toArray());
            }
            else {

                int max = pageNumber * pageSize + pageSize;

                if (max > uniqueVacancyIDs.size()) {
                    max = uniqueVacancyIDs.size();
                }

                idList = Arrays.asList(uniqueVacancyIDs.toArray()).subList(pageNumber * pageSize, max);
            }

//            Session session = entityManager.unwrap(Session.class);
//            Session session = entityManager.unwrap(Session.class);
//            SessionFactory sessionFactory = session.getSessionFactory();
//            Session session = Search.getFullTextEntityManager(entityManager).unwrap(Session.class);
//
//            MultiIdentifierLoadAccess<Vacancy> multiLoadAccess = session.byMultipleIds(Vacancy.class);
//            List<Vacancy> vacancies = multiLoadAccess.multiLoad(idList);
            if (idList.isEmpty()) {
                return new PageImpl<>(new ArrayList<>());
            }
            else {
                List<Vacancy> vacancies = entityManager
                        .createQuery("SELECT v FROM Vacancy v WHERE v.id IN (:ids)")
                        .setParameter("ids", idList)
                        .getResultList();

//                Vacancy[] vacanciesSorted = new Vacancy[pageSize];
//                for (Vacancy vacancy : vacancies) {
//                    vacanciesSorted[idList.indexOf(vacancy.getId())] = vacancy;
//                }

                vacancies.sort(Comparator.comparingLong(item -> idList.indexOf(item.getId())));

                PageImpl<Vacancy> page = new PageImpl<>(vacancies, pageable, uniqueVacancyIDs.size());
//            page.

                return page;
            }

        }
        catch (NoResultException nre) {
            return new PageImpl<>(new ArrayList<>());

        }
    }

    private Query getLocationQuery(SearchParameters searchParameters, QueryBuilder qb) {

        if (searchParameters.getVacancySearchParameters().getLocation() != null) {

            BooleanJunction locationQuery = qb.bool();

            Query spatialQuery = qb
                    .spatial().boostedTo(01.f)
                    .within(searchParameters.getVacancySearchParameters().getLocation().getRadius(), Unit.KM)
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

            boolean includeOverseasJobs = BooleanUtils.isTrue(searchParameters.getVacancySearchParameters().getOverseasJob());

            if (!includeOverseasJobs) {

                locationQuery.must(regionSpatialQuery);
//                    combinedQuery = combinedQuery;
            }
            else {

                Query regionSpatialOverseas = qb.bool().should(regionSpatialQuery).should(overseasQuery).createQuery();
                locationQuery.must(regionSpatialOverseas);

            }

            return locationQuery.createQuery();
        }
        else {
            return qb.all().createQuery();
        }
    }

    private Query getSearchTermQuery(String searchTerm, QueryBuilder qb) {

        if (searchTerm != null && !searchTerm.isEmpty()) {


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
                    .boostedTo(10f)
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
        }
        return qb.all().createQuery();
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

    private Query getOpenClosedQuery(QueryBuilder qb) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");

        Query closedQuery = qb
                .range()
                .onField("vacancy.closingDate")
                .ignoreFieldBridge()
                .above(sdf.format(new Date()))
                .excludeLimit()
                .createQuery();
        Query openQuery = qb
                .range()
                .onField("vacancy.publicOpeningDate")
                .ignoreFieldBridge()
                .below(sdf.format(new Date()))
                .excludeLimit()
                .createQuery();

        Query openClosedQuery = qb.bool().must(openQuery).must(closedQuery).createQuery();
        return openClosedQuery;
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
        }

        else {
            return qb.all().createQuery();
        }

    }
}
