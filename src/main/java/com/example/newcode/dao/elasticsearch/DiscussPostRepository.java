package com.example.newcode.dao.elasticsearch;

import com.example.newcode.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, String> {
	// 这种方法
}
