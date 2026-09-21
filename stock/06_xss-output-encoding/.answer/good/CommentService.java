package com.example.blog.comment;

import org.springframework.stereotype.Service;

import java.util.List;

// @Service … 「業務ロジックの担当」であることを示す部品。
//   Controllerは「入口の交通整理」に専念し、保存などの処理はここに寄せる。
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // コメントを1件作って保存する。
    //   ※ここでは値の「保存」だけを行い、危険文字の除去はしない。
    //     XSS対策は「入力時に消す」より「出力時にエスケープする」のが基本方針だから
    //     （保存された生データは正しく残し、表示する瞬間に無害化する）。
    public void add(Long articleId, String author, String body) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setAuthor(author);
        comment.setBody(body);
        commentRepository.save(comment);
    }

    // 指定記事のコメント一覧を取得する。
    public List<Comment> findByArticle(Long articleId) {
        return commentRepository.findByArticleId(articleId);
    }
}
