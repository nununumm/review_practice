import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// JpaRepository を継承すると、findAll(Pageable) や count() が最初から使える。
//   findAll(Pageable) … DBに「LIMIT / OFFSET」を付けて“そのページ分だけ”取ってくる
//   count()           … DBに「SELECT COUNT(*)」を投げて“件数だけ”数えてくる
// どちらも全件をメモリに載せないので、商品が何百万件あってもスケールする。
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
