package bad;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchParamParser {

    public SearchCondition parse(String pageParam, String maxPriceParam,
                                 String categoryCode, List<Category> categories) {

        SearchCondition condition = new SearchCondition();

        // ページ番号：数字なら使う、数字でなければ1ページ目
        try {
            condition.setPage(Integer.parseInt(pageParam));
        } catch (NumberFormatException e) {
            condition.setPage(1);
        }

        // 価格上限：数字ならその値、数字でなければ上限なし扱いで0
        int maxPrice;
        try {
            maxPrice = Integer.parseInt(maxPriceParam);
        } catch (NumberFormatException e) {
            maxPrice = 0;
        }
        condition.setMaxPrice(maxPrice);

        // 区分コードに一致するカテゴリを一覧から探す
        // 終端まで来たら IndexOutOfBoundsException が出るので、それで打ち切る
        Category matched = null;
        int i = 0;
        try {
            while (true) {
                Category c = categories.get(i);
                if (c.getCode().equals(categoryCode)) {
                    matched = c;
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            matched = null;
        }
        condition.setCategory(matched);

        return condition;
    }
}
