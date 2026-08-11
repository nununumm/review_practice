package bad;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 口座間の送金を行うサービス
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;

    // fromId の口座から toId の口座へ amount 円を送金する。
    // 送金額の 0.5% を手数料として送金元から追加で引く。
    public boolean transfer(Long fromId, Long toId, double amount) {
        try {
            Account from = accountRepository.findById(fromId).get();
            Account to = accountRepository.findById(toId).get();

            // 送金元から出金する
            from.setBalance(from.getBalance() - amount);
            accountRepository.save(from);

            // 手数料（送金額の0.5%）を送金元から引く
            double fee = amount * 0.005;
            from.setBalance(from.getBalance() - fee);
            accountRepository.save(from);

            // 送金先へ入金する
            to.setBalance(to.getBalance() + amount);
            accountRepository.save(to);

            System.out.println(fromId + " から " + toId + " へ " + amount + " 円を送金しました");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
