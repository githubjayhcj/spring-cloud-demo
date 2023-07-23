package com.example.webService.ESdao;

import com.example.webService.entity.MyElastic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyElasticDao {

    private ElasticsearchOperations elasticsearchOperations;

    public MyElasticDao(ElasticsearchOperations elasticsearchOperations){
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public SearchHits<MyElastic> highlightPageSearch(String name, Pageable pageable,Sort sort){

        // DSL query
        StringQuery stringQuery =  new StringQuery("{\"match\":{\"name\":{\"query\":\""+name+"\"}}}");
        // highlight
        HighlightField highlightField = new HighlightField("name");
        List<HighlightField> highlightFields = new ArrayList<>();
        highlightFields.add(highlightField);
        Highlight highlight = new Highlight(highlightFields);
        // highlight query
        HighlightQuery highlightQuery = new HighlightQuery(highlight,MyElastic.class);
        //
        stringQuery.setHighlightQuery(highlightQuery);
        // 分页
        stringQuery.setPageable(pageable);
        // 排序
        stringQuery.setSort(sort);
        //
        SearchHits<MyElastic> searchHits = elasticsearchOperations.search(stringQuery,MyElastic.class);

        return searchHits;
    }


}
