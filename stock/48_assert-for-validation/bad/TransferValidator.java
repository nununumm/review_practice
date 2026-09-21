package bad;

import org.springframework.stereotype.Component;

@Component
public class TransferValidator {

    public void validate(long amount, String toAccount) {
        assert amount > 0 : "送金額は正の数でなければならない";
        assert toAccount != null && toAccount.length() == 10 : "口座番号が不正";
        assert amount <= 1000000 : "送金額が上限を超えている";
    }
}
