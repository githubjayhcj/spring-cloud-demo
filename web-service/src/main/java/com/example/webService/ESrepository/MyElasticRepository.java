package com.example.webService.ESrepository;

import com.example.webService.entity.MyElastic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.web.PageableDefault;

public interface MyElasticRepository extends ElasticsearchRepository<MyElastic, String> {

    @Query("{\"match\":{\"name\":{\"query\":\"?0\"}}}")
    Page<MyElastic> findByName(String name, Pageable pageable);


    @Query("{\"match\":{\"name\":{\"query\":\"?0\"}}}")
    @Highlight(fields = {
            @HighlightField(name = "name")
    })
    SearchHits<MyElastic> findByNameHit(String name);

}
