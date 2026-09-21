package com.example.blog.comment;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

// @Controller … 「HTMLページ（テンプレート）を返す」コントローラーであることを表す。
//   ＝ @RestController（＝文字列やJSONをそのまま本文で返す）とは違い、
//     戻り値の文字列を「テンプレートの名前」として解釈してくれる。
//     テンプレートを通すことで、後述の「自動エスケープ」が効くのがポイント。
@Controller
@RequestMapping("/articles/{articleId}/comments") // この記事のコメントを扱うURLの入口
public class CommentController {

    // フィールドではなくコンストラクタで受け取る（＝コンストラクタインジェクション）。
    //   テストで差し替えやすく、必須の依存が抜けないので安全。
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ---- コメント投稿 ----
    @PostMapping
    public String post(@PathVariable Long articleId,         // URLの一部から記事IDを受け取る（?articleId= ではなくパスで固定）
                       @Valid CommentForm form) {             // @Valid … 下記CommentFormの検証ルールを自動でチェックする
        // 入力の検証（長さ・許可文字）はCommentFormのアノテーションが担当。
        // ここではドメインの処理（保存）だけに集中できる。
        commentService.add(articleId, form.getAuthor(), form.getBody());

        // リダイレクト先はユーザー入力(returnUrl)を使わず、サーバー側で組み立てる。
        //   ＝ 外部サイトへ飛ばされる「オープンリダイレクト」を根本から断つ。
        //   "redirect:" プレフィックスで、そのパスへ302リダイレクトになる。
        return "redirect:/articles/" + articleId;
    }

    // ---- コメント一覧の表示 ----
    @GetMapping
    public String list(@PathVariable Long articleId, Model model) {
        List<Comment> comments = commentService.findByArticle(articleId);
        // モデルにデータを載せてテンプレートへ渡す。HTMLは自分で文字列連結しない。
        model.addAttribute("comments", comments);
        // "comments" という名前のテンプレート（templates/comments.html）を描画する。
        //   テンプレート側の th:text が「自動エスケープ」してくれるので、
        //   <script> などの危険な文字は無害な文字（&lt;script&gt;）に変換されて表示される。
        return "comments";
    }
}
