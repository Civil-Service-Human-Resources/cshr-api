package uk.gov.cshr.vcm.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public Page<VacancyLocation> search(SearchParameters searchParameters, Pageable pageable) throws LocationServiceException, IOException {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(VacancyLocation.class).get();

        BooleanJunction combinedQuery = qb.bool();

        combinedQuery = searchTermQuery(searchParameters.getVacancySearchParameters().getKeyword(), qb, combinedQuery);

        combinedQuery = addSalaryQuery(searchParameters, qb, combinedQuery);

        combinedQuery = addOpenClosedQuery(qb, combinedQuery);

        if (BooleanUtils.isTrue(searchParameters.getVacancySearchParameters().getOverseasJob())) {

            Query overseasQuery = qb.keyword().onField("vacancy.overseasJob")
                    .matching("true").createQuery();
            combinedQuery = combinedQuery.should(overseasQuery);

        }
        else {

            if (searchParameters.getVacancySearchParameters().getLocation() != null) {

                Query spatialQuery = qb
                        .spatial()
                        .within(searchParameters.getVacancySearchParameters().getLocation().getRadius(), Unit.KM)
                        .ofLatitude(searchParameters.getCoordinates().getLatitude())
                        .andLongitude(searchParameters.getCoordinates().getLongitude())
                        .createQuery();

                Query regionQuery = qb.phrase()
                        .onField("vacancy.regions")
                        .sentence(searchParameters.getCoordinates().getRegion())
                        .createQuery();

                combinedQuery = combinedQuery.must(spatialQuery).should(regionQuery);
            }
        }
        

        System.out.println("luceneQuery=" + combinedQuery.createQuery());

        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(
                combinedQuery.createQuery(), VacancyLocation.class);

        // execute search
        try {
            List<VacancyLocation> results = jpaQuery.getResultList();
            PageImpl<VacancyLocation> page = new PageImpl<>(results, pageable, 1);
//            page.

            return page;
        }
        catch (NoResultException nre) {
            return new PageImpl<>(new ArrayList<>());

        }
    }

    private BooleanJunction searchTermQuery(String searchTerm, QueryBuilder qb, BooleanJunction combinedQuery) {
        if (searchTerm != null && !searchTerm.isEmpty()) {

            Query titleQuery = qb.keyword().fuzzy()
                    .withEditDistanceUpTo(1)
                    .withPrefixLength(1)
                    .onFields("vacancy.title")
                    .boostedTo(1)
                    .matching(searchTerm).createQuery();

            Query titlePhraseQuery = qb.phrase()
                    .onField("vacancy.title")
                    .boostedTo(1)
                    .sentence(searchTerm)
                    .createQuery();

            Query descriptionQuery = qb.keyword().fuzzy()
                    .withEditDistanceUpTo(1)
                    .withPrefixLength(1)
                    .onFields("vacancy.description", "vacancy.shortDescription")
                    .matching(searchTerm).createQuery();

            Query keywordQuery = qb.bool()
                    .should(titleQuery)
                    .should(descriptionQuery)
                    .should(titlePhraseQuery)
                    .createQuery();

            combinedQuery = combinedQuery.must(keywordQuery);
        }
        return combinedQuery;
    }

    private BooleanJunction addSalaryQuery(SearchParameters searchParameters, QueryBuilder qb, BooleanJunction combinedQuery) {

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
                .must(maxQuery)
                .createQuery();

        return combinedQuery.must(salaryQuery);
    }

    private BooleanJunction addOpenClosedQuery(QueryBuilder qb, BooleanJunction combinedQuery) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");

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
        combinedQuery = combinedQuery.must(openClosedQuery);
        return combinedQuery;
    }
}
