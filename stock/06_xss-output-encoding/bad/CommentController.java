package com.example.blog.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping("/articles/{articleId}/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @PostMapping
    public RedirectView post(@RequestParam Long articleId,
                             @RequestParam String author,
                             @RequestParam String body,
                             @RequestParam String returnUrl) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setAuthor(author);
        comment.setBody(body);
        commentRepository.save(comment);
        return new RedirectView(returnUrl);
    }

    @GetMapping(produces = "text/html")
    public String list(@RequestParam Long articleId) {
        List<Comment> comments = commentRepository.findByArticleId(articleId);
        String html = "<div class='comments'>";
        for (Comment c : comments) {
            html += "<div class='comment' title='" + c.getAuthor() + "'>";
            html += "<span class='author'>" + c.getAuthor() + "</span>";
            html += "<p class='body'>" + c.getBody() + "</p>";
            html += "</div>";
        }
        html += "</div>";
        return html;
    }
}
