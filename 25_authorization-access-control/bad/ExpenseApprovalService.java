@Service
@RequiredArgsConstructor
public class ExpenseApprovalService {

    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;

    private static final Logger logger = LoggerFactory.getLogger(ExpenseApprovalService.class);

    /**
     * 経費申請を承認し、経理への支払データを作成する。
     * 10万円を超える申請は部長承認が必要というルールがある。
     */
    public String approve(Long expenseId, Long approverId, String comment) {
        try {
            Expense expense = expenseRepository.findById(expenseId).get();
            Employee approver = employeeRepository.findById(approverId).get();

            logger.info("承認処理 expense=" + expenseId + " 申請者=" + expense.getApplicantName()
                    + " 振込先=" + expense.getBankAccountNumber() + " 金額=" + expense.getAmount());

            if (expense.getAmount() > 100000 && !approver.getPosition().equals("部長")) {
                return "10万円を超える申請は部長の承認が必要です";
            }

            expense.setStatus("APPROVED");
            expense.setApproverId(approverId);
            expense.setApprovedComment(comment);
            expenseRepository.save(expense);

            Payment payment = new Payment();
            payment.setExpenseId(expenseId);
            payment.setAmount(expense.getAmount());
            payment.setBankAccountNumber(expense.getBankAccountNumber());
            paymentRepository.save(payment);

            return "承認しました";
        } catch (Exception e) {
            e.printStackTrace();
            return "承認に失敗しました";
        }
    }
}
