package com.example.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ブログ記事の一覧を、著者名を添えて返すサービス。
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ArticleDto> getArticleList() {
        // 記事を全部取得する
        List<Article> articles = articleRepository.findAll();

        List<ArticleDto> result = new ArrayList<>();
        for (Article article : articles) {
            ArticleDto dto = new ArticleDto();
            dto.setId(article.getId());
            dto.setTitle(article.getTitle());

            // その記事の著者名を取得して詰める
            User author = userRepository.findById(article.getAuthorId()).get();
            dto.setAuthorName(author.getName());

            result.add(dto);
        }
        return result;
    }
}
