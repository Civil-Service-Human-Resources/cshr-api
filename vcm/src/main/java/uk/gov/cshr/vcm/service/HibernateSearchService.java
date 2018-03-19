package uk.gov.cshr.vcm.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.SearchParameters;
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

    @Transactional
    public Page<VacancyLocation> search(SearchParameters searchParameters, Pageable pageable) throws LocationServiceException, IOException {

        String searchTerm = searchParameters.getKeyword();

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(VacancyLocation.class).get();

        Query fuzzyQuery = qb.keyword().fuzzy()
                .withEditDistanceUpTo(1)
                .withPrefixLength(1)
                .onFields("vacancy.title", "vacancy.description", "vacancy.shortDescription")
                .matching(searchTerm).createQuery();
        
        Integer minSalary = searchParameters.getSalaryMin();
        if (minSalary == null) {
            minSalary = 0;
        }

        Integer maxSalary = searchParameters.getSalaryMax();
        if (maxSalary == null) {
            maxSalary = Integer.MAX_VALUE;
        }
//
//        if (minSalary == null) {
//            minSalary = 0;
//        }
//
        Query minQuery = qb
                .range()
                .onField("vacancy.salaryMax")
                .above(minSalary)
                .createQuery();
//
        Query maxQuery = qb
                .range()
                .onField("vacancy.salaryMin")
                .below(maxSalary)
                .createQuery();

//        qb.range().

//
        Query salaryQuery = qb
                .bool()
                .must(minQuery)
                .must(maxQuery)
                .createQuery();
//
//        Query openQuery = qb
//                .range()
//                .onField("publicOpeningDate")
//                .below(new Date())
//                .createQuery();
        // 201803130819
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

        Query spatialQuery = qb
                .spatial()
                .within(searchParameters.getRadius(), Unit.KM)
                .ofLatitude(searchParameters.getLatitude())
                .andLongitude(searchParameters.getLongitude())
                .createQuery();

        Query regionQuery = qb.phrase()
                .onField("vacancy.regions")
                .sentence(searchParameters.getCoordinates().getRegion())
                .createQuery();

        Query spatialRegion = qb.bool().should(spatialQuery).should(regionQuery).createQuery();

        if (searchParameters.getOverseasJob() != null && searchParameters.getOverseasJob()) {

            Query overseasQuery = qb.keyword().onField("vacancy.overseasJob")
                    .matching("true").createQuery();

            spatialRegion = qb.bool()
                    .should(spatialRegion)
                    .should(overseasQuery)
                    .createQuery();
        }

        Query combinedQuery = qb.bool()
                .must(fuzzyQuery)
                .must(spatialRegion)
                .must(openClosedQuery)
                .must(salaryQuery)
                .createQuery();

        System.out.println("luceneQuery=" + combinedQuery);

        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(combinedQuery, VacancyLocation.class);

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
}
