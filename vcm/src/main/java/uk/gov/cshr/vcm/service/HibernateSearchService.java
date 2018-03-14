package uk.gov.cshr.vcm.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.hibernate.search.SearchFactory;
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
import uk.gov.cshr.vcm.model.Vacancy;

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
    public Page<Vacancy> search(SearchParameters searchParameters, Pageable pageable) throws LocationServiceException, IOException {

        String searchTerm = searchParameters.getKeyword();

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Vacancy.class).get();

        Query fuzzyQuery = qb.keyword().fuzzy()
                .withEditDistanceUpTo(1)
                .withPrefixLength(1)
                .onFields("title", "description", "shortDescription")
                .matching(searchTerm).createQuery();
        
        Integer minSalary = searchParameters.getSalaryMin();
        Integer maxSalary = searchParameters.getSalaryMax();
//
//        if (minSalary == null) {
//            minSalary = 0;
//        }
//
        Query minQuery = qb
                .range()
                .onField("salaryMax")
                .from(minSalary).to(Integer.MAX_VALUE)
                .createQuery();
//
        Query maxQuery = qb
                .range()
                .onField("salaryMin")
                .from(0).to(maxSalary)
                .createQuery();
//
//        Query minMax = qb.bool().must(minQuery).must(maxQuery).createQuery();
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
                .onField("closingDate")
                .ignoreFieldBridge()
                .above(sdf.format(new Date()))
                .excludeLimit()
                .createQuery();

        Query openQuery = qb
                .range()
                .onField("publicOpeningDate")
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

        Query regionQuery = qb.phrase().onField("regions").sentence(searchParameters.getCoordinates().getRegion()).createQuery();

        Query combinedQuery = qb.bool()
                .should(fuzzyQuery)
                .should(regionQuery)
                .should(spatialQuery)
                .must(openClosedQuery)
                .must(minQuery)
                .must(maxQuery)
                .createQuery();


        System.out.println("luceneQuery=" + combinedQuery);

        {
            SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
            IndexReader reader = searchFactory.getIndexReaderAccessor().open(Vacancy.class);
            try {
                int docs = reader.numDocs();
                for (int i = 0; i < docs; i++) {
                    Document document = reader.document(i);
                    System.out.println("document=" + document);
                }
            }
            finally {
                searchFactory.getIndexReaderAccessor().close(reader);
            }
        }

        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(combinedQuery, Vacancy.class);

        // execute search
        try {
            List<Vacancy> results = jpaQuery.getResultList();
            PageImpl<Vacancy> page = new PageImpl<>(results, pageable, 1);
//            page.

            return page;
        }
        catch (NoResultException nre) {
            return new PageImpl<>(new ArrayList<>());

        }
    }
}
